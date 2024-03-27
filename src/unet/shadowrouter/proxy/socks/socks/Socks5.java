package unet.shadowrouter.proxy.socks.socks;

import unet.shadowrouter.proxy.socks.SocksProxy;

import java.io.IOException;

public class Socks5 extends SocksBase {

    public Socks5(SocksProxy proxy){
        super(proxy);
    }

    @Override
    public byte getCommand()throws IOException {
        if(!authenticate()){
            //SEND ERROR CODE...
            throw new IOException("Failed to authenticate.");
        }

        proxy.getOutputStream().write(new byte[]{ 0x05, 0x00 });

        byte version = (byte) proxy.getInputStream().read();
        byte command = (byte) proxy.getInputStream().read();

        //RSV
        proxy.getInputStream().read();

        //byte atype = (byte) proxy.getInputStream().read();

        switch(proxy.getInputStream().read()){ //A-Type ( IPv4, DOMAIN, IPv6 )
            case 0x01: {//IPv4
                    byte[] addr = new byte[4];
                    proxy.getInputStream().read(addr);
                    System.out.println("IPv4 "+new String(addr));
                }
                break;

            case 0x03: {//DOMAIN
                    byte[] addr = new byte[proxy.getInputStream().read()];
                    proxy.getInputStream().read(addr);
                    System.out.println("DOMAIN "+new String(addr));
                }
                break;

            case 0x04: {//IPv6
                    byte[] addr = new byte[16];
                    proxy.getInputStream().read(addr);
                    System.out.println("IPv6 "+new String(addr));
                }
                break;

            default:
                throw new IOException("Invalid A-Type.");
        }

        int port = ((proxy.getInputStream().read() & 0xff) << 8) | (proxy.getInputStream().read() & 0xff);
        System.out.println(port+"  PORT");

        /*
        int[] addressSize = { -1, 4, -1, -1, 16 };
        int addressLength = addressSize[atype];
        byteAddress[0] = tunnel.getByte();
        if(atype == 0x03){
            addressLength = byteAddress[0]+1;
        }

        for(int i = 1; i < addressLength; i++){
            byteAddress[i] = tunnel.getByte();
        }
        */


        return command;
    }

    @Override
    public void connect()throws IOException {

    }

    @Override
    public void bind()throws IOException {

    }

    private boolean authenticate()throws IOException {
        byte method = (byte) proxy.getInputStream().read();
        String methods = "";

        for(int i = 0; i < method; i++){
            methods += ",-"+proxy.getInputStream().read()+'-';
        }

        return (methods.indexOf("-0-") != -1 || methods.indexOf("-00-") != -1);
    }
}
