package com.jrmf.domain;

/**
 * @Title: UserRegisterRecord
 * @Description: 用户登记咨询记录
 * @create 2020/2/21 14:37
 */
public class UserRegisterRecord {

    private Integer id;
    /**
     * 信息渠道
     */
    private String channel;

    /**
     * 用户ip
     */
    private String userIPAddress;

    /**
     * 用户区域位置
     */
    private String userAddress;

    /**
     * 手机号
     */
    private String phoneNo;

    /**
     * 姓名
     */
    private String userName;

    /**
     * 公司名
     */
    private String companyName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 修改时间
     */
    private String updateTime;

    /**
     * 留言
     */
    private String leaveMessage;

    /**
     * 职位
     */
    private String position;

    /**
     * 登记来源：1-小黄蜂官网 2-财税工具栏目
     */
    private String registerType;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getUserIPAddress() {
        return userIPAddress;
    }

    public void setUserIPAddress(String userIPAddress) {
        this.userIPAddress = userIPAddress;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getLeaveMessage() {
        return leaveMessage;
    }

    public void setLeaveMessage(String leaveMessage) {
        this.leaveMessage = leaveMessage;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getRegisterType() {
        return registerType;
    }

    public void setRegisterType(String registerType) {
        this.registerType = registerType;
    }

    @Override
    public String toString() {
        return "UserRegisterRecord{" +
                "id=" + id +
                ", channel='" + channel + '\'' +
                ", userIPAddress='" + userIPAddress + '\'' +
                ", userAddress='" + userAddress + '\'' +
                ", phoneNo='" + phoneNo + '\'' +
                ", userName='" + userName + '\'' +
                ", companyName='" + companyName + '\'' +
                ", email='" + email + '\'' +
                ", createTime='" + createTime + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", leaveMessage='" + leaveMessage + '\'' +
                ", position='" + position + '\'' +
                ", registerType='" + registerType + '\'' +
                '}';
    }
}
