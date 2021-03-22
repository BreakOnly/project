package com.jrmf.taxsettlement.api.gateway.batch.form;

import java.util.ArrayList;
import java.util.List;

import com.jrmf.taxsettlement.api.service.ActionParams;

public class BatchFormDistillResult {

	private List<ActionParams> distilledActionParamsList = new ArrayList<ActionParams>();
	
	private List<String> undistillDataBriefMsgList = new ArrayList<String>();
	
	public void addDistill(ActionParams actionParams) {
		distilledActionParamsList.add(actionParams);
	}

	public void addUndistill(String briefMsg) {
		undistillDataBriefMsgList.add(briefMsg);
	}

	public List<ActionParams> getDistilledActionParamsList() {
		return distilledActionParamsList;
	}

	public List<String> getUndistillDataBriefMsgList() {
		return undistillDataBriefMsgList;
	}
}
