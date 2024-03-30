package unet.shadowrouter.kad.messages;

import unet.kad4.messages.inter.Message;
import unet.kad4.messages.inter.MessageException;
import unet.kad4.messages.inter.MessageType;
import unet.shadowrouter.kad.messages.inter.MethodMessageBase;

@Message(method = "get_port", type = MessageType.REQ_MSG)
public class GetPortRequest extends MethodMessageBase {

    public GetPortRequest(){
    }

    public GetPortRequest(byte[] tid)throws MessageException {
        super(tid);
    }
}
