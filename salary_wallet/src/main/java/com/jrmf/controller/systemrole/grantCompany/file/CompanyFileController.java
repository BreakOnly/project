package com.jrmf.controller.systemrole.grantCompany.file;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.BaseInfo;
import com.jrmf.controller.constant.CertType;
import com.jrmf.controller.constant.CommissionStatus;
import com.jrmf.controller.constant.PayType;
import com.jrmf.domain.UserCommission;
import com.jrmf.service.ChannelHistoryService;
import com.jrmf.service.ChannelInterimBatchService2;
import com.jrmf.service.UserCommissionService;
import com.jrmf.service.UserSerivce;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.ExcelFileGenerator;
/**
 * 服务公司文件类接口
 * @author 张泽辉
 *
 */
@Controller
@RequestMapping("/company/file")
public class CompanyFileController extends BaseController {

	@Autowired
	protected UserSerivce userSerivce;
	@Autowired
	private UserCommissionService commissionService;
	@Autowired
	private ChannelHistoryService channelHistoryService;
	@Autowired
	private ChannelInterimBatchService2 batchService2;
	@Autowired
	private BaseInfo baseInfo;
	
	/**
	 * 服务公司批次交易结果查询
	 * 
	 * @throws Exception
	 */
	@RequestMapping(value = "/queryBatchExceport")
	public void queryBatchExceport(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String customkey = (String)request.getSession().getAttribute("customkey");// 商户标识
		String customName =  request.getParameter("customName");//商户名称
		String batchName = request.getParameter("batchName");//批次名称
		String batchDesc = request.getParameter("batchDesc");//批次说明
		String contentName =  request.getParameter("contentName");//项目名称
		String payType = request.getParameter("payType");//下发通道
		String status = request.getParameter("status");//订单状态
		String submitTimeStart = request.getParameter("startTime");//开始时间
		String submitTimeEnd = request.getParameter("endTime");//结束时间
		String completeTimeStart = request.getParameter("startProvidetime");//到账时间区间——开始
		String completeTimeEnd = request.getParameter("endProvidetime");//到账时间区间——结束
		String amount = request.getParameter("amount");amount=ArithmeticUtil.formatDecimals(amount);//批次总金额
		String fileName = request.getParameter("fileName");//文件名称
		//平台ID
		String businessPlatform = request.getParameter("businessPlatform");
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("customkey", customkey); 
		param.put("customName", customName); 
		param.put("batchName", batchName); 
		param.put("batchDesc", batchDesc); 
		param.put("contentName", contentName); 
		param.put("payType", payType); 
		param.put("status", status); 
		param.put("submitTimeStart", submitTimeStart);  
		param.put("submitTimeEnd", submitTimeEnd);  
		param.put("completeTimeStart", completeTimeStart);  
		param.put("completeTimeEnd", completeTimeEnd);  
		param.put("amount", amount);  
		param.put("fileName", fileName);
		param.put("businessPlatform", businessPlatform);
		List<Map<String, Object>> list = channelHistoryService.batchResultQueryByCompany(param);
		getFitCommissions(list, customkey);
		List<Map<String, Object>> data = new ArrayList<>();
		String filename = "批次交易结果统计";
		String[] colunmName = new String[] {"商户所属平台名称", "商户名称", "项目名称", "批次名称", "批次说明", "批次状态", "下发通道", "提交时间", "批次总金额", "批次总笔数",
				"服务费", "批次成功总金额", "批次成功总笔数", "批次失败总金额", "批次失败总笔数", "到账时间", "服务公司", "批次导入文件名称"};
		for (Map<String, Object> userCommission : list) {
			String statusDesc = userCommission.get("status")+"";
			if("1".equals(statusDesc)){
				statusDesc = "全部成功";
			}else if("2".equals(statusDesc)){
				statusDesc = "全部失败";
			}else if("3".equals(statusDesc)){
				statusDesc = "已提交，处理中";
			}else if("4".equals(statusDesc)){
				statusDesc = "驳回";
			}else if("5".equals(statusDesc)){
				statusDesc = "部分失败";
			}else if("7".equals(statusDesc)){
				statusDesc = "已开票";
			}else if("8".equals(statusDesc)){
				statusDesc = "待审核";
			}else if("9".equals(statusDesc)){
				statusDesc = "已审核";
			}
			
			String payTypeDesc = userCommission.get("payType")+"";
			if("1".equals(payTypeDesc)){
				payTypeDesc = "银行电子户";
			}else if("2".equals(payTypeDesc)){
				payTypeDesc = "支付宝";
			}else if("3".equals(payTypeDesc)){
				payTypeDesc = "微信";
			}else if("4".equals(payTypeDesc)){
				payTypeDesc = "银行卡";
			}
			
			Map<String, Object> dataMap = new HashMap<>();
			dataMap.put("1", userCommission.get("businessPlatform"));
			dataMap.put("2", userCommission.get("customName"));
			dataMap.put("3", userCommission.get("contentName"));
			dataMap.put("4", userCommission.get("batchName"));
			dataMap.put("5", userCommission.get("batchDesc"));
			dataMap.put("6", statusDesc);
			dataMap.put("7", payTypeDesc);
			dataMap.put("8", userCommission.get("createTime"));
			dataMap.put("9", userCommission.get("batchAmount"));
			dataMap.put("10", userCommission.get("batchNum"));
			dataMap.put("11", userCommission.get("serviceFee"));
			dataMap.put("12", userCommission.get("amount"));
			dataMap.put("13", userCommission.get("passNum"));
			dataMap.put("14", userCommission.get("failedAmount"));
			dataMap.put("15", userCommission.get("failedNum"));
			dataMap.put("16", userCommission.get("providetime"));
			dataMap.put("17", userCommission.get("companyName"));
			dataMap.put("18", userCommission.get("fileName"));
			data.add(sortMapByKey(dataMap));
		}
		ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
	}

