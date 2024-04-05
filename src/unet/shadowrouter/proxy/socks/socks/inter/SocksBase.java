package unet.shadowrouter.proxy.socks.socks.inter;

import unet.kad4.rpc.events.ErrorResponseEvent;
import unet.kad4.rpc.events.ResponseEvent;
import unet.kad4.rpc.events.StalledEvent;
import unet.kad4.rpc.events.inter.ResponseCallback;
import unet.kad4.utils.Node;
import unet.shadowrouter.kad.messages.GetPortRequest;
import unet.shadowrouter.kad.messages.GetPortResponse;
import unet.shadowrouter.kad.utils.SecureNode;
import unet.shadowrouter.proxy.socks.SocksProxy;
import unet.shadowrouter.tunnel.tcp.Tunnel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

public abstract class SocksBase {

    protected SocksProxy proxy;
    protected AType atype;
    protected byte[] address;
    protected int port;

    public SocksBase(SocksProxy proxy){
        this.proxy = proxy;
    }

    public abstract Command getCommand()throws IOException;

    public abstract void connect()throws IOException;

    public abstract void bind()throws IOException;

    //private boolean complete;

    public void relay(Tunnel tunnel)throws IOException {
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run(){
                try{
                    transfer(proxy.getInputStream(), tunnel.getOutputStream());
                    //while(!socket.isClosed() && !relay.isClosed()){
                    //    in.transferTo(relay.getOutputStream());
                    //}
                }catch(IOException e){
                    //e.printStackTrace();
                }

                /*
                if(complete){
                    try{
                        tunnel.close();
                        proxy.getSocket().close();
                    }catch(IOException e){
                    }
                    return;
                }
                complete = true;
                */
            }
        });//.start();
        thread.start();

        try{
            transfer(tunnel.getInputStream(), proxy.getOutputStream());
        }catch(IOException e){
            //e.printStackTrace();
        }

        try{
            thread.join();
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        tunnel.getInputStream().close();
        tunnel.getOutputStream().close();
        tunnel.close();

        proxy.getInputStream().close();
        proxy.getOutputStream().close();
        proxy.getSocket().close();

        //while(!relay.isClosed() && !socket.isClosed()){
        //    relay.getInputStream().transferTo(out);
        //}
        /*
        if(complete){
            tunnel.close();
            proxy.getSocket().close();
            return;
        }
        complete = true;*/
    }

    private void transfer(InputStream in, OutputStream out)throws IOException {
        byte[] buf = new byte[4096];
        int len;
        while((len = in.read(buf)) != -1){
            out.write(buf, 0, len);
            out.flush();
        }

        //out.flush();
        //in.close();
        //out.close();
    }

    protected void resolve(){
        /*
        try{
            List<Node> nodes = proxy.getKademlia().getRoutingTable().getAllNodes();
            if(nodes.size() < 3){
                replyCommand(ReplyCode.GENERAL_FAILURE);
                throw new IOException("Not enough nodes to relay off of.");
            }

            Collections.shuffle(nodes);


            GetPortRequest request = new GetPortRequest();
            request.setDestination(nodes.get(0).getAddress());
            proxy.getKademlia().getServer().send(request, new ResponseCallback(){
                @Override
                public void onResponse(ResponseEvent event){
                    GetPortResponse response = (GetPortResponse) event.getMessage();

                    Tunnel tunnel = new Tunnel();
                    try{
                        tunnel.connect((SecureNode) nodes.get(0), response.getPort()); //ENTRY
                        tunnel.relay((SecureNode) nodes.get(1));
                        tunnel.relay((SecureNode) nodes.get(2));
                        tunnel.exit(address, port, atype);

                        replyCommand(ReplyCode.GRANTED);

                    }catch(Exception e){
                        try{
                            replyCommand(ReplyCode.HOST_UNREACHABLE);//, address);
                            proxy.getSocket().close();

                        }catch(IOException ex){
                        }
                        return;
                    }
                }

                @Override
                public void onErrorResponse(ErrorResponseEvent event){
                    try{
                        System.out.println("ERROR - GET_PORT");
                        replyCommand(ReplyCode.GENERAL_FAILURE);//, address);
                        proxy.getSocket().close();

                    }catch(IOException e){
                    }
                }

                @Override
                public void onStalled(StalledEvent event){
                    try{
                        System.out.println("Stalled - GET_PORT");
                        replyCommand(ReplyCode.GENERAL_FAILURE);//, address);
                        proxy.getSocket().close();

                    }catch(IOException e){
                    }
                }
            });
        }catch(IOException e){
        }
        */
    }
}
