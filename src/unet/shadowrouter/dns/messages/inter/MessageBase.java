package unet.shadowrouter.dns.messages.inter;

public abstract class MessageBase {

    protected int id;
    protected String domain;
    protected Type type = Type.A;
    protected DnsClass dnsClass = DnsClass.IN;

    public abstract byte[] encode();

    public abstract void decode(byte[] buf);

    public void setID(int id){
        this.id = id;
    }

    public int getID(){
        return id;
    }

    public void setDomain(String domain){
        this.domain = domain;
    }

    public String getDomain(){
        return domain;
    }

    public void setType(Type type){
        this.type = type;
    }

    public Type getType(){
        return type;
    }

    public void setDnsClass(DnsClass dnsClass){
        this.dnsClass = dnsClass;
    }

    public DnsClass getDnsClass(){
        return dnsClass;
    }
}
