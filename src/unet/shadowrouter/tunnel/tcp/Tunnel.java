package unet.shadowrouter.tunnel.tcp;

import unet.kad4.utils.net.AddressUtils;
import unet.shadowrouter.kad.utils.KeyUtils;
import unet.shadowrouter.kad.utils.SecureNode;
import unet.shadowrouter.tunnel.inter.AddressType;
import unet.shadowrouter.tunnel.inter.Command;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

public class Tunnel {

    public static final byte[] SHADOW_ROUTER_HEADER = new byte[]{ 'S', 'R' };

    private Socket socket;
    private InputStream in;
    private OutputStream out;

    public Tunnel(){
    }

    public Tunnel(SecureNode node, int port)throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException,
            InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        connect(node, port);
    }

    public void connect(SecureNode node, int port)throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException,
            InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        socket = new Socket();
        //System.out.println(node.getHostAddress()+"  "+node.getPort()+"  "+port);
        socket.connect(new InetSocketAddress(node.getHostAddress(), port));
        in = socket.getInputStream();
        out = socket.getOutputStream();
        handshake(node);
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
    public void relay(SecureNode node)throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException,
            InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        out.write(Command.RESOLVE_PORT.getCode());
        out.write((node.getHostAddress() instanceof Inet4Address) ? AddressType.IPv4.getCode() : AddressType.IPv6.getCode());
        out.write(AddressUtils.packAddress(node.getAddress()));
        out.flush();

        if(in.read() != 0x00){
            throw new IOException("Failed to connect");
        }

        handshake(node);
    }

    //WE ARE NOT USING INET ADDRESS AS IT IMMEDIATLY DNS RESOLVES DOMAINS
    public void exit(byte[] address, int port, AddressType type)throws IOException {
        out.write(Command.RELAY.getCode());
        out.write(type.getCode());

        switch(type){
            case IPv4:
            case IPv6:
                out.write(address);
                break;

            case DOMAIN:
                out.write((byte) address.length);
                out.write(address);
                break;
        }

        out.write(new byte[]{
                (byte) ((port & 0xff00) >> 8),
                (byte) (port & 0xff)
        });

        out.flush();

        if(in.read() != 0x00){
            throw new IOException("Failed to connect");
        }
    }

    private void handshake(SecureNode node)throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException,
            InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        KeyPair keyPair = KeyUtils.generateKeyPair("DH");
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

        if(!KeyUtils.verify(node.getPublicKey(), signature, data)){
            throw new IOException("Signature is Invalid.");
        }

        byte[] secret = KeyUtils.generateSecret(keyPair.getPrivate(), KeyUtils.decodePublic(data, "DH"));
        //System.out.println("CLIENT: "+secret.length+"  "+Base64.getEncoder().encodeToString(secret));


        MessageDigest digest = MessageDigest.getInstance("SHA-256");// Or use a proper KDF like HKDF
        SecretKey secretKey = new SecretKeySpec(digest.digest(secret), "AES");

        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));//, new GCMParameterSpec(128, iv));
        in = new SecureInputStream(in, cipher);

        cipher = Cipher.getInstance("AES/CTR/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));//, new GCMParameterSpec(128, iv));
        out = new SecureOutputStream(out, cipher);
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
