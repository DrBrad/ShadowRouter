package unet.shadowrouter.proxy.dns.messages.inter;

public enum ResponseCodes {

    //RCODE - 4 bit field

    NO_ERROR {
        public int getCode(){
            return 0;
        }
    },
    FORMAT_ERROR {
        public int getCode(){
            return 1;
        }
    },
    SERVER_FAILURE {
        public int getCode(){
            return 2;
        }
    },
    NAME_ERROR {
        public int getCode(){
            return 3;
        }
    },
    NOT_IMPLEMENTED {
        public int getCode(){
            return 4;
        }
    },
    REFUSED {
        public int getCode(){
            return 5;
        }
    },
    INVALID;

    public static ResponseCodes getResponseCodeFromCode(int code){
        for(ResponseCodes rcode : values()){
            if(code == rcode.getCode()){
                return rcode;
            }
        }

        return INVALID;
    }

    public int getCode(){
        return 6;
    }
}
