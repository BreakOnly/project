package com.jrmf.controller.systemrole.merchant.invoice;

import com.google.common.base.Joiner;
import com.jrmf.common.CommonString;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.BaseInfo;
import com.jrmf.controller.constant.BestSignConfig;
import com.jrmf.controller.constant.CustomType;
import com.jrmf.controller.constant.InvoiceStatus;
import com.jrmf.controller.constant.InvoiceType;
import com.jrmf.controller.constant.LoginRole;
import com.jrmf.controller.constant.QueryType;
import com.jrmf.controller.constant.TaxpayerType;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.OrganizationNode;
import com.jrmf.domain.Page;
import com.jrmf.domain.QbInvoiceBase;
import com.jrmf.persistence.CustomProxyDao;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.ChannelRelatedService;
import com.jrmf.service.OrganizationTreeService;
import com.jrmf.service.QbInvoiceBaseService;
import com.jrmf.service.QbInvoiceRecordService;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.ExcelFileGenerator;
import com.jrmf.utils.FtpTool;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
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
@RequestMapping("/invoice")
public class MerchantInvoiceBaseController extends BaseController{

	private static Logger logger = LoggerFactory.getLogger(MerchantInvoiceBaseController.class);

	@Autowired
	private QbInvoiceBaseService qbInvoiceBaseService;
	@Autowired
	private BaseInfo baseInfo;
	@Autowired
	private OrganizationTreeService organizationTreeService;
	@Autowired
	private BestSignConfig bestSignConfig;
	@Autowired
	private  CustomProxyDao customProxyDao;
	@Autowired
	private ChannelCustomService customService;
	@Autowired
	private QbInvoiceRecordService qbInvoiceRecordService;
	@Autowired
	private ChannelRelatedService channelRelatedService;

