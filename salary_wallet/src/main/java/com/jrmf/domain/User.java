package com.jrmf.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;

/**
* @author 种路路
* @version 创建时间：2017年8月17日 下午4:03:41
* 类说明   用户
*/
public class User implements Serializable {

	/**
	* @Fields serialVersionUID : TODO()
	*/

	private static final long serialVersionUID = 6771255163127995200L;
	private int id;//主键userId
	private String userNo;//渠道用户id
	private String userName;//姓名
	private String certId;//用户身份证号码
	private String mobilePhone;//用户手机号
	private String mobileNo;//用户手机号
	private String account;//支付宝账号
	private String createTime;//注册时间
	private String merchantId;//商户id
	private int  companyType;//爱员工商户类型  1 合伙人企业电子账户，其他  2 爱员工电子账户
	private String transPassword;//交易密码
	private String channelBankNo;//商户业务对应的银行
	private String companyUserNo;//用户
	private String companyName;//用户所在企业名称
	private String userCertFrontPicture;//用户身份证照片（正面）
	private String userCertBackPicture;//用户身份证照片（反面）
	private int userType;//用户类型，，  0 错误数据， -1  待激活  11  补全信息  ,-2待激活商户  1 成功  12 错误信息
	private String remark;//备注
	private int signType;//针对于薪税钱包，上上签签约状态；
	private String batcheId;//导入批次id
    /**
     * 1 身份证  2 港澳通行证 3 护照  4 军官证 5 台胞证
     */
	private String documentType;

	/**
	 * 新增字段（薪税wallect）
	 */
	private String wechartId;//用户微信登陆标识
	/**
	 * 二要素真实性校验
     * 0  创建或者修改  未校验
     * 1  校验成功
     * 2  校验失败
	 */
	private int checkTruth;

	/**
	 * 最后更新时间
	 */
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lastModifyTime;

	/**
	 * 用户状态：1-正常，0-无效
	 */
	private String userStatus;

	/**
	 * 认证等级
	 */
	private String checkLevel;

	/**
	 * 是否通过证照认证
	 */
	private Integer checkByPhoto;

    public int getCheckTruth() {
        return checkTruth;
    }

    public void setCheckTruth(int checkTruth) {
        this.checkTruth = checkTruth;
    }

	@Override
	public String toString() {
		return "User{" +
				"id=" + id +
				", userNo='" + userNo + '\'' +
				", userName='" + userName + '\'' +
				", certId='" + certId + '\'' +
				", mobilePhone='" + mobilePhone + '\'' +
				", account='" + account + '\'' +
				", createTime='" + createTime + '\'' +
				", merchantId='" + merchantId + '\'' +
				", companyType=" + companyType +
				", transPassword='" + transPassword + '\'' +
				", channelBankNo='" + channelBankNo + '\'' +
				", companyUserNo='" + companyUserNo + '\'' +
				", companyName='" + companyName + '\'' +
				", userCertFrontPicture='" + userCertFrontPicture + '\'' +
				", userCertBackPicture='" + userCertBackPicture + '\'' +
				", userType=" + userType +
				", remark='" + remark + '\'' +
				", signType=" + signType +
				", batcheId='" + batcheId + '\'' +
				", documentType='" + documentType + '\'' +
				", wechartId='" + wechartId + '\'' +
				", checkTruth=" + checkTruth +
				", lastModifyTime=" + lastModifyTime +
				", userStatus='" + userStatus + '\'' +
				", checkLevel='" + checkLevel + '\'' +
				", checkByPhoto=" + checkByPhoto +
				", customname='" + customname + '\'' +
				", balance='" + balance + '\'' +
				", bankNo='" + bankNo + '\'' +
				", appId='" + appId + '\'' +
				", templateId='" + templateId + '\'' +
				", status='" + status + '\'' +
				'}';
	}

	public String getAccount() {
        return account;
    }
    public void setAccount(String account) {
        this.account = account;
    }
    public int getSignType() {
		return signType;
	}
	public void setSignType(int signType) {
		this.signType = signType;
	}
	/**
	 * 冗余字段
	 * @return
	 */
	private String customname;//渠道名称
	private String balance;//账户余额
	private String bankNo; //银行卡号
	private String appId;
	private String templateId;
	private String status;//薪税钱包字段-1 未开户 0 未签约 1 签约

	public User() {
	}

	public User(String userNo, String userName, String certId, String mobilePhone,
			String documentType) {
		this.userNo = userNo;
		this.userName = userName;
		this.certId = certId;
		this.mobilePhone = mobilePhone;
		this.documentType = documentType;
	}

	public String getBatcheId() {
		return batcheId;
	}
	public void setBatcheId(String batcheId) {
		this.batcheId = batcheId;
	}
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public String getTemplateId() {
		return templateId;
	}
	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUserNo() {
		return userNo;
	}
	public void setUserNo(String userNo) {
		this.userNo = userNo;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getCertId() {
		return certId;
	}
	public void setCertId(String certId) {
		this.certId = certId;
	}
	public String getMobilePhone() {
		return mobilePhone;
	}
	public void setMobilePhone(String mobilePhone) {
		this.mobilePhone = mobilePhone;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public String getMerchantId() {
		return merchantId;
	}
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}
	public String getTransPassword() {
		return transPassword;
	}
	public void setTransPassword(String transPassword) {
		this.transPassword = transPassword;
	}
	public String getChannelBankNo() {
		return channelBankNo;
	}
	public void setChannelBankNo(String channelBankNo) {
		this.channelBankNo = channelBankNo;
	}

	public int getUserType() {
		return userType;
	}
	public void setUserType(int userType) {
		this.userType = userType;
	}
	public String getCompanyUserNo() {
		return companyUserNo;
	}
	public void setCompanyUserNo(String companyUserNo) {
		this.companyUserNo = companyUserNo;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public String getCustomname() {
		return customname;
	}
	public void setCustomname(String customname) {
		this.customname = customname;
	}
	public String getBalance() {
		return balance;
	}
	public void setBalance(String balance) {
		this.balance = balance;
	}
	public String getUserCertFrontPicture() {
		return userCertFrontPicture;
	}
	public void setUserCertFrontPicture(String userCertFrontPicture) {
		this.userCertFrontPicture = userCertFrontPicture;
	}
	public String getUserCertBackPicture() {
		return userCertBackPicture;
	}
	public void setUserCertBackPicture(String userCertBackPicture) {
		this.userCertBackPicture = userCertBackPicture;
	}
	public int getCompanyType() {
		return companyType;
	}
	public void setCompanyType(int companyType) {
		this.companyType = companyType;
	}
	public String getBankNo() {
		return bankNo;
	}
	public void setBankNo(String bankNo) {
		this.bankNo = bankNo;
	}

    public String getWechartId() {
		return wechartId;
	}
	public void setWechartId(String wechartId) {
		this.wechartId = wechartId;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

	public Date getLastModifyTime() {
		return lastModifyTime;
	}

	public void setLastModifyTime(Date lastModifyTime) {
		this.lastModifyTime = lastModifyTime;
	}

	public String getUserStatus() {
		return userStatus;
	}

	public void setUserStatus(String userStatus) {
		this.userStatus = userStatus;
	}

	public String getCheckLevel() {
		return checkLevel;
	}

	public void setCheckLevel(String checkLevel) {
		this.checkLevel = checkLevel;
	}

	public Integer getCheckByPhoto() {
		return checkByPhoto;
	}

	public void setCheckByPhoto(Integer checkByPhoto) {
		this.checkByPhoto = checkByPhoto;
	}

	public String getMobileNo() {
		return mobileNo;
	}

	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}
}
