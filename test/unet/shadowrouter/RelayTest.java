package unet.shadowrouter;

import unet.kad4.kad.KademliaBase;
import unet.shadowrouter.proxy.socks.SocksProxyServer;

import java.net.*;
import java.util.*;

public class RelayTest {

    //IF COMMAND NOT FOUND - SEND ERROR RESPONSE...
    //FIX DNS CNAME ISSUES

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
        }
    }
}
