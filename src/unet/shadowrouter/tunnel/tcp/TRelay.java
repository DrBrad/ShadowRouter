package unet.shadowrouter.tunnel.tcp;

import unet.kad4.utils.net.AddressUtils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

import static unet.shadowrouter.utils.KeyRing.*;

public class TRelay implements Runnable {

    public static final byte[] SHADOW_ROUTER_HEADER = new byte[]{ 'S', 'R' };

    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private PrivateKey myKey;

    private byte[] secret, iv;

    public TRelay(PrivateKey myKey, Socket socket){
        this.myKey = myKey;
        this.socket = socket;
    }

    @Override
    public void run(){
        try{
            in = socket.getInputStream();
            out = socket.getOutputStream();

            handshake();

            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] derivedKey = digest.digest(secret); // Or use a proper KDF like HKDF
            SecretKey secretKey = new SecretKeySpec(derivedKey, "AES");

            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));//new GCMParameterSpec(128, iv));
            in = new CipherInputStream(in, cipher);

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));//, new GCMParameterSpec(128, iv));
            out = new CipherOutputStream(out, cipher);

            byte[] addr = new byte[in.read()];
            in.read(addr);
            relay(AddressUtils.unpackAddress(addr));

            in.close();
            out.close();
            socket.close();

        }catch(IOException | NoSuchAlgorithmException | InvalidKeyException | SignatureException | InvalidKeySpecException |
               NoSuchPaddingException | InvalidAlgorithmParameterException e){
            e.printStackTrace();
        }
    }

    public void handshake()throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException,
            InvalidKeySpecException {
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


        KeyPair keyPair = generateKeyPair("DH");
        secret = generateSecret(keyPair.getPrivate(), decodePublic(data, "DH"));
        System.out.println("SERVER: "+secret.length+"  "+Base64.getEncoder().encodeToString(secret));

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
        out.write(sign(myKey, ecdhKey));
        out.flush();
    }

    public void relay(InetSocketAddress address)throws IOException {
        Socket relay = new Socket();
        System.out.println(address.getAddress().getHostAddress()+"  "+address.getPort());
        relay.connect(address);

        Thread thread = new Thread(new Runnable(){
            @Override
            public void run(){
                try{
                    relay(in, relay.getOutputStream());
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        });//.start();
        thread.start();

        relay(relay.getInputStream(), out);
        try{
            thread.join();
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        relay.close();
        System.err.println("CLOSED");
    }

    private void relay(InputStream in, OutputStream out)throws IOException {
        byte[] buf = new byte[4096];
        int len;
        while((len = in.read(buf)) != -1){
            out.write(buf, 0, len);
            out.flush();
        }
    }
}