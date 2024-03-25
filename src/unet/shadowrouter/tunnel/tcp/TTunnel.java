package unet.shadowrouter.tunnel.tcp;

import unet.kad4.utils.net.AddressUtils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

import static unet.shadowrouter.utils.KeyRing.*;

public class TTunnel {

    public static final byte[] SHADOW_ROUTER_HEADER = new byte[]{ 'S', 'R' };

    private Socket socket;
    private InputStream in;
    private OutputStream out;

    public TTunnel(){
    }

    public TTunnel(InetSocketAddress address)throws IOException {
        connect(address);
    }

    public void connect(/*Node node*/ InetSocketAddress address)throws IOException {
        socket = new Socket();
        socket.connect(address);
        in = socket.getInputStream();
        out = socket.getOutputStream();
    }

    /*
    REQUEST
    +-----------------+-----------------------------+---------------+------------+
    | 2 BYTE CONSTANT | 4 BYTE DH PUBLIC_KEY LENGTH | DH PUBLIC_KEY | 16 BYTE IV |
    +-----------------+-----------------------------+---------------+------------+

    RESPONSE
    +-----------------+-----------------------------+---------------+-----------+
    | 2 BYTE CONSTANT | 4 BYTE DH PUBLIC_KEY LENGTH | DH PUBLIC_KEY | SIGNATURE |
    +-----------------+-----------------------------+---------------+-----------+
    */
    public void handshake(PublicKey publicKey, InetSocketAddress address)throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException,
            InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        KeyPair keyPair = generateKeyPair("DH");
        SecureRandom random = new SecureRandom();

        out.write(SHADOW_ROUTER_HEADER);

        byte[] ecdhKey = keyPair.getPublic().getEncoded();

        byte[] len = new byte[4];
        len[0] = ((byte) ecdhKey.length);
        len[1] = ((byte) (ecdhKey.length >> 8));
        len[2] = ((byte) (ecdhKey.length >> 16));
        len[3] = ((byte) (ecdhKey.length >> 24));

        out.write(len);
        out.write(ecdhKey);

        byte[] iv = new byte[16];
        random.nextBytes(iv);
        out.write(iv);
        out.flush();

        byte[] header = new byte[SHADOW_ROUTER_HEADER.length];
        in.read(header);

        if(!Arrays.equals(header, SHADOW_ROUTER_HEADER)){
            throw new IOException("Missing Shadow Router header");
        }

        int length = ((in.read() & 0xff) |
                ((in.read() & 0xff) << 8) |
                ((in.read() & 0xff) << 16) |
                ((in.read() & 0xff) << 24));

        byte[] data = new byte[length];
        in.read(data, 0, length);

        byte[] signature = new byte[256];
        in.read(signature);

        if(!verify(publicKey, signature, data)){
            throw new IOException("Signature is Invalid.");
        }

        byte[] secret = generateSecret(keyPair.getPrivate(), decodePublic(data, "DH"));
        System.out.println("CLIENT: "+secret.length+"  "+Base64.getEncoder().encodeToString(secret));

        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] derivedKey = digest.digest(secret); // Or use a proper KDF like HKDF
        SecretKey secretKey = new SecretKeySpec(derivedKey, "AES");

        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));//, new GCMParameterSpec(128, iv));
        in = new CipherInputStream(in, cipher);

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));//, new GCMParameterSpec(128, iv));
        out = new CipherOutputStream(out, cipher);

        byte[] addr = AddressUtils.packAddress(address);
        out.write((byte) addr.length);
        out.write(addr);
        out.flush();
    }

    public InputStream getInputStream(){
        return in;
    }

    public OutputStream getOutputStream(){
        return out;
    }

    public void close()throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}
