package com.jrmf.domain;

import java.util.Date;

public class AgreementTemplateMapping {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column agreement_template_mapping.id
     *
     * @mbg.generated Wed Oct 21 17:48:13 CST 2020
     */
    private Integer id;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column agreement_template_mapping.agreement_template_consumer
     *
     * @mbg.generated Wed Oct 21 17:48:13 CST 2020
     */
    private String agreementTemplateConsumer;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column agreement_template_mapping.agreement_template_provider
     *
     * @mbg.generated Wed Oct 21 17:48:13 CST 2020
     */
    private String agreementTemplateProvider;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column agreement_template_mapping.create_time
     *
     * @mbg.generated Wed Oct 21 17:48:13 CST 2020
     */
    private Date createTime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column agreement_template_mapping.last_update_time
     *
     * @mbg.generated Wed Oct 21 17:48:13 CST 2020
     */
    private Date lastUpdateTime;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column agreement_template_mapping.id
     *
     * @return the value of agreement_template_mapping.id
     *
     * @mbg.generated Wed Oct 21 17:48:13 CST 2020
     */
    public Integer getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column agreement_template_mapping.id
     *
     * @param id the value for agreement_template_mapping.id
     *
     * @mbg.generated Wed Oct 21 17:48:13 CST 2020
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column agreement_template_mapping.agreement_template_consumer
     *
     * @return the value of agreement_template_mapping.agreement_template_consumer
     *
     * @mbg.generated Wed Oct 21 17:48:13 CST 2020
     */
    public String getAgreementTemplateConsumer() {
        return agreementTemplateConsumer;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column agreement_template_mapping.agreement_template_consumer
     *
     * @param agreementTemplateConsumer the value for agreement_template_mapping.agreement_template_consumer
     *
     * @mbg.generated Wed Oct 21 17:48:13 CST 2020
     */
    public void setAgreementTemplateConsumer(String agreementTemplateConsumer) {
        this.agreementTemplateConsumer = agreementTemplateConsumer;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column agreement_template_mapping.agreement_template_provider
     *
     * @return the value of agreement_template_mapping.agreement_template_provider
     *
     * @mbg.generated Wed Oct 21 17:48:13 CST 2020
     */
    public String getAgreementTemplateProvider() {
        return agreementTemplateProvider;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column agreement_template_mapping.agreement_template_provider
     *
     * @param agreementTemplateProvider the value for agreement_template_mapping.agreement_template_provider
     *
     * @mbg.generated Wed Oct 21 17:48:13 CST 2020
     */
    public void setAgreementTemplateProvider(String agreementTemplateProvider) {
        this.agreementTemplateProvider = agreementTemplateProvider;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column agreement_template_mapping.create_time
     *
     * @return the value of agreement_template_mapping.create_time
     *
     * @mbg.generated Wed Oct 21 17:48:13 CST 2020
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column agreement_template_mapping.create_time
     *
     * @param createTime the value for agreement_template_mapping.create_time
     *
     * @mbg.generated Wed Oct 21 17:48:13 CST 2020
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column agreement_template_mapping.last_update_time
     *
     * @return the value of agreement_template_mapping.last_update_time
     *
     * @mbg.generated Wed Oct 21 17:48:13 CST 2020
     */
    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column agreement_template_mapping.last_update_time
     *
     * @param lastUpdateTime the value for agreement_template_mapping.last_update_time
     *
     * @mbg.generated Wed Oct 21 17:48:13 CST 2020
     */
    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}
