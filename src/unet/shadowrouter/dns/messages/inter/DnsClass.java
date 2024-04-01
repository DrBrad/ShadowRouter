package unet.shadowrouter.dns.messages.inter;

public enum DnsClass {

    IN {
        public int getCode(){
            return 1;
        }
    },
    CS {
        public int getCode(){
            return 2;
        }
    },
    CH {
        public int getCode(){
            return 3;
        }
    },
    HS {
        public int getCode(){
            return 4;
        }
    }, INVALID;

    public static DnsClass getClassFromCode(int code){
        for(DnsClass c : values()){
            if(code == c.getCode()){
                return c;
            }
        }

        return INVALID;
    }

    public int getCode(){
        return 0;
    }
}
