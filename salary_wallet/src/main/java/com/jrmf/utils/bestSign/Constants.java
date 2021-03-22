package com.jrmf.utils.bestSign;

import static com.jrmf.utils.bestSign.ConstantsEnum.CONTRACT_ORDER_STATE_AUTHING;
import static com.jrmf.utils.bestSign.ConstantsEnum.CONTRACT_ORDER_STATE_AUTH_ERR;
import static com.jrmf.utils.bestSign.ConstantsEnum.CONTRACT_ORDER_STATE_AUTH_FAIL;
import static com.jrmf.utils.bestSign.ConstantsEnum.CONTRACT_ORDER_STATE_CANCEL;
import static com.jrmf.utils.bestSign.ConstantsEnum.CONTRACT_ORDER_STATE_CLOSED;
import static com.jrmf.utils.bestSign.ConstantsEnum.CONTRACT_ORDER_STATE_CLOSE_ERR;
import static com.jrmf.utils.bestSign.ConstantsEnum.CONTRACT_ORDER_STATE_CREATE_ERR;
import static com.jrmf.utils.bestSign.ConstantsEnum.CONTRACT_ORDER_STATE_EXPIRED;
import static com.jrmf.utils.bestSign.ConstantsEnum.CONTRACT_ORDER_STATE_EXPIRE_CLOSED;
import static com.jrmf.utils.bestSign.ConstantsEnum.CONTRACT_ORDER_STATE_IMP_ERR;
import static com.jrmf.utils.bestSign.ConstantsEnum.CONTRACT_ORDER_STATE_IMP_SUCCESS;
import static com.jrmf.utils.bestSign.ConstantsEnum.CONTRACT_ORDER_STATE_NOTIFY_ERR;
import static com.jrmf.utils.bestSign.ConstantsEnum.CONTRACT_ORDER_STATE_REJECTED;
import static com.jrmf.utils.bestSign.ConstantsEnum.CONTRACT_ORDER_STATE_SIGNING;
import static com.jrmf.utils.bestSign.ConstantsEnum.CONTRACT_ORDER_STATE_SIGN_ERR;
import static com.jrmf.utils.bestSign.ConstantsEnum.CONTRACT_ORDER_SUB_STATE_SIGNED;
import static com.jrmf.utils.bestSign.ConstantsEnum.CONTRACT_ORDER_SUB_STATE_SIGNING;
import static com.jrmf.utils.bestSign.ConstantsEnum.CONTRACT_ORDER_SUB_STATE_SIGN_NOTNEED;
import static com.jrmf.utils.bestSign.ConstantsEnum.CONTRACT_ORDER_SUB_STATE_SIGN_REJECTED;

import java.util.HashMap;
import java.util.Map;

public class Constants {

    public static final Map<String, String> impSubStateMap = new HashMap<>();

//    static {
//        impSubStateMap.put(IMP_MSG_ERROR_IDENTITY_TYPE.getCode(), IMP_MSG_ERROR_IDENTITY_TYPE.getMsg());
//        impSubStateMap.put(IMP_MSG_FORMAT_ERROR_ID.getCode(), IMP_MSG_FORMAT_ERROR_ID.getMsg());
//        impSubStateMap.put(IMP_MSG_FORMAT_ERROR_MOBILE.getCode(), IMP_MSG_FORMAT_ERROR_MOBILE.getMsg());
//        impSubStateMap.put(IMP_MSG_DUPL_ID.getCode(), IMP_MSG_DUPL_ID.getMsg());
//        impSubStateMap.put(IMP_MSG_DUPL_MOBILE.getCode(), IMP_MSG_DUPL_MOBILE.getMsg());
//        impSubStateMap.put(IMP_MSG_USER_SIGNED.getCode(), IMP_MSG_USER_SIGNED.getMsg());
//        impSubStateMap.put(IMP_MSG_INVALID_NAME.getCode(),IMP_MSG_INVALID_NAME.getMsg());
//        impSubStateMap.put(IMP_MSG_NAME_OVER_LENGTH.getCode(),IMP_MSG_NAME_OVER_LENGTH.getMsg());
//        impSubStateMap.put(IMP_MSG_IDENTITY_TYPE_OVER_LENGTH.getCode(),IMP_MSG_IDENTITY_TYPE_OVER_LENGTH.getMsg());
//        impSubStateMap.put(IMP_MSG_MOBILE_OVER_LENGTH.getCode(),IMP_MSG_MOBILE_OVER_LENGTH.getMsg());
//        impSubStateMap.put(IMP_MSG_IDENTITY_OVER_LENGTH.getCode(),IMP_MSG_IDENTITY_OVER_LENGTH.getMsg());
//        impSubStateMap.put(IMP_MSG_BLANK_NAME.getCode(),IMP_MSG_BLANK_NAME.getMsg());
//        impSubStateMap.put(IMP_MSG_BLANK_MOBILE.getCode(),IMP_MSG_BLANK_MOBILE.getMsg());
//        impSubStateMap.put(IMP_MSG_DUPL_EXTORDER.getCode(),IMP_MSG_DUPL_EXTORDER.getMsg());
//        impSubStateMap.put(IMP_MSG_BLANK_EXTORDER.getCode(),IMP_MSG_BLANK_EXTORDER.getMsg());
//        impSubStateMap.put(IMP_MSG_EXTORDER_OVER_LENGTH.getCode(),IMP_MSG_EXTORDER_OVER_LENGTH.getMsg());
//        impSubStateMap.put(IMP_MSG_INVALID_EXTORDER.getCode(),IMP_MSG_INVALID_EXTORDER.getMsg());
//    }

