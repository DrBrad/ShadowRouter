package unet.shadowrouter.kad.messages.inter;

import unet.kad4.messages.inter.MessageBase;

import java.security.PublicKey;

public class SecureMessageBase extends MessageBase {

    protected PublicKey publicKey;

    public void setPublicKey(PublicKey publicKey){
        this.publicKey = publicKey;
    }

    public PublicKey getPublicKey(){
        return publicKey;
    }
}
