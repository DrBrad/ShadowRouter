package unet.shadowrouter.proxy.socks.socks;

import unet.shadowrouter.proxy.socks.SocksProxy;

import java.io.IOException;

public class Socks4 extends SocksBase {

    public Socks4(SocksProxy proxy){
        super(proxy);
    }

    @Override
    public byte getCommand()throws IOException {
        return 0;
    }

    @Override
    public void connect()throws IOException {

    }

    @Override
    public void bind()throws IOException {

    }
}
