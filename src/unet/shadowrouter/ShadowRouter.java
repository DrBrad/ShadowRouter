package unet.shadowrouter;

import unet.kad4.Kademlia;
import unet.kad4.kad.KademliaBase;
import unet.kad4.messages.*;
import unet.kad4.refresh.tasks.BucketRefreshTask;
import unet.kad4.refresh.tasks.StaleRefreshTask;
import unet.kad4.routing.BucketTypes;
import unet.kad4.routing.inter.RoutingTable;
import unet.kad4.rpc.JoinNodeListener;
import unet.kad4.rpc.KRequestListener;
import unet.kad4.utils.Node;
import unet.shadowrouter.kad.SRequestListener;
import unet.shadowrouter.tunnel.tcp.RelayServer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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
        super(routingTable);

        routingTable.setSecureOnly(false);
        refresh.setRefreshTime(30000);

        relay = new RelayServer(server.getKeyPair());

        BucketRefreshTask bucketRefreshTask = new BucketRefreshTask();

        routingTable.addRestartListener(new RoutingTable.RestartListener(){
            @Override
            public void onRestart(){
                bucketRefreshTask.execute();
            }
        });

        try{
            registerRequestListener(new KRequestListener());

            registerMessage(PingRequest.class);
            registerMessage(PingResponse.class);
            registerMessage(FindNodeRequest.class);
            registerMessage(FindNodeResponse.class);
            registerMessage(GetPortRequest.class);
            registerMessage(GetPortResponse.class);

            refresh.addOperation(bucketRefreshTask);
            refresh.addOperation(new StaleRefreshTask());

        }catch(NoSuchFieldException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
    }

    public void startRelay(int port)throws IOException {
        relay.start(port);
        try{
            registerRequestListener(new SRequestListener(port));
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
    private Kademlia kad;
    private RelayServer relay;

    public ShadowRouter(){
        kad = new Kademlia();
        kad.getRoutingTable().setSecureOnly(false);
        kad.getRefreshHandler().setRefreshTime(30000);
        relay = new RelayServer(kad.getServer().getKeyPair());
    }

    public void join(Node node, int dhtPort, int tcpPort)throws IOException, NoSuchFieldException, IllegalAccessException,
            InvocationTargetException {
        relay.start(tcpPort);

        kad.registerRequestListener(new SRequestListener(tcpPort));
        kad.join(dhtPort, node);
    }

    public void join(List<Node> nodes, int dhtPort, int tcpPort)throws IOException, NoSuchFieldException, IllegalAccessException,
            InvocationTargetException {
        relay.start(tcpPort);

        for(Node node : nodes){
            kad.getRoutingTable().insert(node);
        }

        kad.registerRequestListener(new SRequestListener(tcpPort));
        kad.join(dhtPort, nodes.get(0));
    }

    public void bind(int dhtPort, int tcpPort)throws IOException, NoSuchFieldException, IllegalAccessException,
            InvocationTargetException {
        relay.start(tcpPort);

        kad.registerRequestListener(new SRequestListener(tcpPort));
        kad.bind(dhtPort);
    }

    public void stop()throws IOException {
        relay.stop();
        kad.stop();
    }

    @Override
    public void onRestart(){
        kad.getRoutingTable().removeRestartListener(this);

        System.out.println("WE CAN NOW START RELAYING");
    }
    */
}
