package unet.shadowrouter.dns.messages;

import unet.shadowrouter.dns.messages.inter.MessageBase;

import java.nio.charset.StandardCharsets;

public class DNSRequest extends MessageBase {

    private String domain;

    public DNSRequest(){
    }

    public DNSRequest(int id){
        this.id = id;
    }

    @Override
    public byte[] encode(){
        byte[] dnsFrame = new byte[18+domain.getBytes().length]; // Assuming max DNS packet size

        // ID (16 bits)
        dnsFrame[0] = (byte) (id >> 8); // First 8 bits
        dnsFrame[1] = (byte) id; // Second 8 bits

        // Flags (16 bits)
        int QR = 0; // Query
        int Opcode = 0; // Standard query
        int AA = 0; // Not authoritative
        int TC = 0; // Not truncated
        int RD = 1; // Recursion desired
        int RA = 0; // Recursion not available yet
        int Z = 0; // Reserved
        int RCODE = 0; // No error

        int flags = (QR << 15) | (Opcode << 11) | (AA << 10) | (TC << 9) |
                (RD << 8) | (RA << 7) | (Z << 4) | RCODE;


        dnsFrame[2] = (byte) (flags >> 8); // First 8 bits
        dnsFrame[3] = (byte) flags; // Second 8 bits

        // QDCOUNT (16 bits)
        dnsFrame[4] = 0x00; // Always 0
        dnsFrame[5] = 0x01; // 1 question

        // ANCOUNT (16 bits)
        dnsFrame[6] = 0x00; // Always 0
        dnsFrame[7] = 0x00; // No answer records

        // NSCOUNT (16 bits)
        dnsFrame[8] = 0x00; // Always 0
        dnsFrame[9] = 0x00; // No authority records

        // ARCOUNT (16 bits)
        dnsFrame[10] = 0x00; // Always 0
        dnsFrame[11] = 0x00; // No additional records

        // Query section
        int offset = 12; // Start of query section

        for (String part : domain.split("\\.")) {
            byte[] domainBytes = part.getBytes(StandardCharsets.UTF_8);
            dnsFrame[offset] = (byte) domainBytes.length;
            System.arraycopy(domainBytes, 0, dnsFrame, offset+1, domainBytes.length);
            offset += domainBytes.length+1;
        }
        // End of domain name (null)
        dnsFrame[offset++] = 0x00;

        // QTYPE (16 bits) - A record
        dnsFrame[offset++] = 0x00;
        dnsFrame[offset++] = 0x01;

        // QCLASS (16 bits) - IN class
        dnsFrame[offset++] = 0x00;
        dnsFrame[offset++] = 0x01;

        // Truncate unused portion of the byte array
        byte[] truncatedFrame = new byte[offset];
        System.arraycopy(dnsFrame, 0, truncatedFrame, 0, offset);


        return truncatedFrame;
    }

    @Override
    public void decode(byte[] buf){

    }

    public void setDomain(String domain){
        this.domain = domain;
    }

    public String getDomain(){
        return domain;
    }
}
