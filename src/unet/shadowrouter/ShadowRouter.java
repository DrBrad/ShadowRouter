package unet.shadowrouter;

import unet.kad4.kad.KademliaBase;
import unet.kad4.kad.Server;
import unet.kad4.refresh.RefreshHandler;
import unet.kad4.routing.BucketTypes;
import unet.kad4.routing.inter.RoutingTable;
import unet.kad4.rpc.KRequestListener;
import unet.kad4.rpc.events.ErrorResponseEvent;
import unet.kad4.rpc.events.ResponseEvent;
import unet.kad4.rpc.events.StalledEvent;
import unet.kad4.rpc.events.inter.ResponseCallback;
import unet.kad4.utils.Node;
import unet.shadowrouter.kad.JoinNodeListener;
import unet.shadowrouter.kad.SRequestListener;
import unet.shadowrouter.kad.ShadowServer;
import unet.shadowrouter.kad.messages.*;
import unet.shadowrouter.kad.refresh.BucketRefreshTask;
import unet.shadowrouter.kad.refresh.StaleRefreshTask;
import unet.shadowrouter.kad.utils.SecureNode;
import unet.shadowrouter.proxy.socks.socks.inter.ReplyCode;
import unet.shadowrouter.tunnel.tcp.RelayServer;
import unet.shadowrouter.tunnel.tcp.Tunnel;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

public class ShadowRouter extends KademliaBase {

    private RelayServer relay;

    public ShadowRouter(){
        this(BucketTypes.KADEMLIA.getRoutingTable());
    }

    public ShadowRouter(String bucketType){
        this(BucketTypes.fromString(bucketType).getRoutingTable());
    }

    public ShadowRouter(RoutingTable routingTable){
        //super(routingTable);

        this.routingTable = routingTable;
        System.out.println("Starting with bucket type: "+routingTable.getClass().getSimpleName());
        server = new ShadowServer(this);
        refresh = new RefreshHandler(this);


        routingTable.setSecureOnly(false);
        //refresh.setRefreshTime(30000);

        relay = new RelayServer(this);

        BucketRefreshTask bucketRefreshTask = new BucketRefreshTask();

        routingTable.addRestartListener(new RoutingTable.RestartListener(){
            @Override
            public void onRestart(){
                bucketRefreshTask.execute();
            }
        });

        try{
            server.registerMessage(PingRequest.class);
            server.registerMessage(PingResponse.class);
            server.registerMessage(FindNodeRequest.class);
            server.registerMessage(FindNodeResponse.class);
            server.registerMessage(GetPortRequest.class);
            server.registerMessage(GetPortResponse.class);

            refresh.addOperation(bucketRefreshTask);
            refresh.addOperation(new StaleRefreshTask());

        }catch(NoSuchFieldException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
    }

    public void startRelay(int port)throws IOException {
        relay.start(port);
        try{
            server.registerRequestListener(new SRequestListener(port));
        }catch(NoSuchFieldException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
    }

    @Override
    public void join(int localPort, InetSocketAddress address)throws IOException {
        super.join(localPort, address);

        FindNodeRequest request = new FindNodeRequest();
        request.setDestination(address);
        request.setTarget(routingTable.getDerivedUID());
        server.send(request, new JoinNodeListener(this));
    }

    @Override
    public void stop(){
        try{
            relay.stop();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /*
    public void tcpRelay(Socket socket, )throws IOException {
            List<Node> nodes = getRoutingTable().getAllNodes();
            if(nodes.size() < 3){
                //socket.replyCommand(ReplyCode.GENERAL_FAILURE);
                throw new IOException("Not enough nodes to relay off of.");
            }

            Collections.shuffle(nodes);


            GetPortRequest request = new GetPortRequest();
            request.setDestination(nodes.get(0).getAddress());
            server.send(request, new ResponseCallback(){
                @Override
                public void onResponse(ResponseEvent event){
                    GetPortResponse response = (GetPortResponse) event.getMessage();

                    Tunnel tunnel = new Tunnel();
                    try{
                        tunnel.connect((SecureNode) nodes.get(0), response.getPort()); //ENTRY
                        tunnel.relay((SecureNode) nodes.get(1));
                        tunnel.relay((SecureNode) nodes.get(2));
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
    }
    */
}
