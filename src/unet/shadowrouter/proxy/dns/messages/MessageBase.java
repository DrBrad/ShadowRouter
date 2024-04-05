package unet.shadowrouter.proxy.dns.messages;

import unet.shadowrouter.proxy.dns.messages.inter.OpCodes;
import unet.shadowrouter.proxy.dns.messages.inter.ResponseCodes;
import unet.shadowrouter.proxy.dns.messages.inter.Types;
import unet.shadowrouter.proxy.dns.records.*;
import unet.shadowrouter.proxy.dns.records.inter.DnsRecord;
import unet.shadowrouter.proxy.dns.utils.DnsQuery;
import unet.shadowrouter.proxy.dns.utils.DomainUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageBase {

    /*
                                   1  1  1  1  1  1
     0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                      ID                       |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |QR|   Opcode  |AA|TC|RD|RA|   Z    |   RCODE   |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                    QDCOUNT                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                    ANCOUNT                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                    NSCOUNT                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                    ARCOUNT                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    */

    private int id;
    private OpCodes opCode = OpCodes.QUERY;
    private ResponseCodes responseCode = ResponseCodes.NO_ERROR;

    private boolean qr, authoritative, truncated, recursionDesired, recursionAvailable;
    private int length = 12;

    private InetSocketAddress origin, destination;

    private List<DnsQuery> queries;
    private List<DnsRecord> answers, nameServers, additionalRecords;

    public MessageBase(){
        queries = new ArrayList<>();
        answers = new ArrayList<>();
        nameServers = new ArrayList<>();
        additionalRecords = new ArrayList<>();
    }

    public MessageBase(int id){
        this();
        this.id = id;
    }

    public byte[] encode(){
        byte[] buf = new byte[length];
        buf[0] = (byte) (id >> 8); // First 8 bits
        buf[1] = (byte) id; // Second 8 bits

        int z = 0;

        int flags = (qr ? 0x8000 : 0) | // QR bit
                ((opCode.getCode() & 0x0F) << 11) | // Opcode bits
                (authoritative ? 0x0400 : 0) | // AA bit
                (truncated ? 0x0200 : 0) | // TC bit
                (recursionDesired ? 0x0100 : 0) | // RD bit
                (recursionAvailable ? 0x0080 : 0) | // RA bit
                ((z & 0x07) << 4) | // Z bits
                (responseCode.getCode() & 0x0F); // RCODE bits

        buf[2] = (byte) (flags >> 8); // First 8 bits
        buf[3] = (byte) flags; // Second 8 bits

        // QDCOUNT (16 bits)
        buf[4] = (byte) (queries.size() >> 8);
        buf[5] = (byte) queries.size();

        // ANCOUNT (16 bits)
        buf[6] = (byte) (answers.size() >> 8);
        buf[7] = (byte) answers.size();

        // NSCOUNT (16 bits)
        buf[8] = (byte) (nameServers.size() >> 8);
        buf[9] = (byte) nameServers.size();

        // ARCOUNT (16 bits)
        buf[10] = (byte) (additionalRecords.size() >> 8);
        buf[11] = (byte) additionalRecords.size();

        Map<String, Integer> queryMap = new HashMap<>();
        int offset = 12;

        for(DnsQuery query : queries){
            byte[] q = query.encode();
            System.arraycopy(q, 0, buf, offset, q.length);
            queryMap.put(query.getQuery(), offset);
            offset += q.length;
        }

        System.err.println(queries.size()+"  "+answers.size()+"  "+nameServers.size()+"  "+additionalRecords.size());

        for(DnsRecord record : answers){
            int pointer = queryMap.get(record.getQuery());
            buf[offset] = (byte) (pointer >> 8);
            buf[offset+1] = (byte) pointer;

            byte[] q = record.encode();
            System.arraycopy(q, 0, buf, offset+2, q.length);

            offset += q.length+2;
        }

        return buf;
    }

    public void decode(byte[] buf){
        qr = ((buf[2] >> 7) & 0x1) == 1;
        opCode = OpCodes.getOpFromCode((buf[2] >> 3) & 0xF);
        authoritative = ((buf[2] >> 2) & 0x1) == 1;
        truncated = ((buf[2] >> 1) & 0x1) == 1;
        recursionDesired = (buf[2] & 0x1) == 1;
        recursionAvailable = ((buf[3] >> 7) & 0x1) == 1;
        int z = (buf[3] >> 4) & 0x3;
        responseCode = ResponseCodes.getResponseCodeFromCode(buf[3] & 0xF);

        int qdCount = ((buf[4] & 0xFF) << 8) | (buf[5] & 0xFF);
        int anCount = ((buf[6] & 0xFF) << 8) | (buf[7] & 0xFF);
        int nsCount = ((buf[8] & 0xFF) << 8) | (buf[9] & 0xFF);
        int arCount = ((buf[10] & 0xFF) << 8) | (buf[11] & 0xFF);

        System.out.println(qdCount+"  "+anCount+"  "+nsCount+"  "+arCount);

        int offset = 12;

        for(int i = 0; i < qdCount; i++){
            DnsQuery query = new DnsQuery();
            query.decode(buf, offset);
            queries.add(query);
            offset += query.getLength();
        }

        for(int i = 0; i < anCount; i++){
            int pointer = (((buf[offset] & 0x3F) << 8) | (buf[offset+1] & 0xFF)) & 0x3FFF;
            offset += 2;
            String query = DomainUtils.unpackDomain(buf, pointer);

            DnsRecord record = createRecordByType(Types.getTypeFromCode(((buf[offset] & 0xFF) << 8) | (buf[offset+1] & 0xFF)));
            record.setQuery(query);
            record.decode(buf, offset+2);
            answers.add(record);
            offset += ((buf[offset+8] & 0xFF) << 8) | (buf[offset+9] & 0xFF)+10;
        }

        for(int i = 0; i < nsCount; i++){
            int pointer = (((buf[offset] & 0x3F) << 8) | (buf[offset+1] & 0xFF)) & 0x3FFF;
            offset += 2;
            String query = DomainUtils.unpackDomain(buf, pointer);

            DnsRecord record = createRecordByType(Types.getTypeFromCode(((buf[offset] & 0xFF) << 8) | (buf[offset+1] & 0xFF)));
            record.setQuery(query);
            record.decode(buf, offset+2);
            nameServers.add(record);
            offset += ((buf[offset+8] & 0xFF) << 8) | (buf[offset+9] & 0xFF)+10;
        }

        for(int i = 0; i < arCount; i++){
            int pointer = (((buf[offset] & 0x3F) << 8) | (buf[offset+1] & 0xFF)) & 0x3FFF;
            offset += 2;
            String query = DomainUtils.unpackDomain(buf, pointer);

            DnsRecord record = createRecordByType(Types.getTypeFromCode(((buf[offset] & 0xFF) << 8) | (buf[offset+1] & 0xFF)));
            record.setQuery(query);
            record.decode(buf, offset+2);
            additionalRecords.add(record);
            offset += ((buf[offset+8] & 0xFF) << 8) | (buf[offset+9] & 0xFF)+10;
        }

        length = offset;
    }

    private DnsRecord createRecordByType(Types type){
        switch(type){
            case A:
                return new ARecord();

            case AAAA:
                return new AAAARecord();

            case NS:
                return new NSRecord();

            case CNAME:
                return new CNameRecord();

            case SOA:
                return new SOARecord();

            case PTR:
                return new PTRRecord();

            case MX:
                return new MXRecord();

            case TXT:
                return new TXTRecord();

            case SRV:
                System.out.println("SRV");
                return null;

            case CAA:
                System.out.println("CAA");
                return null;

            default:
                System.out.println("UNKNOWN");
                return null;
        }
    }

    public void setID(int id){
        this.id = id;
    }

    public int getID(){
        return id;
    }

    public void setQR(boolean qr){
        this.qr = qr;
    }

    public boolean isQR(){
        return qr;
    }

    public void setOpCode(OpCodes opCode){
        this.opCode = opCode;
    }

    public OpCodes getOpCode(){
        return opCode;
    }

    public InetSocketAddress getDestination(){
        return destination;
    }

    public InetAddress getDestinationAddress(){
        return destination.getAddress();
    }

    public int getDestinationPort(){
        return destination.getPort();
    }

    public void setDestination(InetAddress address, int port){
        destination = new InetSocketAddress(address, port);
    }

    public void setDestination(InetSocketAddress destination){
        this.destination = destination;
    }

    public InetSocketAddress getOrigin(){
        return origin;
    }

    public InetAddress getOriginAddress(){
        return origin.getAddress();
    }

    public int getOriginPort(){
        return origin.getPort();
    }

    public void setOrigin(InetAddress address, int port){
        origin = new InetSocketAddress(address, port);
    }

    public void setOrigin(InetSocketAddress origin){
        this.origin = origin;
    }

    public void setAuthoritative(boolean authoritative){
        this.authoritative = authoritative;
    }

    public boolean isAuthoritative(){
        return authoritative;
    }

    public void setTruncated(boolean truncated){
        this.truncated = truncated;
    }

    public boolean isTruncated(){
        return truncated;
    }

    public void setRecursionDesired(boolean recursionDesired){
        this.recursionDesired = recursionDesired;
    }

    public boolean isRecursionDesired(){
        return recursionDesired;
    }

    public void setRecursionAvailable(boolean recursionAvailable){
        this.recursionAvailable = recursionAvailable;
    }

    public boolean isRecursionAvailable(){
        return recursionAvailable;
    }

    public void setResponseCode(ResponseCodes responseCode){
        this.responseCode = responseCode;
    }

    public ResponseCodes getResponseCode(){
        return responseCode;
    }

    public int totalQueries(){
        return queries.size();
    }

    public void addQuery(DnsQuery query){
        queries.add(query);
        length += query.getLength();
    }

    public List<DnsQuery> getQueries(){
        return queries;
    }

    public void addAnswer(DnsRecord record){
        answers.add(record);
        length += record.getLength()+2;
    }

    public List<DnsRecord> getAnswers(){
        return answers;
    }

    public List<DnsRecord> getNameServers(){
        return nameServers;
    }

    public List<DnsRecord> getAdditionalRecords(){
        return additionalRecords;
    }
}
