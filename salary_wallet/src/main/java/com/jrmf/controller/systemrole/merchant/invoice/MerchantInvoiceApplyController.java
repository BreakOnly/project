package com.jrmf.controller.systemrole.merchant.invoice;

import com.alibaba.fastjson.JSONArray;
import com.google.common.base.Joiner;
import com.jrmf.common.CommonString;
import com.jrmf.common.Constant;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.BestSignConfig;
import com.jrmf.controller.constant.CustomType;
import com.jrmf.controller.constant.InvoiceOrderStatus;
import com.jrmf.controller.constant.LoginRole;
import com.jrmf.controller.constant.PayType;
import com.jrmf.controller.constant.QueryType;
import com.jrmf.controller.constant.RechargeType;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.ChannelHistory;
import com.jrmf.domain.Company;
import com.jrmf.domain.OrganizationNode;
import com.jrmf.domain.Page;
import com.jrmf.domain.QbInvoicePic;
import com.jrmf.domain.QbInvoiceRecord;
import com.jrmf.domain.QbInvoiceReserve;
import com.jrmf.domain.QbInvoiceVoucher;
import com.jrmf.persistence.CustomProxyDao;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.ChannelHistoryService;
import com.jrmf.service.OrganizationTreeService;
import com.jrmf.service.QbInvoicePicService;
import com.jrmf.service.QbInvoiceRecordService;
import com.jrmf.service.QbInvoiceReserveService;
import com.jrmf.service.QbInvoiceVoucherService;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.ExcelFileGenerator;
import com.jrmf.utils.FtpTool;
import com.jrmf.utils.OrderNoUtil;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/invoiceApply")
public class MerchantInvoiceApplyController extends BaseController{

	private static Logger logger = LoggerFactory.getLogger(MerchantInvoiceApplyController.class);

	@Autowired
	private OrganizationTreeService organizationTreeService;
	@Autowired
	private ChannelHistoryService channelHistoryService;
	@Autowired
	private QbInvoiceRecordService qbInvoiceRecordService;
	@Autowired
	private ChannelCustomService customService;
	@Autowired
	private  CustomProxyDao customProxyDao;
	@Autowired
	private QbInvoicePicService qbInvoicePicService;
	@Autowired
	private BestSignConfig bestSignConfig;
	//????????????service
	@Autowired
	private QbInvoiceReserveService qbInvoiceReserveService;
	//??????????????????
	@Autowired
	private QbInvoiceVoucherService qbInvoiceVoucherService;

