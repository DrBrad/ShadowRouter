package unet.shadowrouter.tunnel.inter;

public enum Command {

    RESOLVE_PORT {
        public byte getCode(){
            return 0x00;
        }
    },
    RELAY {
        public byte getCode(){
            return 0x01;
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
        return (byte) 0xff;
    }
}
