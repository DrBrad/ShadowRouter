package unet.shadowrouter.dns.messages;

import unet.shadowrouter.dns.messages.inter.*;
import unet.shadowrouter.dns.utils.DnsRecord;

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
        super.decode(buf);

        //QNAME
        StringBuilder builder = new StringBuilder();
        builder.append(new String(buf, 13, buf[12]));

        int offset = buf[12]+13;

        while(buf[offset] > 0){
            builder.append('.'+new String(buf, offset+1, buf[offset]));
            offset += buf[offset]+1;
        }

        domain = builder.toString();

        type = Types.getTypeFromCode(((buf[offset+1] & 0xFF) << 8) | (buf[offset+2] & 0xFF));
        dnsClass = DnsClass.getClassFromCode(((buf[offset+3] & 0xFF) << 8) | (buf[offset+4] & 0xFF));

        offset += 5;

        for(int i = 0; i < anCount; i++){
            switch((buf[offset] & 0b11000000) >>> 6){
                case 3:
                    byte current = buf[offset+1];

                    Types type = Types.getTypeFromCode(((buf[offset+2] & 0xFF) << 8) | (buf[offset+3] & 0xFF));

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
