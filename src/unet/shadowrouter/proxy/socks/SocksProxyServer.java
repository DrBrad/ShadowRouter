package unet.shadowrouter.proxy.socks;

import unet.kad4.kad.KademliaBase;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocksProxyServer {

    private KademliaBase kademlia;
    private ServerSocket server;

    public SocksProxyServer(KademliaBase kademlia){
        this.kademlia = kademlia;
    }

    public void start(int port)throws IOException {
        if(isRunning()){
            throw new IllegalArgumentException("Server has already started.");
        }

        server = new ServerSocket(port);

        new Thread(new Runnable(){
            @Override
            public void run(){
                try{
                    Socket socket;
                    while((socket = server.accept()) != null){
                        new Thread(new SocksProxy(kademlia, socket)).start();
                    }
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void stop()throws IOException {
        if(!isRunning()){
            throw new IllegalArgumentException("Server is not currently running.");
        }

        server.close();
    }

    public boolean isRunning(){
        return (server != null && !server.isClosed());
    }

    public int getPort(){
        return server.getLocalPort();
    }
}
