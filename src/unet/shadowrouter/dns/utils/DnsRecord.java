package unet.shadowrouter.dns.utils;

import unet.shadowrouter.dns.messages.inter.DnsClass;
import unet.shadowrouter.dns.messages.inter.Types;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class DnsRecord {

    private InetAddress address;
    private Types type;
    private DnsClass dnsClass;
    private int ttl;

    public DnsRecord(byte[] addr, Types type, DnsClass dnsClass, int ttl){
        try{
            address = InetAddress.getByAddress(addr);
            this.type = type;
            this.dnsClass = dnsClass;
            this.ttl = ttl;
        }catch(UnknownHostException e){
            throw new IllegalArgumentException(e);
        }
    }

    public DnsRecord(InetAddress address, Types type, DnsClass dnsClass, int ttl){
        this.address = address;
        this.type = type;
        this.dnsClass = dnsClass;
        this.ttl = ttl;
    }

    public void setAddress(InetAddress address){
        this.address = address;
    }

    public InetAddress getAddress(){
        return address;
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
}
