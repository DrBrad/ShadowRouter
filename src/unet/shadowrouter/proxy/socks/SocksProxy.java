package unet.shadowrouter.proxy.socks;

import unet.kad4.kad.KademliaBase;
import unet.shadowrouter.proxy.socks.socks.Socks4;
import unet.shadowrouter.proxy.socks.socks.Socks5;
import unet.shadowrouter.proxy.socks.socks.inter.SocksBase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SocksProxy implements Runnable {

    private KademliaBase kademlia;
    private Socket socket;
    private InputStream in;
    private OutputStream out;

    public SocksProxy(KademliaBase kademlia, Socket socket){
        this.kademlia = kademlia;
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
                    //System.out.println("SOCKS 4");
                    socket.close();
                    return;

                case 0x05:
                    socks = new Socks5(this);
                    //System.out.println("SOCKS 5");
                    break;

                default:
                    socket.close();
                    return;
            }

            //COMMAND
            switch(socks.getCommand()){
                case CONNECT:
                    socks.connect();
                    //System.out.println("CONNECT");
                    break;

                case BIND:
                    //System.out.println("BIND");
                    socks.bind();
                    break;

                case UDP:
                    if(socks instanceof Socks5){
                        ((Socks5) socks).udp();
                    }
                    //System.out.println("UDP");
                    break;
            }

            socket.close();

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public KademliaBase getKademlia(){
        return kademlia;
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
