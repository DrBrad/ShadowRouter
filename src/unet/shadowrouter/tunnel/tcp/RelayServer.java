package unet.shadowrouter.tunnel.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;

public class RelayServer {

    private ServerSocket server;
    private KeyPair keyPair;

    public RelayServer(KeyPair keyPair){
        this.keyPair = keyPair;
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
                        new Thread(new Relay(keyPair.getPrivate(), socket)).start();
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
