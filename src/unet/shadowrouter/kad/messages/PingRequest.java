package unet.shadowrouter.kad.messages;

import unet.kad4.messages.inter.Message;
import unet.kad4.messages.inter.MessageType;
import unet.shadowrouter.kad.messages.inter.MethodMessageBase;

@Message(method = "ping", type = MessageType.REQ_MSG)
public class PingRequest extends MethodMessageBase {

    public PingRequest(){
    }

    public PingRequest(byte[] tid){
        super(tid);
    }
}
