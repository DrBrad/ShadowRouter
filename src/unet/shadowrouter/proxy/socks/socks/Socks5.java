package unet.shadowrouter.proxy.socks.socks;

import unet.kad4.rpc.events.ErrorResponseEvent;
import unet.kad4.rpc.events.ResponseEvent;
import unet.kad4.rpc.events.StalledEvent;
import unet.kad4.rpc.events.inter.ResponseCallback;
import unet.kad4.utils.Node;
import unet.shadowrouter.kad.messages.GetPortRequest;
import unet.shadowrouter.kad.messages.GetPortResponse;
import unet.shadowrouter.kad.utils.SecureNode;
import unet.shadowrouter.proxy.dns.messages.MessageBase;
import unet.shadowrouter.proxy.dns.messages.inter.DnsClass;
import unet.shadowrouter.proxy.dns.messages.inter.Types;
import unet.shadowrouter.proxy.dns.records.AAAARecord;
import unet.shadowrouter.proxy.dns.records.ARecord;
import unet.shadowrouter.proxy.dns.records.inter.DnsRecord;
import unet.shadowrouter.proxy.dns.utils.DnsQuery;
import unet.shadowrouter.proxy.socks.SocksProxy;
import unet.shadowrouter.proxy.socks.socks.inter.AType;
import unet.shadowrouter.proxy.socks.socks.inter.Command;
import unet.shadowrouter.proxy.socks.socks.inter.ReplyCode;
import unet.shadowrouter.proxy.socks.socks.inter.SocksBase;
import unet.shadowrouter.tunnel.tcp.Tunnel;

import java.io.IOException;
import java.net.*;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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

        atype = AType.getATypeFromCode((byte) proxy.getInputStream().read());
        //byte[] addr;
        //InetAddress address;

        switch(atype){
            case IPv4:
                address = new byte[atype.getLength()];
                proxy.getInputStream().read(address);
                //address = InetAddress.getByAddress(addr);
                //System.out.println(new String(addr));
                break;

            case DOMAIN:
                address = new byte[proxy.getInputStream().read()];
                proxy.getInputStream().read(address);
                //System.out.println(new String(addr));
                //address = InetAddress.getByName(new String(addr));
                break;

            case IPv6:
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
            InetAddress address = null;

            switch(atype){
                case DOMAIN:
                    DatagramSocket socket = new DatagramSocket();

                    MessageBase request = new MessageBase();
                    int id = new Random().nextInt(32767);
                    request.setID(id);
                    request.addQuery(new DnsQuery(new String(this.address), Types.A, DnsClass.IN));
                    request.setDestination(InetAddress.getByName("8.8.8.8"), 53);

                    byte[] data = request.encode();

                    socket.send(new DatagramPacket(data, data.length, request.getDestinationAddress(), request.getDestinationPort()));

                    DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
                    socket.receive(packet);

                    MessageBase response = new MessageBase();
                    response.decode(packet.getData());

                    if(response.getAnswers().size() < 1){
                        System.out.println(request.getResponseCode()+"  "+new String(this.address));
                    }

                    for(DnsRecord record : response.getAnswers()){
                        if(record.getType() == Types.A){
                            address = ((ARecord) record).getAddress();
                            break;
                        }

                        if(record.getType() == Types.AAAA){
                            address = ((AAAARecord) record).getAddress();
                            break;
                        }
                    }

                    break;

                default:
                    address = InetAddress.getByAddress(this.address);
                    break;
            }

            if(address == null){
                return;
            }

            InetAddress b = address;


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
                        tunnel.connect((SecureNode) nodes.get(0), response.getPort()); //ENTRY
                        tunnel.relay((SecureNode) nodes.get(1));
                        tunnel.relay((SecureNode) nodes.get(2));
                        tunnel.exit(new InetSocketAddress(b, port));

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
                                new String(Socks5.this.address)+" > "+b.getHostAddress()+" : "+port);
                        relay(tunnel);

                    }catch(Exception e){
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
