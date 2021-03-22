package com.jrmf.taxsettlement.api.gateway.batch.form;

import java.util.Map;

public class SerialFieldDataFormTemplate extends AbstractDataFormTemplate<SerialFieldDataUnit> {

	private Map<String, Integer> fieldNameSerialIndexTable;
	
	protected SerialFieldDataFormTemplate(Map<String, Object> presetFieldValueTable, Map<String, Integer> fieldNameSerialIndexTable) {
		super(presetFieldValueTable);
		this.fieldNameSerialIndexTable = fieldNameSerialIndexTable;
	}	
	
	@Override
	protected Object getFieldValue(String name, SerialFieldDataUnit dataFormUnit, Class<?> fieldType) {
		Integer serialIndex = fieldNameSerialIndexTable.get(name);
		if(serialIndex == null)
			return null;
		return dataFormUnit.getFieldValue(serialIndex.intValue(), fieldType);
	}

}
