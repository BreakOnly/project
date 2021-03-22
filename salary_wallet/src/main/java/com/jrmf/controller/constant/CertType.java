package com.jrmf.controller.constant;

/**
 * @author 种路路
 * @create 2018-11-01 21:08
 * @desc 身份证类型
 * //
 **/
public enum CertType {

    /**
     * 1 身份证  2 港澳台通行证 3 护照  4 军官证
     */
    ID_CARD(1,"身份证"),

    LAISSEZ_PASSER_TO_HONGKONG_MACAO(2,"港澳台通行证"),

    PASSPORT(3,"护照"),

    CERTIFICATE_OF_OFFICERS(4,"军官证");

    private final  int code;
    private final String desc;

    CertType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }


    public static CertType codeOf(int code) {
        for(CertType certType : values()) {
            if(certType.getCode() == code){
                return certType;
            }
        }
        return ID_CARD;
    }

    public static CertType codeOfDefault(int code) {
        for(CertType certType : values()) {
            if(certType.getCode() == code){
                return certType;
            }
        }
        return null;
    }
    public static CertType descOfDefault(String desc) {
        for(CertType certType : values()) {
            if(desc.equals(certType.getDesc())){
                return certType;
            }
        }
        return null;
    }

    public static String codeOfTwo(int code) {
        for(CertType certType : values()) {
            if(certType.getCode() == code){
                return certType.getDesc();
            }
        }
        return null;
    }
}
