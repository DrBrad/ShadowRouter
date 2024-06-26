package unet.shadowrouter.kad.refresh;

import unet.kad4.refresh.tasks.inter.Task;
import unet.kad4.routing.kb.KBucket;
import unet.kad4.rpc.PingResponseListener;
import unet.kad4.rpc.events.ErrorResponseEvent;
import unet.kad4.rpc.events.ResponseEvent;
import unet.kad4.rpc.events.StalledEvent;
import unet.kad4.rpc.events.inter.ResponseCallback;
import unet.kad4.utils.Node;
import unet.kad4.utils.UID;
import unet.shadowrouter.kad.messages.FindNodeRequest;
import unet.shadowrouter.kad.messages.FindNodeResponse;
import unet.shadowrouter.kad.messages.PingRequest;
import unet.shadowrouter.kad.utils.SecureNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BucketRefreshTask extends Task {

    @Override
    public void execute(){
        FindNodeResponseListener listener = new FindNodeResponseListener();
        //System.out.println("EXECUTING BUCKET REFRESH");

        for(int i = 1; i < UID.ID_LENGTH; i++){
            if(getRoutingTable().getBucketSize(i) < KBucket.MAX_BUCKET_SIZE){ //IF THE BUCKET IS FULL WHY SEARCH... WE CAN REFILL BY OTHER PEER PINGS AND LOOKUPS...
                UID k = getRoutingTable().getDerivedUID().generateNodeIdByDistance(i);

                List<Node> closest = getRoutingTable().findClosest(k, KBucket.MAX_BUCKET_SIZE);
                if(closest.isEmpty()){
                    continue;
                }

                for(Node n : closest){
                    FindNodeRequest request = new FindNodeRequest();
                    request.setDestination(n.getAddress());
                    request.setTarget(k);

                    try{
                        getServer().send(request, n, listener);

                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private class FindNodeResponseListener extends ResponseCallback {

        private PingResponseListener listener;
        private List<Node> queries;

        public FindNodeResponseListener(){
            listener = new PingResponseListener(getRoutingTable());
            queries = new ArrayList<>();
        }

        @Override
        public void onResponse(ResponseEvent event){
            event.getNode().setSeen();
            //System.out.println("SEEN FN "+event.getNode());
            FindNodeResponse response = (FindNodeResponse) event.getMessage();

            if(response.hasNodes()){
                List<SecureNode> nodes = response.getAllNodes();

                long now = System.currentTimeMillis();
                for(int i = nodes.size()-1; i > -1; i--){
                    if(queries.contains(nodes.get(i)) || getRoutingTable().hasQueried(nodes.get(i), now)){
                        nodes.remove(nodes.get(i));
                    }
                }

                queries.addAll(nodes);

                for(SecureNode n : nodes){
                    if(getRoutingTable().isSecureOnly() && !n.hasSecureID()){
                        //System.out.println("SKIPPING "+now+"  "+n.getLastSeen()+"  "+n);
                        continue;
                    }

                    PingRequest req = new PingRequest();
                    req.setDestination(n.getAddress());
                    try{
                        getServer().send(req, n, listener);
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public void onErrorResponse(ErrorResponseEvent event){
            event.getNode().setSeen();
        }

        @Override
        public void onStalled(StalledEvent event){
            event.getNode().markStale();
        }
    }
}
