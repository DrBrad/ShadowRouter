package unet.shadowrouter;

import unet.kad4.kad.KademliaBase;
import unet.kad4.kad.Server;
import unet.kad4.refresh.RefreshHandler;
import unet.kad4.routing.BucketTypes;
import unet.kad4.routing.inter.RoutingTable;
import unet.kad4.rpc.KRequestListener;
import unet.shadowrouter.kad.JoinNodeListener;
import unet.shadowrouter.kad.SRequestListener;
import unet.shadowrouter.kad.ShadowServer;
import unet.shadowrouter.kad.messages.*;
import unet.shadowrouter.kad.refresh.BucketRefreshTask;
import unet.shadowrouter.kad.refresh.StaleRefreshTask;
import unet.shadowrouter.tunnel.tcp.RelayServer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;

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
}
