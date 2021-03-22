package com.jrmf.domain;

/**
 * @Title: PageVisitRecord
 * @Description: 页面访问记录
 * @create 2020/2/24 10:00
 */
public class PageVisitRecord {

    private int id;
    /**
     * 页面名称
     */
    private String name;
    /**
     * 页面日期
     */
    private String pageDate;
    /**
     * 页面访问时间
     */
    private String visitTime;
    /**
     * 请求IP
     */
    private String userIp;
    /**
     * 请求IP地址
     */
    private String userIpAddress;
    /**
     * 创建时间
     */
    private String createTime;
    /**
     * 类型1-首页，2-申请，3-分享
     */
    private String type;
    /**
     * 页面访问数量id
     */
    private int amountId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPageDate() {
        return pageDate;
    }

    public void setPageDate(String pageDate) {
        this.pageDate = pageDate;
    }

    public String getVisitTime() {
        return visitTime;
    }

    public void setVisitTime(String visitTime) {
        this.visitTime = visitTime;
    }

    public String getUserIp() {
        return userIp;
    }

    public void setUserIp(String userIp) {
        this.userIp = userIp;
    }

    public String getUserIpAddress() {
        return userIpAddress;
    }

    public void setUserIpAddress(String userIpAddress) {
        this.userIpAddress = userIpAddress;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getAmountId() {
        return amountId;
    }

    public void setAmountId(int amountId) {
        this.amountId = amountId;
    }

}
