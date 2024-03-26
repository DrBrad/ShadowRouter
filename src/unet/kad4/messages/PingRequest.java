package unet.kad4.messages;

import unet.kad4.messages.inter.Message;
import unet.kad4.messages.inter.MessageType;
import unet.kad4.messages.inter.MethodMessageBase;

@Message(method = "ping", type = MessageType.REQ_MSG)
public class PingRequest extends MethodMessageBase {

    public PingRequest(){
    }

    public PingRequest(byte[] tid){
        super(tid);
    }
}
