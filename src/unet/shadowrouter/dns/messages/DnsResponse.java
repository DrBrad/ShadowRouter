package unet.shadowrouter.dns.messages;

import unet.shadowrouter.dns.messages.inter.DnsClass;
import unet.shadowrouter.dns.messages.inter.MessageBase;
import unet.shadowrouter.dns.messages.inter.Type;
import unet.shadowrouter.dns.utils.DnsRecord;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class DnsResponse extends MessageBase {

    private List<DnsRecord> records;

    public DnsResponse(int id){
        this.id = id;
        records = new ArrayList<>();
    }

    @Override
    public byte[] encode(){
        return new byte[0];
    }

    @Override
    public void decode(byte[] buf){
        int qr = (buf[2] >> 7) & 0x01;
        int opcode = (buf[2] >> 3) & 0x0F;
        int aa = (buf[2] >> 2) & 0x01;
        int tc = (buf[2] >> 1) & 0x01;
        int rd = buf[2] & 0x01;
        int ra = (buf[3] >> 7) & 0x01;
        int z = (buf[3] >> 4) & 0x01;
        int rcode = buf[3] & 0x0F;

        int qdcount = ((buf[4] & 0xFF) << 8) | (buf[5] & 0xFF);
        int ancount = ((buf[6] & 0xFF) << 8) | (buf[7] & 0xFF);
        int nscount = ((buf[8] & 0xFF) << 8) | (buf[9] & 0xFF);
        int arcount = ((buf[10] & 0xFF) << 8) | (buf[11] & 0xFF);

        //QNAME
        StringBuilder builder = new StringBuilder();
        builder.append(new String(buf, 13, buf[12]));

        int offset = buf[12]+13;

        while(buf[offset] > 0){
            builder.append('.'+new String(buf, offset+1, buf[offset]));
            offset += buf[offset]+1;
        }

        domain = builder.toString();

        type = Type.getTypeFromCode(((buf[offset+1] & 0xFF) << 8) | (buf[offset+2] & 0xFF));
        dnsClass = DnsClass.getClassFromCode(((buf[offset+3] & 0xFF) << 8) | (buf[offset+4] & 0xFF));

        offset += 5;

        for(int i = 0; i < ancount; i++){
            switch((buf[offset] & 0b11000000) >>> 6){
                case 3:
                    byte current = buf[offset+1];

                    Type type = Type.getTypeFromCode(((buf[offset+2] & 0xFF) << 8) | (buf[offset+3] & 0xFF));

                    DnsClass dnsClass = DnsClass.getClassFromCode(((buf[offset+4] & 0xFF) << 8) | (buf[offset+5] & 0xFF));

                    int ttl = (((buf[offset+6] & 0xff) << 24) |
                            ((buf[offset+7] & 0xff) << 16) |
                            ((buf[offset+8] & 0xff) << 8) |
                            (buf[offset+9] & 0xff));

                    byte[] addr = new byte[((buf[offset+10] & 0xFF) << 8) | (buf[offset+11] & 0xFF)];
                    System.arraycopy(buf, offset+12, addr, 0, addr.length);

                    records.add(new DnsRecord(addr, type, dnsClass, ttl));
                    offset += addr.length+12;
                    break;

                case 0:
                    offset++;
                    break;
            }
        }
    }

    public boolean containsRecord(DnsRecord record){
        return records.contains(record);
    }

    public void addRecord(DnsRecord record){
        records.add(record);
    }

    public void removeRecord(DnsRecord record){
        records.remove(record);
    }

    public List<DnsRecord> getRecords(){
        return records;
    }

    public int totalRecords(){
        return records.size();
    }
}
