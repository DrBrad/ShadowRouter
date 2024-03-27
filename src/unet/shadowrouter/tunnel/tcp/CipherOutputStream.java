package unet.shadowrouter.tunnel.tcp;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NullCipher;
import java.io.*;

public class CipherOutputStream extends FilterOutputStream {

    private Cipher cipher;

    public CipherOutputStream(OutputStream out){
        this(out, new NullCipher());
    }

    public CipherOutputStream(OutputStream out, Cipher cipher){
        super(out);
        this.cipher = cipher;
    }

    public void flush()throws IOException {
        out.flush();
    }

    public void write(int value)throws IOException {
        write(new byte[]{ (byte) value }, 0, 1);
    }

    public void write(byte[] buf)throws IOException {
        write(buf, 0, buf.length);
    }

    public void write(byte[] buf, int off, int len)throws IOException {
        //System.out.println(buf.length+"  "+off+"  "+len);
        out.write(cipher.update(buf, off, len));
    }

    public void close()throws IOException {
        try{
            byte[] buf = cipher.doFinal();
            if(buf.length > 0){
                out.write(buf);
                out.flush();
            }
        }catch(IllegalBlockSizeException | BadPaddingException e){
            e.printStackTrace();
        }
        out.close();
    }
}
