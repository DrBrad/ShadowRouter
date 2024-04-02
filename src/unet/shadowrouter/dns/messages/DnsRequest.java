package unet.shadowrouter.dns.messages;

import unet.shadowrouter.dns.messages.inter.MessageBase;
import unet.shadowrouter.dns.messages.inter.OpCodes;
import unet.shadowrouter.dns.messages.inter.ResponseCodes;

import java.nio.charset.StandardCharsets;

public class DnsRequest extends MessageBase {

    public DnsRequest(){
    }

    public DnsRequest(int id){
        this.id = id;
        qdCount = 1;
    }

    @Override
    public byte[] encode(){
        byte[] dnsFrame = super.encode();

        int offset = 12;

        for(String part : domain.split("\\.")){
            byte[] domainBytes = part.getBytes(StandardCharsets.UTF_8);
            dnsFrame[offset] = (byte) domainBytes.length;
            System.arraycopy(domainBytes, 0, dnsFrame, offset+1, domainBytes.length);
            offset += domainBytes.length+1;
        }
        // End of domain name (null)
        dnsFrame[offset++] = 0x00;

        // QTYPE (16 bits) - A record
        dnsFrame[offset++] = ((byte) (type.getCode() >> 8));
        dnsFrame[offset++] = ((byte) type.getCode());

        // QCLASS (16 bits) - IN class
        dnsFrame[offset++] = ((byte) (dnsClass.getCode() >> 8));
        dnsFrame[offset++] = ((byte) dnsClass.getCode());

        // Truncate unused portion of the byte array
        byte[] truncatedFrame = new byte[offset];
        System.arraycopy(dnsFrame, 0, truncatedFrame, 0, offset);

        return truncatedFrame;
    }

    @Override
    public void decode(byte[] buf){
    }

    @Override
    public int getLength(){
        return super.getLength()+domain.getBytes().length+6;
    }
}
