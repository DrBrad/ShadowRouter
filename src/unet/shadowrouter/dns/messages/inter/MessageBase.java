package unet.shadowrouter.dns.messages.inter;

public class MessageBase {

    protected int id;
    protected String domain;
    protected Types type = Types.A;
    protected OpCodes opCode = OpCodes.QUERY;
    protected DnsClass dnsClass = DnsClass.IN;
    protected ResponseCodes responseCode = ResponseCodes.NO_ERROR;

    protected boolean qr, authoritative, truncated, recursionDesired, recursionAvailable;
    protected int qdCount, anCount, nsCount, arCount;

    public byte[] encode(){
        byte[] buf = new byte[getLength()];
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
        buf[4] = (byte) (qdCount >> 8);
        buf[5] = (byte) qdCount;

        // ANCOUNT (16 bits)
        buf[6] = (byte) (anCount >> 8);
        buf[7] = (byte) anCount;

        // NSCOUNT (16 bits)
        buf[8] = (byte) (nsCount >> 8);
        buf[9] = (byte) nsCount;

        // ARCOUNT (16 bits)
        buf[10] = (byte) (arCount >> 8);
        buf[11] = (byte) arCount;

        return buf;
    }

    public void decode(byte[] buf){
        qr = ((buf[2] >> 7) & 0x1) == 1;
        opCode = OpCodes.getOpFromCode((buf[2] >> 3) & 0xF);
        authoritative = ((buf[2] >> 2) & 0x1) == 1;
        truncated = ((buf[2] >> 1) & 0x1) == 1;
        //boolean recursionDesired = (buf[2] & 0x1) == 1;
        recursionAvailable = ((buf[3] >> 7) & 0x1) == 1;
        int z = (buf[3] >> 4) & 0x3;
        responseCode = ResponseCodes.getResponseCodeFromCode(buf[3] & 0xF);

        qdCount = ((buf[4] & 0xFF) << 8) | (buf[5] & 0xFF);
        anCount = ((buf[6] & 0xFF) << 8) | (buf[7] & 0xFF);
        nsCount = ((buf[8] & 0xFF) << 8) | (buf[9] & 0xFF);
        arCount = ((buf[10] & 0xFF) << 8) | (buf[11] & 0xFF);
    }

    public int getLength(){
        return 12;
    }

    public void setID(int id){
        this.id = id;
    }

    public int getID(){
        return id;
    }

    public void setDomain(String domain){
        this.domain = domain;
    }

    public String getDomain(){
        return domain;
    }

    public void setType(Types type){
        this.type = type;
    }

    public Types getType(){
        return type;
    }

    public void setDnsClass(DnsClass dnsClass){
        this.dnsClass = dnsClass;
    }

    public DnsClass getDnsClass(){
        return dnsClass;
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

    public void setQdCount(int qdCount){
        this.qdCount = qdCount;
    }

    public int getQdCount(){
        return qdCount;
    }

    public void setAnCount(int anCount){
        this.anCount = anCount;
    }

    public int getAnCount(){
        return anCount;
    }

    public void setNsCount(int nsCount){
        this.nsCount = nsCount;
    }

    public int getNsCount(){
        return nsCount;
    }

    public void setArCount(int arCount){
        this.arCount = arCount;
    }

    public int getArCount(){
        return arCount;
    }
}
