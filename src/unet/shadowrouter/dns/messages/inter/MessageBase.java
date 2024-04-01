package unet.shadowrouter.dns.messages.inter;

public abstract class MessageBase {

    protected int id;

    public abstract byte[] encode();

    public abstract void decode(byte[] buf);

    public void setID(int id){
        this.id = id;
    }

    public int getID(){
        return id;
    }
}
