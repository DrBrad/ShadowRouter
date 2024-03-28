package unet.shadowrouter.tunnel.tcp;

import javax.crypto.Cipher;
import javax.crypto.NullCipher;
import javax.crypto.ShortBufferException;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CipherInputStream extends FilterInputStream {

    private Cipher cipher;

    public CipherInputStream(InputStream in){
        this(in, new NullCipher());
    }

    public CipherInputStream(InputStream in, Cipher cipher){
        super(in);
        this.cipher = cipher;
    }

    public int read()throws IOException {
        byte[] buf = new byte[1];
        read(buf, 0, 1);
        return buf[0];
    }

    public int read(byte[] buf)throws IOException {
        return read(buf, 0, buf.length);
    }

    public int read(byte[] buf, int off, int len)throws IOException {
        try{
            int length = in.read(buf, off, len);
            if(length > 0){
                //length = cipher.update(buf, off, length, buf, off);
                byte[] encrypted = cipher.update(buf, off, length);
                System.arraycopy(encrypted, 0, buf, off, encrypted.length); // Copy encrypted data back to input buffer
                length = encrypted.length;
            }
            return length;
        }catch(Exception e){
        //}catch(ShortBufferException e){
            //return 0;
            throw new IOException("Error reading from input stream", e);
        }
    }

    public void readFully(byte[] b, int off, int len)throws Exception {
        if(len < 0){
            throw new IndexOutOfBoundsException("Negative length: "+len);
        }

        while(len > 0){
            int numread = read(b, off, len);
            if(numread < 0){
                throw new EOFException();
            }
            len -= numread;
            off += numread;
        }
    }
}
