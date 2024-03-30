package unet.shadowrouter.kad;

import unet.kad4.routing.kb.KBucket;
import unet.kad4.rpc.RequestListener;
import unet.kad4.rpc.events.RequestEvent;
import unet.kad4.rpc.events.inter.RequestMapping;
import unet.shadowrouter.kad.messages.FindNodeRequest;
import unet.shadowrouter.kad.messages.FindNodeResponse;
import unet.shadowrouter.kad.messages.GetPortResponse;
import unet.shadowrouter.kad.messages.PingResponse;
import unet.shadowrouter.kad.utils.SecureNode;

import java.util.List;

public class SRequestListener extends RequestListener {

    private int port;

    public SRequestListener(int port){
        this.port = port;
    }

    @RequestMapping("ping")
    public void onPingRequest(RequestEvent event){
        if(event.isPreventDefault()){
            return;
        }

        PingResponse response = new PingResponse(event.getMessage().getTransactionID());
        response.setDestination(event.getMessage().getOrigin());
        response.setPublic(event.getMessage().getOrigin());
        event.setResponse(response);
    }

    @RequestMapping("find_node")
    public void onFindNodeRequest(RequestEvent event){
        if(event.isPreventDefault()){
            return;
        }

        FindNodeRequest request = (FindNodeRequest) event.getMessage();

        List<SecureNode> nodes = (List<SecureNode>)(List<?>) getRoutingTable().findClosest(request.getTarget(), KBucket.MAX_BUCKET_SIZE);
        nodes.remove((SecureNode) event.getNode());

        if(!nodes.isEmpty()){
            FindNodeResponse response = new FindNodeResponse(request.getTransactionID());
            response.setDestination(event.getMessage().getOrigin());
            response.setPublic(event.getMessage().getOrigin());
            response.addNodes(nodes);
            event.setResponse(response);
        }
    }

    @RequestMapping("get_port")
    public void onGetPortRequest(RequestEvent event){
        if(event.isPreventDefault()){
            return;
        }

        GetPortResponse response = new GetPortResponse(event.getMessage().getTransactionID());
        response.setDestination(event.getMessage().getOrigin());
        response.setPublic(event.getMessage().getOrigin());
        response.setPort(port);
        event.setResponse(response);
    }
}
