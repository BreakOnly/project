package com.jrmf.taxsettlement.api.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CommonActionSet<AP extends ActionParams, AA extends ActionAttachment> implements ActionSet<AP, AA> {

	private static final String BATCH_ACTION_KEY_PREFIX = "BATCH_";

	private static final String BATCH_ACTION_NAME_PREFIX = "批量";

	private Map<String, String> actionNameTable = new HashMap<String, String>();

	private Map<String, Action<AP, AA>> supportActions = new HashMap<String, Action<AP, AA>>();

	public CommonActionSet(List<Action<AP, AA>> supportActions) {
		super();

		for (Action<AP, AA> action : supportActions) {
			String actionType = action.getActionType();
			this.supportActions.put(actionType, action);

			ActionConfig config = action.getClass().getAnnotation(ActionConfig.class);
			String actionName = config.name();
			actionNameTable.put(actionType, actionName);

			if (config.supportBatch()) {
				actionNameTable.put(new StringBuilder(BATCH_ACTION_KEY_PREFIX).append(actionType).toString(),
						new StringBuilder(BATCH_ACTION_NAME_PREFIX).append(actionName).toString());
			}
		}

	}

	@Override
	public Iterator<Action<AP, AA>> listActions() {
		return supportActions.values().iterator();
	}

	@Override
	public <SAP extends AP, SAA extends AA> Action<SAP, SAA> routeAction(String actionType, SAP actionParams) {
		return (Action<SAP, SAA>) supportActions.get(actionType);
	}

	@Override
	public Map<String, String> listActionNames() {
		return new HashMap<String, String>(actionNameTable);
	}

}
