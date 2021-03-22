package com.jrmf.utils;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component  
@ConfigurationProperties(prefix ="salary") 
@PropertySource("classpath:salaryConfig.properties")
public class SalaryConfigUtil {
	
	private String encode;
	private String yeePayUrl;
	private String aliDataInputFilePath;
	private String bankDataInputFilePath;
	private int testModeAmountLimit;
	private String alipayTempleFormat;
	private String bankTempleFormat1;
	private String bankTempleFormat2;
	private String taskDataInputFilePath;
	private String blackUsersTempFormat;
	private String littleBeeUserDataInputFilePath;

	public String getAlipayTempleFormat() {
		return alipayTempleFormat;
	}

	public void setAlipayTempleFormat(String alipayTempleFormat) {
		this.alipayTempleFormat = alipayTempleFormat;
	}

	public String getBankTempleFormat1() {
		return bankTempleFormat1;
	}

	public void setBankTempleFormat1(String bankTempleFormat1) {
		this.bankTempleFormat1 = bankTempleFormat1;
	}

	public String getBankTempleFormat2() {
		return bankTempleFormat2;
	}

	public void setBankTempleFormat2(String bankTempleFormat2) {
		this.bankTempleFormat2 = bankTempleFormat2;
	}

	public String getAliDataInputFilePath() {
		return aliDataInputFilePath;
	}

	public void setAliDataInputFilePath(String aliDataInputFilePath) {
		this.aliDataInputFilePath = aliDataInputFilePath;
	}

	public String getBankDataInputFilePath() {
		return bankDataInputFilePath;
	}

	public void setBankDataInputFilePath(String bankDataInputFilePath) {
		this.bankDataInputFilePath = bankDataInputFilePath;
	}

	public String getEncode() {
		return encode;
	}

	public void setEncode(String encode) {
		this.encode = encode;
	}

	public String getYeePayUrl() {
		return yeePayUrl;
	}

	public void setYeePayUrl(String yeePayUrl) {
		this.yeePayUrl = yeePayUrl;
	}

	public int getTestModeAmountLimit() {
		return testModeAmountLimit;
	}

	public void setTestModeAmountLimit(int testModeAmountLimit) {
		this.testModeAmountLimit = testModeAmountLimit;
	}

	public String getTaskDataInputFilePath() {
		return taskDataInputFilePath;
	}

	public void setTaskDataInputFilePath(String taskDataInputFilePath) {
		this.taskDataInputFilePath = taskDataInputFilePath;
	}

	public String getBlackUsersTempFormat() {
		return blackUsersTempFormat;
	}

	public void setBlackUsersTempFormat(String blackUsersTempFormat) {
		this.blackUsersTempFormat = blackUsersTempFormat;
	}

	public String getLittleBeeUserDataInputFilePath() {
		return littleBeeUserDataInputFilePath;
	}

	public void setLittleBeeUserDataInputFilePath(String littleBeeUserDataInputFilePath) {
		this.littleBeeUserDataInputFilePath = littleBeeUserDataInputFilePath;
	}
}
