package unet.shadowrouter;

import unet.kad4.Kademlia;
import unet.kad4.kad.KademliaBase;
import unet.shadowrouter.dns.Resolver;
import unet.shadowrouter.proxy.socks.SocksProxyServer;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.*;

public class RelayTest {

    //IF COMMAND NOT FOUND - SEND ERROR RESPONSE...

    public static void main(String[] args)throws Exception {


        Resolver resolver = new Resolver("google.com");



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
}
