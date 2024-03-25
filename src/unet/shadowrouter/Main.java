package unet.shadowrouter;

import unet.kad4.Kademlia;
import unet.shadowrouter.kad.SRequestListener;
import unet.shadowrouter.kad.messages.*;
import unet.shadowrouter.tunnel.tcp.TClient;
import unet.shadowrouter.tunnel.tcp.TTunnelServer;
import unet.shadowrouter.utils.KeyRing;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.KeyPair;

public class Main {

    /*
    aes-128-gcm, aes-192-gcm, aes-256-gcm,
    aes-128-cfb, aes-192-cfb, aes-256-cfb,
    aes-128-ctr, aes-192-ctr, aes-256-ctr,
    camellia-128-cfb, camellia-192-cfb,
    camellia-256-cfb, bf-cfb,


    ENSURING RELAY INTEGRITY
    - NODES WILL CREATE A BLOCK / DESCRIPTOR CONTAINING THEIR NODE ID
    - THE NODE WILL SEND IT TO THEIR 4 CLOSEST NODES FOR SIGNING AND STORING
    - THE NODE WILL HAVE TO SEND A STORE EVERY 2 HOURS TO KEEP LIVELYNESS

    ENSURE JOINING IS SAFE
    - start with 10 or so nodes with their public keys
    - ask to do a find node - response should be all of the nodes signed by that node

    MANUAL JOIN (assuming 10 defaults are offline)
    - user enters a node ip, port, uid, public-key (descriptor file)
    - users does find node on that node

    JOINING FROM ALTERNATE NODE AFTER
    - use saved file descriptor to try find node

    ENSURING NEW NODE IS LEGITIMATE
    - ask 4 neighboring nodes to the node in question for the nodes public_key
    - verify the validity of the descriptor
    - descriptor must be signed by the node in which you spoke with
    - if 80% of the nodes state the descriptor is valid then it likely is
        - we will obtain 4 certificates by asking & 1 from the node we obtained the new node

    EXAMPLE DESCRIPTOR - BENCODE
    - use node6 for IPv6
    {
        data: {
            version: '1.0',
            node: bytes (1.1.1.1:1000/UID),
            public_key: bytes (public_key),
            signed: {
                node: bytes (1.1.1.1:1000/UID),
                public_key: bytes (public_key),
            }
        }
        signature: byte(signature)
    }

    POSSIBLE DOWNFALLS
    - Sybil attacks (hard to own 1 keyspace, if not impossible

    TAKE INTO ACCOUNT
    - we would have to find node without verification - meaning they could be untruthful



    - or






    we can trust the entry node 100% then we do find_node
    we obtain 20 new nodes that we can trust due to signature of entry
    now we should have 21 total nodes (? do we trust them ?)
    - how do we verify (we dont ask the node in question for neighbors due to trust)
    - take the closest node to the node in question - if the node in question is (1111) we will talk to (1110)
    - do a find_node on that node (repeat until we dont receive closer nodes)



    - we can verify each node by doing a different find_node on that node (repeat find node on those nodes received to ensure the closest)
    - then take the 4 closest nodes to the node in question and do a find_certificate for the node in question

    on restart get new UID, generate public_key, find our keyspace, pass to closest nodes to us

    the method above would only not work if the entry node is not trustworthy
    or if





    - do we simply only have that specific node sign their message of the nodes and dont verify find node
    - only verify the certificates? - this would likely be fine as we dont need authentication for responses
    - we only need authentication for relays...

    - sure a node could lie about the nodes given out but they couldnt own a keyspace
    - not safe for mitm as we dont know if the certs received for trust are valid


    what would have to occur
    joining - find node > trust based off signing - each node has a consensus of trust - new nodes are 1/5
    - somehow authenticate these newly created nodes...




    we will then refresh
    then once we have a good sized routing table







    we can trust the entry node 100% then we do find_node
    we obtain 20 new nodes that we can trust due to signature of entry
    each node will contain public_key
    this will be fine and can function as regular kademlia

    if we wish to start a relay we will first take the public_key
    - hash the public_key hash it and do a find_certificate
    - we will receive 5 nodes stating the certificate alongside that nodes signature

    - we dont verify every certificate as its unnecisary we only care about authenticity of nodes we wish to relay
    - we sign every request because we want to stop MITM

    DOWNFALLS
    ---------
    - if entry is untrustworthy
    - if all 5 nodes that own the keyspace are bad, but this would default in no-accept as what we have received is not
    what we are currently know as the certificate

    LOCAL TRUST AUTHORITY
    ---------------------
    - defaults will be 100% or if user inputs a node it will be 100%
    - if we decide to enter based off of previous it would have to be a node that is 100% trusted
    - a new node can be set to 100% by the user manually or if the user tries to relay and that relay had 100%
    matching response
    - a user shouldn't relay to a node until it does a certificate authentication (being a find_certificate and matching
    currently known with the 5 received & verifying that those 5 received have valid signatures)





    Could we simply auto trust every node received after entry? - assuming entry is legitimate
    - the only attack vector would be so that the node only gives out only evil nodes that they own
        - but when we do a find node on a keyspace we are looking at 20 nodes closest to the identifier


    - FORK KAD4

    https://stackoverflow.com/questions/71998790/using-cipherinputstream-with-sockets-finding-size-of-unencrypted-file

    */

    public static void main(String[] args)throws Exception {
        /*
        Kademlia kad = new Kademlia();
        kad.registerRequestListener(new SRequestListener());

        kad.registerMessage(GetCircuitRequest.class);
        kad.registerMessage(GetCircuitResponse.class);

        kad.registerMessage(PutCircuitRequest.class);
        kad.registerMessage(PutCircuitResponse.class);

        kad.bind();
        */


        //NON GENESIS
        //WE MUST KNOW THE NODES PUBLIC_KEY...
        //kad.join(6881, );

        //WE COULD HAVE A REQUEST SIGN - IN WHICH A > SIGNS FOR B


        /*
        KeyPair alice = generateKeyPair();
        KeyPair bob = generateKeyPair();

        PublicKey alicePublicKey = decodePublic(alice.getPublic().getEncoded());
        PublicKey bobPublicKey = decodePublic(bob.getPublic().getEncoded());

        //System.out.println(Base64.getEncoder().encodeToString(generateSecret(alice.getPrivate(), bobPublicKey)));
        //System.out.println(Base64.getEncoder().encodeToString(generateSecret(bob.getPrivate(), alicePublicKey)));

        byte[] data = "HELLO WORLD".getBytes();
        byte[] signature = sign(alice.getPrivate(), data);
        boolean verify = verify(alicePublicKey, signature, data);
        System.out.println("VERIFY: "+verify);


        data = "HELLO WORLD 2".getBytes();
        verify = verify(alicePublicKey, signature, data);
        System.out.println("VERIFY: "+verify);
        */


        KeyPair keyPairB = KeyRing.generateKeyPair("RSA");

        TTunnelServer server = new TTunnelServer(keyPairB);
        server.start(6969);

        //KeyPair keyPairA = KeyRing.generateKeyPair("RSA");
        TClient client = new TClient(keyPairB.getPublic());
        client.connect(new InetSocketAddress(InetAddress.getLocalHost(), 6969));

        OutputStream out = client.getOutputStream();
        out.write("HELLO WORLD".getBytes());
        client.close();
    }
}
