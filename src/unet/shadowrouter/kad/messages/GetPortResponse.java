package unet.shadowrouter.kad.messages;

import unet.bencode.variables.BencodeObject;
import unet.kad4.messages.inter.Message;
import unet.kad4.messages.inter.MessageException;
import unet.kad4.messages.inter.MessageType;
import unet.shadowrouter.kad.messages.inter.MethodMessageBase;

@Message(method = "get_port", type = MessageType.RSP_MSG)
public class GetPortResponse extends MethodMessageBase {

    private int port;

    public GetPortResponse(byte[] tid){
        super(tid);
    }

    @Override
    public BencodeObject encode(){
        BencodeObject ben = super.encode();
        ben.getBencodeObject(type.innerKey()).put("port", new byte[]{
                (byte) ((port >> 8) & 0xff),
                (byte) (port & 0xff)
        });

        return ben;
    }

    @Override
    public void decode(BencodeObject ben)throws MessageException {
        super.decode(ben);

        if(!ben.getBencodeObject(type.innerKey()).containsKey("port")){
            throw new MessageException("Protocol Error, such as a malformed packet.", 203);
        }

        port = ((ben.getBencodeObject(type.innerKey()).getBytes("port")[0] & 0xff) << 8) |
                (ben.getBencodeObject(type.innerKey()).getBytes("port")[1] & 0xff);
    }

    public void setPort(int port){
        this.port = port;
    }

    public int getPort(){
        return port;
    }
}
