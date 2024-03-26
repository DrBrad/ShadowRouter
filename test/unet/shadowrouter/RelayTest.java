package unet.shadowrouter;

import unet.kad4.Kademlia;
import unet.kad4.messages.GetPortRequest;
import unet.kad4.messages.GetPortResponse;
import unet.kad4.rpc.events.ResponseEvent;
import unet.kad4.rpc.events.inter.ResponseCallback;
import unet.kad4.utils.Node;
import unet.shadowrouter.kad.SRequestListener;
import unet.shadowrouter.server.TestServer;
import unet.shadowrouter.tunnel.tcp.RelayServer;
import unet.shadowrouter.tunnel.tcp.Tunnel;
import unet.kad4.utils.KeyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RelayTest {

    //IF COMMAND NOT FOUND - SEND ERROR RESPONSE...

    public static void main(String[] args)throws Exception {

        TestServer testServer = new TestServer();
        testServer.start(8080);
        System.out.println("TEST SERVER STARTED");

        ShadowRouter shadowRouter = new ShadowRouter();
        shadowRouter.bind(6000, );


        /*
        TestServer testServer = new TestServer();
        testServer.start(8080);
        System.out.println("TEST SERVER STARTED");

        //MAKE SURE WE SIGN THE MESSAGES AS WELL...
        //WHY ARE WE GETTING STALLS... - SHOULDNT HAPPEN

        Kademlia kad = new Kademlia();
        kad.getRoutingTable().setSecureOnly(false);
        kad.getRefreshHandler().setRefreshTime(30000);
        kad.registerRequestListener(new SRequestListener(5880));
        kad.bind(7000);

        RelayServer relayServer = new RelayServer(kad.getServer().getKeyPair());
        relayServer.start(5880);
        System.out.println("RELAY SERVER STARTED");

        System.out.println();

        Kademlia k2 = startNode(7001, 7000, 5881);
        Kademlia k3 = startNode(7002, 7000, 5882);
        Kademlia k4 = startNode(7003, 7000, 5883);
        Kademlia k5 = startNode(7004, 7001, 5884);

        Thread.sleep(5000);


        System.out.println(kad.getRoutingTable().getAllNodes().size()+" "+
                k2.getRoutingTable().getAllNodes().size()+" "+
                k3.getRoutingTable().getAllNodes().size()+" "+
                k4.getRoutingTable().getAllNodes().size()+" "+
                k5.getRoutingTable().getAllNodes().size());

        Thread.sleep(27000);

        System.out.println(kad.getRoutingTable().getAllNodes().size()+" "+
                k2.getRoutingTable().getAllNodes().size()+" "+
                k3.getRoutingTable().getAllNodes().size()+" "+
                k4.getRoutingTable().getAllNodes().size()+" "+
                k5.getRoutingTable().getAllNodes().size());


        /*
        Map<Integer, Integer> portExchange = new HashMap<>();
        portExchange.put(7001, 5881);
        portExchange.put(7002, 5882);
        portExchange.put(7003, 5883);
        portExchange.put(7004, 5884);
        */

        List<Node> nodes = kad.getRoutingTable().getAllNodes();

        GetPortRequest request = new GetPortRequest();
        request.setDestination(nodes.get(0).getAddress());
        kad.getServer().send(request, new ResponseCallback(){
            @Override
            public void onResponse(ResponseEvent event){
                GetPortResponse response = (GetPortResponse) event.getMessage();

                try{
                    Tunnel tunnel = new Tunnel();
                    tunnel.connect(nodes.get(0), response.getPort()); //ENTRY

                    tunnel.exit(new InetSocketAddress(InetAddress.getLocalHost(), 8080));

                    InputStream in = tunnel.getInputStream();
                    OutputStream out = tunnel.getOutputStream();

                    out.write("HELLO WORLD".getBytes());
                    out.flush();

                    byte[] buf = new byte[4096];
                    int len = in.read(buf);
                    System.out.println("CLIENT: "+new String(buf, 0, len));

                    tunnel.close();
                    System.err.println("CLOSED 2");
                }catch(Exception e){
                    e.printStackTrace();
                }

                //System.out.println(nodes.get(0)+"  "+((GetPortResponse) event.getMessage()).getPort());
            }
        });


        /*
        Tunnel tunnel = new Tunnel();
        tunnel.connect(nodes.get(0), portExchange.get(nodes.get(0).getPort())); //ENTRY
        //HANDSHAKE

        //GET ONLY 3 RANDOM NODES IN NETWORK
        for(int i = 1; i < 3; i++){
            tunnel.relay(nodes.get(i), portExchange.get(nodes.get(i).getPort()));
            //tunnel.handshake(nodes.get(i));
        }

        tunnel.exit(new InetSocketAddress(InetAddress.getLocalHost(), 8080));



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

    public static Kademlia startNode(int port, int remotePort, int tcpPort)throws Exception {
        Kademlia kad = new Kademlia();
        kad.getRoutingTable().setSecureOnly(false);
        kad.getRefreshHandler().setRefreshTime(30000);
        kad.registerRequestListener(new SRequestListener(tcpPort));
        kad.join(port, InetAddress.getLocalHost(), remotePort);
        System.out.println();

        RelayServer relayServer = new RelayServer(kad.getServer().getKeyPair());
        relayServer.start(tcpPort);
        System.out.println("RELAY SERVER STARTED");
        return kad;
    }
}
