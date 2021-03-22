package com.jrmf.taxsettlement.api.service;

public interface ActionExecutor<AP extends ActionParams, AA extends ActionAttachment> {
	
	<SAP extends AP, SAA extends AA> ActionResult<SAA> executeAction(String actionType, SAP actionParams);

}