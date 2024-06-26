package unet.shadowrouter.tunnel.tcp;

import unet.kad4.kad.KademliaBase;
import unet.kad4.rpc.events.ErrorResponseEvent;
import unet.kad4.rpc.events.ResponseEvent;
import unet.kad4.rpc.events.StalledEvent;
import unet.kad4.rpc.events.inter.ResponseCallback;
import unet.kad4.utils.net.AddressType;
import unet.kad4.utils.net.AddressUtils;
import unet.shadowrouter.kad.ShadowServer;
import unet.shadowrouter.kad.messages.GetPortRequest;
import unet.shadowrouter.kad.messages.GetPortResponse;
import unet.shadowrouter.kad.utils.KeyUtils;
import unet.shadowrouter.tunnel.inter.Command;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

public class Relay implements Runnable {

    public static final byte[] SHADOW_ROUTER_HEADER = new byte[]{ 'S', 'R' };

    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private KademliaBase kademlia;

    private byte[] secret, iv;

    public Relay(KademliaBase kademlia, Socket socket){
        this.kademlia = kademlia;
        this.socket = socket;
    }

    @Override
    public void run(){
        try{
            in = socket.getInputStream();
            out = socket.getOutputStream();

            handshake();

            Command command = Command.getCommandFromCode((byte) in.read());

            byte[] addr = new byte[in.read()+2];
            in.read(addr);
            InetSocketAddress address = AddressUtils.unpackAddress(addr);

            if(AddressUtils.isBogon(address)){
                //IF ACCEPTED EXIT...

                //REPLY THAT GONNA BE A NO BRO
            }

            switch(command){
                case RESOLVE_PORT:
                    resolve(address);
                    break;

                case RELAY:
                    relay(address);
                    break;
            }

        }catch(IOException | NoSuchAlgorithmException | InvalidKeyException | SignatureException | InvalidKeySpecException |
               NoSuchPaddingException | InvalidAlgorithmParameterException e){
            //e.printStackTrace();
        }
    }

    public void handshake()throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException,
            InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        byte[] header = new byte[SHADOW_ROUTER_HEADER.length];
        in.read(header);

        if(!Arrays.equals(header, SHADOW_ROUTER_HEADER)){
            throw new IOException("Missing Shadow Router header");
        }

        int length = ((in.read() & 0xff) |
                ((in.read() & 0xff) << 8) |
                ((in.read() & 0xff) << 16) |
                ((in.read() & 0xff) << 24));

        byte[] data = new byte[length];
        in.read(data);

        iv = new byte[16];
        in.read(iv);


        KeyPair keyPair = KeyUtils.generateKeyPair("DH");
        secret = KeyUtils.generateSecret(keyPair.getPrivate(), KeyUtils.decodePublic(data, "DH"));
        //System.out.println("SERVER: "+secret.length+"  "+Base64.getEncoder().encodeToString(secret));

        out.write(SHADOW_ROUTER_HEADER);

        byte[] ecdhKey = keyPair.getPublic().getEncoded();

        byte[] len = new byte[4];
        len[0] = ((byte) ecdhKey.length);
        len[1] = ((byte) (ecdhKey.length >> 8));
        len[2] = ((byte) (ecdhKey.length >> 16));
        len[3] = ((byte) (ecdhKey.length >> 24));

        out.write(len);
        out.write(ecdhKey);
        //byte[] sign = sign(myKey, ecdhKey);
        //out.write(sign);
        out.write(KeyUtils.sign(((ShadowServer) kademlia.getServer()).getKeyPair().getPrivate(), ecdhKey));
        out.flush();

        MessageDigest digest = MessageDigest.getInstance("SHA-256");// Or use a proper KDF like HKDF
        SecretKey secretKey = new SecretKeySpec(digest.digest(secret), "AES");

        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));//new GCMParameterSpec(128, iv));
        in = new SecureInputStream(in, cipher);

        cipher = Cipher.getInstance("AES/CTR/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));//, new GCMParameterSpec(128, iv));
        out = new SecureOutputStream(out, cipher);
    }

    public void resolve(InetSocketAddress address)throws IOException {
        GetPortRequest request = new GetPortRequest();
        request.setDestination(address);
        kademlia.getServer().send(request, new ResponseCallback(){
            @Override
            public void onResponse(ResponseEvent event){
                GetPortResponse response = (GetPortResponse) event.getMessage();
                try{
                    relay(new InetSocketAddress(address.getAddress(), response.getPort()));

                }catch(IOException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onErrorResponse(ErrorResponseEvent event){
                try{
                    System.out.println("ERROR - GET_PORT");
                    in.close();
                    out.close();
                    socket.close();

                }catch(IOException e){
                }
            }

            @Override
            public void onStalled(StalledEvent event){
                try{
                    System.out.println("Stalled - GET_PORT");
                    in.close();
                    out.close();
                    socket.close();

                }catch(IOException e){
                }
            }
        });
    }

    //private boolean complete;

    public void relay(InetSocketAddress address)throws IOException {
        Socket relay = new Socket();
        //System.out.println(address.getAddress().getHostAddress()+"  "+address.getPort());
        relay.connect(address);

        out.write(0x00);
        out.flush();

        Thread thread = new Thread(new Runnable(){
            @Override
            public void run(){
                try{
                    transfer(in, relay.getOutputStream());
                    //while(!socket.isClosed() && !relay.isClosed()){
                    //    in.transferTo(relay.getOutputStream());
                    //}
                }catch(IOException e){
                    //e.printStackTrace();
                }

                /*
                if(complete){
                    System.err.println("CLOSED");
                    try{
                        relay.close();
                        socket.close();
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
            transfer(relay.getInputStream(), out);
        }catch(IOException e){
            //e.printStackTrace();
        }
        //while(!relay.isClosed() && !socket.isClosed()){
        //    relay.getInputStream().transferTo(out);
        //}
        try{
            thread.join();
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        relay.getInputStream().close();
        relay.getOutputStream().close();
        relay.close();

        in.close();
        out.close();
        socket.close();
        /*
        if(complete){
            System.err.println("CLOSED");
            relay.close();
            socket.close();
            return;
        }
        complete = true;
        */
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
}
