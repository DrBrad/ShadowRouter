package unet.shadowrouter.kad.messages;

import unet.bencode.variables.BencodeObject;
import unet.kad4.messages.inter.Message;
import unet.kad4.messages.inter.MessageException;
import unet.kad4.messages.inter.MessageType;
import unet.kad4.utils.net.AddressType;
import unet.shadowrouter.kad.utils.SecureNode;
import unet.shadowrouter.kad.messages.inter.MethodMessageBase;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.ArrayList;
import java.util.List;

import static unet.shadowrouter.kad.utils.NodeUtils.packNodes;
import static unet.shadowrouter.kad.utils.NodeUtils.unpackNodes;

@Message(method = "find_node", type = MessageType.RSP_MSG)
public class FindNodeResponse extends MethodMessageBase {

    /*
    * THIS CLASS COULD BE MADE MUCH BETTER BUT IT WORKS FOR NOW...
    * */

    public static final int NODE_CAP = 20;
    private List<SecureNode> nodes;

    public FindNodeResponse(byte[] tid){
        super(tid);//, Method.FIND_NODE, Type.RSP_MSG);
        nodes = new ArrayList<>();
        //ipv4Nodes = new ArrayList<>();
        //ipv6Nodes = new ArrayList<>();
    }

    @Override
    public BencodeObject encode(){
        BencodeObject ben = super.encode();

        if(nodes.isEmpty()){
            return ben;
        }

        List<SecureNode> nodes = getIPv4Nodes();
        if(!nodes.isEmpty()){
            ben.getBencodeObject(type.innerKey()).put("nodes", packNodes(nodes, AddressType.IPv4));
        }

        nodes = getIPv6Nodes();
        if(!nodes.isEmpty()){
            ben.getBencodeObject(type.innerKey()).put("nodes6", packNodes(nodes, AddressType.IPv6));
        }
        return ben;
    }

    @Override
    public void decode(BencodeObject ben)throws MessageException {
        super.decode(ben);

        if(!ben.getBencodeObject(type.innerKey()).containsKey("nodes") &&
                !ben.getBencodeObject(type.innerKey()).containsKey("nodes6")){
            throw new MessageException("Protocol Error, such as a malformed packet.", 203);
        }

        if(ben.getBencodeObject(type.innerKey()).containsKey("nodes")){
            nodes.addAll(unpackNodes(ben.getBencodeObject(type.innerKey()).getBytes("nodes"), AddressType.IPv4));
        }

        if(ben.getBencodeObject(type.innerKey()).containsKey("nodes6")){
            nodes.addAll(unpackNodes(ben.getBencodeObject(type.innerKey()).getBytes("nodes6"), AddressType.IPv6));
        }
    }

    public void addNode(SecureNode node){
        nodes.add(node);
    }

    public SecureNode getNode(int i){
        return nodes.get(i);
    }

    public void removeNode(SecureNode node){
        nodes.remove(node);
    }

    public boolean containsNode(SecureNode node){
        return nodes.contains(node);
    }

    public boolean hasNodes(){
        return !nodes.isEmpty();
    }

    public void addNodes(List<SecureNode> nodes){
        if(nodes.size()+this.nodes.size() > NODE_CAP){
            throw new IllegalArgumentException("Adding nodes would exceed Node Cap of "+NODE_CAP);
        }

        this.nodes.addAll(nodes);
    }

    public List<SecureNode> getAllNodes(){
        return nodes;
    }

    public List<SecureNode> getIPv4Nodes(){
        List<SecureNode> r = new ArrayList<>();

        for(SecureNode node : nodes){
            if(node.getHostAddress() instanceof Inet4Address){
                r.add(node);
            }
        }
        return r;
    }

    public List<SecureNode> getIPv6Nodes(){
        List<SecureNode> r = new ArrayList<>();

        for(SecureNode node : nodes){
            if(node.getHostAddress() instanceof Inet6Address){
                r.add(node);
            }
        }
        return r;
    }
    /*
    public void addNode(Node node){
        if(node.getHostAddress() instanceof Inet4Address){
            if(ipv4Nodes.size() > NODE_CAP){
                throw new IllegalArgumentException("Node cap already reached, the node cap is "+NODE_CAP);
            }

            ipv4Nodes.add(node);
            return;
        }

        if(ipv6Nodes.size() > NODE_CAP){
            throw new IllegalArgumentException("Node cap already reached, the node cap is "+NODE_CAP);
        }

        ipv6Nodes.add(node);
    }

    public void addNodes(List<Node> nodes){
        for(Node n : nodes){
            if(n.getHostAddress() instanceof Inet4Address){
                if(ipv4Nodes.size() < NODE_CAP){
                    ipv4Nodes.add(n);
                }
                continue;
            }

            if(ipv6Nodes.size() < NODE_CAP){
                ipv6Nodes.add(n);
            }
        }
    }

    public void addNodes(List<Node> nodes, AddressType type){
        switch(type){
            case IPv4:
                ipv4Nodes.addAll(nodes);
                break;

            case IPv6:
                ipv6Nodes.addAll(nodes);
                break;
        }
    }

    public boolean containsNode(Node node){
        if(node.getHostAddress() instanceof Inet4Address){
            return ipv4Nodes.contains(node);
        }
        return ipv6Nodes.contains(node);
    }

    public boolean removeNode(Node node){
        if(node.getHostAddress() instanceof Inet4Address){
            return ipv4Nodes.remove(node);
        }
        return ipv6Nodes.remove(node);
    }

    public List<Node> getIPv4Nodes(){
        return ipv4Nodes;
    }

    public List<Node> getIPv6Nodes(){
        return ipv6Nodes;
    }

    public List<Node> getAllNodes(){
        List<Node> nodes = new ArrayList<>();
        nodes.addAll(ipv4Nodes);
        nodes.addAll(ipv6Nodes);
        return nodes;
    }
    */

    /*
    @Override
    public BencodeObject getBencode(){
        BencodeObject ben = super.getBencode();

        if(!ipv4Nodes.isEmpty()){
            ben.getBencodeObject(t.innerKey()).put("nodes", packNodes(ipv4Nodes, AddressType.IPv4));
        }

        if(!ipv6Nodes.isEmpty()){
            ben.getBencodeObject(t.innerKey()).put("nodes6", packNodes(ipv6Nodes, AddressType.IPv6));
        }

        return ben;
    }
    */
}
