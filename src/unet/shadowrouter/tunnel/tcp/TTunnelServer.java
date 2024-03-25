package unet.shadowrouter.tunnel.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;

public class TTunnelServer {

    private ServerSocket server;
    private KeyPair keyPair;

    public TTunnelServer(KeyPair keyPair){
        this.keyPair = keyPair;
    }

    public void start(int port)throws IOException {
        server = new ServerSocket(port);

        new Thread(new Runnable(){
            @Override
            public void run(){
                try{
                    Socket socket;
                    while((socket = server.accept()) != null){
                        new Thread(new TTunnel(keyPair.getPrivate(), socket)).start();
                    }
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void stop()throws IOException {
        if(!server.isClosed()){
            server.close();
        }
    }

    public int getPort(){
        return server.getLocalPort();
    }
}
