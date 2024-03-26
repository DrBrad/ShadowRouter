package unet.shadowrouter;

import unet.kad4.kad.KademliaBase;
import unet.kad4.messages.*;
import unet.kad4.refresh.tasks.BucketRefreshTask;
import unet.kad4.refresh.tasks.StaleRefreshTask;
import unet.kad4.routing.BucketTypes;
import unet.kad4.routing.inter.RoutingTable;
import unet.kad4.rpc.JoinNodeListener;
import unet.kad4.rpc.KRequestListener;
import unet.shadowrouter.kad.SRequestListener;
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
        super(routingTable);

        routingTable.setSecureOnly(false);
        refresh.setRefreshTime(30000);

        relay = new RelayServer(this);

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
}
