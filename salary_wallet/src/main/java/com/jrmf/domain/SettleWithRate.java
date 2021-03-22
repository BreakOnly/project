package com.jrmf.domain;

/**
 * @Title: settleWithRate
 * @Description: 清结算变更费率基表
 * @create 2020/2/14 16:41
 */
public class SettleWithRate {

    private Integer id;

    private String customkey;

    private Integer customType;

    private String modifyRate;

    private String modifyEffectStartTime;

    private String modifyEffectEndTime;

    private Integer companyId;

    private Integer netfileId;

    private String createTime;

    private String updateTime;

    private String modifyAddUser;

    private String customName;

    private String username;

    private String rate;

    private String companyName;

    private String gearPositionShorthand;

    public String getGearPositionShorthand() {
        return gearPositionShorthand;
    }

    public void setGearPositionShorthand(String gearPositionShorthand) {
        this.gearPositionShorthand = gearPositionShorthand;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

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

    public Integer getCustomType() {
        return customType;
    }

    public void setCustomType(Integer customType) {
        this.customType = customType;
    }

    public String getModifyRate() {
        return modifyRate;
    }

    public void setModifyRate(String modifyRate) {
        this.modifyRate = modifyRate;
    }

    public String getModifyEffectStartTime() {
        return modifyEffectStartTime;
    }

    public void setModifyEffectStartTime(String modifyEffectStartTime) {
        this.modifyEffectStartTime = modifyEffectStartTime;
    }

    public String getModifyEffectEndTime() {
        return modifyEffectEndTime;
    }

    public void setModifyEffectEndTime(String modifyEffectEndTime) {
        this.modifyEffectEndTime = modifyEffectEndTime;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    public Integer getNetfileId() {
        return netfileId;
    }

    public void setNetfileId(Integer netfileId) {
        this.netfileId = netfileId;
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

    public String getModifyAddUser() {
        return modifyAddUser;
    }

    public void setModifyAddUser(String modifyAddUser) {
        this.modifyAddUser = modifyAddUser;
    }
}
