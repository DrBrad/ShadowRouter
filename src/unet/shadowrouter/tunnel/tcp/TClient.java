package unet.shadowrouter.tunnel.tcp;

import unet.kad4.utils.Node;
import unet.shadowrouter.utils.KeyRing;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;

import static unet.shadowrouter.utils.KeyRing.*;

public class TClient {

    public static final byte[] SHADOW_ROUTER_HEADER = new byte[]{ 'S', 'R' };

    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private PublicKey peerKey;

    private byte[] secret, iv;

    public TClient(PublicKey peerKey){
        this.peerKey = peerKey;
    }

    public void connect(/*Node node*/ InetSocketAddress address)throws IOException {
        socket = new Socket();
        socket.connect(address);
        in = socket.getInputStream();
        out = socket.getOutputStream();

        handshake();

        try{
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] derivedKey = digest.digest(secret); // Or use a proper KDF like HKDF

            SecretKey secretKey = new SecretKeySpec(derivedKey, "AES");

            //byte[] additionalData = "Metadata".getBytes();
            //cipher.updateAAD(additionalData);

            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));//, new GCMParameterSpec(128, iv));
            in = new CipherInputStream(in, cipher);

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));//, new GCMParameterSpec(128, iv));
            out = new CipherOutputStream(out, cipher);



            //out.write("Knowledge nay estimable questions repulsive daughters boy. Solicitude gay way unaffected expression for. His mistress ladyship required off horrible disposed rejoiced. Unpleasing pianoforte unreserved as oh he unpleasant no inquietude insipidity. Advantages can discretion possession add favourable cultivated admiration far. Why rather assure how esteem end hunted nearer and before. By an truth after heard going early given he. Charmed to it excited females whether at".getBytes());
            out.write("HELLO WORLD".getBytes());
            out.write("HELLO WORLD".getBytes());
            out.flush();
            //out.close();

            Thread.sleep(1000);
            //socket.close();


            /*
            System.out.println(in.available());
            byte[] buf = new byte[4096];
            int len = in.read(buf);
            System.out.println("CLIENT: "+new String(buf, 0, len));
            */

        }catch(Exception e){
            e.printStackTrace();
        }
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
    public void handshake()throws IOException {
        try{
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

            iv = new byte[16];
            random.nextBytes(iv);
            out.write(iv);

            out.flush();

            byte[] header = new byte[SHADOW_ROUTER_HEADER.length];
            in.read(header);

            if(!Arrays.equals(header, SHADOW_ROUTER_HEADER)){
                //CLOSE
                System.err.println("MISSING SHADOW ROUTER HEADER");
                return;
            }

            int length = ((in.read() & 0xff) |
                    ((in.read() & 0xff) << 8) |
                    ((in.read() & 0xff) << 16) |
                    ((in.read() & 0xff) << 24));

            byte[] data = new byte[length];
            in.read(data);

            byte[] signature = new byte[256];
            in.read(signature);

            if(!verify(peerKey, signature, data)){
                //CLOSE
                System.err.println("SIGNATURE IS INVALID");
                return;
            }

            secret = generateSecret(keyPair.getPrivate(), decodePublic(data, "DH"));
            System.out.println("CLIENT: "+secret.length+"  "+Base64.getEncoder().encodeToString(secret));

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
