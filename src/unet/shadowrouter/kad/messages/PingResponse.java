package unet.shadowrouter.kad.messages;

import unet.kad4.messages.inter.Message;
import unet.kad4.messages.inter.MessageType;
import unet.shadowrouter.kad.messages.inter.MethodMessageBase;

@Message(method = "ping", type = MessageType.RSP_MSG)
public class PingResponse extends MethodMessageBase {

    public PingResponse(byte[] tid){
        super(tid);
    }
}
