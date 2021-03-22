package com.jrmf.taxsettlement.api.gateway.batch.form;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

import com.jrmf.taxsettlement.api.service.ActionParams;

public abstract class AbstractDataFormTemplate<D extends DataFormUnit> implements DataFormTemplate<D> {

	private Map<String, Object> presetFieldValueTable;
	
	protected AbstractDataFormTemplate(Map<String, Object> presetFieldValueTable) {
		super();
		this.presetFieldValueTable = presetFieldValueTable;
	}

	@Override
	public ActionParams parse(D dataFormUnit, Class<? extends ActionParams> exactalParamClass) throws Exception {

		ActionParams newInstance = exactalParamClass.newInstance();
		Class<?> thisClass = exactalParamClass;
		do {
			for(Field field : thisClass.getDeclaredFields()) {
				
				int modifiers = field.getModifiers();
				if(Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers))
					continue;
				
				boolean originalAccessable = field.isAccessible();
				try {
					field.setAccessible(true);
					String fieldName = field.getName();
					Object value = getFieldValue(fieldName, dataFormUnit, field.getType());
					value = value == null ? presetFieldValueTable.get(fieldName) : value;
					field.set(newInstance, value);
				} finally {
					field.setAccessible(originalAccessable);
				}
			}
			
			thisClass = thisClass.getSuperclass();
		} while (!thisClass.equals(Object.class));

		return newInstance;
	}

	protected abstract Object getFieldValue(String name, D dataFormUnit, Class<?> fieldType);

}
