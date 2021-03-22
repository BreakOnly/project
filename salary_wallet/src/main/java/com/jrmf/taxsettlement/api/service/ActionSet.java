package com.jrmf.taxsettlement.api.service;

import java.util.Iterator;
import java.util.Map;

public interface ActionSet<AP extends ActionParams, AA extends ActionAttachment> {

	Map<String, String> listActionNames();
	
	Iterator<Action<AP, AA>> listActions();
	
	<SAP extends AP, SAA extends AA> Action<SAP, SAA> routeAction(String actionType, SAP actionParams);

}
