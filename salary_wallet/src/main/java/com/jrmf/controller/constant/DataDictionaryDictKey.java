package com.jrmf.controller.constant;

public enum DataDictionaryDictKey {

    EMAIL("email", "邮件方式"),
    MINUTE("minute", "间隔分钟数"),
    UPAMOUNT("upAmount", "上偏移金额"),
    DOWNAMOUNT("downAmount", "下偏移金额"),
    WORDS("words", "关键字");

    private final String dictKey;
    private final String dictValue;

    DataDictionaryDictKey(String dictKey, String dictValue) {
        this.dictKey = dictKey;
        this.dictValue = dictValue;
    }

    public String getDictKey() {
        return dictKey;
    }

    public String getDictValue() {
        return dictValue;
    }

    public static DataDictionaryDictKey codeOf(String dictKey) {
        for (DataDictionaryDictKey dictionaryType : values()) {
            if (dictionaryType.getDictKey().equals(dictKey)) {
                return dictionaryType;
            }
        }
        return null;
    }

}
