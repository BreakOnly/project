package com.jrmf.taxsettlement.api;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MerchantAPITransferRecordDao {

	void addNewTransferRequest(Map<String, Object> params);

	void updateTransferRequest(Map<String, Object> params);

	String matchDealNo(@Param("merchantId") String merchantId, @Param("requestNo") String requestNo);

	APITransferRecordDO getDealRecord(String dealNo);

	int countDealRecord(Map<String, Object> params);
	
	List<APITransferRecordDO> listDealRecord(Map<String, Object> params);

}
