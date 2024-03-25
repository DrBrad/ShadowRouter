package unet.shadowrouter.tunnel.tcp;

import unet.shadowrouter.utils.ECDHUtils;
import unet.shadowrouter.utils.RSAUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PublicKey;

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

    private Socket socket;
    private InputStream in;
    private OutputStream out;

    public TTunnel(Socket socket){
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





            socket.close();

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void handshake(){
        /*
        * CLIENT SIDE HANDSHAKE
        * */


        try{
            KeyPair rsa = RSAUtils.generateKeyPair();
            //SHOULD NEVER CHANGE... DONT GEN HERE

            KeyPair keyPair = ECDHUtils.generateKeyPair();


            //WE NEED TO KNOW THE NODES PUBLIC_KEY
            PublicKey nodesPublicKey = null;

            //IF VERIFY THE AUTHENTICITY

            byte[] data = new byte[2048];
            in.read(data);


            byte[] signature = new byte[2048];
            in.read(signature);

            if(!RSAUtils.verify(nodesPublicKey, signature, data)){
                //CLOSE
                return;
            }

            byte[] secret = ECDHUtils.generateSecret(keyPair.getPrivate(), ECDHUtils.decodePublic(data));




        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
