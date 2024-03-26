package unet.shadowrouter.kad;

import unet.kad4.messages.GetPortResponse;
import unet.kad4.rpc.RequestListener;
import unet.kad4.rpc.events.RequestEvent;
import unet.kad4.rpc.events.inter.RequestMapping;

public class SRequestListener extends RequestListener {

    private int port;

    public SRequestListener(int port){
        this.port = port;
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
