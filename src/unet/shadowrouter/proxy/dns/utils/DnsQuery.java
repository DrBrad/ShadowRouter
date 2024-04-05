package unet.shadowrouter.proxy.dns.utils;

import unet.shadowrouter.proxy.dns.messages.inter.DnsClass;
import unet.shadowrouter.proxy.dns.messages.inter.Types;

public class DnsQuery {

    private String query;
    private Types type;
    private DnsClass dnsClass;

    public DnsQuery(){
    }

    public DnsQuery(String query, Types type, DnsClass dnsClass){
        this.query = query;
        this.type = type;
        this.dnsClass = dnsClass;
    }

    public byte[] encode(){
        byte[] buf = new byte[getLength()];
        int offset = 0;

        byte[] addr = DomainUtils.packDomain(query);
        System.arraycopy(addr, 0, buf, offset, addr.length);
        offset += addr.length;

        // QTYPE (16 bits) - A record
        buf[offset] = ((byte) (type.getCode() >> 8));
        buf[offset+1] = ((byte) type.getCode());

        // QCLASS (16 bits) - IN class
        buf[offset+2] = ((byte) (dnsClass.getCode() >> 8));
        buf[offset+3] = ((byte) dnsClass.getCode());

        return buf;
    }

    public void decode(byte[] buf, int off){
        query = DomainUtils.unpackDomain(buf, off);
        off += query.length()+2;

        type = Types.getTypeFromCode(((buf[off] & 0xFF) << 8) | (buf[off+1] & 0xFF));
        dnsClass = DnsClass.getClassFromCode(((buf[off+2] & 0xFF) << 8) | (buf[off+3] & 0xFF));
    }

    public int getLength(){
        return query.getBytes().length+6;
    }

    public void setQuery(String query){
        this.query = query;
    }

    public String getQuery(){
        return query;
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

    @Override
    public String toString(){
        return "TYPE: "+type+"\r\nCLASS: "+dnsClass+"\r\nQUERY: "+query;
    }
}
