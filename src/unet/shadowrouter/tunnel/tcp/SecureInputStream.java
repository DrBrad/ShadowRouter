package unet.shadowrouter.tunnel.tcp;

import javax.crypto.Cipher;
import javax.crypto.NullCipher;
import javax.crypto.ShortBufferException;
import java.io.IOException;
import java.io.InputStream;

public class SecureInputStream extends InputStream {

    private InputStream in;
    private Cipher cipher;

    public SecureInputStream(InputStream in){
        this(in, new NullCipher());
    }

    public SecureInputStream(InputStream in, Cipher cipher){
        this.in = in;
        this.cipher = cipher;
    }

    @Override
    public int read()throws IOException {
        byte[] buf = new byte[1];
        read(buf, 0, 1);
        return buf[0];
    }

    @Override
    public int read(byte[] buf)throws IOException {
        return read(buf, 0, buf.length);
    }

    @Override
    public int read(byte[] buf, int off, int len)throws IOException {
        try{
            int length = in.read(buf, off, len);
            if(length > 0){
                length = cipher.update(buf, 0, length, buf, 0);
            }
            return length;
        }catch(ShortBufferException e){
            return 0;
        }
    }
}
