package com.jrmf.taxsettlement.api.gateway.batch.form;

public interface SerialFieldDataUnit extends DataFormUnit {

	Object getFieldValue(int index, Class<?> fieldType);
}
