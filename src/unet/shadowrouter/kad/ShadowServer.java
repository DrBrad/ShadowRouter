package unet.shadowrouter.kad;

import unet.bencode.variables.BencodeObject;
import unet.kad4.kad.KademliaBase;
import unet.kad4.kad.Server;
import unet.kad4.messages.ErrorResponse;
import unet.kad4.messages.inter.MessageBase;
import unet.kad4.messages.inter.MessageKey;
import unet.kad4.messages.inter.MessageType;
import unet.kad4.rpc.Call;
import unet.kad4.rpc.events.ErrorResponseEvent;
import unet.kad4.rpc.events.RequestEvent;
import unet.kad4.rpc.events.ResponseEvent;
import unet.kad4.rpc.events.inter.ResponseCallback;
import unet.kad4.utils.ByteWrapper;
import unet.kad4.utils.Node;
import unet.kad4.utils.ReflectMethod;
import unet.kad4.utils.net.AddressUtils;
import unet.shadowrouter.kad.messages.inter.MethodMessageBase;
import unet.shadowrouter.kad.messages.inter.SecureMessageBase;
import unet.shadowrouter.kad.utils.KeyUtils;
import unet.shadowrouter.kad.utils.SecureNode;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.DatagramPacket;
import java.security.*;

import static unet.kad4.messages.inter.MessageBase.TID_KEY;

public class ShadowServer extends Server {

    public static final int PUBLIC_KEY_LENGTH = 294;

    private KeyPair keyPair;

    public ShadowServer(KademliaBase kademlia){
        super(kademlia);

        try{
            keyPair = KeyUtils.generateKeyPair("RSA");
        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
        }
    }

    public KeyPair getKeyPair(){
        return keyPair;
    }

