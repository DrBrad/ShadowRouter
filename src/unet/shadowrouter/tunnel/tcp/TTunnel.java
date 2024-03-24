package unet.shadowrouter.tunnel.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TTunnel implements Runnable {

    /*
    HEADER WILL BE CONSTANT
    [ SR ]

    BYTE COMMANDS
    - 0x00 CONNECT
    - 0x01 RELAY

    +---------------+----------------+
    | 2 BYTE HEADER | 1 BYTE COMMAND |
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
}
