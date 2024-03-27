package unet.shadowrouter.proxy.socks.socks;

import unet.kad4.messages.GetPortRequest;
import unet.kad4.messages.GetPortResponse;
import unet.kad4.rpc.events.ErrorResponseEvent;
import unet.kad4.rpc.events.ResponseEvent;
import unet.kad4.rpc.events.StalledEvent;
import unet.kad4.rpc.events.inter.ResponseCallback;
import unet.kad4.utils.Node;
import unet.shadowrouter.proxy.socks.SocksProxy;
import unet.shadowrouter.proxy.socks.socks.inter.AType;
import unet.shadowrouter.proxy.socks.socks.inter.Command;
import unet.shadowrouter.proxy.socks.socks.inter.ReplyCode;
import unet.shadowrouter.proxy.socks.socks.inter.SocksBase;
import unet.shadowrouter.tunnel.inter.AddressType;
import unet.shadowrouter.tunnel.tcp.Tunnel;

import java.io.IOException;
import java.net.*;
import java.util.Collections;
import java.util.List;

public class Socks5 extends SocksBase {

    public static final byte SOCKS_VERSION = 0x05;

    //private InetSocketAddress address;

    public Socks5(SocksProxy proxy){
        super(proxy);
    }

    @Override
    public Command getCommand()throws IOException {
        if(!authenticate()){
            replyCommand(ReplyCode.GENERAL_FAILURE);
            throw new IOException("Failed to authenticate.");
        }

        proxy.getOutputStream().write(new byte[]{ SOCKS_VERSION, 0x00 });

        if(proxy.getInputStream().read() != SOCKS_VERSION){
            replyCommand(ReplyCode.UNASSIGNED);
            throw new IOException("Invalid Socks version");
        }
        Command command = Command.getCommandFromCode((byte) proxy.getInputStream().read());

        if(command.equals(Command.INVALID)){
            replyCommand(ReplyCode.COMMAND_NOT_SUPPORTED);
        }

        //RSV
        proxy.getInputStream().read();

        AType atype = AType.getATypeFromCode((byte) proxy.getInputStream().read());
        //byte[] addr;
        //InetAddress address;

        switch(atype){
            case IPv4:
                this.atype = AddressType.IPv4;
                address = new byte[atype.getLength()];
                proxy.getInputStream().read(address);
                //address = InetAddress.getByAddress(addr);
                //System.out.println(new String(addr));
                break;

            case DOMAIN:
                this.atype = AddressType.DOMAIN;
                address = new byte[proxy.getInputStream().read()];
                proxy.getInputStream().read(address);
                //System.out.println(new String(addr));
                //address = InetAddress.getByName(new String(addr));
                break;

            case IPv6:
                this.atype = AddressType.IPv6;
                address = new byte[atype.getLength()];
                proxy.getInputStream().read(address);
                //address = InetAddress.getByAddress(addr);
                //System.out.println(new String(addr));
                break;

            default:
                replyCommand(ReplyCode.A_TYPE_NOT_SUPPORTED);
                throw new IOException("Invalid A-Type.");
        }

        port = ((proxy.getInputStream().read() & 0xff) << 8) | (proxy.getInputStream().read() & 0xff);
        //this.address = new InetSocketAddress(address, port);

        return command;
    }

