package unet.shadowrouter.dns.messages;

import unet.shadowrouter.dns.messages.inter.MessageBase;

import java.nio.charset.StandardCharsets;

public class DnsRequest extends MessageBase {

    public DnsRequest(){
    }

    public DnsRequest(int id){
        this.id = id;
        qdCount = 1;
        recursionDesired = true;
    }

    @Override
    public byte[] encode(){
        byte[] buf = super.encode();

        int offset = 12;

        for(String part : query.split("\\.")){
            byte[] domainBytes = part.getBytes(StandardCharsets.UTF_8);
            buf[offset] = (byte) domainBytes.length;
            System.arraycopy(domainBytes, 0, buf, offset+1, domainBytes.length);
            offset += domainBytes.length+1;
        }


        // End of domain name (null)
        buf[offset] = 0x00;

        // QTYPE (16 bits) - A record
        buf[offset+1] = ((byte) (type.getCode() >> 8));
        buf[offset+2] = ((byte) type.getCode());

        // QCLASS (16 bits) - IN class
        buf[offset+3] = ((byte) (dnsClass.getCode() >> 8));
        buf[offset+4] = ((byte) dnsClass.getCode());

        // Truncate unused portion of the byte array
        byte[] truncatedFrame = new byte[offset+5];
        System.arraycopy(buf, 0, truncatedFrame, 0, offset+5);

        return truncatedFrame;
    }

    @Override
    public void decode(byte[] buf){
    }

    @Override
    public int getLength(){
        return super.getLength()+query.getBytes().length+6;
    }
}
