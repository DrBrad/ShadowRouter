package unet.shadowrouter.kad.messages;


import unet.bencode.variables.BencodeObject;
import unet.kad4.messages.inter.Message;
import unet.kad4.messages.inter.MessageException;
import unet.kad4.messages.inter.MessageType;
import unet.kad4.messages.inter.MethodMessageBase;

@Message(method = "find_circuit", type = MessageType.REQ_MSG)
public class PutCircuitRequest extends MethodMessageBase {

    //private TunnelType tunnelType;

    public PutCircuitRequest(){
    }

    public PutCircuitRequest(byte[] tid){
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

    /*
    public void setTunnelType(TunnelType tunnelType){
        this.tunnelType = tunnelType;
    }

    public void*/
}
