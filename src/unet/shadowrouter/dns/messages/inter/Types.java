package unet.shadowrouter.dns.messages.inter;

public enum Types {

    //Type - 16 bit field

    A {
        public int getCode(){
            return 1;
        }
    },
    AAAA {
        public int getCode(){
            return 28;
        }
    },
    NS {
        public int getCode(){
            return 2;
        }
    },
    CNAME {
        public int getCode(){
            return 5;
        }
    },
    SOA {
        public int getCode(){
            return 6;
        }
    },
    PTR {
        public int getCode(){
            return 12;
        }
    },
    MX {
        public int getCode(){
            return 15;
        }
    },
    TXT {
        public int getCode(){
            return 16;
        }
    },
    SRV {
        public int getCode(){
            return 33;
        }
    },
    CAA {
        public int getCode(){
            return 257;
        }
    }, INVALID;

    public static Types getTypeFromCode(int code){
        for(Types type : values()){
            if(code == type.getCode()){
                return type;
            }
        }

        return INVALID;
    }

    public int getCode(){
        return 0;
    }
}
