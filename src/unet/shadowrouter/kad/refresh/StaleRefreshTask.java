package unet.shadowrouter.kad.refresh;

import unet.kad4.refresh.tasks.inter.Task;
import unet.kad4.rpc.PingResponseListener;
import unet.kad4.utils.Node;
import unet.shadowrouter.kad.messages.PingRequest;

import java.io.IOException;
import java.util.List;

public class StaleRefreshTask extends Task {

    @Override
    public void execute(){
        PingResponseListener listener = new PingResponseListener(getRoutingTable());
        List<Node> nodes = getRoutingTable().getAllUnqueriedNodes();

        for(Node node : nodes){
            PingRequest request = new PingRequest();
            request.setDestination(node.getAddress());

            try{
                getServer().send(request, node, listener);
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}
