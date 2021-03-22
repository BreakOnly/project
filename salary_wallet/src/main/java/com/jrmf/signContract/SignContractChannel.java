package com.jrmf.signContract;

import java.io.File;
import java.util.Map;

public interface SignContractChannel {
	
	//上传证件信息
	public Map<String, String> uploadPicInfo(String reqInfo,File backFile, File frontFile);
	
	//签约
	public Map<String, String> signContract(String reqInfo);
	
	//签约查询
	public Map<String, String> signContractQuery(String reqInfo);

}
