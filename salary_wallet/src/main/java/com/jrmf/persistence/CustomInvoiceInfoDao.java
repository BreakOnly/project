package com.jrmf.persistence;

import com.jrmf.domain.CustomInvoiceInfoDO;
import com.jrmf.domain.Page;
import com.jrmf.domain.vo.CustomInvoiceInfoVO;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author guoto
 */
@Mapper
public interface CustomInvoiceInfoDao {
    /**商户下所有收货地址*/
    List<CustomInvoiceInfoDO> listCustomInvoiceInfo(String customkey);
    /**新增收货地址*/
    int insertCustomInvoiceInfo(CustomInvoiceInfoDO customInvoiceInfoDO);
    /**按照查询条件筛选*/
    List<CustomInvoiceInfoDO> listCustomInvoiceInfoByParams(HashMap<String,Object> params);
    /**删除*/
    int deleteCustomInvoiceInfo(int id);
    /**按照id获取*/
    CustomInvoiceInfoDO getCustomInvoiceInfoById(int id);
    /**把当前设置为默认*/
    int setCurrentDefault(int id);
    /**把除默认以外的设置为非默认*/
    int setOtherNotDefault(@Param("customkey") String customKey,@Param("id") int id);
    /**修改*/
    int updateCustomInvoiceByParam(CustomInvoiceInfoDO customInvoiceInfoDO);
	List<CustomInvoiceInfoVO> listCustomInvoiceInfoByPage(Page page);
	int listCustomInvoiceInfoCountByPage(Page page);
	int getMerchantInvoiceAddressCount(Page page);
	List<Map<String, Object>> getMerchantInvoiceAddressByPage(Page page);
	List<Map<String, Object>> getMerchantInvoiceAddressNoPage(Page page);

  String getInvoicedAmountByParam(Map<String, Object> paramMap);
}
