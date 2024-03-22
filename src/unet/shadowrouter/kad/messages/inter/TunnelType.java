package unet.shadowrouter.kad.messages.inter;

public enum TunnelType {

    UTP {
        public String getTypeName(){
            return "utp";
        }
    },
    TCP {
        public String getTypeName(){
            return "tcp";
        }
    }, INVALID;

    public static TunnelType getFromTypeName(String name){
        for(TunnelType type : values()){
            if(name.equals(type.getTypeName())){
                return type;
            }
        }

        return INVALID;
    }

    public String getTypeName(){
        return null;
    }
}
