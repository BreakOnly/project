package com.jrmf.taxsettlement.api;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MerchantAPITransferBatchDao {

	void addNewTransferBatch(Map<String, Object> params);

	void updateTransferBatch(Map<String, Object> params);

	APITransferBatchDO getDealBatch(@Param("merchantId") String merchantId, @Param("batchNo") String batchNo);
}
