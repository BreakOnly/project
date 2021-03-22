package com.jrmf.domain;

public class CustomLdConfig {
    private Integer id;

    private String customkey;

    private String companyidOne;

    private String companyidTwo;

    private String companyidPriority;

    private String priorityMonthLimit;

    private String priorityQuarterLimit;

    private String pathno;

    private String lowestAmount;

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
        this.customkey = customkey == null ? null : customkey.trim();
    }

    public String getCompanyidOne() {
        return companyidOne;
    }

    public void setCompanyidOne(String companyidOne) {
        this.companyidOne = companyidOne == null ? null : companyidOne.trim();
    }

    public String getCompanyidTwo() {
        return companyidTwo;
    }

    public void setCompanyidTwo(String companyidTwo) {
        this.companyidTwo = companyidTwo == null ? null : companyidTwo.trim();
    }

    public String getCompanyidPriority() {
        return companyidPriority;
    }

    public void setCompanyidPriority(String companyidPriority) {
        this.companyidPriority = companyidPriority == null ? null : companyidPriority.trim();
    }

    public String getPriorityMonthLimit() {
        return priorityMonthLimit;
    }

    public void setPriorityMonthLimit(String priorityMonthLimit) {
        this.priorityMonthLimit = priorityMonthLimit == null ? null : priorityMonthLimit.trim();
    }

    public String getPriorityQuarterLimit() {
        return priorityQuarterLimit;
    }

    public void setPriorityQuarterLimit(String priorityQuarterLimit) {
        this.priorityQuarterLimit = priorityQuarterLimit == null ? null : priorityQuarterLimit.trim();
    }

    public String getPathno() {
        return pathno;
    }

    public void setPathno(String pathno) {
        this.pathno = pathno == null ? null : pathno.trim();
    }

    public String getLowestAmount() {
        return lowestAmount;
    }

    public void setLowestAmount(String lowestAmount) {
        this.lowestAmount = lowestAmount == null ? null : lowestAmount.trim();
    }
}