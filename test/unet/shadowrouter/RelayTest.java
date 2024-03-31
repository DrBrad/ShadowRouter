package unet.shadowrouter;

import unet.kad4.Kademlia;
import unet.kad4.kad.KademliaBase;
import unet.shadowrouter.proxy.socks.SocksProxyServer;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.*;

public class RelayTest {

    //IF COMMAND NOT FOUND - SEND ERROR RESPONSE...

    public static void main(String[] args)throws Exception {






        // Domain to query
        String domain = "google.com";

        // Create DNS query message
        byte[] query = createDNSQuery(domain);

        // Create UDP socket
        DatagramSocket socket = new DatagramSocket(8090);

        // Send DNS query
        socket.send(new DatagramPacket(query, query.length, InetAddress.getByName("1.1.1.1"), 53));

        // Receive DNS response
        DatagramPacket responsePacket = new DatagramPacket(new byte[1024], 1024);
        socket.receive(responsePacket);

        decodeDNSResponse(responsePacket.getData());
        // Print DNS response
        //String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
        System.out.println("DNS response:\n" + response);

        // Close socket
        socket.close();




        /*
        ShadowRouter router = new ShadowRouter();
        router.startRelay(7000);
        router.bind(6000);

        SocksProxyServer server = new SocksProxyServer(router);
        server.start(8080);


        List<KademliaBase> nodes = new ArrayList<>();

        for(int i = 1; i < 2; i++){
            ShadowRouter r = new ShadowRouter();
            r.startRelay(7000+i);
            r.join(6000+i, InetAddress.getLoopbackAddress(), 6000+(i-1));
            nodes.add(r);
            System.err.println("STARTING NODE "+i);

            //Thread.sleep(1000);
        }

        int i = 0;

        while(true){
            StringBuilder b = new StringBuilder();
            b.append(router.getRoutingTable().getAllNodes().size());
            for(KademliaBase k : nodes){
                b.append(" | "+k.getRoutingTable().getAllNodes().size());
            }
            System.out.println(b.toString());

            Thread.sleep(3000);

            if(i == 11){
                break;
            }

            i++;
        }*/
    }

    private static byte[] createDNSQuery(String domain) {
        // DNS header (12 bytes)
        byte[] header = new byte[12];
        ByteBuffer headerBuffer = ByteBuffer.wrap(header);

        // Random identification
        headerBuffer.putShort((short) (Math.random() * Short.MAX_VALUE));
        // QR, Opcode, AA, TC, RD flags (0 for query)
        headerBuffer.putShort((short) 0);
        // QDCOUNT (1 question)
        headerBuffer.putShort((short) 1);
        // ANCOUNT, NSCOUNT, ARCOUNT (0 additional sections)
        headerBuffer.putShort((short) 0);
        headerBuffer.putShort((short) 0);

        // Domain name (variable length)
        String[] parts = domain.split("\\.");
        byte[] question = new byte[domain.length() + parts.length + 6]; // Length of domain + length bytes + terminator
        int index = 0;
        for (String part : parts) {
            question[index++] = (byte) part.length(); // Length of the part
            for (char c : part.toCharArray()) {
                question[index++] = (byte) c; // Part as ASCII bytes
            }
        }
        question[index++] = 0; // Terminator for domain name
        // QTYPE (A record)
        question[index++] = 0;
        question[index++] = 1;
        // QCLASS (IN class)
        question[index++] = 0;
        question[index++] = 1;

        // Combine header and question section
        byte[] queryMessage = new byte[header.length + question.length];
        System.arraycopy(header, 0, queryMessage, 0, header.length);
        System.arraycopy(question, 0, queryMessage, header.length, question.length);

        return queryMessage;
    }


    // Method to decode DNS response packet
    private static void decodeDNSResponse(byte[] packetData) {
        // DNS header (12 bytes)
        ByteBuffer headerBuffer = ByteBuffer.wrap(packetData, 0, 12);

        // Parse header fields
        short identification = headerBuffer.getShort(); // Identification field
        // Parse other header fields as needed

        // Parse question section
        int questionSectionOffset = parseQuestionSection(packetData, 12); // Start after header

        // Parse answer section (if present)
        int answerSectionOffset = parseAnswerSection(packetData, questionSectionOffset);

        // Print or process parsed DNS information as needed
    }

    // Method to parse the question section of the DNS response packet
    private static int parseQuestionSection(byte[] packetData, int offset) {
        // Get the length of the domain name (terminated by 0x00)
        int length = 0;
        while (packetData[offset] != 0x00) {
            length += packetData[offset] + 1; // Length of label + label itself
            offset += packetData[offset] + 1; // Move to next label
        }
        length++; // Include the terminating 0x00

        // The question section typically contains the domain name, QTYPE, and QCLASS fields
        // Extract and parse these fields as needed

        return offset + 4; // Skip over QTYPE and QCLASS fields (each is 2 bytes)
    }

    // Method to parse the answer section of the DNS response packet
    private static int parseAnswerSection(byte[] packetData, int offset) {
        // The answer section contains resource records (RRs) providing information about the queried domain
        // Each RR consists of several fields, including TYPE, CLASS, TTL, RDLENGTH, and RDATA
        // Parse each RR in the answer section as needed

        // For simplicity, this example assumes there is only one RR in the answer section

        // Skip over the domain name to reach the TYPE field
        offset += 2; // Skip the domain name length field (2 bytes)

        // Extract and parse the TYPE field (2 bytes)
        short type = ByteBuffer.wrap(packetData, offset, 2).getShort();
        System.out.println("Type: " + type); // Print or process the TYPE field

        // Skip over the CLASS, TTL, and RDLENGTH fields
        offset += 8; // Skip 2 bytes for CLASS, 4 bytes for TTL, and 2 bytes for RDLENGTH

        // Extract and parse the RDATA field (variable length)
        byte[] rdata = Arrays.copyOfRange(packetData, offset, offset + 4); // Assume 4 bytes for simplicity
        System.out.println("RDATA: " + bytesToHexString(rdata)); // Print or process the RDATA field

        // Return the offset to the end of the answer section
        return offset + 4; // Assuming RDLENGTH is 4 bytes
    }

    // Utility method to convert a hexadecimal string to a byte array
    private static byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) +
                    Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }

    // Utility method to convert a byte array to a hexadecimal string
    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
}
