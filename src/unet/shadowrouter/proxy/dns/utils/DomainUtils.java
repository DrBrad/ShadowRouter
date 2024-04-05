package unet.shadowrouter.proxy.dns.utils;

public class DomainUtils {

    public static byte[] packDomain(String domain){
        byte[] buf = new byte[domain.length()+2];

        int offset = 0;
        for(String part : domain.split("\\.")){
            byte[] addr = part.getBytes();
            buf[offset] = (byte) addr.length;
            System.arraycopy(addr, 0, buf, offset+1, addr.length);
            offset += addr.length+1;
        }

        buf[offset] = 0x00;

        return buf;
    }

    public static String unpackDomain(byte[] addr, int off){
        StringBuilder builder = new StringBuilder();

        int i = off;
        while(i < addr.length){
            int length = addr[i++];

            if(length == 0){
                break;
            }

            if((length & 0xc0) == 0xc0){
                i = ((length & 0x3f) << 8) | (addr[i++] & 0xff);

            }else{
                if(builder.length() > 0){
                    builder.append(".");
                }

                byte[] label = new byte[length];
                System.arraycopy(addr, i, label, 0, length);
                builder.append(new String(label));
                i += length;
            }
        }

        return builder.toString();
    }
}
