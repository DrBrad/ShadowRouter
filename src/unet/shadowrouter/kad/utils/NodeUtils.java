package unet.shadowrouter.kad.utils;

import unet.kad4.utils.net.AddressType;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import static unet.kad4.utils.UID.ID_LENGTH;

public class NodeUtils {

    public static byte[] packNodes(List<SecureNode> nodes, AddressType type){
        byte[][] buf = new byte[nodes.size()][];
        //byte[] buf = new byte[nodes.size()*(ID_LENGTH+PUBLIC_KEY_LENGTH+type.getAddressLength()+2)];
        //int position = 0;
        int length = 0;

        for(int i = 0; i < nodes.size(); i++){
            byte[] key = nodes.get(i).getPublicKey().getEncoded();
            buf[i] = new byte[ID_LENGTH+type.getAddressLength()+key.length+4];
            length += buf[i].length;

            byte[] bid = nodes.get(i).getUID().getBytes();
            System.arraycopy(bid, 0, buf[i], 0, bid.length);
            int offset = bid.length;

            byte[] addr = nodes.get(i).getHostAddress().getAddress();
            System.arraycopy(addr, 0, buf[i], offset, addr.length);
            offset += addr.length;

            //PORT TIME...
            buf[i][offset] = (byte) ((nodes.get(i).getPort() >> 8) & 0xff);
            buf[i][offset+1] = (byte) (nodes.get(i).getPort() & 0xff);
            offset += 2;


            buf[i][offset] = (byte) ((key.length >> 8) & 0xff);
            buf[i][offset+1] = (byte) (key.length & 0xff);

            System.arraycopy(key, 0, buf[i], offset+2, key.length);
        }

        byte[] data = new byte[length];
        int offset = 0;
        for(byte[] b : buf){
            System.arraycopy(b, 0, data, offset, b.length);
            offset += b.length;
        }

        return data;
    }

    public static List<SecureNode> unpackNodes(byte[] buf, AddressType type){
        List<SecureNode> nodes = new ArrayList<>();

        byte[] bid = new byte[ID_LENGTH];
        byte[] addr = new byte[type.getAddressLength()];
        byte[] key;
        int position = 0;
        int port;

        while(position < buf.length){
            System.arraycopy(buf, position, bid, 0, bid.length);
            position += bid.length;

            System.arraycopy(buf, position, addr, 0, addr.length);
            position += addr.length;

            port = ((buf[position] & 0xff) << 8) | (buf[position+1] & 0xff);
            position += 2;

            key = new byte[((buf[position] & 0xff) << 8) | (buf[position+1] & 0xff)];
            System.arraycopy(buf, position+2, key, 0, key.length);
            position += key.length+2;

            try{
                nodes.add(new SecureNode(bid, InetAddress.getByAddress(addr), port, KeyUtils.decodePublic(key, "RSA")));

            }catch(UnknownHostException | NoSuchAlgorithmException | InvalidKeySpecException e){
                e.printStackTrace();
            }
        }

        return nodes;
    }
}
