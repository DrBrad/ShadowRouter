package unet.shadowrouter.kad;

import unet.kad4.kad.KademliaBase;
import unet.kad4.rpc.PingResponseListener;
import unet.kad4.rpc.events.ErrorResponseEvent;
import unet.kad4.rpc.events.ResponseEvent;
import unet.kad4.rpc.events.StalledEvent;
import unet.kad4.rpc.events.inter.ResponseCallback;
import unet.shadowrouter.kad.messages.FindNodeResponse;
import unet.shadowrouter.kad.messages.PingRequest;
import unet.shadowrouter.kad.utils.SecureNode;

import java.io.IOException;
import java.util.List;

public class JoinNodeListener extends ResponseCallback {

    private KademliaBase kademlia;

    public JoinNodeListener(KademliaBase kademlia){
        this.kademlia = kademlia;
    }

    @Override
    public void onResponse(ResponseEvent event){
        kademlia.getRoutingTable().insert(event.getNode());
        System.out.println("JOINED "+event.getNode());

        FindNodeResponse response = (FindNodeResponse) event.getMessage();

        if(response.hasNodes()){
            List<SecureNode> nodes = response.getAllNodes();

            PingResponseListener listener = new PingResponseListener(kademlia.getRoutingTable());

            long now = System.currentTimeMillis();
            for(SecureNode n : nodes){
                if((kademlia.getRoutingTable().isSecureOnly() && !n.hasSecureID()) || n.hasQueried(now)){
                    System.out.println("SKIPPING "+now+"  "+n.getLastSeen()+"  "+n);
                    continue;
                }

                PingRequest req = new PingRequest();
                req.setDestination(n.getAddress());
                try{
                    kademlia.getServer().send(req, n, listener);
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }

        if(!kademlia.getRefreshHandler().isRunning()){
            kademlia.getRefreshHandler().start();
        }
    }

    @Override
    public void onErrorResponse(ErrorResponseEvent event){
    }

    @Override
    public void onStalled(StalledEvent event){

    }
}
