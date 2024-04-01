package unet.shadowrouter.dns;

import unet.shadowrouter.dns.messages.DNSRequest;
import unet.shadowrouter.dns.messages.DNSResponse;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Resolver {

    public static final int DNS_SERVER_PORT = 53;

    public Resolver(String domain)throws IOException {
        InetAddress ipAddress = InetAddress.getByName("1.1.1.1");


        Random random = new Random();
        int id = random.nextInt(32767);
        DNSRequest request = new DNSRequest(id);
        request.setDomain(domain);
        byte[] buf = request.encode();

        DatagramSocket socket = new DatagramSocket();
        DatagramPacket dnsReqPacket = new DatagramPacket(buf, buf.length, ipAddress, DNS_SERVER_PORT);
        socket.send(dnsReqPacket);


        System.out.println(id);


        buf = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);

        id = ((buf[0] & 0xFF) << 8) | (buf[1] & 0xFF);

        System.out.println("RESPONSE LENGTH: "+packet.getLength());

        test2(buf, 0, packet.getLength());

        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("");

        DNSResponse response = new DNSResponse(id);
        response.decode(buf);
    }

    public void test2(byte[] response, int off, int len)throws IOException {
        for (int i = 0; i < len; i++) {
            System.out.print(String.format("%s", response[i]) + " ");
        }
        System.out.println("\n");

        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(response));
        System.out.println("\n\nStart response decode");
        System.out.println("Transaction ID: " + dataInputStream.readShort()); // ID
        short flags = dataInputStream.readByte();
        int QR = (flags & 0b10000000) >>> 7;
        int opCode = ( flags & 0b01111000) >>> 3;
        int AA = ( flags & 0b00000100) >>> 2;
        int TC = ( flags & 0b00000010) >>> 1;
        int RD = flags & 0b00000001;
        /*
        System.out.println("QR "+QR);
        System.out.println("Opcode "+opCode);
        System.out.println("AA "+AA);
        System.out.println("TC "+TC);
        System.out.println("RD "+RD);
        */
        flags = dataInputStream.readByte();
        int RA = (flags & 0b10000000) >>> 7;
        int Z = ( flags & 0b01110000) >>> 4;
        int RCODE = flags & 0b00001111;
        /*
        System.out.println("RA "+RA);
        System.out.println("Z "+ Z);
        System.out.println("RCODE " +RCODE);
        */

        short QDCOUNT = dataInputStream.readShort();
        short ANCOUNT = dataInputStream.readShort();
        short NSCOUNT = dataInputStream.readShort();
        short ARCOUNT = dataInputStream.readShort();

        /*
        System.out.println("Questions: " + String.format("%s",QDCOUNT ));
        System.out.println("Answers RRs: " + String.format("%s", ANCOUNT));
        System.out.println("Authority RRs: " + String.format("%s", NSCOUNT));
        System.out.println("Additional RRs: " + String.format("%s", ARCOUNT));
        */

        String QNAME = "";
        int recLen;
        while ((recLen = dataInputStream.readByte()) > 0) {
            byte[] record = new byte[recLen];
            for (int i = 0; i < recLen; i++) {
                record[i] = dataInputStream.readByte();
            }
            QNAME = new String(record, StandardCharsets.UTF_8);
        }
        short QTYPE = dataInputStream.readShort();
        short QCLASS = dataInputStream.readShort();
        //System.out.println("Record: " + QNAME);
        //System.out.println("Record Type: " + String.format("%s", QTYPE));
        //System.out.println("Class: " + String.format("%s", QCLASS));

        System.out.println("\n\nstart answer, authority, and additional sections\n");

        byte firstBytes = dataInputStream.readByte();
        int firstTwoBits = (firstBytes & 0b11000000) >>> 6;

        ByteArrayOutputStream label = new ByteArrayOutputStream();
        Map<String, String> domainToIp = new HashMap<>();

        for(int i = 0; i < ANCOUNT; i++) {
            if(firstTwoBits == 3) {
                byte currentByte = dataInputStream.readByte();
                boolean stop = false;
                byte[] newArray = Arrays.copyOfRange(response, currentByte, response.length);
                DataInputStream sectionDataInputStream = new DataInputStream(new ByteArrayInputStream(newArray));
                ArrayList<Integer> RDATA = new ArrayList<>();
                ArrayList<String> DOMAINS = new ArrayList<>();
                while(!stop) {
                    byte nextByte = sectionDataInputStream.readByte();
                    if(nextByte != 0) {
                        byte[] currentLabel = new byte[nextByte];
                        for(int j = 0; j < nextByte; j++) {
                            currentLabel[j] = sectionDataInputStream.readByte();
                        }
                        label.write(currentLabel);
                    } else {
                        stop = true;
                        short TYPE = dataInputStream.readShort();
                        short CLASS = dataInputStream.readShort();
                        int TTL = dataInputStream.readInt();
                        int RDLENGTH = dataInputStream.readShort();
                        for(int s = 0; s < RDLENGTH; s++) {
                            int nx = dataInputStream.readByte() & 255;// and with 255 to
                            RDATA.add(nx);
                        }

                        System.out.println("Type: " + TYPE);
                        System.out.println("Class: " + CLASS);
                        System.out.println("Time to live: " + TTL);
                        System.out.println("Rd Length: " + RDLENGTH);
                    }

                    DOMAINS.add(label.toString(StandardCharsets.UTF_8));
                    label.reset();
                }

                StringBuilder ip = new StringBuilder();
                StringBuilder domainSb = new StringBuilder();
                for(Integer ipPart:RDATA) {
                    ip.append(ipPart).append(".");
                }

                for(String domainPart:DOMAINS) {
                    if(!domainPart.equals("")) {
                        domainSb.append(domainPart).append(".");
                    }
                }
                String domainFinal = domainSb.toString();
                String ipFinal = ip.toString();
                domainToIp.put(ipFinal.substring(0, ipFinal.length()-1), domainFinal.substring(0, domainFinal.length()-1));

            }else if(firstTwoBits == 0){
                System.out.println("It's a label");
            }

            firstBytes = dataInputStream.readByte();
            firstTwoBits = (firstBytes & 0b11000000) >>> 6;
        }

        domainToIp.forEach((key, value) -> System.out.println(key + " : " + value));
    }
}
