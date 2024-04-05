package unet.shadowrouter.proxy.dns.records;


import unet.shadowrouter.proxy.dns.messages.inter.DnsClass;
import unet.shadowrouter.proxy.dns.messages.inter.Types;
import unet.shadowrouter.proxy.dns.records.inter.DnsRecord;
import unet.shadowrouter.proxy.dns.utils.DomainUtils;

public class NSRecord extends DnsRecord {

    private String domain;

    public NSRecord(){
        type = Types.NS;
    }

    public NSRecord(String query, DnsClass dnsClass, int ttl, String domain){
        this();
        this.query = query;
        this.dnsClass = dnsClass;
        this.ttl = ttl;
        this.domain = domain;
    }

    @Override
    public byte[] encode(){
        byte[] buf = super.encode();

        byte[] record = DomainUtils.packDomain(domain);
        buf[8] = (byte) (record.length >> 8);
        buf[9] = (byte) record.length;

        System.arraycopy(record, 0, buf, 10, record.length);

        return buf;
    }

    @Override
    public void decode(byte[] buf, int off){
        super.decode(buf, off);

        domain = DomainUtils.unpackDomain(buf, off+8);
    }

    @Override
    public int getLength(){
        return super.getLength()+domain.length()+2;
    }

    public void setDomain(String domain){
        this.domain = domain;
    }

    public String getDomain(){
        return domain;
    }

    @Override
    public String toString(){
        return super.toString()+"\r\nDOMAIN: "+domain;
    }
}
