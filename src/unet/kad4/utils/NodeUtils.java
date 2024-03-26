package unet.kad4.utils;

import unet.kad4.utils.net.AddressType;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import static unet.kad4.Server.PUBLIC_KEY_LENGTH;
import static unet.kad4.utils.UID.ID_LENGTH;

public class NodeUtils {

    public static byte[] packNodes(List<Node> nodes, AddressType type){
        byte[] buf = new byte[nodes.size()*(ID_LENGTH+PUBLIC_KEY_LENGTH+type.getAddressLength()+2)];
        int position = 0;

        for(Node n : nodes){
            byte[] bid = n.getUID().getBytes();
            System.arraycopy(bid, 0, buf, position, bid.length);
            position += bid.length;

            byte[] addr = n.getHostAddress().getAddress();
            System.arraycopy(addr, 0, buf, position, addr.length);
            position += addr.length;

            //PORT TIME...
            buf[position] = (byte) ((n.getPort() >> 8) & 0xff);
            buf[position+1] = (byte) (n.getPort() & 0xff);
            position += 2;

            byte[] key = n.getPublicKey().getEncoded();
            System.arraycopy(key, 0, buf, position, key.length);
            position += key.length;
        }

        return buf;
    }

    public static List<Node> unpackNodes(byte[] buf, AddressType type){
        List<Node> nodes = new ArrayList<>();

        byte[] bid = new byte[ID_LENGTH];
        byte[] addr = new byte[type.getAddressLength()];
        byte[] key = new byte[PUBLIC_KEY_LENGTH];
        int position = 0;
        int port;

        while(position < buf.length){
            System.arraycopy(buf, position, bid, 0, bid.length);
            position += bid.length;

            System.arraycopy(buf, position, addr, 0, addr.length);
            position += addr.length;

            port = ((buf[position] & 0xff) << 8) | (buf[position+1] & 0xff);
            position += 2;

            System.arraycopy(buf, position, key, 0, key.length);
            position += key.length;

            try{
                nodes.add(new Node(bid, InetAddress.getByAddress(addr), port, KeyUtils.decodePublic(key, "RSA")));

            }catch(UnknownHostException | NoSuchAlgorithmException | InvalidKeySpecException e){
                e.printStackTrace();
            }
        }

        return nodes;
    }
}
