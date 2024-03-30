package unet.shadowrouter.kad.messages;

import unet.kad4.messages.ErrorResponse;

import java.security.PublicKey;

public class SecureErrorResponse extends ErrorResponse {

    protected PublicKey publicKey;

    public SecureErrorResponse(){
        super();
    }

    public SecureErrorResponse(byte[] tid){
        super(tid);
    }

    public void setPublicKey(PublicKey publicKey){
        this.publicKey = publicKey;
    }

    public PublicKey getPublicKey(){
        return publicKey;
    }
}
