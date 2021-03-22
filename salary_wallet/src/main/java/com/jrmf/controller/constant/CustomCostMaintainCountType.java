package com.jrmf.controller.constant;

/**
 * @author chonglulu
 * @time: 2019年11月4日09:51:34
 * 代理类型
 */
public enum CustomCostMaintainCountType {

    /**
     * 统计计算方式 0:下级代理差额成本统计 1：本级代理商直接成本统计
     */
    NEXT_LEVEL_DIFF_COST(0,"下级代理差额成本统计"),

    THIS_LEVEL_DIRECT_COST(1,"本级代理商直接成本统计");


    private final int code;
    private final String desc;

    CustomCostMaintainCountType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static CustomCostMaintainCountType codeOf(int code){
        for (CustomCostMaintainCountType type : values()) {
            if(code == type.code){
                return type;
            }
        }
        return null;
    }

    public static CustomCostMaintainCountType codeOfDefault(int code){
        for (CustomCostMaintainCountType type : values()) {
            if(code == type.code){
                return type;
            }
        }
        return NEXT_LEVEL_DIFF_COST;
    }
}
