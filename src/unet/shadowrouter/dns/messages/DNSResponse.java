package unet.shadowrouter.dns.messages;

import unet.shadowrouter.dns.messages.inter.MessageBase;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class DNSResponse extends MessageBase {

    public DNSResponse(int id){
        this.id = id;
    }

    @Override
    public byte[] encode(){
        return new byte[0];
    }

    @Override
    public void decode(byte[] buf){
        int QR = (buf[2] >> 7) & 0x01;
        int Opcode = (buf[2] >> 3) & 0x0F;
        int AA = (buf[2] >> 2) & 0x01;
        int TC = (buf[2] >> 1) & 0x01;
        int RD = buf[2] & 0x01;
        int RA = (buf[3] >> 7) & 0x01;
        int Z = (buf[3] >> 4) & 0x01;
        int RCODE = buf[3] & 0x0F;

        //System.out.println("QR "+QR);
        //System.out.println("Opcode "+Opcode);
        //System.out.println("AA "+AA);
        //System.out.println("TC "+TC);
        //System.out.println("RD "+RD);
        //System.out.println("RA "+RA);
        //System.out.println("Z "+ Z);
        //System.out.println("RCODE " +RCODE);

        int QDCOUNT = ((buf[4] & 0xFF) << 8) | (buf[5] & 0xFF);
        int ANCOUNT = ((buf[6] & 0xFF) << 8) | (buf[7] & 0xFF);
        int NSCOUNT = ((buf[8] & 0xFF) << 8) | (buf[9] & 0xFF);
        int ARCOUNT = ((buf[10] & 0xFF) << 8) | (buf[11] & 0xFF);

        //System.out.println("QDCOUNT: " + QDCOUNT);
        //System.out.println("ANCOUNT: " + ANCOUNT);
        //System.out.println("NSCOUNT: " + NSCOUNT);
        //System.out.println("ARCOUNT: " + ARCOUNT);


        //QNAME

        byte[] qname = null;

        int offset = 12;
        while(buf[offset] > 0){
            qname = new byte[buf[offset]];
            System.arraycopy(buf, offset+1, qname, 0, qname.length);
            offset += qname.length+1;
        }

        String QNAME = new String(qname);
        int QTYPE = ((buf[offset+1] & 0xFF) << 8) | (buf[offset+2] & 0xFF);
        int QCLASS = ((buf[offset+3] & 0xFF) << 8) | (buf[offset+4] & 0xFF);
        offset += 5;

        //System.out.println("Record: " + QNAME);
        //System.out.println("Record Type: " + String.format("%s", QTYPE));
        //System.out.println("Class: " + String.format("%s", QCLASS));


        for(int i = 0; i < ANCOUNT; i++){
            switch((buf[offset] & 0b11000000) >>> 6){
                case 3:
                    byte current = buf[offset+1];

                    int TYPE = ((buf[offset+2] & 0xFF) << 8) | (buf[offset+3] & 0xFF);

                    int CLASS = ((buf[offset+4] & 0xFF) << 8) | (buf[offset+5] & 0xFF);

                    int TTL = (((buf[offset+6] & 0xff) << 24) |
                            ((buf[offset+7] & 0xff) << 16) |
                            ((buf[offset+8] & 0xff) << 8) |
                            (buf[offset+9] & 0xff));

                    byte[] addr = new byte[((buf[offset+10] & 0xFF) << 8) | (buf[offset+11] & 0xFF)];
                    System.arraycopy(buf, offset+12, addr, 0, addr.length);
                    try{
                        InetAddress address = InetAddress.getByAddress(addr);

                        System.out.println("Type: " + TYPE);
                        System.out.println("Class: " + CLASS);
                        System.out.println("Time to live: " + TTL);
                        System.out.println("Rd Length: " + addr.length);
                        System.out.println(address.getHostAddress());

                    }catch(UnknownHostException e){
                        e.printStackTrace();
                    }

                    offset += addr.length+12;
                    break;

                case 0:

                    break;
            }
            offset++;
        }
    }
}
