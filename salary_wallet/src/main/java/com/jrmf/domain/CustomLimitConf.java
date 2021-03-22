package com.jrmf.domain;

public class CustomLimitConf {
    private Integer id;
    // 商户标识
    private String customkey;
    // 服务公司标识
    private String companyId;
    // 单笔交易限额
    private String singleOrderLimit;
    // 单日交易限额
    private String singleDayLimit;
    // 单月交易限额
    private String singleMonthLimit;
    // 季度交易限额
    private String singleQuarterLimit;
    // 创建时间
    private String createTime;
    // 最后一次更新时间
    private String updateTime;
    /**
     * 不自动补差价  Y  不自动补偿   N 自动补偿
      */
    private String unAutoCompensatable;
    /**
     * 服务公司名称
      */
    private String companyName;
    // 预留1
    private String reserved1;
    // 预留2
    private String reserved2;
    
    private Integer realCompanyOperate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCustomkey() {
        return customkey;
    }

    public void setCustomkey(String customkey) {
        this.customkey = customkey;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getSingleOrderLimit() {
        return singleOrderLimit;
    }

    public void setSingleOrderLimit(String singleOrderLimit) {
        this.singleOrderLimit = singleOrderLimit;
    }

    public String getSingleDayLimit() {
        return singleDayLimit;
    }

    public void setSingleDayLimit(String singleDayLimit) {
        this.singleDayLimit = singleDayLimit;
    }

    public String getSingleMonthLimit() {
        return singleMonthLimit;
    }

    public void setSingleMonthLimit(String singleMonthLimit) {
        this.singleMonthLimit = singleMonthLimit;
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

    public String getReserved1() {
        return reserved1;
    }

    public void setReserved1(String reserved1) {
        this.reserved1 = reserved1;
    }

    public String getReserved2() {
        return reserved2;
    }

    public void setReserved2(String reserved2) {
        this.reserved2 = reserved2;
    }

    public String getUnAutoCompensatable() {
        return unAutoCompensatable;
    }

    public void setUnAutoCompensatable(String unAutoCompensatable) {
        this.unAutoCompensatable = unAutoCompensatable;
    }

    @Override
    public String toString() {
        return "CustomLimitConf{" + "id=" + id + ", customkey='" + customkey + '\'' + ", companyId='" + companyId + '\'' + ", singleOrderLimit='" + singleOrderLimit + '\'' + ", singleDayLimit='" + singleDayLimit + '\'' + ", singleMonthLimit='" + singleMonthLimit + '\'' + ", singleQuarterLimit='" + singleQuarterLimit + '\'' + ", createTime='" + createTime + '\'' + ", updateTime='" + updateTime + '\'' + ", unAutoCompensatable='" + unAutoCompensatable + '\'' + ", companyName='" + companyName + '\'' + ", reserved1='" + reserved1 + '\'' + ", reserved2='" + reserved2 + '\'' + '}';
    }

    public String getSingleQuarterLimit() {
        return singleQuarterLimit;
    }

    public void setSingleQuarterLimit(String singleQuarterLimit) {
        this.singleQuarterLimit = singleQuarterLimit;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

	public Integer getRealCompanyOperate() {
		return realCompanyOperate;
	}

	public void setRealCompanyOperate(Integer realCompanyOperate) {
		this.realCompanyOperate = realCompanyOperate;
	}
    
    
}
