package unet.shadowrouter.tunnel.tcp;

import unet.shadowrouter.utils.KeyRing;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Base64;

import static unet.shadowrouter.utils.KeyRing.*;

public class TTunnel implements Runnable {

    /*
    HEADER WILL BE CONSTANT
    [ SR ]

    BYTE COMMANDS
    - 0x00 TCP
    - 0x01 UDP


    +---------------+-----------------+
    | 2 BYTE HEADER | 1 BYTE PROTOCOL |
    +---------------+-----------------+
    */

    public static final byte[] SHADOW_ROUTER_HEADER = new byte[]{ 'S', 'R' };

    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private PrivateKey myKey;

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



            KeyPair keyPair = generateKeyPair("DH");
            byte[] secret = generateSecret(keyPair.getPrivate(), decodePublic(data, "DH"));
            System.out.println("SERVER: "+Base64.getEncoder().encodeToString(secret));

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
