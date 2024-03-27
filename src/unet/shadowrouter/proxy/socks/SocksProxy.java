package unet.shadowrouter.proxy.socks;

import unet.shadowrouter.proxy.socks.socks.Socks4;
import unet.shadowrouter.proxy.socks.socks.Socks5;
import unet.shadowrouter.proxy.socks.socks.SocksBase;

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

            SocksBase socks;

            //SOCKS VERSION
            switch(in.read()){
                case 0x04:
                    socks = new Socks4(this);
                    System.out.println("SOCKS 4");
                    break;

                case 0x05:
                    socks = new Socks5(this);
                    System.out.println("SOCKS 5");
                    break;

                default:
                    socket.close();
                    return;
            }

            //COMMAND
            switch(socks.getCommand()){
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

    public Socket getSocket(){
        return socket;
    }

    public InputStream getInputStream(){
        return in;
    }

    public OutputStream getOutputStream(){
        return out;
    }
}
