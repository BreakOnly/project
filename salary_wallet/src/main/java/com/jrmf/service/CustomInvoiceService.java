package com.jrmf.service;


import com.jrmf.domain.CustomInvoiceInfoDO;
import com.jrmf.domain.Page;
import com.jrmf.domain.vo.CustomInvoiceInfoVO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface CustomInvoiceService {

    boolean addCustomInvoiceInfo(CustomInvoiceInfoVO customInvoiceInfoVO);
    List<CustomInvoiceInfoVO> listCustomInvoiceInfo(String customkey);
    List<CustomInvoiceInfoVO> listCustomInvoiceInfoByParams(HashMap<String,Object> params);
    boolean deleteCustomInvoiceInfo(String customkey,int id);
    CustomInvoiceInfoVO getCustomInvoiceInfoVOById(int id);
    CustomInvoiceInfoDO getCustomInvoiceInfoDOById(int id);
    boolean updateCustomInvoiceByParam(CustomInvoiceInfoVO customInvoiceInfoVO);
    boolean setCurrentDefault(String customkey, int id);
	List<CustomInvoiceInfoVO> listCustomInvoiceInfoByPage(Page page);
	int listCustomInvoiceInfoCountByPage(Page page);
	int getMerchantInvoiceAddressCount(Page page);
	List<Map<String, Object>> getMerchantInvoiceAddressByPage(Page page);
	List<Map<String, Object>> getMerchantInvoiceAddressNoPage(Page page);

  String getInvoicedAmountByParam(Map<String, Object> paramMap);
}
