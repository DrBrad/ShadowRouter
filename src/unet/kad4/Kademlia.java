package unet.kad4;

import unet.kad4.kad.KademliaBase;
import unet.kad4.kad.Server;
import unet.kad4.messages.*;
import unet.kad4.messages.inter.Message;
import unet.kad4.messages.inter.MessageBase;
import unet.kad4.messages.inter.MessageKey;
import unet.kad4.refresh.RefreshHandler;
import unet.kad4.refresh.tasks.BucketRefreshTask;
import unet.kad4.refresh.tasks.StaleRefreshTask;
import unet.kad4.routing.BucketTypes;
import unet.kad4.routing.inter.RoutingTable;
import unet.kad4.rpc.JoinNodeListener;
import unet.kad4.rpc.KRequestListener;
import unet.kad4.rpc.RequestListener;
import unet.kad4.rpc.events.inter.MessageEvent;
import unet.kad4.rpc.events.inter.PriorityComparator;
import unet.kad4.rpc.events.inter.RequestMapping;
import unet.kad4.utils.Node;
import unet.kad4.utils.ReflectMethod;

import java.io.IOException;
import java.lang.reflect.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Kademlia extends KademliaBase {

    public Kademlia(){
        this(BucketTypes.KADEMLIA.getRoutingTable());
    }

    public Kademlia(String bucketType){
        this(BucketTypes.fromString(bucketType).getRoutingTable());
    }

    public Kademlia(RoutingTable routingTable){
        super(routingTable);
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

    public void join(int localPort, InetSocketAddress address)throws IOException {
        super.join(localPort, address);

        FindNodeRequest request = new FindNodeRequest();
        request.setDestination(address);
        request.setTarget(routingTable.getDerivedUID());
        server.send(request, new JoinNodeListener(this));
    }
}
