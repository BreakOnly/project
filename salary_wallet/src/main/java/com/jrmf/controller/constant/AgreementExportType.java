package com.jrmf.controller.constant;

/**
 * @author chonglulu
 * 协议导出类型
 */

public enum AgreementExportType {
    PDF(1,"导出pdf"),
    WORD(1,"导出word"),
    PICTURE(1,"导出身份证照片");
    private final  int code;
    private final String desc;

    AgreementExportType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static AgreementExportType codeOfDefault(int code) {
        for(AgreementExportType type : values()) {
            if(type.getCode() == code){
                return type;
            }
        }
        return null;
    }
}
