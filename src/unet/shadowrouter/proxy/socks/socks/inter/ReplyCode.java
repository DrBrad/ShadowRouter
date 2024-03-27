package unet.shadowrouter.proxy.socks.socks.inter;

public enum ReplyCode {

    GRANTED {
        public byte getCode(){
            return 0x00;
        }
    },
    GENERAL_FAILURE {
        public byte getCode(){
            return 0x01;
        }
    },
    CONNECTION_NOT_ALLOWED {
        public byte getCode(){
            return 0x02;
        }
    },
    NETWORK_UNREACHABLE {
        public byte getCode(){
            return 0x03;
        }
    },
    HOST_UNREACHABLE {
        public byte getCode(){
            return 0x04;
        }
    },
    CONNECTION_REFUSED {
        public byte getCode(){
            return 0x05;
        }
    },
    TTL_EXPIRED {
        public byte getCode(){
            return 0x06;
        }
    },
    COMMAND_NOT_SUPPORTED {
        public byte getCode(){
            return 0x07;
        }
    },
    A_TYPE_NOT_SUPPORTED {
        public byte getCode(){
            return 0x08;
        }
    },
    UNASSIGNED {
        public byte getCode(){
            return 0x09;
        }
    };

    public static ReplyCode getReplyCodeFromCode(byte code){
        for(ReplyCode replyCode : values()){
            if(replyCode.getCode() == code){
                return replyCode;
            }
        }

        return UNASSIGNED;
    }

    public byte getCode(){
        return 0x00;
    }
}
