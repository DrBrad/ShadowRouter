package unet.shadowrouter.proxy.dns.messages.inter;

public enum OpCodes {

    QUERY {
        public int getCode(){
            return 0;
        }
    },
    IQUERY {
        public int getCode(){
            return 1;
        }
    },
    STATUS {
        public int getCode(){
            return 2;
        }
    }, INVALID;

    public static OpCodes getOpFromCode(int code){
        for(OpCodes opCode : values()){
            if(code == opCode.getCode()){
                return opCode;
            }
        }

        return INVALID;
    }

    public int getCode(){
        return 16;
    }
}
