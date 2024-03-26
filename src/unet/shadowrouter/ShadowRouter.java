package unet.shadowrouter;

import unet.kad4.Kademlia;
import unet.kad4.routing.inter.RoutingTable;
import unet.kad4.utils.Node;
import unet.shadowrouter.kad.SRequestListener;
import unet.shadowrouter.tunnel.tcp.RelayServer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.util.List;

public class ShadowRouter implements RoutingTable.RestartListener {

    private Kademlia kad;
    private RelayServer relay;

    public ShadowRouter(){
        kad = new Kademlia();
        kad.getRoutingTable().setSecureOnly(false);
        kad.getRefreshHandler().setRefreshTime(30000);
        relay = new RelayServer(kad.getServer().getKeyPair());
    }

    public void join(Node node, int dhtPort, int tcpPort)throws IOException, NoSuchFieldException, IllegalAccessException,
            InvocationTargetException {
        relay.start(tcpPort);

        kad.registerRequestListener(new SRequestListener(tcpPort));
        kad.join(dhtPort, node);
    }

    public void join(List<Node> nodes, int dhtPort, int tcpPort)throws IOException, NoSuchFieldException, IllegalAccessException,
            InvocationTargetException {
        relay.start(tcpPort);

        for(Node node : nodes){
            kad.getRoutingTable().insert(node);
        }

        kad.registerRequestListener(new SRequestListener(tcpPort));
        kad.join(dhtPort, nodes.get(0));
    }

    public void bind(int dhtPort, int tcpPort)throws IOException, NoSuchFieldException, IllegalAccessException,
            InvocationTargetException {
        relay.start(tcpPort);

        kad.registerRequestListener(new SRequestListener(tcpPort));
        kad.bind(dhtPort);
    }

    public void stop()throws IOException {
        relay.stop();
        kad.stop();
    }

    @Override
    public void onRestart(){
        kad.getRoutingTable().removeRestartListener(this);

        System.out.println("WE CAN NOW START RELAYING");
    }
}
