package com.jrmf.controller.systemrole.merchant.user;

import com.jrmf.controller.BaseController;
import com.jrmf.domain.*;
import com.jrmf.service.*;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.*;

@Controller
@RequestMapping("/merchant/user")
public class MerchantUserController extends BaseController {

	private static Logger logger = LoggerFactory.getLogger(MerchantUserController.class);
	@Autowired
	protected UserSerivce userSerivce;
	@Autowired
	private UserCommissionService commissionService;
	@Autowired
	private TransferBankService transferBankService;
	@Autowired
	private UserRelatedService userRelatedService;
	@Autowired
	private ChannelRelatedService channelRelatedService;
	
	/**
	 * 用户列表
	 * @param response
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/user/listData", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> listData(HttpServletResponse response,HttpServletRequest request){
		int respstat = RespCode.success;
		String originalId = (String) request.getSession().getAttribute(
				"customkey");//商户标识
		Map<String, Object> model = new HashMap<String, Object>();
		String userType = (String) request.getParameter("userType");//用户类型，1普通 ， 2商户，  0禁用， -1  待激活  11  补全信息  ,-2待激活商户  
		String startTime = (String) request.getParameter("startTime");
		String endTime = (String) request.getParameter("endTime");
		String userName = (String) request.getParameter("userName");
		String pageNo = (String) request.getParameter("pageNo");
		String status = (String) request.getParameter("status");
		logger.info("userList方法  传参：startTime="+startTime+" endTime="+endTime+"customkey="+originalId+"pageNo="+pageNo);
		if(StringUtil.isEmpty(originalId) || StringUtil.isEmpty(pageNo)){
			respstat = RespCode.error101;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "请求参数不全");
			return model;
		}else{
			try {
				Map<String,Object> paramMap= new HashMap<String,Object>();
				paramMap.put("userType",userType);
				paramMap.put("startTime",startTime);
				paramMap.put("endTime",endTime);
				paramMap.put("userName",userName);
				paramMap.put("originalId",originalId);
				paramMap.put("status",status);
				
				int total = userSerivce.getUserRelatedCountByParam(paramMap);
				model.put("total", total);
				int pageSize = 10;
				if(!StringUtil.isEmpty(pageNo)){
					paramMap.put("start",(Integer.parseInt(pageNo)-1)*pageSize);
					paramMap.put("limit",pageSize);
				}
				List<User> userList = userSerivce.getUserRelatedByParam(paramMap);
				model.put("userList", userList);
			} catch (Exception e) {
				respstat = RespCode.error107;
				model.put(RespCode.RESP_STAT, respstat);
				model.put(RespCode.RESP_MSG, "接口查询失败，请联系管理员！");
				logger.error("", e);
				return model;
			}
		}
		model.put(RespCode.RESP_STAT, respstat);
		model.put(RespCode.RESP_MSG, "成功");
		logger.info("返回结果：" + model);
		return model;
	}
	
	
	/**
	 * 导入开户信息
	 * @param fileName
	 */
	@RequestMapping(value = "/openAccount/inputData", method = RequestMethod.POST)
	public @ResponseBody Map<String,Object> inputData(HttpServletRequest request,
			HttpServletResponse response,String name,MultipartFile file) throws Exception {
		int respstat = RespCode.success;
		Map<String, Object> model = new HashMap<>(5);
		//商户标识
		String originalId = (String) request.getSession().getAttribute("customkey");
		String companyId = request.getParameter("companyId");
		int passNum = 0; 
		int batchNum = 0 ; 
		int errorNum = 0;
		String batcheId = StringUtil.formatDate(new Date(),"yyyyMMddHHmmss");
		model.put("batchId", batcheId);
        try {
			if(file!=null){
	        	InputStream is = file.getInputStream();
				Workbook workbook = null;  
		        try {
		        	workbook = new XSSFWorkbook(is);
		        } catch (Exception ex) {
		        	workbook = new HSSFWorkbook(is);
		        }	
	        	Sheet sheet = workbook.getSheetAt(0); 
	        	/**
        		 * 取默认配置信息
        		 */
        		ChannelRelated related = channelRelatedService.getRelatedByCompAndOrig(originalId,companyId);
        		if(related==null){
        			logger.info("返回结果：" + model);
        			respstat = RespCode.error107;
        			model.put(RespCode.RESP_STAT, respstat);
        			model.put(RespCode.RESP_MSG, "请联系管理员配置薪资服务公司配置信息！");
    		        return model;
        		}
        		/**
				 * 验证模板正确性
				 */
				XSSFRow title = (XSSFRow)sheet.getRow(0);
				if( title != null){
					String titleName = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(title.getCell(0)));
					if(!titleName.contains("徽商银行")){
						respstat = RespCode.error107;
						model.put(RespCode.RESP_STAT, respstat);
						model.put(RespCode.RESP_MSG, "请导入正确的徽商银行下发模板！！");
						return model;
					}
				}
				/**
				 * 验证数据amount,idCard, bankCard, mobileNo, name
				 */
        		int num = 0;
				Set<String> result = new HashSet<String>();
				for (int j = 2; j < sheet.getPhysicalNumberOfRows(); j++) {// 获取每行
					XSSFRow row = (XSSFRow) sheet.getRow(j);
					if(row==null){
	        			continue;
	        		}
					if(StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(0))) &&
							StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(1))) &&
							StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(2)))){
	        			continue;
	        		}
					++num; 
					if(num>2000 || num<0){
						respstat = RespCode.error107;
						model.put(RespCode.RESP_STAT, respstat);
						model.put(RespCode.RESP_MSG, "导入数据不得大于2000条或小于0条，请重新导入！");
						return model;
					}
					String msg = commissionService.isValidateData(null, 
							StringUtil.getXSSFCell(row.getCell(1)), 
							StringUtil.getXSSFCell(row.getCell(2)), 
							StringUtil.getXSSFCell(row.getCell(3)), 
							StringUtil.getXSSFCell(row.getCell(0)));
					String userName = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(0)));// 收款人真实姓名(必要)
					String cardNo = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(1)));//身份证号码
					if(StringUtil.isEmpty(userName+cardNo)){
						continue;
					}
					if(!result.add(userName+cardNo)){
						respstat = RespCode.error107;
						model.put(RespCode.RESP_STAT, respstat);
						model.put(RespCode.RESP_MSG,userName +":数据重复，请修改后再上传！");
						return model;
					}
					if(!StringUtil.isEmpty(msg)){
						respstat = RespCode.error107;
						model.put(RespCode.RESP_STAT, respstat);
						model.put(RespCode.RESP_MSG,userName +":"+ msg);
						return model;
					}
				}
	        	for (int j = 2; j < sheet.getPhysicalNumberOfRows(); j++) {//获取每行
	        		XSSFRow row = (XSSFRow) sheet.getRow(j);
	        		if(row==null){
	        			break;
	        		}
	        		if(StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(0))) &&
							StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(1))) &&
							StringUtil.isEmpty(StringUtil.getXSSFCell(row.getCell(2)))){
	        			continue;
	        		}
	        		batchNum++;
	        		String userName = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(0)));//姓名
	        		String cardNo = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(1)));//身份证号码
	        		String bankcardNo = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(2)));//银行卡号码
	        		String mobile = ArithmeticUtil.subZeroAndDot(StringUtil.getXSSFCell(row.getCell(3)));//手机号
	        		String userNo = ArithmeticUtil.subZeroAndDot(row.getCell(4)+"");//渠道用户id
	        		if(StringUtil.isEmpty(userNo)){
	        			userNo = mobile;
	        		}
	        		/**
	        		 * 增加校验：  四要素不能为空
	        		 */
	        		if(StringUtil.isEmpty(cardNo) && StringUtil.isEmpty(userName)
	        				&& StringUtil.isEmpty(bankcardNo) && StringUtil.isEmpty(mobile)){
	        			createErrorUser(cardNo, userName, mobile, userNo, related.getMerchantId(), 
	        					related.getCompanyId(), "四要素不完整",batcheId,bankcardNo,"");
	        			errorNum++;
	        			continue;
	        		}
	        		/**
	        		 * 空格剔除
	        		 */
	        		userName = userName.replace(" ", "");
	        		cardNo = cardNo.replace(" ", "");
	        		bankcardNo = bankcardNo.replace(" ", "");
	        		mobile = mobile.replace(" ", "");
	        		
	        		/**
	        		 * 校验银行卡是否支持
	        		 */
	        		BankCard bankInfo = transferBankService.getBankInfo(bankcardNo);
	        		if(bankInfo==null){
	        			createErrorUser(cardNo, userName, mobile, userNo, related.getMerchantId(),
	        					related.getCompanyId(), "不支持该银行卡",batcheId,bankcardNo,"");
	        			errorNum++;
	        			continue;
	        		}
	        		
	        		/**
	        		 *  用户手机号信息在该商户下是否存在
	        		 */
	        		Map<String,Object> paramMap= new HashMap<String,Object>();
	        		paramMap.put("merchantId",related.getMerchantId());
					paramMap.put("mobilePhone",mobile);
					paramMap.put("userType",1);
					paramMap.put("originalId",originalId);
					int size = userSerivce.getUsersCountByParam(paramMap);
					if(size>0){
						createErrorUser(cardNo, userName, mobile, userNo, related.getMerchantId(),
	        					related.getCompanyId(), "该手机号用户已经创建银行电子户",batcheId,bankcardNo,bankInfo.getBankNo()+"");
						errorNum++;
						continue;
					}
					/**
					 * 用户身份证和姓名信息在该商户下是否开户
					 */
					paramMap.clear();
					paramMap.put("merchantId",related.getMerchantId());
					paramMap.put("certId",cardNo);
					paramMap.put("userName",userName);
					paramMap.put("userType",1);
					paramMap.put("originalId",originalId);
					int size1 = userSerivce.getUsersCountByParam(paramMap);
					if(size1>0){
						createErrorUser(cardNo, userName, mobile, userNo, related.getMerchantId(),
	        					related.getCompanyId(), "该用户已开通银行电子户",batcheId,bankcardNo,bankInfo.getBankNo()+"");
						errorNum++;
						continue;
					}
					passNum++;
					/**
					 * 情况1：用户信息存在于其他商户下
					 */
					paramMap.clear();
					paramMap.put("merchantId",related.getMerchantId());
					paramMap.put("certId",cardNo);
					paramMap.put("userName",userName);
					paramMap.put("userType",1);
					int size2 = userSerivce.getUsersCountByParam(paramMap);
					if(size2>0){
						paramMap.clear();
						paramMap.put("userName",userName);
						paramMap.put("certId",cardNo);
						User oldUser = userSerivce.getUsersCountByCard(paramMap);
						UserRelated oldRelated = userRelatedService.getRelatedByUserId(oldUser.getId()+"", originalId);
						if(oldRelated==null){
							/**
			        		 * 插入商户-用户关联关系
			        		 */
			        		UserRelated userRelated = new UserRelated();
			        		userRelated.setStatus(0);//初始状态：未开户
			        		userRelated.setCreateTime(DateUtils.getNowDate());
			        		userRelated.setOriginalId(originalId);
			        		userRelated.setUserNo(userNo);
			        		userRelated.setUserId(oldUser.getId());
			        		userRelated.setCompanyId(related.getCompanyId());
			        		userRelatedService.createUserRelated(userRelated);
						}
						continue;
					}
					/**
					 * 情况2：用户信息处于（导入信息且待开户状态、错误信息等状态，直接保存）
					 */
					createSuccessUser(originalId, cardNo, userName, mobile, userNo,
							related.getMerchantId(), related.getCompanyId(), "", batcheId, bankcardNo, bankInfo.getBankNo()+"");
					continue;
					
	        		
	        	}
	        	UserBatch batch = new UserBatch();
	        	batch.setBatchId(batcheId);
	        	batch.setBatchNum(batchNum);
	        	batch.setCustomkey(originalId);
	        	batch.setPassNum(passNum);
	        	batch.setErrorNum(errorNum);
	        	userSerivce.addUserBatch(batch);
	         }
		} catch (Exception e) {
			logger.error("", e);
			logger.error(e.getMessage());
			respstat = RespCode.error107;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "接口查询失败，请联系管理员！");
			logger.error("", e);
			return model;
		}
		model.put(RespCode.RESP_STAT, respstat);
		model.put(RespCode.RESP_MSG, "成功");
	    logger.info("返回结果：" + model);
        return model;
	}
	
	/**
	 * 用户批次信息
	 * @param response
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/openAccount/batchData", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> openAccountBatchData(HttpServletResponse response,HttpServletRequest request){
		int respstat = RespCode.success;
		String originalId = (String) request.getSession().getAttribute(
				"customkey");//商户标识
		Map<String, Object> model = new HashMap<String, Object>();
		String batchId = (String) request.getParameter("batchId");
		if(StringUtil.isEmpty(originalId) || StringUtil.isEmpty(batchId)){
			respstat = RespCode.error101;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "请求参数不全");
			return model;
		}else{
			try {
				UserBatch batch = userSerivce.getUserBatchByBatchId(batchId);
				model.put("batch", batch);
			} catch (Exception e) {
				respstat = RespCode.error107;
				model.put(RespCode.RESP_STAT, respstat);
				model.put(RespCode.RESP_MSG, "接口查询失败，请联系管理员！");
				logger.error("", e);
				return model;
			}
		}
		model.put(RespCode.RESP_STAT, respstat);
		model.put(RespCode.RESP_MSG, "成功");
		logger.info("返回结果：" + model);
		return model;
	}
	
	/**
	 * 开户批次列表
	 * @param response
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/openAccount/batchList", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> openAccountBatchList(HttpServletResponse response,HttpServletRequest request){
		int respstat = RespCode.success;
		String originalId = (String) request.getSession().getAttribute(
				"customkey");//商户标识
		Map<String, Object> model = new HashMap<String, Object>();
		String startTime = (String) request.getParameter("startTime");
		String endTime = (String) request.getParameter("endTime");
		String name = (String) request.getParameter("name");
		String pageNo = (String) request.getParameter("pageNo");
		if(StringUtil.isEmpty(originalId) || StringUtil.isEmpty(pageNo)){
			respstat = RespCode.error101;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "请求参数不全");
			return model;
		}else{
			try {
				Map<String,Object> paramMap= new HashMap<String,Object>();
				paramMap.put("startTime",startTime);
				paramMap.put("endTime",endTime);
				paramMap.put("name",name);
				paramMap.put("customkey",originalId);
				int total = userSerivce.getUserBatchByParam(paramMap).size();
				model.put("total", total);
				int pageSize = 10;
				if(!StringUtil.isEmpty(pageNo)){
					paramMap.put("start",(Integer.parseInt(pageNo)-1)*pageSize);
					paramMap.put("limit",pageSize);
				}
				List<UserBatch> list = userSerivce.getUserBatchByParam(paramMap);
				model.put("list", list);
			} catch (Exception e) {
				respstat = RespCode.error107;
				model.put(RespCode.RESP_STAT, respstat);
				model.put(RespCode.RESP_MSG, "接口查询失败，请联系管理员！");
				logger.error("", e);
				return model;
			}
		}
		model.put(RespCode.RESP_STAT, respstat);
		model.put(RespCode.RESP_MSG, "成功");
		logger.info("返回结果：" + model);
		return model;
	}
	
	/**
	 * 开户批次明细
	 * @param response
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/openAccount/userList", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> companyUserList(HttpServletResponse response,HttpServletRequest request){
		int respstat = RespCode.success;
		String originalId = (String) request.getSession().getAttribute(
				"customkey");//商户标识
		Map<String, Object> model = new HashMap<String, Object>();
		String userName = (String) request.getParameter("userName");
		String userType = (String) request.getParameter("userType");
		String batcheId = (String) request.getParameter("batcheId");
		String pageNo = (String) request.getParameter("pageNo");
		if(StringUtil.isEmpty(originalId) || StringUtil.isEmpty(pageNo)
				|| StringUtil.isEmpty(batcheId)){
			respstat = RespCode.error101;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "请求参数不全");
			return model;
		}else{
			try {
				Map<String,Object> paramMap= new HashMap<String,Object>();
				paramMap.put("userType",userType);
				paramMap.put("batcheId",batcheId);
				paramMap.put("userName",userName);
				int total = userSerivce.getUserRelatedCountByParam(paramMap);
				model.put("total", total);
				int pageSize = 10;
				if(!StringUtil.isEmpty(pageNo)){
					paramMap.put("start",(Integer.parseInt(pageNo)-1)*pageSize);
					paramMap.put("limit",pageSize);
				}
				List<User> userList = userSerivce.getUserRelatedByParam(paramMap);
				model.put("userList", userList);
			} catch (Exception e) {
				respstat = RespCode.error107;
				model.put(RespCode.RESP_STAT, respstat);
				model.put(RespCode.RESP_MSG, "接口查询失败，请联系管理员！");
				logger.error("", e);
				return model;
			}
		}
		model.put(RespCode.RESP_STAT, respstat);
		model.put(RespCode.RESP_MSG, "成功");
		logger.info("返回结果：" + model);
		return model;
	}
	
	
	/**
	 * 删除单个用户信息
	 * @param response
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/openAccount/deleteUser", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> deleteUser(HttpServletResponse response,HttpServletRequest request){
		int respstat = RespCode.success;
		Map<String, Object> model = new HashMap<String, Object>();
		String customkey = (String) request.getSession().getAttribute("customkey");//商户标识
		String ids = (String) request.getParameter("ids");
		String batchId = (String) request.getParameter("batchId");
		logger.info("deleteUser方法  传参： ids=" + ids+"customkey="+customkey);
		if(StringUtil.isEmpty(customkey) || StringUtil.isEmpty(ids)){
			respstat = RespCode.error101;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "请求参数不全");
			return model;
		}else{
			try {
				User user = userSerivce.getUserByUserId(Integer.parseInt(ids));
				userSerivce.deleteByIds(ids);
				transferBankService.deleteByUserIds(ids);
				userRelatedService.deleteByOriginalId(ids, customkey);
				UserBatch userBatch = userSerivce.getUserBatchByBatchId(batchId);
				int passnum = userBatch.getPassNum();
				int errornum = userBatch.getErrorNum();
				int batchnum = userBatch.getBatchNum();
				if(user.getUserType()==11){
					passnum = passnum - 1;
				}else if(user.getUserType()==0){
					errornum = errornum - 1;
				}else if(user.getUserType()==1){
					passnum = passnum - 1;
				}
				batchnum = batchnum -1;
				userSerivce.updateUserBatch(batchId, passnum, batchnum, errornum);
			} catch (Exception e) {
				respstat = RespCode.error107;
				model.put(RespCode.RESP_STAT, respstat);
				model.put(RespCode.RESP_MSG, "接口执行失败，请联系管理员！");
				logger.error("", e);
				return model;
			}
		}
		model.put(RespCode.RESP_STAT, respstat);
		model.put(RespCode.RESP_MSG, "成功");
		logger.info("返回结果：" + model);
		return model;
	}
	
	/**
	 * 删除用户批次信息
	 * @param response
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/openAccount/deleteBatch", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> deleteBatch(HttpServletResponse response,HttpServletRequest request){
		int respstat = RespCode.success;
		Map<String, Object> model = new HashMap<String, Object>();
		String customkey = (String) request.getSession().getAttribute("customkey");//商户标识
		String batchId = (String) request.getParameter("batchId");
		if(StringUtil.isEmpty(customkey) || StringUtil.isEmpty(batchId)){
			respstat = RespCode.error101;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "请求参数不全");
			return model;
		}else{
			try {
				userSerivce.deleteByBatcheId(batchId, customkey);
				transferBankService.deleteByBatcheId(batchId, customkey);
				userRelatedService.deleteByBatchId(batchId, customkey);
				userSerivce.deleteUserBatch(batchId);
			} catch (Exception e) {
				respstat = RespCode.error107;
				model.put(RespCode.RESP_STAT, respstat);
				model.put(RespCode.RESP_MSG, "接口执行失败，请联系管理员！");
				logger.error("", e);
				return model;
			}
		}
		model.put(RespCode.RESP_STAT, respstat);
		model.put(RespCode.RESP_MSG, "成功");
		logger.info("返回结果：" + model);
		return model;
	}
	
	
	private void createErrorUser(String cardNo,String userName,String mobile,String userNo
			,String merchantId,String companyId, String remark,String batcheId, String bankcardNo, String bankNo){
		User user = new User();
		user.setCertId(cardNo);
		user.setUserName(userName);
		user.setMobilePhone(mobile);
		user.setMerchantId(merchantId);
		user.setUserType(0);//补全信息
		user.setUserNo(userNo);
		user.setCompanyUserNo(companyId);
		user.setRemark(remark);
		user.setBatcheId(batcheId);
		userSerivce.addUser(user);
		
		/**
		 *  记录用户银行卡信息
		 */
		TransferBank transferBank = new TransferBank();
		transferBank.setBankNo(bankNo);
		transferBank.setBankCardPhoneNo(mobile);
		transferBank.setUser_id(user.getId()+"");
		transferBank.setBankCardNo(bankcardNo);
		transferBank.setTransferType("2");
		transferBank.setStatus(1);
		transferBankService.addTransferBank(transferBank);
	}
	
	private int createSuccessUser(String originalId,String cardNo,String userName,String mobile,String userNo
			,String merchantId,String companyId, String remark,String batcheId, String bankcardNo, String bankNo){
		/**
		 *  记录用户原始信息
		 */
		User user = new User();
		user.setCertId(cardNo);
		user.setUserName(userName);
		user.setMobilePhone(mobile);
		user.setMerchantId(merchantId);
		user.setUserType(11);//补全信息
		user.setUserNo(userNo);
		user.setCompanyUserNo(companyId);
		user.setBatcheId(batcheId);
		user.setAccount(bankcardNo);
		userSerivce.addUser(user);
		int userid = user.getId();
		/**
		 *  记录用户银行卡信息
		 */
		TransferBank transferBank = new TransferBank();
		transferBank.setBankNo(bankNo);
		transferBank.setStatus(1);
		transferBank.setBankCardPhoneNo(mobile);
		transferBank.setUser_id(user.getId()+"");
		transferBank.setBankCardNo(bankcardNo);
		transferBank.setTransferType("2");
		transferBankService.addTransferBank(transferBank);
		
		/**
		 * 插入用户中间表关联
		 */
		UserRelated userRelated = new UserRelated();
		userRelated.setStatus(0);//初始状态：未开户
		userRelated.setCreateTime(DateUtils.getNowDate());
		userRelated.setOriginalId(originalId);
		userRelated.setUserNo(userNo);
		userRelated.setUserId(userid);
		userRelated.setCompanyId(companyId);
		userRelatedService.createUserRelated(userRelated);
		return userid;
	}
	
}
