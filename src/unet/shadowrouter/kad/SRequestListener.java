package unet.shadowrouter.kad;

import unet.kad4.rpc.RequestListener;
import unet.kad4.rpc.events.RequestEvent;
import unet.kad4.rpc.events.inter.RequestMapping;

public class SRequestListener extends RequestListener {

    @RequestMapping("relay")
    public void onPingRequest(RequestEvent event){

    }
}
