package unet.shadowrouter.tunnel.tcp;

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
import java.net.Socket;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Base64;

import static unet.shadowrouter.utils.KeyRing.*;

public class TTunnel implements Runnable {

    public static final byte[] SHADOW_ROUTER_HEADER = new byte[]{ 'S', 'R' };

    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private PrivateKey myKey;

    private byte[] secret, iv;

    public TTunnel(PrivateKey myKey, Socket socket){
        this.myKey = myKey;
        this.socket = socket;
    }

    @Override
    public void run(){
        try{
            in = socket.getInputStream();
            out = socket.getOutputStream();

            //HANDSHAKE...
            /*
            CONSISTS OF:
                - USER SENDS:
                    - UID SUDO RANDOM
                    - PUBLIC-KEY
                - SERVER SENDS:
                    - 
            */
            //
            handshake();


            try{
                Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");

                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] derivedKey = digest.digest(secret); // Or use a proper KDF like HKDF

                SecretKey secretKey = new SecretKeySpec(derivedKey, "AES");

                //byte[] additionalData = "Metadata".getBytes();
                //cipher.updateAAD(additionalData);

                cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));//new GCMParameterSpec(128, iv));
                in = new CipherInputStream(in, cipher);

                cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));//, new GCMParameterSpec(128, iv));
                out = new CipherOutputStream(out, cipher);


                byte[] buf = new byte[4096];
                int len = in.read(buf);
                System.out.println("SERVER: "+len+" "+new String(buf, 0, len));


                //len = in.read(buf);
                //System.out.println("SERVER: "+len+" "+new String(buf, 0, len));

                out.write("HELLO WORLD".getBytes());
                out.flush();

            }catch(Exception e){
                e.printStackTrace();
            }



            socket.close();

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void handshake()throws IOException {
        /*
        * CLIENT SIDE HANDSHAKE
        * */

        try{
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

            iv = new byte[16];
            in.read(iv);


            KeyPair keyPair = generateKeyPair("DH");
            secret = generateSecret(keyPair.getPrivate(), decodePublic(data, "DH"));
            System.out.println("SERVER: "+secret.length+"  "+Base64.getEncoder().encodeToString(secret));

            out.write(SHADOW_ROUTER_HEADER);

            byte[] ecdhKey = keyPair.getPublic().getEncoded();

            byte[] len = new byte[4];
            len[0] = ((byte) ecdhKey.length);
            len[1] = ((byte) (ecdhKey.length >> 8));
            len[2] = ((byte) (ecdhKey.length >> 16));
            len[3] = ((byte) (ecdhKey.length >> 24));

            out.write(len);
            out.write(ecdhKey);
            out.write(sign(myKey, ecdhKey));
            out.flush();


        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
