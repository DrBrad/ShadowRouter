package unet.shadowrouter.kad.messages;


import unet.bencode.variables.BencodeObject;
import unet.kad4.messages.inter.Message;
import unet.kad4.messages.inter.MessageException;
import unet.kad4.messages.inter.MessageType;
import unet.kad4.messages.inter.MethodMessageBase;

@Message(method = "find_circuit", type = MessageType.RSP_MSG)
public class PutCircuitResponse extends MethodMessageBase {

    public PutCircuitResponse(byte[] tid){
        super(tid);
    }

    @Override
    public BencodeObject encode(){
        return super.encode();
    }

    @Override
    public void decode(BencodeObject ben)throws MessageException {
        super.decode(ben);
    }
}
