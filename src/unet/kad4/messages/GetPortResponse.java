package unet.kad4.messages;

import unet.kad4.messages.inter.Message;
import unet.kad4.messages.inter.MessageType;
import unet.kad4.messages.inter.MethodMessageBase;

@Message(method = "get_port", type = MessageType.RSP_MSG)
public class GetPortResponse extends MethodMessageBase {

    public GetPortResponse(byte[] tid){
        super(tid);
    }
}
