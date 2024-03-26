package unet.kad4.messages;

import unet.kad4.messages.inter.Message;
import unet.kad4.messages.inter.MessageType;
import unet.kad4.messages.inter.MethodMessageBase;

@Message(method = "get_port", type = MessageType.REQ_MSG)
public class GetPortRequest extends MethodMessageBase {

    public GetPortRequest(){
    }

    public GetPortRequest(byte[] tid){
        super(tid);
    }
}
