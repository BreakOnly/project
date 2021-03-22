package com.jrmf.interceptor;


public enum CronJobMethod {

    GETLISTBYTYPEANDSTATUSONJOB("getListByTypeAndStatusOnJob", "c.originalId"),
    GETAPILISTBYTYPEANDSTATUSONJOB("getApiListByTypeAndStatusOnJob", "c.originalId"),
    GETLDLISTBYTYPEANDSTATUSONJOB("getLdListByTypeAndStatusOnJob", "c.originalId"),
    GETLDCORRECTLISTBYTYPEANDSTATUSONJOB("getLdCorrectListByTypeAndStatusOnJob", "c.originalId"),
    GETCHANNELHISTORYBYPARAMONJOB("getChannelHistoryByParamOnJob", "customkey"),
    GETAUTOCONFIRMLIST("getAutoConfirmList", "qc.customkey"),
    GETWARNINGRECHARGELIST("getWarningRechargeList", "qc.customkey"),
    GETSYNCBALANCELIST("getSyncBalanceList", "customKey"),
    GETPAYINGLIST("getPayingList", "customKey"),
    GETUSERSAGREEMENTSBYCHANNELTYPE("getUsersAgreementsByChannelType", "at.originalId");

    private final String methodName;
    private final String aliasName;

    CronJobMethod(String methodName, String aliasName) {
        this.methodName = methodName;
        this.aliasName = aliasName;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getAliasName() {
        return aliasName;
    }

    public static CronJobMethod codeOf(String methodName) {
        for (CronJobMethod cronJobMethod : values()) {
            if (cronJobMethod.getMethodName().equals(methodName)) {
                return cronJobMethod;
            }
        }
        return null;
    }
}
