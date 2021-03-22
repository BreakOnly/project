package com.jrmf.taxsettlement.api.gateway.batch;

import java.util.ArrayList;
import java.util.List;

import com.jrmf.taxsettlement.api.service.ActionAttachment;
import com.jrmf.taxsettlement.api.service.ActionResult;
import com.jrmf.taxsettlement.api.service.transfer.AbstractTransferServiceParams;

public class TransformAndCheckResult {

	private List<AbstractTransferServiceParams> transferServiceParamsList;

	private List<ActionResult<ActionAttachment>> unacceptActionResults = new ArrayList<ActionResult<ActionAttachment>>();

	public List<AbstractTransferServiceParams> getTransferServiceParamsList() {
		return transferServiceParamsList;
	}

	public List<ActionResult<ActionAttachment>> getUnacceptActionResults() {
		return unacceptActionResults;
	}

	public void setTransferServiceParamsList(List<AbstractTransferServiceParams> transferServiceParamsList) {
		this.transferServiceParamsList = transferServiceParamsList;
	}

	public void addUnacceptActionResults(ActionResult<ActionAttachment> unacceptActionResult) {
		this.unacceptActionResults.add(unacceptActionResult);
	}
}
