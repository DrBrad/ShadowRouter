package unet.shadowrouter.proxy.socks.socks.inter;

import unet.shadowrouter.proxy.socks.SocksProxy;
import unet.shadowrouter.tunnel.inter.AddressType;
import unet.shadowrouter.tunnel.tcp.Tunnel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public abstract class SocksBase {

    protected SocksProxy proxy;
    protected AddressType atype;
    protected byte[] address;
    protected int port;

    public SocksBase(SocksProxy proxy){
        this.proxy = proxy;
    }

    public abstract Command getCommand()throws IOException;

    public abstract void connect()throws IOException;

    public abstract void bind()throws IOException;

    //private boolean complete;

    public void relay(Tunnel tunnel)throws IOException {
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run(){
                try{
                    transfer(proxy.getInputStream(), tunnel.getOutputStream());
                    //while(!socket.isClosed() && !relay.isClosed()){
                    //    in.transferTo(relay.getOutputStream());
                    //}
                }catch(IOException e){
                    //e.printStackTrace();
                }

                /*
                if(complete){
                    try{
                        tunnel.close();
                        proxy.getSocket().close();
                    }catch(IOException e){
                    }
                    return;
                }
                complete = true;
                */
            }
        });//.start();
        thread.start();

        try{
            transfer(tunnel.getInputStream(), proxy.getOutputStream());
        }catch(IOException e){
            //e.printStackTrace();
        }

        try{
            thread.join();
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        tunnel.close();
        proxy.getSocket().close();

        //while(!relay.isClosed() && !socket.isClosed()){
        //    relay.getInputStream().transferTo(out);
        //}
        /*
        if(complete){
            tunnel.close();
            proxy.getSocket().close();
            return;
        }
        complete = true;*/
    }

    private void transfer(InputStream in, OutputStream out)throws IOException {
        byte[] buf = new byte[4096];
        int len;
        while((len = in.read(buf)) != -1){
            out.write(buf, 0, len);
        }

        //in.close();
        //out.close();
    }
}