    @Override
    protected void onReceive(DatagramPacket packet){
        if(AddressUtils.isBogon(packet.getAddress(), packet.getPort())){
            //return;
        }

        //SPAM THROTTLE...

        //CATCH IF NO TID... - MESSAGE IS POINTLESS - IGNORE

        BencodeObject ben = new BencodeObject(packet.getData());

        if(!ben.getBencodeObject("d").containsKey(TID_KEY) || !ben.getBencodeObject("d").containsKey(MessageType.TYPE_KEY)){
            return;
        }

        MessageType t = MessageType.fromRPCTypeName(ben.getBencodeObject("d").getString(MessageType.TYPE_KEY));

        try{
            switch(t){
                case REQ_MSG: {
                    MessageKey k = new MessageKey(ben.getBencodeObject("d").getString(t.getRPCTypeName()), t);
                    if(!messages.containsKey(k)){
                        return;
                    }

                    MethodMessageBase m = (MethodMessageBase) messages.get(k)/*.getDeclaredConstructor(byte[].class)*/.newInstance(ben.getBencodeObject("d").getBytes(TID_KEY));//.decode(ben);
                    m.decode(ben.getBencodeObject("d")); //ERROR THROW - SEND ERROR MESSAGE

                    try{
                        if(!KeyUtils.verify(m.getPublicKey(), ben.getBytes("s"), ben.getBencodeObject("d").encode())){
                            return;
                        }
                    }catch(NoSuchAlgorithmException | InvalidKeyException | SignatureException e){
                        e.printStackTrace();
                    }

                    m.setOrigin(packet.getAddress(), packet.getPort());

                    if(!requestMapping.containsKey(m.getMethod())){
                        return;
                    }

                    Node node = new SecureNode(m.getUID(), m.getOrigin(), m.getPublicKey());
                    kademlia.getRoutingTable().insert(node);
                    //System.out.println("SEEN REQ "+node);

                    RequestEvent event = new RequestEvent(m, node);
                    event.received();
                    //event.setResponse(messages.get(new MessageKey(ben.getString(t.getRPCTypeName()), Type.RSP_MSG)).newInstance(ben.getBytes(TID_KEY)));

                    for(ReflectMethod r : requestMapping.get(m.getMethod()/*new EventKey(m.getMethod(), m.getType())*/)){
                        r.getMethod().invoke(r.getInstance(), event); //THROW ERROR - SEND ERROR MESSAGE
                    }

                    if(event.isPreventDefault() || !event.hasResponse()){
                        return;
                    }

                    send(event.getResponse());

                    if(!kademlia.getRefreshHandler().isRunning()){
                        kademlia.getRefreshHandler().start();
                    }
                }
                break;

                case RSP_MSG: {
                    byte[] tid = ben.getBencodeObject("d").getBytes(TID_KEY);
                    Call call = tracker.poll(new ByteWrapper(tid));
                    if(call == null){
                        return;
                    }

                    MessageKey k = new MessageKey(((MethodMessageBase) call.getMessage()).getMethod(), t);
                    if(!messages.containsKey(k)){
                        return;
                    }

                    MethodMessageBase m = (MethodMessageBase) messages.get(k)/*.getDeclaredConstructor(byte[].class)*/.newInstance(tid);//.decode(ben);
                    m.decode(ben.getBencodeObject("d"));

                    try{
                        if(!KeyUtils.verify(m.getPublicKey(), ben.getBytes("s"), ben.getBencodeObject("d").encode())){
                            return;
                        }
                    }catch(NoSuchAlgorithmException | InvalidKeyException | SignatureException e){
                        e.printStackTrace();
                    }

                    m.setOrigin(packet.getAddress(), packet.getPort());

                    if(m.getPublic() != null){
                        kademlia.getRoutingTable().updatePublicIPConsensus(m.getOriginAddress(), m.getPublicAddress());
                    }

                    //!req.getMessage().getUID().equals(m.getUID()) - THAT WOULDNT MATCH UP...
                    if(!call.getMessage().getDestination().equals(m.getOrigin())){
                        return;
                    }

                    ResponseEvent event;

                    if(call.hasNode()){
                        if(!call.getNode().getUID().equals(m.getUID())){
                            return;
                        }
                        event = new ResponseEvent(m, call.getNode());

                    }else{
                        event = new ResponseEvent(m, new SecureNode(m.getUID(), m.getOrigin(), m.getPublicKey()));
                    }

                    event.received();
                    event.setSentTime(call.getSentTime());
                    event.setRequest(call.getMessage());

                    if(call.hasResponseCallback()){
                        call.getResponseCallback().onResponse(event);
                    }
                }
                break;

                case ERR_MSG: {
                    byte[] tid = ben.getBencodeObject("d").getBytes(TID_KEY);
                    Call call = tracker.poll(new ByteWrapper(tid));
                    if(call == null){
                        return;
                    }

                    ErrorResponse m = new ErrorResponse(tid);
                    m.decode(ben.getBencodeObject("d"));

                        /*
                        try{
                            if(!KeyUtils.verify(m.getPublicKey(), ben.getBytes("s"), ben.getBencodeObject("d").encode())){
                                return;
                            }
                        }catch(NoSuchAlgorithmException | InvalidKeyException | SignatureException e){
                            e.printStackTrace();
                        }
                        */

                    m.setOrigin(packet.getAddress(), packet.getPort());

                    //if(m.getPublic() != null){
                    //    kademlia.getRoutingTable().updatePublicIPConsensus(m.getOriginAddress(), m.getPublicAddress());
                    //}

                    //!req.getMessage().getUID().equals(m.getUID()) - THAT WOULDNT MATCH UP...
                    if(!call.getMessage().getDestination().equals(m.getOrigin())){
                        return;
                    }

                    /*
                    ErrorResponseEvent event;// = new ErrorResponseEvent(m);
                    //

                    if(call.hasNode()){
                        if(!call.getNode().getUID().equals(m.getUID())){
                            return;
                        }
                        event = new ErrorResponseEvent(m, call.getNode());

                    }else{
                        event = new ErrorResponseEvent(m, new SecureNode(m.getUID(), m.getOrigin(), m.getPublicKey()));
                    }

                    event.received();
                    event.setSentTime(call.getSentTime());
                    event.setRequest(call.getMessage());

                    if(call.hasResponseCallback()){
                        call.getResponseCallback().onErrorResponse(event);
                    }
                    */
                }
                break;
            }

        }catch(IllegalArgumentException | InvocationTargetException | InstantiationException | IllegalAccessException |
               IOException/* | MessageException*/ e){
            e.printStackTrace();
        }
    }

    @Override
    public void send(MessageBase message)throws IOException {
        if(!(message instanceof SecureMessageBase)){
            throw new IllegalArgumentException("Message must inherit SecureMessageBase");
        }
        SecureMessageBase secureMessage = (SecureMessageBase) message;

        if(secureMessage.getDestination() == null){
            throw new IllegalArgumentException("Message destination set to null");
        }

        if(AddressUtils.isBogon(secureMessage.getDestination())){
            //throw new IllegalArgumentException("Message destination set to bogon");
        }

        secureMessage.setUID(kademlia.getRoutingTable().getDerivedUID());
        secureMessage.setPublicKey(keyPair.getPublic());

        try{
            BencodeObject ben = new BencodeObject();
            ben.put("d", secureMessage.encode());
            ben.put("s", KeyUtils.sign(keyPair.getPrivate(), ben.getBencodeObject("d").encode()));
            byte[] data = ben.encode();

            server.send(new DatagramPacket(data, 0, data.length, secureMessage.getDestination()));

        }catch(NoSuchAlgorithmException | InvalidKeyException | SignatureException e){
            e.printStackTrace();
        }
    }
}
