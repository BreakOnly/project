package com.jrmf.taxsettlement.api.service;

public interface Action<AP extends ActionParams, AA extends ActionAttachment> {

	String getActionType();
	
	ActionResult<AA> execute(AP actionParams);
}
