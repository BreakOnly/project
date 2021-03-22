package com.jrmf.utils.bestSign;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.xwork.StringUtils;

@SuppressWarnings("unused")
public class ContractOrderExtrResult extends CommonExtrResult {

    /**
	* @Fields serialVersionUID : TODO()
	*/

	private static final long serialVersionUID = 3296474546587839618L;
	private String impSubStateDesc;
    private String partyaSubStateDesc;
    private String partybSubStateDesc;
    private String partycSubStateDesc;
    private String stateDesc;
    private String orderId;
    private Date expireTime;
    private Date createTime;
    private String partyaUserName;
    private String partyaSignUrl;
    private Date partyaSignTime;
    private String partybUserName;
    private String partybSignUrl;
    private Date partybSignTime;
    private String partycUserName;
    private String partycSignUrl;
    private Date partycSignTime;
    private String state;
    private String impSubState;
    private String subState;
    private String msg;
    private String personalName;
    private String personalMobile;
    private String personalIdentityType;
    private String personalIdentity;
    private Date lastNotifyTime;
    private String outerDownloadUrl;
    private String extrOrderId;
    private String extrSystemId;
    private String notifyResultcode;
    private String notifyResultmessage;
    private Date notifyTime;
    private String notifyUrl;

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public String getImpSubStateDesc() {
        if (ConstantsEnum.CONTRACT_ORDER_STATE_IMP_ERR.getCode().equals(this.getState())) {
            List errs = new ArrayList();
            if(StringUtils.isNotEmpty(this.getImpSubState())) {
                for (int i = 0; i < this.getImpSubState().length(); i++) {
                    if (this.getImpSubState().charAt(i) == '1') {
                        errs.add(Constants.impSubStateMap.get(i + ""));
                    }
                }
                if (CollectionUtils.isEmpty(errs)) {
                    return "";
                } else {
                    return StringUtils.join(errs, ",");
                }
            }
        }
        return "";
    }


    public String getPartyaSubStateDesc() {
        return (StringUtils.isBlank(this.getSubState())||StringUtils.isBlank(this.getPartyaUserName()))?"":Constants.subStateMap.get(String.valueOf(this.getSubState().charAt(0)));
    }

    public String getPartybSubStateDesc() {
        return (StringUtils.isBlank(this.getSubState())||StringUtils.isBlank(this.getPartybUserName()))?"":Constants.subStateMap.get(String.valueOf(this.getSubState().charAt(1)));
    }

    public String getPartycSubStateDesc() {
        return (StringUtils.isBlank(this.getSubState())||StringUtils.isBlank(this.getPartycUserName()))?"":Constants.subStateMap.get(String.valueOf(this.getSubState().charAt(2)));
    }

    public String getStateDesc() {
        return StringUtils.isBlank(this.getState())?"":Constants.stateMap.get(this.getState());
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getPartyaSignUrl() {
        return partyaSignUrl;
    }

    public void setPartyaSignUrl(String partyaSignUrl) {
        this.partyaSignUrl = partyaSignUrl;
    }

    public Date getPartyaSignTime() {
        return partyaSignTime;
    }

    public void setPartyaSignTime(Date partyaSignTime) {
        this.partyaSignTime = partyaSignTime;
    }

    public String getPartybSignUrl() {
        return partybSignUrl;
    }

    public void setPartybSignUrl(String partybSignUrl) {
        this.partybSignUrl = partybSignUrl;
    }

    public Date getPartybSignTime() {
        return partybSignTime;
    }

    public void setPartybSignTime(Date partybSignTime) {
        this.partybSignTime = partybSignTime;
    }

    public String getPartycSignUrl() {
        return partycSignUrl;
    }

    public void setPartycSignUrl(String partycSignUrl) {
        this.partycSignUrl = partycSignUrl;
    }

    public Date getPartycSignTime() {
        return partycSignTime;
    }

    public void setPartycSignTime(Date partycSignTime) {
        this.partycSignTime = partycSignTime;
    }

    public String getPartyaUserName() {
        return partyaUserName;
    }

    public void setPartyaUserName(String partyaUserName) {
        this.partyaUserName = partyaUserName;
    }

    public String getPartybUserName() {
        return partybUserName;
    }

    public void setPartybUserName(String partybUserName) {
        this.partybUserName = partybUserName;
    }

    public String getPartycUserName() {
        return partycUserName;
    }

    public void setPartycUserName(String partycUserName) {
        this.partycUserName = partycUserName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getImpSubState() {
        return impSubState;
    }

    public void setImpSubState(String impSubState) {
        this.impSubState = impSubState;
    }

    public String getSubState() {
        return subState;
    }

    public void setSubState(String subState) {
        this.subState = subState;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getPersonalName() {
        return personalName;
    }

    public void setPersonalName(String personalName) {
        this.personalName = personalName;
    }

    public String getPersonalMobile() {
        return personalMobile;
    }

    public void setPersonalMobile(String personalMobile) {
        this.personalMobile = personalMobile;
    }

    public String getPersonalIdentityType() {
        return personalIdentityType;
    }

    public void setPersonalIdentityType(String personalIdentityType) {
        this.personalIdentityType = personalIdentityType;
    }

    public String getPersonalIdentity() {
        return personalIdentity;
    }

    public void setPersonalIdentity(String personalIdentity) {
        this.personalIdentity = personalIdentity;
    }

    public Date getLastNotifyTime() {
        return lastNotifyTime;
    }

    public void setLastNotifyTime(Date lastNotifyTime) {
        this.lastNotifyTime = lastNotifyTime;
    }

    public String getOuterDownloadUrl() {
        return outerDownloadUrl;
    }

    public void setOuterDownloadUrl(String outerDownloadUrl) {
        this.outerDownloadUrl = outerDownloadUrl;
    }

    public String getExtrOrderId() {
        return extrOrderId;
    }

    public void setExtrOrderId(String extrOrderId) {
        this.extrOrderId = extrOrderId;
    }

    public String getExtrSystemId() {
        return extrSystemId;
    }

    public void setExtrSystemId(String extrSystemId) {
        this.extrSystemId = extrSystemId;
    }

    public String getNotifyResultcode() {
        return notifyResultcode;
    }

    public void setNotifyResultcode(String notifyResultcode) {
        this.notifyResultcode = notifyResultcode;
    }

    public String getNotifyResultmessage() {
        return notifyResultmessage;
    }

    public void setNotifyResultmessage(String notifyResultmessage) {
        this.notifyResultmessage = notifyResultmessage;
    }

    public Date getNotifyTime() {
        return notifyTime;
    }

    public void setNotifyTime(Date notifyTime) {
        this.notifyTime = notifyTime;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

}