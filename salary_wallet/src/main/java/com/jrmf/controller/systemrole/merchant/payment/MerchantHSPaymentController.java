package com.jrmf.controller.systemrole.merchant.payment;

import com.jrmf.controller.BaseController;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.ChannelRelated;
import com.jrmf.service.ChannelRelatedService;
import com.jrmf.service.CustomInfoService;
import com.jrmf.service.UserCommissionService;
import com.jrmf.service.UserSerivce;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
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
import java.util.HashMap;
import java.util.Map;
/**
 * 
 * @author 张泽辉
 * @version v1.0
 *
 */
@Controller
@RequestMapping("/merchant/hsPay")
public class MerchantHSPaymentController extends BaseController {

	private static Logger logger = LoggerFactory.getLogger(MerchantHSPaymentController.class);
	@Autowired
	protected UserSerivce userSerivce;
	@Autowired
	protected CustomInfoService customInfoService;
	@Autowired
	private UserCommissionService commissionService;
	@Autowired
	private ChannelRelatedService channelRelatedService;

	
	/**
	 * 导入佣金信息
	 */
	@RequestMapping(value = "/hsBank/inputCommission", method = RequestMethod.POST)
	public @ResponseBody Map<String,Object> inputCommission(HttpServletRequest request,
			HttpServletResponse response,String name,MultipartFile file) throws Exception {
		int respstat = RespCode.success;
		Map<String, Object> model = new HashMap<String, Object>();
		String customkey = (String) request.getSession().getAttribute("customkey");//商户标识
		ChannelCustom loginUser = (ChannelCustom) request.getSession()
				.getAttribute("customLogin");
		String operatorName = loginUser.getUsername();//操作人
		if(StringUtil.isEmpty(customkey)){
			respstat = RespCode.error101;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "请求参数不全");
			return model;
		}
        try {
			if(file!=null){

	        	InputStream is = file.getInputStream();
				Workbook workbook = null;  
		        try {
		        	workbook = new XSSFWorkbook(is);
		        } catch (Exception ex) {
		        	workbook = new HSSFWorkbook(is);
		        }	
		        model = commissionService.inputHsBankCommissionDate(workbook, customkey, operatorName);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			logger.error(e.getMessage());
			respstat = RespCode.error107;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "接口查询失败");
			return model;
		}
        model.put(RespCode.RESP_STAT, respstat);
	    model.put(RespCode.RESP_MSG, "成功");
        logger.info("返回结果：" + model);
        return model;
	}
	

	/**
	 * 说明: 导入银企直联资金下发批次信息
	 * 
	 * @param request
	 * @param response
	 * @param file
	 * @return:
	 */
	@RequestMapping("/banbkPay/inputBatchInfo")
	@ResponseBody
	public Map<String, Object> inputBatchInfo(HttpServletRequest request, HttpServletResponse response,
			MultipartFile file) {
		int respstat = RespCode.success;
		Map<String, Object> model = new HashMap<String, Object>();
		String customkey = (String) request.getSession().getAttribute("customkey");// 商户标识
		String menuId = (String) request.getParameter("menuId");//项目id
		String companyId = (String) request.getParameter("companyId");//项目id
		String batchName = (String) request.getParameter("batchName");//批次名称
		String batchDesc = (String) request.getParameter("batchDesc");//批次说明
		ChannelRelated channelRelated = channelRelatedService.getRelatedByCompAndOrig(customkey,companyId);
		ChannelCustom loginUser = (ChannelCustom) request.getSession()
				.getAttribute("customLogin");
		String operatorName = loginUser.getUsername();//操作人
		if (StringUtil.isEmpty(customkey) || channelRelated == null) {
			respstat = RespCode.error101;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "登陆超时！");
			return model;
		}
		if (StringUtil.isEmpty(menuId) && StringUtil.isEmpty(batchName)  
				&& StringUtil.isEmpty(batchDesc) ) {
			respstat = RespCode.error101;
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "请补全批次信息！");
			return model;
		}
		try {
			if (file != null) {
				InputStream is = file.getInputStream();
				String fileName = file.getName();
				Workbook workbook = null;
				try {
					workbook = new XSSFWorkbook(is);
				} catch (Exception ex) {
					workbook = new HSSFWorkbook(is);
				}
				Map<String, String> batchaData = new HashMap<String, String>();
				batchaData.put("operatorName", operatorName);
				batchaData.put("batchName", batchName);
				batchaData.put("menuId", menuId);
				batchaData.put("fileName", fileName);
				batchaData.put("batchDesc", batchDesc);
				batchaData.put("customkey", customkey);
				batchaData.put("companyId", companyId);
				/**
				 * 处理批次信息
				 */
				model = commissionService.inputPABankCommissionDate(workbook,batchaData);
			}else{
				respstat = RespCode.error101;
				model.put(RespCode.RESP_STAT, respstat);
				model.put(RespCode.RESP_MSG, "上传文件不能为空！");
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			respstat = RespCode.error101;
			logger.error(e.getMessage());
			model.put(RespCode.RESP_STAT, respstat);
			model.put(RespCode.RESP_MSG, "导入失败");
			return model;
		}
		logger.info("返回结果：" + model);
		return model;
	}
	
}
