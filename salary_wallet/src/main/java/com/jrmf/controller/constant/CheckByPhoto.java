package com.jrmf.controller.constant;

/**
 * 是否证照认证
 */
public enum  CheckByPhoto {

    YES(1),
    NO(0);

    private int code;


    CheckByPhoto(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }


}
