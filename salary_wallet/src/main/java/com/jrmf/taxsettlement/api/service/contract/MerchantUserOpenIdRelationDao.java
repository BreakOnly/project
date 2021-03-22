package com.jrmf.taxsettlement.api.service.contract;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MerchantUserOpenIdRelationDao {

	List<MerchantUserOpenIdRelationDO> listRelation(@Param("merchantId") String merchantId,
			@Param("userOpenId") String userOpenId);

	void addRelation(Map<String, String> params);

}
