package unet.shadowrouter;

import unet.shadowrouter.server.TestServer;
import unet.shadowrouter.tunnel.tcp.TRelayServer;
import unet.shadowrouter.tunnel.tcp.TTunnel;
import unet.shadowrouter.utils.KeyRing;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.KeyPair;

public class Main {

    public static void main(String[] args)throws Exception {
        TestServer testServer = new TestServer();
        testServer.start(6968);
        System.out.println("TEST SERVER STARTED");

        KeyPair keyPair = KeyRing.generateKeyPair("RSA");

        TRelayServer relayServer = new TRelayServer(keyPair);
        relayServer.start(6969);
        System.out.println("RELAY SERVER STARTED");

        //KeyPair keyPairA = KeyRing.generateKeyPair("RSA");
        TTunnel tunnel = new TTunnel();
        tunnel.connect(new InetSocketAddress(InetAddress.getLocalHost(), 6969));
        tunnel.handshake(keyPair.getPublic(), new InetSocketAddress(InetAddress.getLocalHost(), 6968));

        OutputStream out = tunnel.getOutputStream();
        out.write("HELLO WORLD".getBytes());
        tunnel.close();
    }
}
