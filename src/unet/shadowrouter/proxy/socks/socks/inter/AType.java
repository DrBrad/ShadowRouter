package unet.shadowrouter.proxy.socks.socks.inter;

public enum AType {

    IPv4 {
        public byte getCode(){
            return 0x01;
        }

        public int getLength(){
            return 4;
        }
    },
    DOMAIN {
        public byte getCode(){
            return 0x03;
        }
    },
    IPv6 {
        public byte getCode(){
            return 0x04;
        }

        public int getLength(){
            return 16;
        }
    }, INVALID;

    public static AType getATypeFromCode(byte code){
        for(AType type : values()){
            if(type.getCode() == code){
                return type;
            }
        }

        return INVALID;
    }

    public byte getCode(){
        return 0x00;
    }

    public int getLength(){
        return 0;
    }
}
