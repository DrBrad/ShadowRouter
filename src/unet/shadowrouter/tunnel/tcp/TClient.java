package unet.shadowrouter.tunnel.tcp;

import unet.kad4.utils.Node;

import java.io.IOException;
import java.net.Socket;

public class TClient {

    public TClient(){

    }

    public void connect(Node node)throws IOException {
        Socket socket = new Socket();
        socket.connect(node.getAddress());
    }
}