	/**
	 * ?????????????????????????????????
	 * @param request
	 * @return
	 * @throws ParseException 
	 */
	@RequestMapping("/billingList")
	@ResponseBody
	public Map<String, Object> billingList(HttpServletRequest request) throws ParseException{
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		Page page = new Page(request);
		String endTime = page.getParams().get("endTime");
		//???2020???08???01???????????????????????????????????????????????????????????????????????????
		String nowDay = DateUtils.getNowDay();
		if (nowDay.compareTo(Constant.COMMISSION_INVOICE_START_DAY) >= 0
				&& (StringUtil.isEmpty(endTime) || endTime.compareTo(Constant.COMMISSION_INVOICE_START_DAY) >= 0)) {
			//???????????????????????????????????????
			Map<String,Object> companyParam = new HashMap<>();
			companyParam.put("invoiceCategory",2);
			List<Company> companyList = companyService.getCompanyListByParam(companyParam);
			StringBuilder sb = new StringBuilder();
			for (Company company : companyList) {
				sb.append(company.getUserId()).append(",");
			}
			Map<String, String> params = page.getParams();
			params.put("commissionCompanyIds",sb.toString());
		}
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		int total = 0 ;
		List<Map<String, Object>> relationList=null;
		if(customLogin.getCustomType()==4&&customLogin.getMasterCustom()!=null){
			ChannelCustom masterChannelCustom = customService.getCustomByCustomkey(customLogin.getMasterCustom());
			if((CommonString.ROOT.equals(masterChannelCustom.getCustomkey()) || (CustomType.ROOT.getCode() == masterChannelCustom.getCustomType()
					&& CommonString.ROOT.equals(masterChannelCustom.getMasterCustom()) && LoginRole.ADMIN_ACCOUNT.getCode() == masterChannelCustom.getLoginRole()))){
				//??????
				total = channelHistoryService.querybillingListCount(page);
				relationList = channelHistoryService.billingList(page);
				result.put(RespCode.RESP_STAT, respstat);
				result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
				result.put("total", total);
				result.put("relationList", relationList);
			}else if(masterChannelCustom.getCustomType() == CustomType.CUSTOM.getCode()){
				//????????????
				page.getParams().put("loginCustomer", masterChannelCustom.getCustomkey());
				total = channelHistoryService.querybillingListCount(page);
				relationList = channelHistoryService.billingList(page);
				result.put(RespCode.RESP_STAT, respstat);
				result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
				result.put("total", total);
				result.put("relationList", relationList);
			} else if (masterChannelCustom.getCustomType() == CustomType.COMPANY.getCode()) {
				page.getParams().put("companyId", masterChannelCustom.getCustomkey());
				page.getParams().put("originalIds", masterChannelCustom.getCustomkey());
				total = channelHistoryService.querybillingListCount(page);
				relationList = channelHistoryService.billingList(page);
				result.put(RespCode.RESP_STAT, respstat);
				result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
				result.put("total", total);
				result.put("relationList", relationList);
			} else if(masterChannelCustom.getCustomType() == CustomType.GROUP.getCode()){
				//????????????
				int nodeId = organizationTreeService.queryNodeIdByCustomKey(masterChannelCustom.getCustomkey());
				List<String> customKeys = organizationTreeService.queryNodeCusotmKey(masterChannelCustom.getCustomType(), QueryType.QUERY_CURRENT_AND_CHILDREN, nodeId);
				page.getParams().put("loginCustomer", String.join(",", customKeys));
				total = channelHistoryService.querybillingListCount(page);
				relationList = channelHistoryService.billingList(page);
				result.put(RespCode.RESP_STAT, respstat);
				result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
				result.put("total", total);
				result.put("relationList", relationList);
			}else if(masterChannelCustom.getCustomType()==CustomType.PROXY.getCode()){
				//?????????????????????????????????
				if (masterChannelCustom.getProxyType() == 1) {
					OrganizationNode node = customProxyDao.getProxyChildenNodeByCustomKey(masterChannelCustom.getCustomkey(),null);
					List<String> stringList = organizationTreeService.queryNodeCusotmKey(CustomType.PROXYCHILDEN.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN, node.getId());
					if (stringList != null && stringList.size() > 0) {
						List<String> customStringList = new ArrayList<String>();
						for (String customKey : stringList) {
							OrganizationNode itemNode = customProxyDao.getNodeByCustomKey(customKey,null);
							List<String> customKeyList= organizationTreeService.queryNodeCusotmKey(CustomType.PROXY.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN, itemNode.getId());
							customStringList.addAll(customKeyList);
						}
						String customKeys = Joiner.on(",").join(customStringList);
						page.getParams().put("loginCustomer", String.join(",",customKeys));
					}
				} else {
					OrganizationNode node = customProxyDao.getNodeByCustomKey(masterChannelCustom.getCustomkey(),null);
					List<String> stringList = organizationTreeService.queryNodeCusotmKey(CustomType.PROXY.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN, node.getId());
					String customKeys = Joiner.on(",").join(stringList);
					page.getParams().put("loginCustomer", String.join(",",customKeys));
				}
				total = channelHistoryService.querybillingListCount(page);
				relationList = channelHistoryService.billingList(page);
				result.put(RespCode.RESP_STAT, respstat);
				result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
				result.put("total", total);
				result.put("relationList", relationList);
			}else{
				result.put(RespCode.RESP_STAT, RespCode.DO_NOT_HAVE_APPROVAL_RIGHT);
				result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.DO_NOT_HAVE_APPROVAL_RIGHT));
				result.put("total", total);
				result.put("relationList", relationList);
			}
		}else{
			if((CommonString.ROOT.equals(customLogin.getCustomkey()) || (CustomType.ROOT.getCode() == customLogin.getCustomType()
					&& CommonString.ROOT.equals(customLogin.getMasterCustom()) && LoginRole.ADMIN_ACCOUNT.getCode() == customLogin.getLoginRole()))){
				//??????
				total = channelHistoryService.querybillingListCount(page);
				relationList = channelHistoryService.billingList(page);
				result.put(RespCode.RESP_STAT, respstat);
				result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
				result.put("total", total);
				result.put("relationList", relationList);
			}else if(customLogin.getCustomType() == CustomType.CUSTOM.getCode()){
				//????????????
				page.getParams().put("loginCustomer", customLogin.getCustomkey());
				total = channelHistoryService.querybillingListCount(page);
				relationList = channelHistoryService.billingList(page);
				result.put(RespCode.RESP_STAT, respstat);
				result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
				result.put("total", total);
				result.put("relationList", relationList);
			} else if (customLogin.getCustomType() == CustomType.COMPANY.getCode()) {
				page.getParams().put("companyId", customLogin.getCustomkey());
				page.getParams().put("originalIds", customLogin.getCustomkey());
				total = channelHistoryService.querybillingListCount(page);
				relationList = channelHistoryService.billingList(page);
				result.put(RespCode.RESP_STAT, respstat);
				result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
				result.put("total", total);
				result.put("relationList", relationList);
			} else if(customLogin.getCustomType() == CustomType.GROUP.getCode()){
				//????????????
				int nodeId = organizationTreeService.queryNodeIdByCustomKey(customLogin.getCustomkey());
				List<String> customKeys = organizationTreeService.queryNodeCusotmKey(customLogin.getCustomType(), QueryType.QUERY_CURRENT_AND_CHILDREN, nodeId);
				page.getParams().put("loginCustomer", String.join(",", customKeys));
				total = channelHistoryService.querybillingListCount(page);
				relationList = channelHistoryService.billingList(page);
				result.put(RespCode.RESP_STAT, respstat);
				result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
				result.put("total", total);
				result.put("relationList", relationList);
			}else if(customLogin.getCustomType()==CustomType.PROXY.getCode()){
				//?????????????????????????????????
				if (customLogin.getProxyType() == 1) {
					OrganizationNode node = customProxyDao.getProxyChildenNodeByCustomKey(customLogin.getCustomkey(),null);
					List<String> stringList = organizationTreeService.queryNodeCusotmKey(CustomType.PROXYCHILDEN.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN, node.getId());
					if (stringList != null && stringList.size() > 0) {
						List<String> customStringList = new ArrayList<String>();
						for (String customKey : stringList) {
							OrganizationNode itemNode = customProxyDao.getNodeByCustomKey(customKey,null);
							List<String> customKeyList= organizationTreeService.queryNodeCusotmKey(CustomType.PROXY.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN, itemNode.getId());
							customStringList.addAll(customKeyList);
						}
						String customKeys = Joiner.on(",").join(customStringList);
						page.getParams().put("loginCustomer", String.join(",",customKeys));
					}
				} else {
					OrganizationNode node = customProxyDao.getNodeByCustomKey(customLogin.getCustomkey(),null);
					List<String> stringList = organizationTreeService.queryNodeCusotmKey(CustomType.PROXY.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN, node.getId());
					String customKeys = Joiner.on(",").join(stringList);
					page.getParams().put("loginCustomer", String.join(",",customKeys));
				}
				total = channelHistoryService.querybillingListCount(page);
				relationList = channelHistoryService.billingList(page);
				result.put(RespCode.RESP_STAT, respstat);
				result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
				result.put("total", total);
				result.put("relationList", relationList);
			}else{
				result.put(RespCode.RESP_STAT, RespCode.DO_NOT_HAVE_APPROVAL_RIGHT);
				result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.DO_NOT_HAVE_APPROVAL_RIGHT));
				result.put("total", total);
				result.put("relationList", relationList);
			}
		}
		return result;
	}

	/**
	 * ??????????????????
	 * @param invoiceRecord
	 * @return
	 */
	@RequestMapping("/commitInvoice")
	@ResponseBody
	public Map<String, Object> commitInvoice(QbInvoiceRecord invoiceRecord,HttpServletRequest request){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		if(invoiceRecord.getId()==null){
			if(StringUtil.isEmpty(invoiceRecord.getOrderNo())){
				//??????????????????
				result.put(RespCode.RESP_STAT, RespCode.NOT_CHECK_RECHARGENO);
				result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.NOT_CHECK_RECHARGENO));
			}else{
				try{
					//????????????????????????
					ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
					String invoiceSerialNo = "P"+OrderNoUtil.getOrderNo();
					invoiceRecord.setAddUser(customLogin.getUsername());
					invoiceRecord.setInvoiceSerialNo(invoiceSerialNo);
					invoiceRecord.setStatus(1);
					invoiceRecord.setIsDiscard(0);
					invoiceRecord.setCreateTime(DateUtils.getNowDate());
					invoiceRecord.setInvoiceMethod(1);
					String []orderNoArray = invoiceRecord.getOrderNo().split(",");
					//???????????????????????????????????????
					if(orderNoArray.length>1){
						//???????????????
						//??????????????????????????????
						String amount = channelHistoryService.getTotalAmountByOrderNo(invoiceRecord.getOrderNo());
						BigDecimal InvoiceAmountDec = new BigDecimal(StringUtil.isEmpty(invoiceRecord.getInvoiceAmount())?"0":invoiceRecord.getInvoiceAmount());
						BigDecimal unInvoiceAmountDec = new BigDecimal(StringUtil.isEmpty(amount)?"0":amount);
						//?????????????????????????????????????????????????????????
						if(InvoiceAmountDec.compareTo(unInvoiceAmountDec)==0){
							if(unInvoiceAmountDec.compareTo(new BigDecimal(0))==1){
								//??????????????????????????????
								boolean checkStatus = true;
								for (String orderNo : orderNoArray) {
									ChannelHistory channelHistory = channelHistoryService.getChannelHistoryByOrderno(orderNo);
									if(channelHistory.getInvoiceStatus()!=0){
										//?????????????????????????????????????????????????????????????????????
										checkStatus = false;
									}
								}
								if(checkStatus){
									//????????????
									for (String orderNo : orderNoArray) {
										//???????????????????????????????????????????????????????????????????????????????????????????????????????????????0
										ChannelHistory channelHistory = channelHistoryService.getChannelHistoryByOrderno(orderNo);
										channelHistory.setInvoiceingAmount(channelHistory.getUnInvoiceAmount());
										channelHistory.setUnInvoiceAmount("0");
										channelHistory.setInvoiceStatus(3);
										channelHistory.setRemark(invoiceRecord.getRemark());
										//????????????????????????
										channelHistoryService.updateChannelHistory(channelHistory);
										result.put(RespCode.RESP_STAT, respstat);
										result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
									}
									invoiceRecord.setIsDiscard(0);
									qbInvoiceRecordService.insert(invoiceRecord);
								}else{
									//???????????????
									result.put(RespCode.RESP_STAT, RespCode.INVOICE_CONTAIN_SUCEESS);
									result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.INVOICE_CONTAIN_SUCEESS));
								}
							}else{
								//?????????????????????
								result.put(RespCode.RESP_STAT, RespCode.INVOICEAMOUNT_SHOULD_GT_ZERO);
								result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.INVOICEAMOUNT_SHOULD_GT_ZERO));
							}
						}else{
							//?????????????????????
							result.put(RespCode.RESP_STAT, RespCode.INVOICE_AMOUNT_DIFFERENT);
							result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.INVOICE_AMOUNT_DIFFERENT));
						}	
					}else{
						//????????????
						//??????????????????
						ChannelHistory channelHistory = channelHistoryService.getChannelHistoryByOrderno(invoiceRecord.getOrderNo());
						//????????????????????????????????????
						if(channelHistory.getInvoiceStatus()!=2){
							//??????????????????
//							List<Integer> billingClassList = qbInvoiceRecordService.groupBillingClassByOrderNo(invoiceRecord.getOrderNo());
							//??????????????????????????????
//							if(billingClassList==null || billingClassList.size()==0||(billingClassList.size()==1&& billingClassList
//									.get(0).equals(invoiceRecord.getBillingClass()))){
								BigDecimal invoiceAmountDec = new BigDecimal(StringUtil.isEmpty(invoiceRecord.getInvoiceAmount())?"0":invoiceRecord.getInvoiceAmount());
								BigDecimal invoiceingAmountDec = new BigDecimal(StringUtil.isEmpty(channelHistory.getInvoiceingAmount())?"0":channelHistory.getInvoiceingAmount());
								BigDecimal unInvoiceAmountDec = new BigDecimal(StringUtil.isEmpty(channelHistory.getUnInvoiceAmount())?"0":channelHistory.getUnInvoiceAmount());
								//???????????????????????????????????????????????????????????????????????????0????????????????????????????????????????????????
								if(invoiceAmountDec.compareTo(new BigDecimal(0))==1&&unInvoiceAmountDec.compareTo(new BigDecimal(0))==1&&invoiceAmountDec.compareTo(unInvoiceAmountDec)!=1){
									invoiceingAmountDec = invoiceingAmountDec.add(invoiceAmountDec);
									unInvoiceAmountDec = unInvoiceAmountDec.subtract(invoiceAmountDec);
									channelHistory.setInvoiceingAmount(invoiceingAmountDec.toString());
									channelHistory.setUnInvoiceAmount(unInvoiceAmountDec.toString());
									channelHistory.setInvoiceStatus(3);
									channelHistory.setRemark(invoiceRecord.getRemark());
									channelHistoryService.updateChannelHistory(channelHistory);
									qbInvoiceRecordService.insert(invoiceRecord);
									result.put(RespCode.RESP_STAT, respstat);
									result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
								}else{
									//?????????
									result.put(RespCode.RESP_STAT, RespCode.INVOICE_AMOUNT_WRONG);
									result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.INVOICE_AMOUNT_WRONG));
								}
