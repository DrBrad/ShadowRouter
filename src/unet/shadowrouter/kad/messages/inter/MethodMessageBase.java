package unet.shadowrouter.kad.messages.inter;

import unet.bencode.variables.BencodeObject;
import unet.kad4.messages.inter.Message;
import unet.kad4.messages.inter.MessageBase;
import unet.kad4.utils.UID;
import unet.kad4.utils.net.AddressUtils;
import unet.shadowrouter.kad.utils.KeyUtils;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

public class MethodMessageBase extends SecureMessageBase {

    protected String method;

    public MethodMessageBase(){
        if(getClass().isAnnotationPresent(Message.class)){
            Message message = getClass().getAnnotation(Message.class);
            method = message.method();
            type = message.type();
        }
    }

    public MethodMessageBase(byte[] tid){
        this();
        this.tid = tid;
    }

    @Override
    public BencodeObject encode(){
        BencodeObject ben = super.encode();

        switch(type){
            case REQ_MSG:
                ben.put(type.getRPCTypeName(), method);
                ben.put(type.innerKey(), new BencodeObject());
                ben.getBencodeObject(type.innerKey()).put("id", uid.getBytes());
                ben.getBencodeObject(type.innerKey()).put("k", publicKey.getEncoded());
                break;

            case RSP_MSG:
                ben.put(type.innerKey(), new BencodeObject());
                ben.getBencodeObject(type.innerKey()).put("id", uid.getBytes());
                ben.getBencodeObject(type.innerKey()).put("k", publicKey.getEncoded());

                if(publicAddress != null){
                    ben.put("ip", AddressUtils.packAddress(publicAddress)); //PACK MY IP ADDRESS
                }
                break;
        }

        return ben;
    }

    @Override
    public void decode(BencodeObject ben){
        super.decode(ben);

        if(!ben.getBencodeObject(type.innerKey()).containsKey("id")){
            //throw new MessageException("Request doesn't contain UID", ErrorMessage.ErrorType.PROTOCOL);
        }

        if(!ben.getBencodeObject(type.innerKey()).containsKey("k")){
            //throw new MessageException("Request doesn't contain Public Key");
        }

        uid = new UID(ben.getBencodeObject(type.innerKey()).getBytes("id"));
        try{
            publicKey = KeyUtils.decodePublic(ben.getBencodeObject(type.innerKey()).getBytes("k"), "RSA");

        }catch(NoSuchAlgorithmException | InvalidKeySpecException e){
            e.printStackTrace();
        }

        switch(type){
            case RSP_MSG:
                if(ben.containsKey("ip")){
                    publicAddress = AddressUtils.unpackAddress(ben.getBytes("ip"));
                }
                break;
        }
    }

    public String getMethod(){
        return method;
    }
}