    @Override
    public void connect()throws IOException {
        try{
            /*
            Socket socket = new Socket();

            InetSocketAddress address;
            switch(atype){
                case IPv4:
                case IPv6:
                    address = new InetSocketAddress(InetAddress.getByAddress(this.address), port);
                    break;

                case DOMAIN:
                    address = new InetSocketAddress(InetAddress.getByName(new String(this.address)), port);
                    break;

                default:
                    return;
            }

            socket.connect(address);
            replyCommand(ReplyCode.GRANTED, address);

            relay(socket);
            */

            List<Node> nodes = proxy.getKademlia().getRoutingTable().getAllNodes();
            if(nodes.size() < 3){
                replyCommand(ReplyCode.GENERAL_FAILURE);
                throw new IOException("Not enough nodes to relay off of.");
            }

            Collections.shuffle(nodes);


            GetPortRequest request = new GetPortRequest();
            request.setDestination(nodes.get(0).getAddress());
            proxy.getKademlia().getServer().send(request, new ResponseCallback(){
                @Override
                public void onResponse(ResponseEvent event){
                    GetPortResponse response = (GetPortResponse) event.getMessage();

                    Tunnel tunnel = new Tunnel();
                    try{
                        tunnel.connect(nodes.get(0), response.getPort()); //ENTRY
                        tunnel.relay(nodes.get(1));
                        tunnel.relay(nodes.get(2));
                        tunnel.exit(address, port, atype);

                        replyCommand(ReplyCode.GRANTED);

                    }catch(Exception e){
                        try{
                            replyCommand(ReplyCode.HOST_UNREACHABLE);//, address);
                            proxy.getSocket().close();

                        }catch(IOException ex){
                        }
                        return;
                    }

                    try{
                        System.out.println(
                                nodes.get(0).getUID()+" > "+
                                nodes.get(1).getUID()+" > "+
                                nodes.get(2).getUID()+" > "+
                                new String(address)+" : "+port);
                        relay(tunnel);

                    }catch(Exception e){
                        try{
                            proxy.getSocket().close();

                        }catch(IOException ex){
                        }
                    }
                }

                @Override
                public void onErrorResponse(ErrorResponseEvent event){
                    try{
                        System.out.println("ERROR - GET_PORT");
                        replyCommand(ReplyCode.GENERAL_FAILURE);//, address);
                        proxy.getSocket().close();

                    }catch(IOException e){
                    }
                }

                @Override
                public void onStalled(StalledEvent event){
                    try{
                        System.out.println("Stalled - GET_PORT");
                        replyCommand(ReplyCode.GENERAL_FAILURE);//, address);
                        proxy.getSocket().close();

                    }catch(IOException e){
                    }
                }
            });
        }catch(IOException e){
        }
    }

    @Override
    public void bind()throws IOException {
        //NOT SURE HOW WE WANT TO HANDLE THIS ONE...
        /*
        try{
            ServerSocket server = new ServerSocket(0);
            replyCommand(ReplyCode.GRANTED, new InetSocketAddress(InetAddress.getLocalHost(), server.getLocalPort()));

            //DO WE LOOP THIS...?
            Socket socket ;
            while((socket = server.accept()) != null){
                replyCommand(ReplyCode.GRANTED, new InetSocketAddress(socket.getInetAddress(), socket.getPort()));
                relay(socket);
            }

            server.close();

        }catch(IOException e){
            replyCommand(ReplyCode.CONNECTION_NOT_ALLOWED);
        }
        */
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

    private void replyCommand(ReplyCode code)throws IOException {
        byte[] reply = new byte[10];

        reply[0] = SOCKS_VERSION;
        reply[1] = code.getCode();
        reply[2] = 0x00;
        reply[3] = AType.IPv4.getCode();

        proxy.getOutputStream().write(reply);
    }

    private void replyCommand(ReplyCode code, InetSocketAddress address)throws IOException {
        byte[] reply = new byte[6+address.getAddress().getAddress().length];

        reply[0] = SOCKS_VERSION;
        reply[1] = code.getCode();
        reply[2] = 0x00;
        reply[3] = (address.getAddress() instanceof Inet4Address) ? AType.IPv4.getCode() : AType.IPv6.getCode();
        System.arraycopy(address.getAddress().getAddress(), 0, reply, 4, reply.length-6);
        reply[reply.length-2] = (byte)((address.getPort() & 0xff00) >> 8);
        reply[reply.length-1] = (byte)(address.getPort() & 0x00ff);

        proxy.getOutputStream().write(reply);
    }
}
