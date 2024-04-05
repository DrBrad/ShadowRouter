package unet.shadowrouter.proxy.dns.records;

import unet.shadowrouter.proxy.dns.messages.inter.DnsClass;
import unet.shadowrouter.proxy.dns.messages.inter.Types;
import unet.shadowrouter.proxy.dns.records.inter.DnsRecord;
import unet.shadowrouter.proxy.dns.utils.DomainUtils;

public class MXRecord extends DnsRecord {

    private int priority;
    private String domain;

    public MXRecord(){
        type = Types.MX;
    }

    public MXRecord(String query, DnsClass dnsClass, int ttl, int priority, String domain){
        this();
        this.query = query;
        this.dnsClass = dnsClass;
        this.ttl = ttl;
        this.priority = priority;
        this.domain = domain;
    }

    @Override
    public byte[] encode(){
        byte[] buf = super.encode();

        byte[] record = DomainUtils.packDomain(domain);
        buf[8] = (byte) ((record.length+2) >> 8);
        buf[9] = (byte) (record.length+2);

        buf[10] = (byte) (priority >> 8);
        buf[11] = (byte) priority;

        System.arraycopy(record, 0, buf, 12, record.length);

        return buf;
    }

    @Override
    public void decode(byte[] buf, int off){
        super.decode(buf, off);

        priority = ((buf[off+8] & 0xFF) << 8) | (buf[off+9] & 0xFF);
        domain = DomainUtils.unpackDomain(buf, off+10);
    }

    @Override
    public int getLength(){
        return super.getLength()+domain.length()+4;
    }

    public void setPriority(int priority){
        this.priority = priority;
    }

    public int getPriority(){
        return priority;
    }

    public void setDomain(String domain){
        this.domain = domain;
    }

    public String getDomain(){
        return domain;
    }

    @Override
    public String toString(){
        return super.toString()+"\r\nPRIORITY: "+priority+"\r\nDOMAIN: "+domain;
    }
}
