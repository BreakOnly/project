package com.jrmf.controller.constant;

public enum Operator {
    /**
     * <小于 >大于 <=小于等于 >=大于等于 =等于
     **/
    LESSTHAN("<", "小于"),
    MORETHEN(">", "大于"),
    LESSTHANOREQUAL("<=", "小于等于"),
    MORETHENOREQUAL(">=", "大于等于"),
    EQUAL("=", "等于");

    private String operator;
    private String operatorDesc;

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getOperatorDesc() {
        return operatorDesc;
    }

    public void setOperatorDesc(String operatorDesc) {
        this.operatorDesc = operatorDesc;
    }

    Operator(String operator, String operatorDesc) {
        this.operator = operator;
        this.operatorDesc = operatorDesc;
    }

    public static Operator codeOf(String operator) {
        for (Operator item : values()) {
            if (item.getOperator().equals(operator)) {
                return item;
            }
        }
        return null;
    }
}