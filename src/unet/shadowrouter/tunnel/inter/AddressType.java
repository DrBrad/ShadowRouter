package unet.shadowrouter.tunnel.inter;

public enum AddressType {

    IPv4 {
        public byte getCode(){
            return 0x00;
        }

        public int getLength(){
            return 6;
        }
    },
    IPv6 {
        public byte getCode(){
            return 0x01;
        }

        public int getLength(){
            return 18;
        }
    },
    DOMAIN {
        public byte getCode(){
            return 0x02;
        }
    }, INVALID;

    public static AddressType getAddressTypeFromCode(byte code){
        for(AddressType type : values()){
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
