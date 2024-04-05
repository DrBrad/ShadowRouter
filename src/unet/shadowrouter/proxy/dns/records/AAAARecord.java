package unet.shadowrouter.proxy.dns.records;

import unet.shadowrouter.proxy.dns.messages.inter.DnsClass;
import unet.shadowrouter.proxy.dns.messages.inter.Types;
import unet.shadowrouter.proxy.dns.records.inter.DnsRecord;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class AAAARecord extends DnsRecord {

    private InetAddress address;

    public AAAARecord(){
        type = Types.AAAA;
    }

    public AAAARecord(String query, DnsClass dnsClass, int ttl, InetAddress address){
        this();
        this.query = query;
        this.dnsClass = dnsClass;
        this.ttl = ttl;
        this.address = address;
    }

    @Override
    public byte[] encode(){
        byte[] buf = super.encode();

        byte[] addr = address.getAddress();
        buf[8] = (byte) (addr.length >> 8);
        buf[9] = (byte) addr.length;

        System.arraycopy(addr, 0, buf, 10, addr.length);

        return buf;
    }

    @Override
    public void decode(byte[] buf, int off){
        super.decode(buf, off);

        byte[] record = new byte[((buf[off+6] & 0xFF) << 8) | (buf[off+7] & 0xFF)];
        System.arraycopy(buf, off+8, record, 0, record.length);

        try{
            address = InetAddress.getByAddress(record);
        }catch(UnknownHostException e){
            throw new IllegalArgumentException("Invalid Inet Address");
        }
    }

    @Override
    public int getLength(){
        return super.getLength()+address.getAddress().length;
    }

    public void setAddress(InetAddress address){
        this.address = address;
    }

    public InetAddress getAddress(){
        return address;
    }

    @Override
    public String toString(){
        return super.toString()+"\r\nADDRESS: "+address.getHostAddress();
    }
}
