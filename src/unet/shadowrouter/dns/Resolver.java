package unet.shadowrouter.dns;

import unet.shadowrouter.dns.messages.DnsRequest;
import unet.shadowrouter.dns.messages.DnsResponse;
import unet.shadowrouter.dns.messages.inter.DnsClass;
import unet.shadowrouter.dns.messages.inter.Type;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

public class Resolver {

    public static final int DNS_SERVER_PORT = 53;

    public Resolver(String domain)throws IOException {
        InetAddress ipAddress = InetAddress.getByName("1.1.1.1");


        Random random = new Random();
        int id = random.nextInt(32767);
        DnsRequest request = new DnsRequest(id);
        request.setDomain(domain);
        request.setType(Type.A);
        request.setDnsClass(DnsClass.IN);
        byte[] buf = request.encode();

        DatagramSocket socket = new DatagramSocket();
        DatagramPacket dnsReqPacket = new DatagramPacket(buf, buf.length, ipAddress, DNS_SERVER_PORT);
        socket.send(dnsReqPacket);


        System.out.println(id);


        buf = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        System.out.println(packet.getLength());

        id = ((buf[0] & 0xFF) << 8) | (buf[1] & 0xFF);

        DnsResponse response = new DnsResponse(id);
        response.decode(buf);
    }
}