	/**
	 * 商户发票基础信息列表
	 * @param request
	 * @return
	 */
	@RequestMapping("/baseList")
	@ResponseBody
	public Map<String, Object> invoiceBaseList(HttpServletRequest request){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		Page page = new Page(request);
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		if(customLogin.getCustomType()==4&&customLogin.getMasterCustom()!=null){
			ChannelCustom masterChannelCustom = customService.getCustomByCustomkey(customLogin.getMasterCustom());
			if((CommonString.ROOT.equals(masterChannelCustom.getCustomkey()) || (CustomType.ROOT.getCode() == masterChannelCustom.getCustomType() && CommonString.ROOT.equals(masterChannelCustom.getMasterCustom()) && LoginRole.ADMIN_ACCOUNT.getCode() == masterChannelCustom.getLoginRole()))){
				//超管
			}else if(masterChannelCustom.getCustomType() == CustomType.CUSTOM.getCode()){
				//普通商户
				page.getParams().put("loginCustomer", masterChannelCustom.getCustomkey());
			}else if(masterChannelCustom.getCustomType() == CustomType.GROUP.getCode()){
				//集团商户
				int nodeId = organizationTreeService.queryNodeIdByCustomKey(masterChannelCustom.getCustomkey());
				List<String> customKeys = organizationTreeService.queryNodeCusotmKey(masterChannelCustom.getCustomType(), QueryType.QUERY_CURRENT_AND_CHILDREN, nodeId);
				page.getParams().put("loginCustomer", String.join(",", customKeys));
			}else if(masterChannelCustom.getCustomType() == CustomType.COMPANY.getCode()){
				//服务公司
				page.getParams().put("companyId", masterChannelCustom.getCustomkey());
				page.getParams().put("originalIds", masterChannelCustom.getCustomkey());
			}else if(masterChannelCustom.getCustomType()==CustomType.PROXY.getCode()){
				//判断是不是关联性代理商
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
				//超管
			}else if(customLogin.getCustomType() == CustomType.CUSTOM.getCode()){
				//普通商户
				page.getParams().put("loginCustomer", customLogin.getCustomkey());
			}else if(customLogin.getCustomType() == CustomType.GROUP.getCode()){
				//集团商户
				int nodeId = organizationTreeService.queryNodeIdByCustomKey(customLogin.getCustomkey());
				List<String> customKeys = organizationTreeService.queryNodeCusotmKey(customLogin.getCustomType(), QueryType.QUERY_CURRENT_AND_CHILDREN, nodeId);
				page.getParams().put("loginCustomer", String.join(",", customKeys));
			}else if(customLogin.getCustomType() == CustomType.COMPANY.getCode()){
				//服务公司
				page.getParams().put("companyId", customLogin.getCustomkey());
				page.getParams().put("originalIds", customLogin.getCustomkey());
			}else if(customLogin.getCustomType()==CustomType.PROXY.getCode()){
				//判断是不是关联性代理商
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
		int total = qbInvoiceBaseService.queryInvoiceBaseListCount(page);
		List<Map<String, Object>> relationList = qbInvoiceBaseService.queryInvoiceBaseList(page);
		result.put("total", total);
		result.put("relationList", relationList);
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return result;
	}

	/**
	 * 商户发票基础信息列表
	 * @param request
	 * @return
	 */
	@RequestMapping("/invoiceClassInfo")
	@ResponseBody
	public Map<String, Object> invoiceClassInfo(HttpServletRequest request){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		Page page = new Page(request);
		int total = qbInvoiceBaseService.queryInvoiceClassInfoListCount(page);
		List<Map<String, Object>> relationList = qbInvoiceBaseService.queryInvoiceClassInfoList(page);
		result.put("total", total);
		result.put("relationList", relationList);
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return result;
	}


	/**
	 * 添加商户发票基础信息
	 * @param
	 */
	@ResponseBody
	@RequestMapping("/saveinvoiceBase")
	public Map<String, Object> saveinvoiceBase(QbInvoiceBase qbInvoiceBase,HttpServletRequest request,MultipartFile []file){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		try {
			//保存路径
			String uploadPath = "/taxFile/";
			//服务域名
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("customkey", qbInvoiceBase.getCustomkey());
			params.put("invoiceType", qbInvoiceBase.getInvoiceType());
			if(qbInvoiceBase.getInvoiceType()==InvoiceType.DEDICATED_TYPE.getCode()){			
				List<QbInvoiceBase> qListMap = qbInvoiceBaseService.getMerInfoByInvoice(params);
				if(qListMap!=null&&qListMap.size()>0){
					QbInvoiceBase base = qListMap.get(0);
					if(StringUtil.isEmpty(base.getTaxpayerPicUrl())||StringUtil.isEmpty(base.getTaxPicUrl())){
						if(file.length>0){
							int count =1;
							String filePath = uploadPath+qbInvoiceBase.getCustomkey()+"/";
							InputStream in;
							for (MultipartFile mf : file) {
								if(!mf.isEmpty()){
									//使用UUID图片重命名
									String name = UUID.randomUUID().toString().replaceAll("-", "");
									//获取文件扩展名
									String ext = FilenameUtils.getExtension(mf.getOriginalFilename());
									//设置文件上传路径
									String fileName =name+"."+ext;
									in = new ByteArrayInputStream(mf.getBytes());
									String uploadFile = FtpTool.uploadFile(bestSignConfig.getFtpURL(), 21, filePath, fileName, in, bestSignConfig.getUsername(), bestSignConfig.getPassword());

									if (!"error".equals(uploadFile)) {
										if(count==1){
											qbInvoiceBase.setTaxPicUrl(filePath+fileName);
										}else if(count==2){
											qbInvoiceBase.setTaxpayerPicUrl(filePath+fileName);
										}
									}
								}
								count++;
							}
							params.put("taxPicUrl", qbInvoiceBase.getTaxPicUrl());
							params.put("taxpayerPicUrl", qbInvoiceBase.getTaxpayerPicUrl());
							qbInvoiceBaseService.updateTaxPicUrl(params);
						}
					}else{
						qbInvoiceBase.setTaxPicUrl(base.getTaxPicUrl());
						qbInvoiceBase.setTaxpayerPicUrl(base.getTaxpayerPicUrl());
					}
				}else{
					if(file.length>0){
						int count =1;
						String filePath = uploadPath+qbInvoiceBase.getCustomkey()+"/";
						InputStream in;
						for (MultipartFile mf : file) {
							if(!mf.isEmpty()){
								//使用UUID图片重命名
								String name = UUID.randomUUID().toString().replaceAll("-", "");
								//获取文件扩展名
								String ext = FilenameUtils.getExtension(mf.getOriginalFilename());
								//设置文件上传路径
								String fileName =name+"."+ext;
								in = new ByteArrayInputStream(mf.getBytes());
								String uploadFile = FtpTool.uploadFile(bestSignConfig.getFtpURL(), 21, filePath, fileName, in, bestSignConfig.getUsername(), bestSignConfig.getPassword());

								if (!"error".equals(uploadFile)) {
									if(count==1){
										qbInvoiceBase.setTaxPicUrl(filePath+fileName);
									}else if(count==2){
										qbInvoiceBase.setTaxpayerPicUrl(filePath+fileName);
									}
								}
							}
							count++;
						}
					}
				}
			}	
			if(qbInvoiceBase.getId()==null){
				//添加
				//校验是否是超管，下发公司）
				boolean flag = checkCustomType(customLogin);
				if (flag) {
					//是直接为确认状态
					qbInvoiceBase.setStatus(1);	
				}else{
					//否则为待审核状态
					qbInvoiceBase.setStatus(0);
				}
				qbInvoiceBase.setAddUser(customLogin.getUsername());
				qbInvoiceBase.setCreateTime(DateUtils.getNowDate());
				qbInvoiceBaseService.insert(qbInvoiceBase);
				result.put(RespCode.RESP_STAT, respstat);
				result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
			}else{
				//修改
				QbInvoiceBase recordBase = qbInvoiceBaseService.selectByPrimaryKey(qbInvoiceBase.getId());
				if(recordBase.getStatus()!=0){
					//校验是否是超管，下发公司）
					boolean flag = checkCustomType(customLogin);
					if(flag){
						//是直接为确认状态
						recordBase.setReviewUser(customLogin.getUsername());
						recordBase.setStatus(1);
					}else{
						//否则为待审核状态
						recordBase.setAddUser(customLogin.getUsername());
						recordBase.setStatus(0);
					}
				}
				recordBase.setCompanyName(qbInvoiceBase.getCompanyName());
				recordBase.setTaxRegistrationNumber(qbInvoiceBase.getTaxRegistrationNumber());
				recordBase.setTaxpayerType(qbInvoiceBase.getTaxpayerType());
				recordBase.setRemark(qbInvoiceBase.getRemark());
				recordBase.setInvoiceType(qbInvoiceBase.getInvoiceType());
				recordBase.setServiceType(qbInvoiceBase.getServiceType());
				recordBase.setBillingClass(qbInvoiceBase.getBillingClass());
				recordBase.setAccountBankName(qbInvoiceBase.getAccountBankName());
				recordBase.setAccountNo(qbInvoiceBase.getAccountNo());
				recordBase.setAddress(qbInvoiceBase.getAddress());
				recordBase.setPhone(qbInvoiceBase.getPhone());
				recordBase.setUpdateTime(DateUtils.getNowDate());
				qbInvoiceBaseService.updateByPrimaryKey(recordBase);
				result.put(RespCode.RESP_STAT, respstat); 
				result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.put(RespCode.RESP_STAT, RespCode.INVOICE_EXCEPTION);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.INVOICE_EXCEPTION));
		}
		return result;
	}

	/**
	 * 确认商户发票基础信息
	 * @param
	 */
	@ResponseBody
	@RequestMapping("/confirmInvoiceBase")
	public Map<String, Object> confirmInvoiceBase(QbInvoiceBase qbInvoiceBase,HttpServletRequest request){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		//校验是否是超管，下发公司
		boolean flag = checkCustomType(customLogin);
		if (flag) {
			//是超管、下发公司有权限
			try {
				if(qbInvoiceBase.getId()!=null){
					QbInvoiceBase qbInvoiceBaseOld = qbInvoiceBaseService.selectByPrimaryKey(qbInvoiceBase.getId());
					if(qbInvoiceBaseOld!=null){
						qbInvoiceBaseOld.setStatus(1);
						qbInvoiceBaseOld.setUpdateTime(DateUtils.getNowDate());
						qbInvoiceBaseOld.setReviewUser(customLogin.getUsername());
						qbInvoiceBaseService.updateByPrimaryKey(qbInvoiceBaseOld);
						result.put(RespCode.RESP_STAT, respstat);
						result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
					}else{
						result.put(RespCode.RESP_STAT, RespCode.DO_NOT_HAVE_APPROVAL_RIGHT);
						result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.DO_NOT_HAVE_APPROVAL_RIGHT));
					}
				}else{
					result.put(RespCode.RESP_STAT, RespCode.error101);
					result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.error101));
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				result.put(RespCode.RESP_STAT, RespCode.INVOICE_EXCEPTION);
				result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.INVOICE_EXCEPTION));
			}
		}else{
			//无权限
			result.put(RespCode.RESP_STAT, RespCode.DO_NOT_HAVE_APPROVAL_RIGHT);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.DO_NOT_HAVE_APPROVAL_RIGHT));
		}
		return result;
	}

	/**
	 * 驳回商户发票基础信息
	 * @param qbInvoiceBase
	 */
	@ResponseBody
	@RequestMapping("/downInvoiceBase")
	public Map<String, Object> downInvoiceBase(QbInvoiceBase qbInvoiceBase,HttpServletRequest request){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		//校验是否是超管，下发公司
		boolean flag = checkCustomType(customLogin);
		if (flag) {
			//是超管、下发公司有权限
			try {
				if(qbInvoiceBase.getId()!=null){
					QbInvoiceBase qbInvoiceBaseOld = qbInvoiceBaseService.selectByPrimaryKey(qbInvoiceBase.getId());
					if(qbInvoiceBaseOld!=null){
						qbInvoiceBaseOld.setStatus(2);
						qbInvoiceBaseOld.setDownReason(qbInvoiceBase.getDownReason());
						qbInvoiceBaseOld.setUpdateTime(DateUtils.getNowDate());
						qbInvoiceBaseOld.setReviewUser(customLogin.getUsername());
						qbInvoiceBaseService.updateByPrimaryKey(qbInvoiceBaseOld);
						result.put(RespCode.RESP_STAT, respstat);
						result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
					}else{
						result.put(RespCode.RESP_STAT, RespCode.INVOICE_RECORD_NOTEXIST);
						result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.INVOICE_RECORD_NOTEXIST));
					}
				}else{
					result.put(RespCode.RESP_STAT, RespCode.error101);
					result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.error101));
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				result.put(RespCode.RESP_STAT, RespCode.INVOICE_EXCEPTION);
				result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.INVOICE_EXCEPTION));
			}
		}else{
			//无权限
			result.put(RespCode.RESP_STAT, RespCode.DO_NOT_HAVE_APPROVAL_RIGHT);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.DO_NOT_HAVE_APPROVAL_RIGHT));
		}	
		return result;
	}

	/**
	 * 删除商户充值配置信息
	 * @param
	 */
	@ResponseBody
	@RequestMapping("/deleteInvoiceBase")
	public Map<String, Object> deleteInvoiceBase(QbInvoiceBase qbInvoiceBase){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		try {
			if(qbInvoiceBase.getId()!=null){
				//删除
				qbInvoiceBaseService.deleteByPrimaryKey(qbInvoiceBase.getId());
				result.put(RespCode.RESP_STAT, respstat);
				result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
			}else{
				result.put(RespCode.RESP_STAT, RespCode.error101);
				result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.error101));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.put(RespCode.RESP_STAT, RespCode.INVOICE_EXCEPTION);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.INVOICE_EXCEPTION));
		}
		return result;
	}


	/**
	 * 查询具体服务内容
	 * @param serviceTypeId
	 */
	@ResponseBody
	@RequestMapping("/querServiceContent")
	public Map<String, Object> querServiceContent(Integer serviceTypeId){
		int respstat = RespCode.success;
		RespCode.codeMaps.get(respstat);
		HashMap<String, Object> result = new HashMap<>();
		try {
			if(serviceTypeId!=null){
				//根据服务id查询服务内容
				String content = qbInvoiceBaseService.queryServiceContent(serviceTypeId);
				result.put("contentInfo", content);
				result.put(RespCode.RESP_STAT, respstat);
				result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
			}else{
				result.put(RespCode.RESP_STAT, RespCode.error101);
				result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.error101));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.put(RespCode.RESP_STAT, RespCode.INVOICE_EXCEPTION);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.INVOICE_EXCEPTION));
		}
		return result;
	}

	/**
	 * 导出excel
	 */
	@RequestMapping("/export")
	public void export(HttpServletResponse response,HttpServletRequest request){
		// 标题
		String[] headers = new String[] {"商户名称", "服务公司","纳税人类型","开票类型",
				"服务类型","开票类目","开票信息状态","公司名称","税务登记号","开户银行名称","开户账号","地址","电话","备注","驳回原因","操作账号","复核账号","创建时间","更新时间"};
		String filename = "商户发票基础信息"; 
		Page page = new Page(request);
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		if(customLogin.getCustomType()==4&&customLogin.getMasterCustom()!=null){
			ChannelCustom masterChannelCustom = customService.getCustomByCustomkey(customLogin.getMasterCustom());
			if((CommonString.ROOT.equals(masterChannelCustom.getCustomkey()) || (CustomType.ROOT.getCode() == masterChannelCustom.getCustomType() && CommonString.ROOT.equals(masterChannelCustom.getMasterCustom()) && LoginRole.ADMIN_ACCOUNT.getCode() == masterChannelCustom.getLoginRole()))){
				//超管
				headers = new String[] {"商户名称", "服务公司","渠道名称","纳税人类型","开票类型",
						"服务类型","开票类目","开票信息状态","公司名称","税务登记号","开户银行名称","开户账号","地址","电话","备注","驳回原因","操作账号","复核账号","创建时间","更新时间"};
			}else if(masterChannelCustom.getCustomType() == CustomType.CUSTOM.getCode()){
				//普通商户
				page.getParams().put("loginCustomer", masterChannelCustom.getCustomkey());
			}else if(masterChannelCustom.getCustomType() == CustomType.GROUP.getCode()){
				//集团商户
				int nodeId = organizationTreeService.queryNodeIdByCustomKey(masterChannelCustom.getCustomkey());
				List<String> customKeys = organizationTreeService.queryNodeCusotmKey(masterChannelCustom.getCustomType(), QueryType.QUERY_CURRENT_AND_CHILDREN, nodeId);
				page.getParams().put("loginCustomer", String.join(",", customKeys));
			}else if(masterChannelCustom.getCustomType() == CustomType.COMPANY.getCode()){
				//服务公司
				page.getParams().put("companyId", masterChannelCustom.getCustomkey());
				page.getParams().put("originalIds", masterChannelCustom.getCustomkey());
			}else if(masterChannelCustom.getCustomType()==CustomType.PROXY.getCode()){
				//判断是不是关联性代理商
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
				//超管
				headers = new String[] {"商户名称", "服务公司","渠道名称","纳税人类型","开票类型",
						"服务类型","开票类目","开票信息状态","公司名称","税务登记号","开户银行名称","开户账号","地址","电话","备注","驳回原因","操作账号","复核账号","创建时间","更新时间"};
			}else if(customLogin.getCustomType() == CustomType.CUSTOM.getCode()){
				//普通商户
				page.getParams().put("loginCustomer", customLogin.getCustomkey());
			}else if(customLogin.getCustomType() == CustomType.GROUP.getCode()){
				//集团商户
				int nodeId = organizationTreeService.queryNodeIdByCustomKey(customLogin.getCustomkey());
				List<String> customKeys = organizationTreeService.queryNodeCusotmKey(customLogin.getCustomType(), QueryType.QUERY_CURRENT_AND_CHILDREN, nodeId);
				page.getParams().put("loginCustomer", String.join(",", customKeys));
			}else if(customLogin.getCustomType() == CustomType.COMPANY.getCode()){
				//服务公司
				page.getParams().put("companyId", customLogin.getCustomkey());
				page.getParams().put("originalIds", customLogin.getCustomkey());
			}else if(customLogin.getCustomType()==CustomType.PROXY.getCode()){
				//判断是不是关联性代理商
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
		List<Map<String, Object>> relationList = qbInvoiceBaseService.queryInvoiceBaseListNoPage(page);
		List<Map<String, Object>> data = new ArrayList<>();
		for (Map<String, Object> invioceBase : relationList) {
			Map<String, Object> dataMap = new HashMap<>(20);
			dataMap.put("1", invioceBase.get("merchantName"));
			dataMap.put("2", invioceBase.get("serviceName"));
			Integer []allowCustomType = new Integer[]{6};
			boolean checkFlag = customService.getCustomKeysByType(page.getParams(), allowCustomType, customLogin);
			//1.操作权限校验
			if(checkFlag){
				dataMap.put("3", invioceBase.get("agentName"));
				dataMap.put("4", TaxpayerType.codeOf(Integer.parseInt(String.valueOf(invioceBase.get("taxpayerType")))).getDesc());
				dataMap.put("5", InvoiceType.codeOf(Integer.parseInt(String.valueOf(invioceBase.get("invoiceType")))).getDesc());
				dataMap.put("6", invioceBase.get("serviceTypeName"));
				dataMap.put("7",invioceBase.get("billingClassName"));
				dataMap.put("8",InvoiceStatus.codeOf(Integer.parseInt(String.valueOf(invioceBase.get("status")))).getDesc());
				dataMap.put("9",invioceBase.get("companyName"));
				dataMap.put("10",invioceBase.get("taxRegistrationNumber"));
				dataMap.put("11",invioceBase.get("accountBankName"));
				dataMap.put("12",invioceBase.get("accountNo"));
				dataMap.put("13",invioceBase.get("address"));
				dataMap.put("14",invioceBase.get("phone"));
				dataMap.put("15",invioceBase.get("remark"));
				dataMap.put("16",invioceBase.get("downReason"));
				dataMap.put("17",invioceBase.get("addUser"));
				dataMap.put("18",invioceBase.get("reviewUser"));
				dataMap.put("19",invioceBase.get("createTime"));
				dataMap.put("20",invioceBase.get("updateTime"));
			}else{
				dataMap.put("3", TaxpayerType.codeOf(Integer.parseInt(String.valueOf(invioceBase.get("taxpayerType")))).getDesc());
				dataMap.put("4", InvoiceType.codeOf(Integer.parseInt(String.valueOf(invioceBase.get("invoiceType")))).getDesc());
				dataMap.put("5", invioceBase.get("serviceTypeName"));
				dataMap.put("6",invioceBase.get("billingClassName"));
				dataMap.put("7",InvoiceStatus.codeOf(Integer.parseInt(String.valueOf(invioceBase.get("status")))).getDesc());
				dataMap.put("8",invioceBase.get("companyName"));
				dataMap.put("9",invioceBase.get("taxRegistrationNumber"));
				dataMap.put("10",invioceBase.get("accountBankName"));
				dataMap.put("11",invioceBase.get("accountNo"));
				dataMap.put("12",invioceBase.get("address"));
				dataMap.put("13",invioceBase.get("phone"));
				dataMap.put("14",invioceBase.get("remark"));
				dataMap.put("15",invioceBase.get("downReason"));
				dataMap.put("16",invioceBase.get("addUser"));
				dataMap.put("17",invioceBase.get("reviewUser"));
				dataMap.put("18",invioceBase.get("createTime"));
				dataMap.put("19",invioceBase.get("updateTime"));
			}

			data.add(sortMapByKey(dataMap));
		}
		ExcelFileGenerator.ExcelExport(response, headers, filename, data);
	}

	/**
	 * 回显商户发票基本配置信息
	 */
	@RequestMapping("/getCustomerInvoice")
	@ResponseBody
	public Map<String, Object> getCustomerInvoice(String customkey){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		ChannelCustom channelCustom = customService.getCustomByCustomkey(customkey);
		QbInvoiceBase qbInvoiceBase = new QbInvoiceBase();
		//获取商户基础信息进行回显
		if(channelCustom!=null){
			qbInvoiceBase.setAccountBankName(channelCustom.getBankname());
			qbInvoiceBase.setAccountNo(channelCustom.getBankcardno());
			qbInvoiceBase.setAddress(channelCustom.getAddress());
			qbInvoiceBase.setPhone(channelCustom.getCompanyPhone());
			qbInvoiceBase.setTaxpayerType(channelCustom.getTaxpayerType());
			qbInvoiceBase.setInvoiceType(channelCustom.getInvoiceType());
			qbInvoiceBase.setTaxRegistrationNumber(channelCustom.getInvoiceNo());
			qbInvoiceBase.setCompanyName(channelCustom.getCompanyName());
		}
		result.put("qbInvoiceBase", qbInvoiceBase);			
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return result;

	}

	/**
	 * 回显商户发票基础信息
	 * @param customkey
	 * @param invoiceType
	 * @return
	 */
	@RequestMapping("/showHaveMerInfo")
	@ResponseBody
	public Map<String, Object> showHaveMerInfo(String customkey,Integer invoiceType){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		int isShowMer=0;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("customkey", customkey);
		params.put("invoiceType", invoiceType);
		//获取商户发票基础信息
		List<QbInvoiceBase> qListMap = qbInvoiceBaseService.getMerInfoByInvoice(params);
		if(qListMap==null||qListMap.size()==0){
			if(InvoiceType.GENERAL_TYPE.getCode()==invoiceType){
				params.put("invoiceType", InvoiceType.DEDICATED_TYPE.getCode());
			}else{
				params.put("invoiceType", InvoiceType.GENERAL_TYPE.getCode());
			}
			//获取商户基础信息
			qListMap = qbInvoiceBaseService.getMerInfoByInvoice(params);
			if(qListMap==null||qListMap.size()==0){
				//不存在发票基础信息
				result.put("isShowMer", isShowMer);
			}else{
				//使用商户基础信息进行回显
				QbInvoiceBase qbInvoiceBase=qListMap.get(0);
				ChannelCustom channelCustom = customService.getCustomByCustomkey(customkey);
				result.put("isShowMer", 1);
				result.put("accountNo", StringUtil.isEmpty(channelCustom.getBankcardno())?qbInvoiceBase.getAccountNo():channelCustom.getBankcardno());
				result.put("accountBankName", StringUtil.isEmpty(channelCustom.getBankname())?qbInvoiceBase.getAccountBankName():channelCustom.getBankname());
				result.put("address", StringUtil.isEmpty(channelCustom.getAddress())?qbInvoiceBase.getAddress():channelCustom.getAddress());
				result.put("taxPicUrl", qbInvoiceBase.getTaxPicUrl());
				result.put("taxpayerPicUrl", qbInvoiceBase.getTaxpayerPicUrl());
			}
		}else{
			//使用发票基础信息进行回显
			QbInvoiceBase qbInvoiceBase=qListMap.get(0);
			ChannelCustom channelCustom = customService.getCustomByCustomkey(customkey);
			result.put("isShowMer", 1);
			result.put("accountNo", StringUtil.isEmpty(channelCustom.getBankcardno())?qbInvoiceBase.getAccountNo():channelCustom.getBankcardno());
			result.put("accountBankName", StringUtil.isEmpty(channelCustom.getBankname())?qbInvoiceBase.getAccountBankName():channelCustom.getBankname());
			result.put("address", StringUtil.isEmpty(channelCustom.getAddress())?qbInvoiceBase.getAddress():channelCustom.getAddress());
			result.put("taxPicUrl", qbInvoiceBase.getTaxPicUrl());
			result.put("taxpayerPicUrl", qbInvoiceBase.getTaxpayerPicUrl());
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return result;
	}

	/**
	 * 查询商户已开票记录的开票基础信息
	 * @param request
	 * @return
	 */
	@RequestMapping("/getMerInvoiceBaseByRecord")
	@ResponseBody
	public Map<String, Object> getMerInvoiceBaseByRecord(HttpServletRequest request){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		Page page = new Page(request);
		int total = qbInvoiceBaseService.getMerInvoiceBaseByRecordCount(page);
		List<Map<String, Object>> relationList = qbInvoiceBaseService.getMerInvoiceBaseByRecord(page);
		result.put("total", total);
		result.put("relationList", relationList);
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return result;
	}

	/**
	 * 获取商户最近一次开票记录默认地址
	 * @param customkey
	 * @return
	 */
	@RequestMapping("/getAddressByRecord")
	@ResponseBody
	public Map<String, Object> getAddressByRecord(String customkey){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		Map<String, Object> customInvoiceInfo = new HashMap<String, Object>();
		//获取最近一次发票记录使用的邮寄地址
		String receiveUser = qbInvoiceRecordService.getRecentAddress(customkey);
		if(!StringUtil.isEmpty(receiveUser)){
			String []receiveUserArray = receiveUser.split(",");
			//用户名
			String userName = receiveUserArray[0];
			//地址
			String address = receiveUserArray[1];
			//联系手机
			String phone = receiveUserArray[2];
			customInvoiceInfo.put("userName", userName);
			customInvoiceInfo.put("address", address);
			customInvoiceInfo.put("phone", phone);
			result.put("isTrue", true);
			result.put("invoiceInfo", customInvoiceInfo);
		}else{
			//不存在开票记录
			result.put("isTrue", false);
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return result;
	}
	
	/**
	 * 校验申请发票角色类型是否是超管和下发公司
	 * @param customLogin
	 * @return
	 */
	private boolean checkCustomType(ChannelCustom customLogin) {
		boolean flag = false;
		if(customLogin.getCustomType()==4&&customLogin.getMasterCustom()!=null){
			//操作账号登陆
			//通过操作账号获取主账号角色身份
			ChannelCustom masterChannelCustom = customService.getCustomByCustomkey(customLogin.getMasterCustom());
			if ((CommonString.ROOT.equals(masterChannelCustom.getCustomkey()) || (CustomType.ROOT.getCode() == masterChannelCustom.getCustomType() && CommonString.ROOT.equals(masterChannelCustom.getMasterCustom()) && LoginRole.ADMIN_ACCOUNT.getCode() == masterChannelCustom.getLoginRole()))) {
				//超管
				flag=true;
			}
			if(masterChannelCustom.getCustomType()==2){
				//下发公司
				flag=true;
			}
		}else{
			//主账号登陆
			if ((CommonString.ROOT.equals(customLogin.getCustomkey()) || (CustomType.ROOT.getCode() == customLogin.getCustomType() && CommonString.ROOT.equals(customLogin.getMasterCustom()) && LoginRole.ADMIN_ACCOUNT.getCode() == customLogin.getLoginRole()))) {
				//超管
				flag=true;
			}
			if(customLogin.getCustomType()==2){
				//下发公司
				flag=true;
			}
		}
		return flag;
	}

}
