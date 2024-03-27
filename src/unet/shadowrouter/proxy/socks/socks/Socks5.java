package unet.shadowrouter.proxy.socks.socks;

import unet.shadowrouter.proxy.socks.SocksProxy;
import unet.shadowrouter.proxy.socks.socks.inter.AType;
import unet.shadowrouter.proxy.socks.socks.inter.Command;
import unet.shadowrouter.proxy.socks.socks.inter.SocksBase;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Socks5 extends SocksBase {

    private InetSocketAddress address;

    public Socks5(SocksProxy proxy){
        super(proxy);
    }

    @Override
    public Command getCommand()throws IOException {
        if(!authenticate()){
            //SEND ERROR CODE...
            throw new IOException("Failed to authenticate.");
        }

        proxy.getOutputStream().write(new byte[]{ 0x05, 0x00 });

        if(proxy.getInputStream().read() != 0x05){
            replyCommand((byte) 0xff);
            throw new IOException("Invalid Socks version");
        }
        Command command = Command.getCommandFromCode((byte) proxy.getInputStream().read());

        if(command.equals(Command.INVALID)){
            replyCommand((byte) 0x07);
        }

        //RSV
        proxy.getInputStream().read();

        AType atype = AType.getATypeFromCode((byte) proxy.getInputStream().read());
        byte[] addr;
        InetAddress address;

        switch(atype){
            case IPv4:
                addr = new byte[atype.getLength()];
                proxy.getInputStream().read(addr);
                address = InetAddress.getByAddress(addr);
                break;

            case DOMAIN:
                addr = new byte[proxy.getInputStream().read()];
                proxy.getInputStream().read(addr);
                address = InetAddress.getByName(new String(addr));
                break;

            case IPv6:
                addr = new byte[atype.getLength()];
                proxy.getInputStream().read(addr);
                address = InetAddress.getByAddress(addr);
                break;

            default:
                replyCommand((byte) 0x08);
                throw new IOException("Invalid A-Type.");
        }

        int port = ((proxy.getInputStream().read() & 0xff) << 8) | (proxy.getInputStream().read() & 0xff);
        this.address = new InetSocketAddress(address, port);

        return command;
    }

    @Override
    public void connect()throws IOException {
        try{
            Socket server = new Socket();
            server.connect(address);
            replyCommand((byte) 0x00);

            relay(server);

            server.close();

        }catch(IOException e){
            replyCommand((byte) 0x04);
        }
    }

    @Override
    public void bind()throws IOException {

    }

    public void udp()throws IOException {

    }

    private boolean authenticate()throws IOException {
        byte method = (byte) proxy.getInputStream().read();
        String methods = "";

        for(int i = 0; i < method; i++){
            methods += ",-"+proxy.getInputStream().read()+'-';
        }

        return (methods.indexOf("-0-") != -1 || methods.indexOf("-00-") != -1);
    }

    private void replyCommand(byte replyCode)throws IOException {
        byte[] reply;

        if(address == null){
            reply = new byte[10];
        }else{
            reply = new byte[6+address.getAddress().getAddress().length];
            reply[3] = (byte) ((address.getAddress() instanceof Inet4Address) ? 0x01 : 0x03);
            System.arraycopy(address.getAddress().getAddress(), 0, reply, 4, reply.length-6);
            reply[reply.length-2] = (byte)((address.getPort() & 0xFF00) >> 8);
            reply[reply.length-1] = (byte)(address.getPort() & 0x00FF);
        }

        reply[0] = 0x05;
        reply[1] = replyCode;
        reply[2] = 0x00;

        proxy.getOutputStream().write(reply);
    }
}
