package unet.shadowrouter.proxy.socks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SocksProxy implements Runnable {

    private Socket socket;
    private InputStream in;
    private OutputStream out;

    public SocksProxy(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run(){
        try{
            in = socket.getInputStream();
            out = socket.getOutputStream();

            //SOCKS VERSION
            switch(in.read()){
                case 0x04:
                    System.out.println("SOCKS 4");
                    break;

                case 0x05:
                    System.out.println("SOCKS 5");
                    break;

                default:
                    socket.close();
                    return;
            }

            //COMMAND
            switch(in.read()){
                case 0x01:
                    System.out.println("CONNECT");
                    //commons.connect();
                    //relay();
                    break;

                case 0x02:
                    System.out.println("BIND");
                    //commons.bind();
                    //relay();
                    break;

                case 0x03:
                    System.out.println("UDP");
                    //((Socks5)commons).udp();
                    break;
            }


        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
