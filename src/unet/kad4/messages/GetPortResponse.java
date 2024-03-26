package unet.kad4.messages;

import unet.bencode.variables.BencodeObject;
import unet.kad4.messages.inter.Message;
import unet.kad4.messages.inter.MessageType;
import unet.kad4.messages.inter.MethodMessageBase;

@Message(method = "get_port", type = MessageType.RSP_MSG)
public class GetPortResponse extends MethodMessageBase {

    private int port;

    public GetPortResponse(byte[] tid){
        super(tid);
    }

    @Override
    public BencodeObject encode(){
        BencodeObject ben = super.encode();
        ben.getBencodeObject(type.innerKey()).put("port", port);

        return ben;
    }

    @Override
    public void decode(BencodeObject ben){
        super.decode(ben);

        if(!ben.getBencodeObject(type.innerKey()).containsKey("port")){
            //throw new MessageException("Response to "+FIND_NODE+" did not contain 'node' or 'node6'", ErrorMessage.ErrorType.PROTOCOL);
        }

        port = ben.getBencodeObject(type.innerKey()).getInteger("port");
    }

    public void setPort(int port){
        this.port = port;
    }

    public int getPort(){
        return port;
    }
}
