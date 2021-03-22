package com.jrmf.controller.constant;

/**
 * @author 种路路
 * @create 2018-11-01 21:08
 * //
 **/
public enum DocumentStep {

    /**
     * 0-创建
     1-上传身份证成功
     2-上传身份证失败
     */
    DOCUMENT_CREATE(0,"创建"),

    DOCUMENT_SUCCESS(1,"上传身份证成功"),

    DOCUMENT_FAIL(2,"上传身份证失败");

    private final  int code;
    private final String desc;

    DocumentStep(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }


    public static DocumentStep codeOf(int code) {
        for(DocumentStep certType : values()) {
            if(certType.getCode() == code){
                return certType;
            }
        }
        return DOCUMENT_CREATE;
    }

    public static DocumentStep codeOfDefault(int code) {
        for(DocumentStep certType : values()) {
            if(certType.getCode() == code){
                return certType;
            }
        }
        return null;
    }
    public static DocumentStep descOfDefault(String desc) {
        for(DocumentStep certType : values()) {
            if(desc.equals(certType.getDesc())){
                return certType;
            }
        }
        return null;
    }
}
