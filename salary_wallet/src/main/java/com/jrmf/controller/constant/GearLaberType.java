package com.jrmf.controller.constant;

public enum GearLaberType {
	
	SMALLAMOUNT(1,"小额"),
	
	BIGAMOUNT(2,"大额");
	
    private final  int code;
    private final String desc;
	
	private GearLaberType(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}
	public int getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}

    public static GearLaberType codeOf(int code) {
        for(GearLaberType gearLaberType : values()) {
            if(gearLaberType.getCode() == code){
                return gearLaberType;
            }
        }
        return null;
    }

    public static GearLaberType codeOfDefault(int code) {
        for(GearLaberType gearLaberType : values()) {
            if(gearLaberType.getCode() == code){
                return gearLaberType;
            }
        }
        return SMALLAMOUNT;
    }
}

