package com.jrmf.taxsettlement.api;

public enum APIDockingMode {

	CLOSED(0, "关闭"), TEST(1, "测试"), PRODUCTION(2, "生产");
	
	private int modeCode;
	
	private String modeDesc;

	private APIDockingMode(int modeCode, String modeDesc) {
		this.modeCode = modeCode;
		this.modeDesc = modeDesc;
	}

	public int getModeCode() {
		return modeCode;
	}

	public String getModeDesc() {
		return modeDesc;
	}
	
	public static APIDockingMode codeOf(int modeCode) {
		for(APIDockingMode mode : values()) {
			if(mode.modeCode == modeCode)
				return mode;
		}
		return null;
	}
}
