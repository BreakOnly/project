package com.jrmf.taxsettlement.api.service.contract;

import com.jrmf.taxsettlement.api.gateway.NotNull;
import com.jrmf.taxsettlement.api.service.ActionParams;

public class SignAgreementServiceParams extends ActionParams {

    /**
     * 姓名
     */
    @NotNull
    private String name;
    /**
     * 证件号
     */
    @NotNull
    private String certificateNo;
    /**
     * 证件类型  参考 com.jrmf.controller.constant.CertType
     */
    @NotNull
    private String certificateType;

    /**
     * 服务公司id
     */
    private String transferCorpId;
    /**
     * 证件照正面图像
     */
    private String certificateImage;
    /**
     * 证件照反面图像
     */
    private String certificateImageBackground;
    /**
     * 手机号
     */
    @NotNull
    private String mobileNo;
    /**
     * 签约类型 ALL 全部签约。  SPECIFIED  特定的。
     */
    @NotNull
    private String signAgreementType;
    /**
     * 回调地址
     */
    @NotNull
    private String notifyUrl;
    /**
     * 流水号
     */
    @NotNull
    private String serialNo;

    private String cardNo;

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCertificateNo() {
        return certificateNo;
    }

    public void setCertificateNo(String certificateNo) {
        this.certificateNo = certificateNo;
    }

    public String getCertificateType() {
        return certificateType;
    }

    public void setCertificateType(String certificateType) {
        this.certificateType = certificateType;
    }

    public String getTransferCorpId() {
        return transferCorpId;
    }

    public void setTransferCorpId(String transferCorpId) {
        this.transferCorpId = transferCorpId;
    }

    public String getCertificateImage() {
        return certificateImage;
    }

    public void setCertificateImage(String certificateImage) {
        this.certificateImage = certificateImage;
    }

    public String getCertificateImageBackground() {
        return certificateImageBackground;
    }

    public void setCertificateImageBackground(String certificateImageBackground) {
        this.certificateImageBackground = certificateImageBackground;
    }

    @Override
    public String toString() {
        return "SignAgreementServiceParams{" + "name='" + name + '\'' + ", certificateNo='" + certificateNo + '\'' + ", certificateType='" + certificateType + '\'' + ", transferCorpId='" + transferCorpId + '\'' + ", certificateImage='" + certificateImage + '\'' + ", certificateImageBackground='" + certificateImageBackground + '\'' + ", mobileNo='" + mobileNo + '\'' + ", signAgreementType='" + signAgreementType + '\'' + ", notifyUrl='" + notifyUrl + '\'' + ", serialNo='" + serialNo + '\'' + '}';
    }

    public String getSignAgreementType() {
        return signAgreementType;
    }

    public void setSignAgreementType(String signAgreementType) {
        this.signAgreementType = signAgreementType;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }
}
