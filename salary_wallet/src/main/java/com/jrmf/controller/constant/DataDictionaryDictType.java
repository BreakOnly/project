package com.jrmf.controller.constant;

public enum DataDictionaryDictType {

    RECHARGE_WARNING("recharge_warning", "充值异常提醒"),
    RECHARGE_CONFIRM_AMOUNT("recharge_confirm_amount", "充值自动认账偏差金额"),
    INPUT_BATCH_WORDS("input_batch_words", "导入批次拦截关键字");

    private final String dictType;
    private final String dictName;

    DataDictionaryDictType(String dictType, String dictName) {
        this.dictType = dictType;
        this.dictName = dictName;
    }

    public String getDictType() {
        return dictType;
    }

    public String getDictName() {
        return dictName;
    }

    public static DataDictionaryDictType codeOf(String dictType) {
        for (DataDictionaryDictType dictionaryType : values()) {
            if (dictionaryType.getDictType().equals(dictType)) {
                return dictionaryType;
            }
        }
        return null;
    }

}
