package com.jrmf.taxsettlement.api.service.recharge;

/**
 * @author 种路路
 * @create 2019年8月23日10:15:24
 * @desc api充值状态
 * //
 **/
public enum APIRechargeStatus {

    /**
     * // 0 待确认       1 成功  2 失败
     */
    SUCCESS(1, "0000", "成功"),

    FAILED(2, "0002", "失败"),

    TO_BE_CONFIRMED(0, "0001", "待确认");

    private final  int code;
    private final String APIStatus;
    private final String desc;

    APIRechargeStatus(int code, String apiStatus, String desc) {
        this.code = code;
        this.APIStatus = apiStatus;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String getAPIStatus() {
        return APIStatus;
    }

    public static APIRechargeStatus codeOfDefault(int code) {
        for(APIRechargeStatus certType : values()) {
            if(certType.getCode() == code){
                return certType;
            }
        }
        return null;
    }
    public static APIRechargeStatus codeOf(int code) {
        for(APIRechargeStatus certType : values()) {
            if(certType.getCode() == code){
                return certType;
            }
        }
        return TO_BE_CONFIRMED;
    }
    public static APIRechargeStatus descOfDefault(String desc) {
        for(APIRechargeStatus certType : values()) {
            if(desc.equals(certType.getDesc())){
                return certType;
            }
        }
        return null;
    }
}
