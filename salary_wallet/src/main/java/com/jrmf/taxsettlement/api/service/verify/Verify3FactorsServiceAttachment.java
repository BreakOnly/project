package com.jrmf.taxsettlement.api.service.verify;

import com.jrmf.taxsettlement.api.service.ActionAttachment;

public class Verify3FactorsServiceAttachment extends ActionAttachment {

	private boolean passing;

	public boolean isPassing() {
		return passing;
	}

	public void setPassing(boolean passing) {
		this.passing = passing;
	}
}
