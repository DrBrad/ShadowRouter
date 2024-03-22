package unet.shadowrouter.kad.messages;


import unet.bencode.variables.BencodeObject;
import unet.kad4.messages.inter.Message;
import unet.kad4.messages.inter.MessageType;
import unet.kad4.messages.inter.MethodMessageBase;

@Message(method = "find_circuit", type = MessageType.RSP_MSG)
public class GetCircuitResponse extends MethodMessageBase {

    public GetCircuitResponse(byte[] tid){
        super(tid);
    }

    @Override
    public BencodeObject encode(){
        return super.encode();
    }

    @Override
    public void decode(BencodeObject ben){
        super.decode(ben);
    }
}
