package unet.shadowrouter.dns.messages;

import unet.shadowrouter.dns.messages.inter.MessageBase;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public class DNSResponse extends MessageBase {

    public DNSResponse(int id){
        this.id = id;
    }

    @Override
    public byte[] encode(){
        return new byte[0];
    }

    @Override
    public void decode(byte[] buf){
        int QR = (buf[2] >> 7) & 0x01;
        int Opcode = (buf[2] >> 3) & 0x0F;
        int AA = (buf[2] >> 2) & 0x01;
        int TC = (buf[2] >> 1) & 0x01;
        int RD = buf[2] & 0x01;
        int RA = (buf[3] >> 7) & 0x01;
        int RCODE = buf[3] & 0x0F;
        int QDCOUNT = ((buf[4] & 0xFF) << 8) | (buf[5] & 0xFF);
        int ANCOUNT = ((buf[6] & 0xFF) << 8) | (buf[7] & 0xFF);
        int NSCOUNT = ((buf[8] & 0xFF) << 8) | (buf[9] & 0xFF);
        int ARCOUNT = ((buf[10] & 0xFF) << 8) | (buf[11] & 0xFF);

        // Displaying the decoded DNS response header fields
        System.out.println("QR: " + QR);
        System.out.println("Opcode: " + Opcode);
        System.out.println("AA: " + AA);
        System.out.println("TC: " + TC);
        System.out.println("RD: " + RD);
        System.out.println("RA: " + RA);
        System.out.println("RCODE: " + RCODE);
        System.out.println("QDCOUNT: " + QDCOUNT);
        System.out.println("ANCOUNT: " + ANCOUNT);
        System.out.println("NSCOUNT: " + NSCOUNT);
        System.out.println("ARCOUNT: " + ARCOUNT);

        // Decode the resource records (answers) if any
        int offset = 12; // Start of resource records section
        for (int i = 0; i < ANCOUNT; i++) {

            // Decode each answer record
            String name = parseDomainName(buf, offset); // Decode domain name
            int type = parseType(buf, offset + name.length() + 2); // Decode record type
            int cls = parseClass(buf, offset + name.length() + 4); // Decode record class
            int ttl = parseTTL(buf, offset + name.length() + 6); // Decode TTL
            int rdLength = parseRDLength(buf, offset + name.length() + 10); // Decode data length
            byte[] rdata = parseRData(buf, offset + name.length() + 12, rdLength); // Decode data
            offset += name.length() + 12 + rdLength; // Move offset to the next resource record

            // Display the decoded resource record
            System.out.println(rdLength);
            System.out.println("Name: " + name + ", Type: " + type + ", Class: " + cls + ", TTL: " + ttl + ", RDATA: " + Arrays.toString(rdata));

            if (type == 1 && rdLength == 4) { // A record (IPv4 address)
                try {
                    InetAddress ipAddress = InetAddress.getByAddress(rdata);
                    System.out.println("IPv4 Address: " + ipAddress.getHostAddress());
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }

            // Decode each answer record
            // Assuming answer format: NAME (domain name), TYPE (record type), CLASS (record class), TTL (time to live), RDLENGTH (data length), RDATA (data)
            // Adjust parsing logic based on your specific use case
            // Example: String name = parseDomainName(dnsResponse, offset);
            // Example: int type = parseType(dnsResponse, offset + nameLength);
            // Example: int cls = parseClass(dnsResponse, offset + nameLength + typeLength);
            // Example: int ttl = parseTTL(dnsResponse, offset + nameLength + typeLength + clsLength);
            // Example: int rdLength = parseRDLength(dnsResponse, offset + nameLength + typeLength + clsLength + ttlLength);
            // Example: byte[] rdata = parseRData(dnsResponse, offset + nameLength + typeLength + clsLength + ttlLength + rdLengthLength);
            // Example: offset += nameLength + typeLength + clsLength + ttlLength + rdLengthLength + rdLength;
            // Example: System.out.println("Name: " + name + ", Type: " + type + ", Class: " + cls + ", TTL: " + ttl + ", RDATA: " + Arrays.toString(rdata));
        }
    }

    private static String parseDomainName(byte[] dnsResponse, int offset) {
        StringBuilder domainName = new StringBuilder();
        int length = dnsResponse[offset++];
        while (length != 0) {
            if ((length & 0xC0) == 0xC0) {
                // Compression pointer
                int pointer = ((length & 0x3F) << 8) | (dnsResponse[offset++] & 0xFF);
                parseDomainName(dnsResponse, pointer); // Recursively parse the domain name from the pointer
                return domainName.toString();
            }
            for (int i = 0; i < length; i++) {
                domainName.append((char) dnsResponse[offset++]);
            }
            domainName.append('.');
            length = dnsResponse[offset++];
        }
        domainName.deleteCharAt(domainName.length() - 1); // Remove the trailing dot
        return domainName.toString();
    }

    private static int parseType(byte[] dnsResponse, int offset) {
        return ((dnsResponse[offset] & 0xFF) << 8) | (dnsResponse[offset + 1] & 0xFF);
    }

    private static int parseClass(byte[] dnsResponse, int offset) {
        return ((dnsResponse[offset] & 0xFF) << 8) | (dnsResponse[offset + 1] & 0xFF);
    }

    private static int parseTTL(byte[] dnsResponse, int offset) {
        return ((dnsResponse[offset] & 0xFF) << 24) |
                ((dnsResponse[offset + 1] & 0xFF) << 16) |
                ((dnsResponse[offset + 2] & 0xFF) << 8) |
                (dnsResponse[offset + 3] & 0xFF);
    }

    private static int parseRDLength(byte[] dnsResponse, int offset) {
        return ((dnsResponse[offset] & 0xFF) << 8) | (dnsResponse[offset + 1] & 0xFF);
    }

    private static byte[] parseRData(byte[] dnsResponse, int offset, int rdLength) {
        byte[] rdata = new byte[rdLength];
        System.arraycopy(dnsResponse, offset, rdata, 0, rdLength);
        return rdata;
    }
}
