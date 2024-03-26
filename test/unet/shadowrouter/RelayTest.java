package unet.shadowrouter;

import unet.kad4.Kademlia;
import unet.kad4.messages.GetPortRequest;
import unet.kad4.messages.GetPortResponse;
import unet.shadowrouter.server.TestServer;
import unet.shadowrouter.tunnel.tcp.RelayServer;
import unet.shadowrouter.tunnel.tcp.Tunnel;
import unet.kad4.utils.KeyUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.security.PublicKey;

public class RelayTest {

    public static void main(String[] args)throws Exception {

        //MAKE SURE WE SIGN THE MESSAGES AS WELL...

        Kademlia kad = new Kademlia();
        kad.bind(7000);
        System.out.println();

        Kademlia k2 = new Kademlia();
        k2.join(7001, InetAddress.getLocalHost(), 7000);
        System.out.println();


        //SHOULD WE MOVE THIS...?
        //kad.getServer().getKeyPair();

        System.out.println(kad.getRoutingTable().getAllNodes().size());
        System.out.println(k2.getRoutingTable().getAllNodes().size());





        System.exit(0);

        for(int i = 0; i < 20; i++){
        KeyPair keyPair = KeyUtils.generateKeyPair("RSA");
        System.out.println(keyPair.getPublic().getEncoded().length);
        }



        System.exit(0);

        TestServer testServer = new TestServer();
        testServer.start(8080);
        System.out.println("TEST SERVER STARTED");

        //KeyPair keyPair = KeyRing.generateKeyPair("RSA");

        PublicKey[] keys = {
                startRelay(6969),
                startRelay(6970),
                startRelay(6971)
        };

        //KeyPair keyPairA = KeyRing.generateKeyPair("RSA");
        Tunnel tunnel = new Tunnel();
        tunnel.connect(new InetSocketAddress(InetAddress.getLocalHost(), 6969)); //ENTRY

        InetSocketAddress[] route = {
                new InetSocketAddress(InetAddress.getLocalHost(), 6970), //ENTRY TO MID
                new InetSocketAddress(InetAddress.getLocalHost(), 6971), //MID TO EXIT
                new InetSocketAddress(InetAddress.getLocalHost(), 8080), //EXIT TO LOCATION
        };

        for(int i = 0; i < keys.length; i++){
            tunnel.relay(keys[i], route[i]);
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

    public static PublicKey startRelay(int port)throws Exception {
        KeyPair keyPair = KeyUtils.generateKeyPair("RSA");
        RelayServer relayServer = new RelayServer(keyPair);
        relayServer.start(port);
        System.out.println("RELAY SERVER STARTED");

        return keyPair.getPublic();
    }
}
