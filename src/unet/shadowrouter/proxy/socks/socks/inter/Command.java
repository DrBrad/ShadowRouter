package unet.shadowrouter.proxy.socks.socks.inter;

public enum Command {

    CONNECT {
        public byte getCode(){
            return 0x01;
        }
    },
    BIND {
        public byte getCode(){
            return 0x02;
        }
    },
    UDP {
        public byte getCode(){
            return 0x03;
        }
    }, INVALID;

    public static Command getCommandFromCode(byte code){
        for(Command command : values()){
            if(command.getCode() == code){
                return command;
            }
        }

        return INVALID;
    }

    public byte getCode(){
        return 0x00;
    }
}
