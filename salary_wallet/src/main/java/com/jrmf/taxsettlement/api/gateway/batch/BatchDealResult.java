package com.jrmf.taxsettlement.api.gateway.batch;

import java.util.ArrayList;
import java.util.List;

import com.jrmf.taxsettlement.api.service.ActionAttachment;
import com.jrmf.taxsettlement.api.service.ActionResult;

public class BatchDealResult {

	private int totalCount;

	private int acceptCount;

	private int unacceptCount;

	private List<String> unacceptList = new ArrayList<String>();

	public void addSingleResult(ActionResult<ActionAttachment> result) {
		totalCount++;
		if (result.isOk()) {
			acceptCount++;
		} else {
			unacceptCount++;
			unacceptList.add(new StringBuilder(result.getRetCode()).append(":").append(result.getRetMsg()).toString());
		}
	}

	public int getTotalCount() {
		return totalCount;
	}

	public int getAcceptCount() {
		return acceptCount;
	}

	public int getUnacceptCount() {
		return unacceptCount;
	}

	public List<String> getUnacceptList() {
		return unacceptList;
	}
}
