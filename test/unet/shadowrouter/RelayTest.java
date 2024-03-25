package unet.shadowrouter;

import unet.shadowrouter.server.TestServer;
import unet.shadowrouter.tunnel.tcp.TRelayServer;
import unet.shadowrouter.tunnel.tcp.TTunnel;
import unet.shadowrouter.utils.KeyRing;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.KeyPair;

public class RelayTest {

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
        tunnel.connect(new InetSocketAddress(InetAddress.getLocalHost(), 6969)); //ENTRY

        InetSocketAddress[] route = {
                new InetSocketAddress(InetAddress.getLocalHost(), 6969), //ENTRY TO MID
                //new InetSocketAddress(InetAddress.getLocalHost(), 6969), //MID TO EXIT
                new InetSocketAddress(InetAddress.getLocalHost(), 6968), //EXIT TO LOCATION
        };

        for(InetSocketAddress address : route){
            tunnel.handshake(keyPair.getPublic(), address);
        }

        InputStream in = tunnel.getInputStream();
        OutputStream out = tunnel.getOutputStream();

        out.write("HELLO WORLD".getBytes());
        out.flush();

        byte[] buf = new byte[4096];
        int len = in.read(buf);
        System.out.println("CLIENT: "+new String(buf, 0, len));

        tunnel.close();
        System.err.println("CLOSED 2");
    }
}
