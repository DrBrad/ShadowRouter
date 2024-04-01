package unet.shadowrouter.dns.messages;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class Request {

    private int id;
    private String domain;

    public Request(String domain, int id){
        this.domain = domain;
        this.id = id;
    }

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
        String[] domainParts = domain.split("\\.");

        for (String part : domainParts) {
            byte[] domainBytes = part.getBytes(StandardCharsets.UTF_8);
            dnsFrame[offset++] = (byte) domainBytes.length;
            System.arraycopy(domainBytes, 0, dnsFrame, offset, domainBytes.length);
            offset += domainBytes.length;
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

        /*
        Random random = new Random();
        short ID = (short)random.nextInt(32767);
        System.out.println(ID);


        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

        short requestFlags = Short.parseShort("0000000100000000", 2);
        ByteBuffer byteBuffer = ByteBuffer.allocate(2).putShort(requestFlags);
        byte[] flagsByteArray = byteBuffer.array();

        short QDCOUNT = 1;
        short ANCOUNT = 0;
        short NSCOUNT = 0;
        short ARCOUNT = 0;

        dataOutputStream.writeShort(ID);
        dataOutputStream.write(flagsByteArray);
        dataOutputStream.writeShort(QDCOUNT);
        dataOutputStream.writeShort(ANCOUNT);
        dataOutputStream.writeShort(NSCOUNT);
        dataOutputStream.writeShort(ARCOUNT);


        String[] domainParts = domain.split("\\.");

        for (int i = 0; i < domainParts.length; i++) {
            byte[] domainBytes = domainParts[i].getBytes(StandardCharsets.UTF_8);
            dataOutputStream.writeByte(domainBytes.length);
            dataOutputStream.write(domainBytes);
        }
        // No more parts
        dataOutputStream.writeByte(0);
        // Type 0x01 = A (Host Request)
        dataOutputStream.writeShort(1);
        // Class 0x01 = IN
        dataOutputStream.writeShort(1);

        byte[] dnsFrame = byteArrayOutputStream.toByteArray();

        System.out.println("SendataInputStreamg: " + dnsFrame.length + " bytes");
        for (int i = 0; i < dnsFrame.length; i++) {
            System.out.print(String.format("%s", dnsFrame[i]) + " ");
        }

        DatagramSocket socket = new DatagramSocket();
        DatagramPacket dnsReqPacket = new DatagramPacket(dnsFrame, dnsFrame.length, ipAddress, DNS_SERVER_PORT);
        socket.send(dnsReqPacket);
        */



        //return null;
    }
}
