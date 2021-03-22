package com.jrmf.payment.ymyf.entity;


import java.io.Serializable;
import java.util.List;

import com.jrmf.payment.ymyf.util.Constant;

/**
 * 商户批量代付接口实体类
 *
 * @author Admin
 */
public class PaymentTransModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总笔数
     */
    private String totalCount;

    /**
     * 总金额
     */
    private String totalAmt;

    /**
     * 商户编号
     */
    private String merId;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 代付明细数据集合
     */
    private List<PayItems> payItems;

    /**
     * 商户批次号
     */
    private String merBatchId;

    private String projectId;

    public static class PayItems {

        /**
         * 商户订单号
         */
        private String merOrderId;

        /**
         * 金额
         */
        private Long amt;

        /**
         * 收款人名称
         */
        private String payeeName;

        /**
         * 收款人账号
         */
        private String payeeAcc;

        /**
         * 身份证号
         */
        private String idCard;

        /**
         * 手机号
         */
        private String mobile;

        /**
         * 联行号
         */
        private String branchNo;

        /**
         * 支行名称
         */
        private String branchName;

        /**
         * 省名称
         */
        private String province;

        /**
         * 市名称
         */
        private String city;

        /**
         * 备注
         */
        private String memo;

        /**
         * 代付类型 0：实时 1：工作日
         */
        private Integer payType;

        /**
         * 代付方式 0：银行卡，1：支付宝，2：微信
         */
        private Integer paymentType;

        /**
         * 结算银行卡账号类型，0：对公，1：对私
         */
        private Integer accType;

        /**
         * 税优通道ID
         */
        private Long levyId;

        /**
         * 返回码
         */
        private String resCode = Constant.SUCCESS;

        /**
         * 返回信息
         */
        private String resMsg = Constant.SUCCESS_INFO;

        private String notifyUrl;

        public String getMerOrderId() {
            return merOrderId;
        }

        public void setMerOrderId(String merOrderId) {
            this.merOrderId = merOrderId;
        }

        public Long getAmt() {
            return amt;
        }

        public void setAmt(Long amt) {
            this.amt = amt;
        }

        public String getPayeeName() {
            return payeeName;
        }

        public void setPayeeName(String payeeName) {
            this.payeeName = payeeName;
        }

        public String getPayeeAcc() {
            return payeeAcc;
        }

        public void setPayeeAcc(String payeeAcc) {
            this.payeeAcc = payeeAcc;
        }

        public String getIdCard() {
            return idCard;
        }

        public void setIdCard(String idCard) {
            this.idCard = idCard;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getBranchNo() {
            return branchNo;
        }

        public void setBranchNo(String branchNo) {
            this.branchNo = branchNo;
        }

        public String getBranchName() {
            return branchName;
        }

        public void setBranchName(String branchName) {
            this.branchName = branchName;
        }

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getMemo() {
            return memo;
        }

        public void setMemo(String memo) {
            this.memo = memo;
        }

        public Integer getPayType() {
            return payType;
        }

        public void setPayType(Integer payType) {
            this.payType = payType;
        }

        public Integer getPaymentType() {
            return paymentType;
        }

        public void setPaymentType(Integer paymentType) {
            this.paymentType = paymentType;
        }

        public Integer getAccType() {
            return accType;
        }

        public void setAccType(Integer accType) {
            this.accType = accType;
        }

        public Long getLevyId() {
            return levyId;
        }

        public void setLevyId(Long levyId) {
            this.levyId = levyId;
        }

        public String getResCode() {
            return resCode;
        }

        public void setResCode(String resCode) {
            this.resCode = resCode;
        }

        public String getResMsg() {
            return resMsg;
        }

        public void setResMsg(String resMsg) {
            this.resMsg = resMsg;
        }

        public String getNotifyUrl() {
            return notifyUrl;
        }

        public void setNotifyUrl(String notifyUrl) {
            this.notifyUrl = notifyUrl;
        }
    }

    public String getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(String totalCount) {
        this.totalCount = totalCount;
    }

    public String getTotalAmt() {
        return totalAmt;
    }

    public void setTotalAmt(String totalAmt) {
        this.totalAmt = totalAmt;
    }

    public String getMerId() {
        return merId;
    }

    public void setMerId(String merId) {
        this.merId = merId;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public List<PayItems> getPayItems() {
        return payItems;
    }

    public void setPayItems(List<PayItems> payItems) {
        this.payItems = payItems;
    }

    public String getMerBatchId() {
        return merBatchId;
    }

    public void setMerBatchId(String merBatchId) {
        this.merBatchId = merBatchId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
}
