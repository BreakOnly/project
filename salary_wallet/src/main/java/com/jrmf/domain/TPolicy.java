package com.jrmf.domain;

public class TPolicy {
    private Integer id;

    private String contentTitle;

    private String contentDesc;

    private Integer contentType;

    private Integer contentChildType;

    private String contentUrl;

    private String contentOrder;

    private String publishArea;

    private Integer repostsCount;

    private Integer visitsCount;

    private Integer attitudesCount;

    private Integer collectCount;

    private String createTime;

    private String updateTime;

    private String publishTime;

    private Integer auditStatus;

    private Integer publishStatus;

    private String operatorName;

    private String checkName;

    //拓展字段非数据库
    private String startCreateTime;
    private String endCreateTime;
    private String startOrder;
    private String endOrder;
    private String startPublishTime;
    private String endPublishTime;
    private Integer startVisitsCount;
    private Integer endVisitsCount;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getContentTitle() {
        return contentTitle;
    }

    public void setContentTitle(String contentTitle) {
        this.contentTitle = contentTitle == null ? null : contentTitle.trim();
    }

    public String getContentDesc() {
        return contentDesc;
    }

    public void setContentDesc(String contentDesc) {
        this.contentDesc = contentDesc == null ? null : contentDesc.trim();
    }

    public Integer getContentType() {
        return contentType;
    }

    public void setContentType(Integer contentType) {
        this.contentType = contentType;
    }

    public Integer getContentChildType() {
        return contentChildType;
    }

    public void setContentChildType(Integer contentChildType) {
        this.contentChildType = contentChildType;
    }

    public String getContentUrl() {
        return contentUrl;
    }

    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl == null ? null : contentUrl.trim();
    }

    public String getContentOrder() {
        return contentOrder;
    }

    public void setContentOrder(String contentOrder) {
        this.contentOrder = contentOrder;
    }

    public String getPublishArea() {
        return publishArea;
    }

    public void setPublishArea(String publishArea) {
        this.publishArea = publishArea == null ? null : publishArea.trim();
    }

    public Integer getRepostsCount() {
        return repostsCount;
    }

    public void setRepostsCount(Integer repostsCount) {
        this.repostsCount = repostsCount;
    }

    public Integer getVisitsCount() {
        return visitsCount;
    }

    public void setVisitsCount(Integer visitsCount) {
        this.visitsCount = visitsCount;
    }

    public Integer getAttitudesCount() {
        return attitudesCount;
    }

    public void setAttitudesCount(Integer attitudesCount) {
        this.attitudesCount = attitudesCount;
    }

    public Integer getCollectCount() {
        return collectCount;
    }

    public void setCollectCount(Integer collectCount) {
        this.collectCount = collectCount;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime == null ? null : createTime.trim();
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime == null ? null : updateTime.trim();
    }

    public String getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(String publishTime) {
        this.publishTime = publishTime == null ? null : publishTime.trim();
    }

    public Integer getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(Integer auditStatus) {
        this.auditStatus = auditStatus;
    }

    public Integer getPublishStatus() {
        return publishStatus;
    }

    public void setPublishStatus(Integer publishStatus) {
        this.publishStatus = publishStatus;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName == null ? null : operatorName.trim();
    }

    public String getCheckName() {
        return checkName;
    }

    public void setCheckName(String checkName) {
        this.checkName = checkName == null ? null : checkName.trim();
    }

    public String getStartCreateTime() {
        return startCreateTime;
    }

    public void setStartCreateTime(String startCreateTime) {
        this.startCreateTime = startCreateTime;
    }

    public String getEndCreateTime() {
        return endCreateTime;
    }

    public void setEndCreateTime(String endCreateTime) {
        this.endCreateTime = endCreateTime;
    }

    public String getStartOrder() {
        return startOrder;
    }

    public void setStartOrder(String startOrder) {
        this.startOrder = startOrder;
    }

    public String getEndOrder() {
        return endOrder;
    }

    public void setEndOrder(String endOrder) {
        this.endOrder = endOrder;
    }

    public String getStartPublishTime() {
        return startPublishTime;
    }

    public void setStartPublishTime(String startPublishTime) {
        this.startPublishTime = startPublishTime;
    }

    public String getEndPublishTime() {
        return endPublishTime;
    }

    public void setEndPublishTime(String endPublishTime) {
        this.endPublishTime = endPublishTime;
    }

    public Integer getStartVisitsCount() {
        return startVisitsCount;
    }

    public void setStartVisitsCount(Integer startVisitsCount) {
        this.startVisitsCount = startVisitsCount;
    }

    public Integer getEndVisitsCount() {
        return endVisitsCount;
    }

    public void setEndVisitsCount(Integer endVisitsCount) {
        this.endVisitsCount = endVisitsCount;
    }
}