    public static final Map<String, String> subStateMap = new HashMap<>();

    static {
        subStateMap.put(CONTRACT_ORDER_SUB_STATE_SIGN_NOTNEED.getCode(), CONTRACT_ORDER_SUB_STATE_SIGN_NOTNEED.getMsg());
        subStateMap.put(CONTRACT_ORDER_SUB_STATE_SIGNING.getCode(), CONTRACT_ORDER_SUB_STATE_SIGNING.getMsg());
        subStateMap.put(CONTRACT_ORDER_SUB_STATE_SIGNED.getCode(), CONTRACT_ORDER_SUB_STATE_SIGNED.getMsg());
        subStateMap.put(CONTRACT_ORDER_SUB_STATE_SIGN_REJECTED.getCode(), CONTRACT_ORDER_SUB_STATE_SIGN_REJECTED.getMsg());
    }

    public static Map<String, String> stateMap = new HashMap<>();

    static {
        stateMap.put(CONTRACT_ORDER_STATE_IMP_SUCCESS.getCode(), CONTRACT_ORDER_STATE_IMP_SUCCESS.getMsg());
        stateMap.put(CONTRACT_ORDER_STATE_IMP_ERR.getCode(), CONTRACT_ORDER_STATE_IMP_ERR.getMsg());
        stateMap.put(CONTRACT_ORDER_STATE_AUTHING.getCode(), CONTRACT_ORDER_STATE_AUTHING.getMsg());
        stateMap.put(CONTRACT_ORDER_STATE_AUTH_ERR.getCode(), CONTRACT_ORDER_STATE_AUTH_ERR.getMsg());
        stateMap.put(CONTRACT_ORDER_STATE_AUTH_FAIL.getCode(), CONTRACT_ORDER_STATE_AUTH_FAIL.getMsg());
        stateMap.put(CONTRACT_ORDER_STATE_CREATE_ERR.getCode(), CONTRACT_ORDER_STATE_CREATE_ERR.getMsg());
        stateMap.put(CONTRACT_ORDER_STATE_SIGN_ERR.getCode(), CONTRACT_ORDER_STATE_SIGN_ERR.getMsg());
        stateMap.put(CONTRACT_ORDER_STATE_SIGNING.getCode(), CONTRACT_ORDER_STATE_SIGNING.getMsg());
        stateMap.put(CONTRACT_ORDER_STATE_NOTIFY_ERR.getCode(), CONTRACT_ORDER_STATE_NOTIFY_ERR.getMsg());
        stateMap.put(CONTRACT_ORDER_STATE_REJECTED.getCode(), CONTRACT_ORDER_STATE_REJECTED.getMsg());
        stateMap.put(CONTRACT_ORDER_STATE_EXPIRED.getCode(), CONTRACT_ORDER_STATE_EXPIRED.getMsg());
        stateMap.put(CONTRACT_ORDER_STATE_EXPIRE_CLOSED.getCode(), CONTRACT_ORDER_STATE_EXPIRE_CLOSED.getMsg());
        stateMap.put(CONTRACT_ORDER_STATE_CANCEL.getCode(), CONTRACT_ORDER_STATE_CANCEL.getMsg());
        stateMap.put(CONTRACT_ORDER_STATE_CLOSED.getCode(), CONTRACT_ORDER_STATE_CLOSED.getMsg());
        stateMap.put(CONTRACT_ORDER_STATE_CLOSE_ERR.getCode(), CONTRACT_ORDER_STATE_CLOSE_ERR.getMsg());
    }
    
}