	private void getFitCommissions(List<Map<String, Object>> list, String currCompanyId){
		if (list != null && !list.isEmpty()) {
			for (Map<String, Object> var : list) {
				String companyId = (String) var.get("companyId");
				if (!currCompanyId.equals(companyId)){
					//通过其它服务公司转包到当前服务公司下发的数据
					//更正商户名称和服务公司名称
					String companyName = (String) var.get("companyName");
					String realCompanyName = (String) var.get("realCompanyName");
					var.put("customName",companyName);
					var.put("companyName",realCompanyName);
				}
			}
		}
	}
	
	/**
	 * 下放公司交易结果查询---明细列表
	 * 
	 * @throws Exception
	 */
	@RequestMapping(value = "/queryBatchDetailExceport")
	public void queryBatchDetailExceport(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String batchId =  request.getParameter("batchId");
		String userName =  request.getParameter("userName");
		String amount = request.getParameter("amount");
		amount = ArithmeticUtil.formatDecimals(amount); 
		String certId = request.getParameter("certId"); 
		String account =  request.getParameter("account"); 
		String status = request.getParameter("status"); 
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("userName", userName); 
		param.put("amount", amount); 
		param.put("certId", certId); 
		param.put("account", account); 
		param.put("status", status);
		param.put("batchId", batchId);
		List<UserCommission> list = commissionService.commissionResultQuery(param);
		List<Map<String, Object>> data = new ArrayList<>();
		String filename = "批次交易明细统计";
		String[] colunmName = new String[]{"订单ID", "收款人姓名", "证件类型", "证件号", "手机号", "收款账号"
				, "交易金额","到账金额", "服务费率", "服务费", "补差价交易金额", "补差价服务费"
				, "服务费计算规则","订单状态","订单状态描述", "订单备注", "账户所属金融机构", "最后更新时间"};
		for (UserCommission userCommission : list) {
			
			Map<String, Object> dataMap = new HashMap<>();
			dataMap.put("1", userCommission.getOrderNo()); 
			dataMap.put("2", userCommission.getUserName());
			dataMap.put("3", CertType.codeOf(userCommission.getDocumentType()).getDesc());
			dataMap.put("4", userCommission.getCertId());
			dataMap.put("5", userCommission.getPhoneNo());
			dataMap.put("6", userCommission.getAccount());
			dataMap.put("7", userCommission.getSourceAmount());
			dataMap.put("8", userCommission.getAmount());
			dataMap.put("9", userCommission.getCalculationRates());
			dataMap.put("10", userCommission.getSumFee());
			dataMap.put("11", userCommission.getSupplementAmount());
			dataMap.put("12", userCommission.getSupplementFee());
			dataMap.put("13", userCommission.getFeeRuleType());
			dataMap.put("14", CommissionStatus.codeOf(userCommission.getStatus()).getDesc());
			dataMap.put("15", userCommission.getStatusDesc());
			dataMap.put("16", userCommission.getSourceRemark());
			dataMap.put("17", userCommission.getBankName());
			dataMap.put("18", userCommission.getUpdatetime());
			data.add(sortMapByKey(dataMap));
		}
		ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
	}
	/**
	 * 服务公司---收款用户交易统计导出
	 * 说明:
	 * @param request
	 * @param response:
	 */
	@RequestMapping("/getUserTradeDataExport")
	public void getUserTradeDataExport(HttpServletRequest request,HttpServletResponse response){
		String companyId = (String)request.getSession().getAttribute("customkey");
		//商户key
		String originalId = request.getParameter("originalId");
		String certId = request.getParameter("certId");
		//批次名称
		String batchName = request.getParameter("batchName");
		//用户名称
		String userName = request.getParameter("userName");
		//交易时间起始
		String tradeTimeStart = request.getParameter("tradeTimeStart");
		//交易时间结束
		String tradeTimeEnd = request.getParameter("tradeTimeEnd");
		//批次备注
		String batchDesc = request.getParameter("batchDesc");
		//商户名称
		String customName = request.getParameter("customName");
		//下发通道2
		String payType = request.getParameter("payType");
		//交易金额起始
		String amountStart = request.getParameter("amountStart");
		//交易金额结束
		String amountEnd = request.getParameter("amountEnd");
		//交易金额结束
		String monthAmount = request.getParameter("monthAmount");
		Map<String,Object> param = new HashMap<>(20);
		param.put("companyId", companyId);
		param.put("certId", certId);
		param.put("batchName", batchName);
		param.put("userName", userName);
		param.put("tradeTimeStart", tradeTimeStart);
		param.put("tradeTimeEnd", tradeTimeEnd);
		param.put("batchDesc", batchDesc);
		param.put("customName", customName);
		param.put("payType", payType);
		param.put("amountStart", amountStart);
		param.put("amountEnd", amountEnd);
		param.put("monthAmount", monthAmount);
		 param.put("calculationLimit", baseInfo.getCalculationLimit());
		List<UserCommission> userTradeData = commissionService.getUserTradeCompany(param);
		//根据金额区间筛选数据
		List<Map<String, Object>> data = new ArrayList<>();
		String filename = "收款用户交易统计表";
		String[] colunmName = new String[] { "用户证件号", "用户姓名", "手机号", "证件类型", "成功交易总额", "成功交易总笔数", "总服务费", "用户创建时间"};
		for (UserCommission userCommission : userTradeData) {
			Map<String, Object> dataMap = new HashMap<>(15);
			dataMap.put("1", userCommission.getCertId());
			dataMap.put("2", userCommission.getUserName());
			dataMap.put("3", userCommission.getPhoneNo());
			dataMap.put("4", CertType.codeOf(userCommission.getDocumentType()).getDesc());
			dataMap.put("5", userCommission.getAmount());
			dataMap.put("6", userCommission.getPassNum());
			dataMap.put("7", userCommission.getSumFee());
			dataMap.put("8", userCommission.getCreatetime());
			data.add(sortMapByKey(dataMap));
		}
		ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
	}
	/**
	 * 服务公司---收款用户交易-明细-统计导出
	 * 说明:
	 * @param request
	 * @param response:
	 */
	@RequestMapping("/getUserTradeDataDetailExport")
	public void getUserTradeDataDetailExport(HttpServletRequest request,HttpServletResponse response){
		String companyId = (String)request.getSession().getAttribute("customkey");
		String userId = request.getParameter("userId");//用户Id
		String certId = request.getParameter("certId");//证件号
		String batchName = request.getParameter("batchName");//批次名称
		String userName = request.getParameter("userName");//用户名称
		String tradeTimeStart = request.getParameter("tradeTimeStart");//交易时间起始
		String tradeTimeEnd = request.getParameter("tradeTimeEnd");//交易时间结束
		String batchDesc = request.getParameter("batchDesc");//批次备注
		String companyName = request.getParameter("companyName");//商户名称
		String payType = request.getParameter("payType");//下发通道
		String amountStart = request.getParameter("amountStart");//交易金额起始
		String amountEnd = request.getParameter("amountEnd");//交易金额结束
		String customName = request.getParameter("customName");//商户名称

		Map<String,Object> param = new HashMap<>(20);
		param.put("companyId", companyId);
		param.put("userId", userId);
		param.put("certId", certId);
		param.put("batchName", batchName);
		param.put("userName", userName);
		param.put("tradeTimeStart", tradeTimeStart);
		param.put("tradeTimeEnd", tradeTimeEnd);
		param.put("batchDesc", batchDesc);
		param.put("companyName", companyName);
		param.put("customName", customName);
		param.put("payType", payType);
		param.put("amountStart", amountStart);
		param.put("amountEnd", amountEnd);
		//sql待修改！！！！！
		List<UserCommission> userDealDetail = commissionService.getUserDealDetail(param);
		List<Map<String, Object>> data = new ArrayList<>();
		String filename = "收款用户交易明细统计表";
		String[] colunmName = new String[] { "订单ID","商户名称","收款人姓名","证件类型","证件号","手机号","收款账号",
				"交易金额","服务费率","服务费(包含补差价)","补差价交易金额","补差价服务费","服务费计算规则","订单状态","状态描述","订单备注",
				"账号所属金融机构","下发通道","服务公司","交易时间","项目名称","批次名称","批次说明","最后更新时间"};
		for (UserCommission userCommission : userDealDetail) {
			int documentType = userCommission.getDocumentType();
			Map<String, Object> dataMap = new HashMap<>();
			dataMap.put("1", userCommission.getOrderNo());
			dataMap.put("2", userCommission.getCustomName());
			dataMap.put("3", userCommission.getUserName());
			dataMap.put("4", CertType.codeOf(userCommission.getDocumentType()).getDesc());
			dataMap.put("5", userCommission.getCertId());
			dataMap.put("6", userCommission.getPhoneNo());
			dataMap.put("7", userCommission.getAccount());
			dataMap.put("8", userCommission.getAmount());
			dataMap.put("9", userCommission.getCalculationRates());
			dataMap.put("10", userCommission.getSumFee());
			dataMap.put("11", userCommission.getSupplementAmount());
			dataMap.put("12", userCommission.getSupplementFee());
			dataMap.put("13", userCommission.getFeeRuleType());
			dataMap.put("14", CommissionStatus.codeOf(userCommission.getStatus()).getDesc());
			dataMap.put("15", userCommission.getStatusDesc());
			dataMap.put("16", userCommission.getRemark());
			dataMap.put("17", userCommission.getBankName());
			dataMap.put("18", PayType.codeOf(userCommission.getPayType()).getDesc());
			dataMap.put("19", userCommission.getCompanyName());
			dataMap.put("20", userCommission.getPaymentTime());
			dataMap.put("21", userCommission.getContentName());
			dataMap.put("22", userCommission.getBatchName());
			dataMap.put("23", userCommission.getBatchDesc());
			dataMap.put("24", userCommission.getUpdatetime());
			data.add(sortMapByKey(dataMap));
		}
		ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
	}

}
