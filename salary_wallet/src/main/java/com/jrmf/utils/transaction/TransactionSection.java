package com.jrmf.utils.transaction;


import java.util.Map;

public interface TransactionSection {

	void doInATransaction(Map<String, Object> paramContext);
}
