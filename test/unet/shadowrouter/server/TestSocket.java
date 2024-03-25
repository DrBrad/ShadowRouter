package unet.shadowrouter.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TestSocket implements Runnable {

    private Socket socket;
    private InputStream in;
    private OutputStream out;

    public TestSocket(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run(){
        try{
            in = socket.getInputStream();
            out = socket.getOutputStream();

            byte[] buf = new byte[4096];
            int len = in.read(buf);
            System.out.println("SERVER: "+new String(buf, 0, len));

            out.write("YO YO YO".getBytes());
            out.flush();

            out.close();

        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
