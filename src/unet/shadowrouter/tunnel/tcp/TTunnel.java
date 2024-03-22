package unet.shadowrouter.tunnel.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TTunnel implements Runnable {

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
