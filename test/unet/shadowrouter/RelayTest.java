package unet.shadowrouter;

import unet.kad4.Kademlia;
import unet.kad4.messages.GetPortRequest;
import unet.kad4.messages.GetPortResponse;
import unet.kad4.utils.Node;
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
import java.util.List;

public class RelayTest {

    public static void main(String[] args)throws Exception {
        TestServer testServer = new TestServer();
        testServer.start(8080);
        System.out.println("TEST SERVER STARTED");

        //MAKE SURE WE SIGN THE MESSAGES AS WELL...

        Kademlia kad = new Kademlia();
        kad.getRoutingTable().setSecureOnly(false);
        kad.getRefreshHandler().setRefreshTime(30000);
        kad.bind(7000);
        System.out.println();

        startNode(7001, 7000, 5881);
        startNode(7002, 7000, 5882);
        startNode(7003, 7000, 5883);
        startNode(7004, 7001, 5884);



        List<Node> nodes = kad.getRoutingTable().getAllNodes();

        Tunnel tunnel = new Tunnel();
        tunnel.connect(nodes.get(0)); //ENTRY
        //HANDSHAKE

        //GET ONLY 3 RANDOM NODES IN NETWORK
        for(int i = 1; i < 3; i++){
            tunnel.relay(nodes.get(i));
            //HANDSHAKE
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

        /*

        //KeyPair keyPair = KeyRing.generateKeyPair("RSA");

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











        //SHOULD WE MOVE THIS...?
        //kad.getServer().getKeyPair();

        /*
        while(true){
            System.out.println(kad.getRoutingTable().getAllNodes().size()+" "+
                    k2.getRoutingTable().getAllNodes().size()+" "+
                    k3.getRoutingTable().getAllNodes().size()+" "+
                    k3.getRoutingTable().getAllNodes().size());

            Thread.sleep(3000);
        }
        */




        /*

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
        System.err.println("CLOSED 2");*/
    }

    public static void startNode(int port, int remotePort, int tcpPort)throws Exception {
        Kademlia kad = new Kademlia();
        kad.getRoutingTable().setSecureOnly(false);
        kad.getRefreshHandler().setRefreshTime(30000);
        kad.join(port, InetAddress.getLocalHost(), remotePort);
        System.out.println();

        RelayServer relayServer = new RelayServer(kad.getServer().getKeyPair());
        relayServer.start(tcpPort);
        System.out.println("RELAY SERVER STARTED");
    }
}
