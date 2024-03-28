package unet.shadowrouter.kad.utils;

import unet.kad4.utils.Node;
import unet.kad4.utils.UID;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.PublicKey;

public class SecureNode extends Node {

    private PublicKey publicKey;

    public SecureNode(String uid, InetAddress address, int port, PublicKey publicKey){
        super(new UID(uid), new InetSocketAddress(address, port));
        this.publicKey = publicKey;
    }

    public SecureNode(String uid, InetSocketAddress address, PublicKey publicKey){
        super(new UID(uid), address);
        this.publicKey = publicKey;
    }

    public SecureNode(byte[] bid, InetAddress address, int port, PublicKey publicKey){
        super(new UID(bid), new InetSocketAddress(address, port));
        this.publicKey = publicKey;
    }

    public SecureNode(byte[] bid, InetSocketAddress address, PublicKey publicKey){
        super(new UID(bid), address);
        this.publicKey = publicKey;
    }

    public SecureNode(UID uid, InetAddress address, int port, PublicKey publicKey){
        super(uid, new InetSocketAddress(address, port));
        this.publicKey = publicKey;
    }

    public SecureNode(UID uid, InetSocketAddress address, PublicKey publicKey){
        super(uid, address);
        this.publicKey = publicKey;
    }

    public PublicKey getPublicKey(){
        return publicKey;
    }
}
