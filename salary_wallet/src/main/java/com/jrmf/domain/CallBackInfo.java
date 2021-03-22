package com.jrmf.domain;

/**
 * @author 种路路
 * @create 2019-05-21 10:05
 * @desc 回调信息
 **/
public class CallBackInfo {
    private int id;
    /**
     * 创建时间
     */
    private String createTime;
    /**
     * 最后修改时间
     */
    private String lastUpdateTime;
    /**
     * 通知地址
     */
    private String notifyUrl;
    /**
     * 通知次数
     */
    private int notifyCount;
    /**
     * 通知内容
     */
    private String notifyContent;
    /**
     * 网络状态
     */
    private String httpStatus;
    /**
     * 通知结果
     */
    private String httpResult;
    /**
     * 序列号
     */
    private String serialNo;
    /**
     * 三方流水号
     */
    private String thirdNo;
    /**
     * status  状态  1,成功   0，失败
     */
    private int status;

    @Override
    public String toString() {
        return "CallBackInfo{" + "id=" + id + ", createTime='" + createTime + '\'' + ", lastUpdateTime='" + lastUpdateTime + '\'' + ", notifyUrl='" + notifyUrl + '\'' + ", notifyCount=" + notifyCount + ", notifyContent='" + notifyContent + '\'' + ", httpStatus='" + httpStatus + '\'' + ", httpResult='" + httpResult + '\'' + ", serialNo='" + serialNo + '\'' + ", thirdNo='" + thirdNo + '\'' + ", status=" + status + ", customkey='" + customkey + '\'' + ", prepareB='" + prepareB + '\'' + ", prepareA='" + prepareA + '\'' + '}';
    }

    public String getCustomkey() {
        return customkey;
    }

    public void setCustomkey(String customkey) {
        this.customkey = customkey;
    }

    /**
     * customkey  商户id

     */
    private String customkey;
    /**
     * 预留
     */
    private String prepareB;
    /**
     * 预留
     */
    private String prepareA;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getThirdNo() {
        return thirdNo;
    }

    public void setThirdNo(String thirdNo) {
        this.thirdNo = thirdNo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public int getNotifyCount() {
        return notifyCount;
    }

    public void setNotifyCount(int notifyCount) {
        this.notifyCount = notifyCount;
    }

    public String getNotifyContent() {
        return notifyContent;
    }

    public void setNotifyContent(String notifyContent) {
        this.notifyContent = notifyContent;
    }

    public String getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(String httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getHttpResult() {
        return httpResult;
    }

    public void setHttpResult(String httpResult) {
        this.httpResult = httpResult;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getPrepareA() {
        return prepareA;
    }

    public void setPrepareA(String prepareA) {
        this.prepareA = prepareA;
    }

    public String getPrepareB() {
        return prepareB;
    }

    public void setPrepareB(String prepareB) {
        this.prepareB = prepareB;
    }
}
