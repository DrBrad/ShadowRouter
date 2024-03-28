package unet.shadowrouter.tunnel.tcp;

import javax.crypto.Cipher;
import javax.crypto.NullCipher;
import java.io.IOException;
import java.io.OutputStream;

public class SecureOutputStream extends OutputStream {

    private OutputStream out;
    private Cipher cipher;

    public SecureOutputStream(OutputStream out){
        this(out, new NullCipher());
    }

    public SecureOutputStream(OutputStream out, Cipher cipher){
        this.out = out;
        this.cipher = cipher;
    }

    @Override
    public void write(int value)throws IOException {
        write(new byte[]{ (byte) value }, 0, 1);
    }

    @Override
    public void write(byte[] buf)throws IOException {
        write(buf, 0, buf.length);
    }

    @Override
    public void write(byte[] buf, int off, int len)throws IOException {
        //System.out.println(buf.length+"  "+off+"  "+len);
        out.write(cipher.update(buf, off, len));
    }

    @Override
    public void flush()throws IOException {
        out.flush();
    }
}
