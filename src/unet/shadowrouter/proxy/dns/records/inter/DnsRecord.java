package unet.shadowrouter.proxy.dns.records.inter;

import unet.shadowrouter.proxy.dns.messages.inter.DnsClass;
import unet.shadowrouter.proxy.dns.messages.inter.Types;

public class DnsRecord {

    protected Types type;
    protected DnsClass dnsClass;
    protected int ttl;

    protected String query;

    public DnsRecord(){
    }

    public DnsRecord(Types type, DnsClass dnsClass, int ttl){
        this.type = type;
        this.dnsClass = dnsClass;
        this.ttl = ttl;
    }

    public byte[] encode(){
        byte[] buf = new byte[getLength()];

        buf[0] = (byte) (type.getCode() >> 8);
        buf[1] = (byte) type.getCode();

        buf[2] = (byte) (dnsClass.getCode() >> 8);
        buf[3] = (byte) dnsClass.getCode();

        buf[4] = (byte) (ttl >> 24);
        buf[5] = (byte) (ttl >> 16);
        buf[6] = (byte) (ttl >> 8);
        buf[7] = (byte) ttl;

        return buf;
    }

    public void decode(byte[] buf, int off){
        dnsClass = DnsClass.getClassFromCode(((buf[off] & 0xFF) << 8) | (buf[off+1] & 0xFF));

        ttl = (((buf[off+2] & 0xff) << 24) |
                ((buf[off+3] & 0xff) << 16) |
                ((buf[off+4] & 0xff) << 8) |
                (buf[off+5] & 0xff));
    }

    public int getLength(){
        //2 for pointer
        //2 for type
        //2 for class
        //4 for TTL
        //2 for record length
        return 10;
    }

    public void setType(Types type){
        this.type = type;
    }

    public Types getType(){
        return type;
    }

    public void setDnsClass(DnsClass dnsClass){
        this.dnsClass = dnsClass;
    }

    public DnsClass getDnsClass(){
        return dnsClass;
    }

    public void setTTL(int ttl){
        this.ttl = ttl;
    }

    public int getTTL(){
        return ttl;
    }

    public void setQuery(String query){
        this.query = query;
    }

    public String getQuery(){
        return query;
    }

    @Override
    public String toString(){
        return "TYPE: "+type+"\r\nCLASS: "+dnsClass+"\r\nTTL: "+ttl+"\r\nQUERY: "+query;//+"\r\nRECORD: "+new String(record);
    }
}
