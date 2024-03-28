package unet.shadowrouter;

import unet.kad4.Kademlia;
import unet.kad4.kad.KademliaBase;
import unet.kad4.messages.GetPortRequest;
import unet.kad4.messages.GetPortResponse;
import unet.kad4.rpc.events.ResponseEvent;
import unet.kad4.rpc.events.inter.ResponseCallback;
import unet.kad4.utils.Node;
import unet.shadowrouter.kad.SRequestListener;
import unet.shadowrouter.proxy.socks.SocksProxyServer;
import unet.shadowrouter.server.TestServer;
import unet.shadowrouter.tunnel.tcp.RelayServer;
import unet.shadowrouter.tunnel.tcp.Tunnel;
import unet.kad4.utils.KeyUtils;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.*;

public class RelayTest {

    //IF COMMAND NOT FOUND - SEND ERROR RESPONSE...

    public static void main(String[] args)throws Exception {
        ShadowRouter router = new ShadowRouter();
        router.startRelay(7000);
        router.bind(6000);

        SocksProxyServer server = new SocksProxyServer(router);
        server.start(8080);


        List<KademliaBase> nodes = new ArrayList<>();

        for(int i = 1; i < 62; i++){
            ShadowRouter r = new ShadowRouter();
            r.startRelay(7000+i);
            r.join(6000+i, InetAddress.getLocalHost(), 6000+(i-1));
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
        }

        //SSLSocketInputStream in;
        //javax.net.ssl.SSLSocketOutputStream

        //ARE WE CHECKING IPS BEFORE INSTERTING - DONT WANT DUPLICATE IPS...

        /*
        TestServer testServer = new TestServer();
        testServer.start(8080);
        System.out.println("TEST SERVER STARTED");
        */

        /*
        ShadowRouter router = new ShadowRouter();
        router.startRelay(7000);
        router.bind(6000);

        List<KademliaBase> nodes = new ArrayList<>();

        for(int i = 1; i < 62; i++){
            ShadowRouter r = new ShadowRouter();
            r.startRelay(7000+i);
            r.join(6000+i, InetAddress.getLocalHost(), 6000+(i-1));
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
        }

        System.err.println("TRYING TUNNEL");

        List<Node> ns = router.getRoutingTable().getAllNodes();
        Collections.shuffle(ns);


        GetPortRequest request = new GetPortRequest();
        request.setDestination(ns.get(0).getAddress());
        router.getServer().send(request, new ResponseCallback(){
            @Override
            public void onResponse(ResponseEvent event){
                GetPortResponse response = (GetPortResponse) event.getMessage();

                try{
                    Tunnel tunnel = new Tunnel();
                    tunnel.connect(ns.get(0), response.getPort()); //ENTRY
                    tunnel.relay(ns.get(1));
                    tunnel.relay(ns.get(2));
                    tunnel.exit(new InetSocketAddress(InetAddress.getByName("info.cern.ch"), 80));
                    //tunnel.exit(new InetSocketAddress(InetAddress.getLocalHost(), 8080));

                    InputStream in = tunnel.getInputStream();
                    OutputStream out = tunnel.getOutputStream();

                    //out.write("HELLO WORLD".getBytes());
                    out.write("GET / HTTP/1.1\r\n".getBytes());
                    out.write("Host: info.cern.ch\r\n\r\n".getBytes());
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


        while(true){
            StringBuilder b = new StringBuilder();
            b.append(router.getRoutingTable().getAllNodes().size());
            for(KademliaBase k : nodes){
                b.append(" | "+k.getRoutingTable().getAllNodes().size());
            }
            System.out.println(b.toString());

            Thread.sleep(3000);
        }
        /*
        */
    }
}
