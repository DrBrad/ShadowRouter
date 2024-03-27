package unet.shadowrouter.proxy.socks.socks.inter;

import unet.shadowrouter.proxy.socks.SocksProxy;

import java.io.IOException;

public abstract class SocksBase {

    protected SocksProxy proxy;

    public SocksBase(SocksProxy proxy){
        this.proxy = proxy;
    }

    public abstract Command getCommand()throws IOException;

    public abstract void connect()throws IOException;

    public abstract void bind()throws IOException;
}
