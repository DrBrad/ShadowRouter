package unet.shadowrouter.proxy.dns.records;

import unet.shadowrouter.proxy.dns.messages.inter.DnsClass;
import unet.shadowrouter.proxy.dns.messages.inter.Types;
import unet.shadowrouter.proxy.dns.records.inter.DnsRecord;

public class TXTRecord extends DnsRecord {

    private String record;

    public TXTRecord(){
        type = Types.TXT;
    }

    public TXTRecord(String query, DnsClass dnsClass, int ttl, String record){
        this();
        this.query = query;
        this.dnsClass = dnsClass;
        this.ttl = ttl;
        this.record = record;
    }

    @Override
    public byte[] encode(){
        byte[] buf = super.encode();

        byte[] rec = record.getBytes();
        buf[8] = (byte) (rec.length >> 8);
        buf[9] = (byte) rec.length;

        System.arraycopy(rec, 0, buf, 10, rec.length);

        return buf;
    }

    @Override
    public void decode(byte[] buf, int off){
        super.decode(buf, off);

        this.record = new String(buf, off+8, ((buf[off+6] & 0xFF) << 8) | (buf[off+7] & 0xFF));
    }

    @Override
    public int getLength(){
        return super.getLength()+record.length();
    }

    public void setRecord(String record){
        this.record = record;
    }

    public String getRecord(){
        return record;
    }

    @Override
    public String toString(){
        return super.toString()+"\r\nRECORD: "+record;
    }
}
