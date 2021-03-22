package com.jrmf.utils.transaction;


import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionRunner {

	private static final Logger logger = LoggerFactory.getLogger(TransactionRunner.class);
	
	private DataSourceTransactionManager dataSourceTransactionManager;
	
	public TransactionRunner(DataSourceTransactionManager dataSourceTransactionManager) {
		this.dataSourceTransactionManager = dataSourceTransactionManager;
	}

	public boolean runTransaction(int propagationType, Map<String,Object> transactionContext, TransactionSection transactionSection) {
		
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setPropagationBehavior(propagationType);
		TransactionStatus status = dataSourceTransactionManager.getTransaction(def);
		try {
			transactionSection.doInATransaction(transactionContext);
			dataSourceTransactionManager.commit(status);
			return true;
		} catch(Throwable t) {
			logger.error("transaction fail and rollback", t);
			dataSourceTransactionManager.rollback(status);
			return false;
		}
	}
}