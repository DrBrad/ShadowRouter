package unet.shadowrouter.proxy.socks.socks;

import unet.shadowrouter.proxy.socks.SocksProxy;

import java.io.IOException;

public abstract class SocksBase {

    protected SocksProxy proxy;

    public SocksBase(SocksProxy proxy){
        this.proxy = proxy;
    }

    public abstract byte getCommand()throws IOException;

    public abstract void connect()throws IOException;

    public abstract void bind()throws IOException;
}