//							}else{
//								result.put(RespCode.RESP_STAT, RespCode.INVOICE_ClASS_DIFFERENT);
//								result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.INVOICE_ClASS_DIFFERENT));
//							}
						}else{
							//???????????????
							result.put(RespCode.RESP_STAT, RespCode.INVOICE_COMPLETE);
							result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.INVOICE_COMPLETE));
						}
					}
				}catch(Exception e){
					logger.error(e.getMessage(), e);
					result.put(RespCode.RESP_STAT, RespCode.INVOICE_EXCEPTION);
					result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.INVOICE_EXCEPTION));
				}

			}
		}else{
			result.put(RespCode.RESP_STAT,RespCode.INVOICE_HANDLE);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.INVOICE_HANDLE));
		}
		return result;
	}

	/**
	 * ????????????????????????
	 * @param invoiceRecord
	 * @return
	 */
	@RequestMapping("/calculationAmount")
	@ResponseBody
	public Map<String, Object> calculationAmount(QbInvoiceRecord invoiceRecord){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		boolean flag = true;
		List<Map<String,Object>> groupInfo = channelHistoryService.checkCommonCompanyAndCustom(invoiceRecord.getOrderNo());
		if(groupInfo!=null && groupInfo.size()>1){
			flag =false;
		}
		if(!flag){
			//???????????????????????????????????????????????????+????????????????????????
			result.put(RespCode.RESP_STAT,RespCode.INVOICE_MERANDCOMPANY_DIFFERENT);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.INVOICE_MERANDCOMPANY_DIFFERENT));
		}else{
			//?????????????????????
			String unInvoiceAmount = channelHistoryService.getTotalAmountByOrderNo(invoiceRecord.getOrderNo());
			unInvoiceAmount = StringUtil.isEmpty(unInvoiceAmount)?"0":unInvoiceAmount;
			result.put(RespCode.RESP_STAT, respstat);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
			result.put("unInvoiceAmount", unInvoiceAmount);
			result.put("invoiceAmount", unInvoiceAmount);
		}
		return result;
	}


	/**
	 * ??????excel
	 */
	@RequestMapping("/export")
	public void export(HttpServletResponse response,HttpServletRequest request) {
		// ??????
		String[] headers = new String[] { "??????", "????????????", "????????????","??????","??????",
				"??????????????????","????????????","????????????","??????????????????","??????????????????","????????????","?????????????????????","?????????????????????","???????????????","????????????","????????????","??????????????????","??????????????????","????????????"};
		String filename = "????????????????????????";
		Page page = new Page(request);
		String endTime = page.getParams().get("endTime");
		//???2020???08???01????????????????????????????????????????????????????????????????????????
		String nowDay = DateUtils.getNowDay();
		if (nowDay.compareTo(Constant.COMMISSION_INVOICE_START_DAY) >= 0
				&& (StringUtil.isEmpty(endTime) || endTime.compareTo(Constant.COMMISSION_INVOICE_START_DAY) >= 0)) {
			//???????????????????????????????????????
			Map<String,Object> companyParam = new HashMap<>();
			companyParam.put("invoiceCategory",2);
			List<Company> companyList = companyService.getCompanyListByParam(companyParam);
			StringBuilder sb = new StringBuilder();
			for (Company company : companyList) {
				sb.append(company.getUserId()).append(",");
			}
			Map<String, String> params = page.getParams();
			params.put("commissionCompanyIds",sb.toString());
		}
		boolean isRoot = false;
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		if(customLogin.getCustomType()==4&&customLogin.getMasterCustom()!=null){
			ChannelCustom masterChannelCustom = customService.getCustomByCustomkey(customLogin.getMasterCustom());
			if((CommonString.ROOT.equals(masterChannelCustom.getCustomkey()) || (CustomType.ROOT.getCode() == masterChannelCustom.getCustomType() && CommonString.ROOT.equals(masterChannelCustom.getMasterCustom()) && LoginRole.ADMIN_ACCOUNT.getCode() == masterChannelCustom.getLoginRole()))){
				isRoot= true;
			}else if(masterChannelCustom.getCustomType() == CustomType.CUSTOM.getCode()){
				//????????????
				page.getParams().put("loginCustomer", masterChannelCustom.getCustomkey());
			} else if (masterChannelCustom.getCustomType() == CustomType.COMPANY.getCode()) {
				page.getParams().put("companyId", masterChannelCustom.getCustomkey());
				page.getParams().put("originalIds", masterChannelCustom.getCustomkey());
			} else if(masterChannelCustom.getCustomType() == CustomType.GROUP.getCode()){
				//????????????
				int nodeId = organizationTreeService.queryNodeIdByCustomKey(masterChannelCustom.getCustomkey());
				List<String> customKeys = organizationTreeService.queryNodeCusotmKey(masterChannelCustom.getCustomType(), QueryType.QUERY_CURRENT_AND_CHILDREN, nodeId);
				page.getParams().put("loginCustomer", String.join(",", customKeys));
			}else if(masterChannelCustom.getCustomType()==CustomType.PROXY.getCode()){
				//?????????
				if (masterChannelCustom.getProxyType() == 1) {
					OrganizationNode node = customProxyDao.getProxyChildenNodeByCustomKey(masterChannelCustom.getCustomkey(),null);
					List<String> stringList = organizationTreeService.queryNodeCusotmKey(CustomType.PROXYCHILDEN.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN, node.getId());
					if (stringList != null && stringList.size() > 0) {
						List<String> customStringList = new ArrayList<String>();
						for (String customKey : stringList) {
							OrganizationNode itemNode = customProxyDao.getNodeByCustomKey(customKey,null);
							List<String> customKeyList= organizationTreeService.queryNodeCusotmKey(CustomType.PROXY.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN, itemNode.getId());
							customStringList.addAll(customKeyList);
						}
						String customKeys = Joiner.on(",").join(customStringList);
						page.getParams().put("loginCustomer", String.join(",",customKeys));
					}
				} else {
					OrganizationNode node = customProxyDao.getNodeByCustomKey(masterChannelCustom.getCustomkey(),null);
					List<String> stringList = organizationTreeService.queryNodeCusotmKey(CustomType.PROXY.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN, node.getId());
					String customKeys = Joiner.on(",").join(stringList);
					page.getParams().put("loginCustomer", String.join(",",customKeys));
				}
			}
		}else{
			if((CommonString.ROOT.equals(customLogin.getCustomkey()) || (CustomType.ROOT.getCode() == customLogin.getCustomType() && CommonString.ROOT.equals(customLogin.getMasterCustom()) && LoginRole.ADMIN_ACCOUNT.getCode() == customLogin.getLoginRole()))){
				isRoot= true;
			}else if(customLogin.getCustomType() == CustomType.CUSTOM.getCode()){
				//????????????
				page.getParams().put("loginCustomer", customLogin.getCustomkey());
			} else if (customLogin.getCustomType() == CustomType.COMPANY.getCode()) {
				page.getParams().put("companyId", customLogin.getCustomkey());
				page.getParams().put("originalIds", customLogin.getCustomkey());
			} else if(customLogin.getCustomType() == CustomType.GROUP.getCode()){
				//????????????
				int nodeId = organizationTreeService.queryNodeIdByCustomKey(customLogin.getCustomkey());
				List<String> customKeys = organizationTreeService.queryNodeCusotmKey(customLogin.getCustomType(), QueryType.QUERY_CURRENT_AND_CHILDREN, nodeId);
				page.getParams().put("loginCustomer", String.join(",", customKeys));
			}else if(customLogin.getCustomType()==CustomType.PROXY.getCode()){
				//?????????
				if (customLogin.getProxyType() == 1) {
					OrganizationNode node = customProxyDao.getProxyChildenNodeByCustomKey(customLogin.getCustomkey(),null);
					List<String> stringList = organizationTreeService.queryNodeCusotmKey(CustomType.PROXYCHILDEN.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN, node.getId());
					if (stringList != null && stringList.size() > 0) {
						List<String> customStringList = new ArrayList<String>();
						for (String customKey : stringList) {
							OrganizationNode itemNode = customProxyDao.getNodeByCustomKey(customKey,null);
							List<String> customKeyList= organizationTreeService.queryNodeCusotmKey(CustomType.PROXY.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN, itemNode.getId());
							customStringList.addAll(customKeyList);
						}
						String customKeys = Joiner.on(",").join(customStringList);
						page.getParams().put("loginCustomer", String.join(",",customKeys));
					}
				} else {
					OrganizationNode node = customProxyDao.getNodeByCustomKey(customLogin.getCustomkey(),null);
					List<String> stringList = organizationTreeService.queryNodeCusotmKey(CustomType.PROXY.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN, node.getId());
					String customKeys = Joiner.on(",").join(stringList);
					page.getParams().put("loginCustomer", String.join(",",customKeys));
				}
			}
		}
		List<Map<String, Object>> relationList = channelHistoryService.queryBillingListNoPage(page);
		List<Map<String, Object>> data = new ArrayList<>();
		if(isRoot){
			headers = new String[] { "??????", "????????????","????????????","????????????","??????","??????",
					"??????????????????","????????????","????????????","??????????????????","??????????????????","????????????","?????????????????????","?????????????????????","???????????????","????????????","????????????","??????????????????","??????????????????","????????????"};
			for (Map<String, Object> invioceBase : relationList) {
				Map<String, Object> dataMap = new HashMap<>(20);
				dataMap.put("1", invioceBase.get("id"));
				dataMap.put("2", invioceBase.get("merchantName"));
				dataMap.put("3", invioceBase.get("agentName"));
				dataMap.put("4", invioceBase.get("orderno"));
				dataMap.put("5",invioceBase.get("createtime"));
				dataMap.put("6", RechargeType.codeOf(Integer.parseInt(String.valueOf(invioceBase.get("rechargeType")==null?"1":invioceBase.get("rechargeType")))).getDesc());
				dataMap.put("7", PayType.codeOf(Integer.parseInt(String.valueOf(invioceBase.get("payType")))).getDesc());
				dataMap.put("8","??????");
				dataMap.put("9",invioceBase.get("refundAmount"));
				dataMap.put("10",invioceBase.get("rechargeAmount"));
				dataMap.put("11",invioceBase.get("realRechargeAmount"));
				dataMap.put("12",InvoiceOrderStatus.codeOf(Integer.parseInt(String.valueOf(invioceBase.get("invoiceStatus")==null?"0":invioceBase.get("invoiceStatus")))).getDesc());
				dataMap.put("13",invioceBase.get("invoiceAmount"));
				dataMap.put("14",invioceBase.get("invoiceingAmount"));
				dataMap.put("15",invioceBase.get("unInvoiceAmount"));
				dataMap.put("16",invioceBase.get("serviceName"));
				dataMap.put("17",invioceBase.get("inAccountNo"));
				dataMap.put("18",invioceBase.get("inAccountBankName"));
				dataMap.put("19",invioceBase.get("accountName"));
				dataMap.put("20",invioceBase.get("accountNo"));
				data.add(sortMapByKey(dataMap));
			}
		}else{
			for (Map<String, Object> invioceBase : relationList) {
				Map<String, Object> dataMap = new HashMap<>(20);
				dataMap.put("1", invioceBase.get("id"));
				dataMap.put("2", invioceBase.get("merchantName"));
				dataMap.put("3", invioceBase.get("orderno"));
				dataMap.put("4",invioceBase.get("createtime"));
				dataMap.put("5", RechargeType.codeOf(Integer.parseInt(String.valueOf(invioceBase.get("rechargeType")==null?"1":invioceBase.get("rechargeType")))).getDesc());
				dataMap.put("6", PayType.codeOf(Integer.parseInt(String.valueOf(invioceBase.get("payType")))).getDesc());
				dataMap.put("7","??????");
				dataMap.put("8",invioceBase.get("refundAmount"));
				dataMap.put("9",invioceBase.get("rechargeAmount"));
				dataMap.put("10",invioceBase.get("realRechargeAmount"));
				dataMap.put("11",InvoiceOrderStatus.codeOf(Integer.parseInt(String.valueOf(invioceBase.get("invoiceStatus")==null?"0":invioceBase.get("invoiceStatus")))).getDesc());
				dataMap.put("12",invioceBase.get("invoiceAmount"));
				dataMap.put("13",invioceBase.get("invoiceingAmount"));
				dataMap.put("14",invioceBase.get("unInvoiceAmount"));
				dataMap.put("15",invioceBase.get("serviceName"));
				dataMap.put("16",invioceBase.get("inAccountNo"));
				dataMap.put("17",invioceBase.get("inAccountBankName"));
				dataMap.put("18",invioceBase.get("accountName"));
				dataMap.put("19",invioceBase.get("accountNo"));
				data.add(sortMapByKey(dataMap));
			}
		}
		ExcelFileGenerator.ExcelExport(response, headers, filename, data);
	}

	/**
	 * ?????????
	 * @param invoiceRecord
	 * @return
	 */
	@RequestMapping("/prepInvoice")
	@ResponseBody
	public Map<String, Object> prepInvoice(QbInvoiceRecord invoiceRecord,HttpServletRequest request,MultipartFile []file,String invoiceVouchers){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		//????????????????????????
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		try{
			boolean flag = false;
			if(customLogin.getCustomType()==4&&customLogin.getMasterCustom()!=null){
				ChannelCustom masterChannelCustom = customService.getCustomByCustomkey(customLogin.getMasterCustom());
				if ((CommonString.ROOT.equals(masterChannelCustom.getCustomkey()) || (CustomType.ROOT.getCode() == masterChannelCustom.getCustomType()
						&& CommonString.ROOT.equals(masterChannelCustom.getMasterCustom())
						&& LoginRole.ADMIN_ACCOUNT.getCode() == masterChannelCustom.getLoginRole()))) {
					flag=true;
				}
				if(masterChannelCustom.getCustomType()==2){
					flag=true;
				}
			}else{
				if ((CommonString.ROOT.equals(customLogin.getCustomkey()) || (CustomType.ROOT.getCode() == customLogin.getCustomType()
						&& CommonString.ROOT.equals(customLogin.getMasterCustom())
						&& LoginRole.ADMIN_ACCOUNT.getCode() == customLogin.getLoginRole()))) {
					flag=true;
				}
				if(customLogin.getCustomType()==2){
					flag=true;
				}
			}
			//??????????????????
			if(flag){
				//????????????????????????????????????
				QbInvoiceReserve invoiceReserve = qbInvoiceReserveService.getReserveByParams(invoiceRecord);
				List<QbInvoiceVoucher> invoiceVoucherList = new ArrayList<QbInvoiceVoucher>();
				if(!StringUtil.isEmpty(invoiceVouchers)){
					invoiceVoucherList = JSONArray.parseArray(invoiceVouchers, QbInvoiceVoucher.class);
				}
				String invoiceSerialNo = "P"+OrderNoUtil.getOrderNo();
				invoiceRecord.setAddUser(customLogin.getUsername());
				invoiceRecord.setInvoiceSerialNo(invoiceSerialNo);
				invoiceRecord.setStatus(4);
				invoiceRecord.setIsDiscard(0);
				invoiceRecord.setCreateTime(DateUtils.getNowDate());
				invoiceRecord.setInvoiceMethod(2);
				invoiceRecord.setApproval(0);
				invoiceRecord.setApprovalAmount("0");
				if(invoiceReserve!=null){
					int alreadyCount = qbInvoiceVoucherService.getAlreadyInvoiceCount(invoiceReserve);
					if(invoiceReserve.getInvoiceTotalNum()>0&&(invoiceReserve.getInvoiceTotalNum()-alreadyCount)>0
							&& (invoiceReserve.getInvoiceTotalNum()-alreadyCount)>=invoiceVoucherList.size()){
						//??????????????????
						insertInvoiceInfo(invoiceRecord, file, customLogin, invoiceReserve, invoiceVoucherList, invoiceSerialNo,1);
					}else{
						respstat = RespCode.INVOICENUM_NOT_ENOUGH;
					}
				}else{
					//??????????????????
					insertInvoiceInfo(invoiceRecord, file, customLogin, invoiceReserve, invoiceVoucherList, invoiceSerialNo,0);
				}
			}else{
				respstat = RespCode.DO_NOT_HAVE_APPROVAL_RIGHT;
			}
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			respstat = RespCode.INVOICE_EXCEPTION;
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return result;
	}

	private void insertInvoiceInfo(QbInvoiceRecord invoiceRecord, MultipartFile[] file, ChannelCustom customLogin,
								   QbInvoiceReserve invoiceReserve, List<QbInvoiceVoucher> invoiceVoucherList,
								   String invoiceSerialNo,Integer hasCount) throws IOException {
		for (QbInvoiceVoucher invoiceVoucher : invoiceVoucherList) {
			invoiceVoucher.setInvoiceType(invoiceRecord.getInvoiceType());
			invoiceVoucher.setCompanyId(invoiceRecord.getCompanyId());
			invoiceVoucher.setCustomkey(invoiceRecord.getCustomkey());
			invoiceVoucher.setInvoiceDate(invoiceRecord.getInvoiceTime());
			if (invoiceReserve != null){
				invoiceVoucher.setInvoiceLimitAmout(invoiceReserve.getInvoiceLimitAmout());
			}
			invoiceVoucher.setInvoiceSerialNo(invoiceRecord.getInvoiceSerialNo());
			invoiceVoucher.setApprovalFlag(1);
			invoiceVoucher.setAddUser(customLogin.getUsername());
			invoiceVoucher.setCreateTime(DateUtils.getNowDate());
			invoiceVoucher.setHasCount(hasCount);
			qbInvoiceVoucherService.insert(invoiceVoucher);
		}
		//????????????
		String uploadPath = "/invoiceFile/";
		String filePath = uploadPath+ invoiceRecord.getCustomkey()+"/";
		List<QbInvoicePic> invoicePics = qbInvoicePicService.getPicListBySerialNo(invoiceRecord.getInvoiceSerialNo());
		//??????
		if(file.length>0){
			for (MultipartFile mf : file) {
				if(!mf.isEmpty()){
					//??????UUID???????????????
					String name = UUID.randomUUID().toString().replaceAll("-", "");
					//?????????????????????
					String ext = FilenameUtils.getExtension(mf.getOriginalFilename());
					//????????????????????????
					String fileName = name+"."+ext;
					InputStream in = new ByteArrayInputStream(mf.getBytes());
					String uploadFile = FtpTool.uploadFile(bestSignConfig.getFtpURL(), 21, filePath, fileName, in, bestSignConfig.getUsername(), bestSignConfig.getPassword());
					QbInvoicePic invoicePic = new QbInvoicePic();
					if (!"error".equals(uploadFile)) {
						invoicePic.setInvoicePicUrl(filePath+fileName);
					}
					invoicePic.setAddUser(customLogin.getUsername());
					invoicePic.setInvoiceSerialNo(invoiceSerialNo);
					invoicePic.setCreateTime(DateUtils.getNowDate());
					qbInvoicePicService.insert(invoicePic);
				}
			}
		}
		if(invoicePics!=null&&invoicePics.size()>0){
			Integer fileSize = invoicePics.size()+ file.length;
			invoiceRecord.setInvoiceNum(fileSize);
		}else{
			invoiceRecord.setInvoiceNum(file.length);
		}
		qbInvoiceRecordService.insert(invoiceRecord);
	}


	/**
	 * ???????????????
	 * @param
	 * @return
	 */
	@RequestMapping("/deletePrepInvoice")
	@ResponseBody
	public Map<String, Object> deletePrepInvoice(String id,HttpServletRequest request){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		//????????????????????????
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		try{
			boolean flag = false;
			if(customLogin.getCustomType()==4&&customLogin.getMasterCustom()!=null){
				ChannelCustom masterChannelCustom = customService.getCustomByCustomkey(customLogin.getMasterCustom());
				if ((CommonString.ROOT.equals(masterChannelCustom.getCustomkey()) || (CustomType.ROOT.getCode() == masterChannelCustom.getCustomType() && CommonString.ROOT.equals(masterChannelCustom.getMasterCustom()) && LoginRole.ADMIN_ACCOUNT.getCode() == masterChannelCustom.getLoginRole()))) {
					flag=true;
				}
				if(masterChannelCustom.getCustomType()==2){
					flag=true;	
				}
			}else{
				if ((CommonString.ROOT.equals(customLogin.getCustomkey()) || (CustomType.ROOT.getCode() == customLogin.getCustomType() && CommonString.ROOT.equals(customLogin.getMasterCustom()) && LoginRole.ADMIN_ACCOUNT.getCode() == customLogin.getLoginRole()))) {
					flag=true;
				}
				if(customLogin.getCustomType()==2){
					flag=true;
				}
			}
			//??????????????????
			if(flag){
				QbInvoiceRecord invoiceRecordOld = qbInvoiceRecordService.selectByPrimaryKey(Integer.parseInt(id));
				List<QbInvoiceVoucher> invoiceVouchers= qbInvoiceVoucherService.getInvoiceVoucherBySerialNo(invoiceRecordOld.getInvoiceSerialNo());
				if(invoiceRecordOld.getInvoiceMethod()==2&&invoiceRecordOld.getApproval()==0&&invoiceVouchers.size()<1){
					qbInvoiceRecordService.deleteByPrimaryKey(Integer.parseInt(id));
				}else{
					respstat = RespCode.NO_DELETE_PREPINVOICE;
				}
			}else{
				respstat = RespCode.DO_NOT_HAVE_APPROVAL_RIGHT;
			}
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			respstat = RespCode.DELETE_FAIL;
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return result;
	}
}
