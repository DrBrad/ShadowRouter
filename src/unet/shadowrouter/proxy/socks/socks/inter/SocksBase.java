package unet.shadowrouter.proxy.socks.socks.inter;

import unet.shadowrouter.proxy.socks.SocksProxy;
import unet.shadowrouter.tunnel.inter.AddressType;
import unet.shadowrouter.tunnel.tcp.Tunnel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
            }
        });//.start();
        thread.start();

        transfer(tunnel.getInputStream(), proxy.getOutputStream());
        //while(!relay.isClosed() && !socket.isClosed()){
        //    relay.getInputStream().transferTo(out);
        //}
        try{
            thread.join();
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        tunnel.close();
    }

    private void transfer(InputStream in, OutputStream out)throws IOException {
        byte[] buf = new byte[4096];
        int len;
        while((len = in.read(buf)) != -1){
            out.write(buf, 0, len);
            out.flush();
        }
    }
}
