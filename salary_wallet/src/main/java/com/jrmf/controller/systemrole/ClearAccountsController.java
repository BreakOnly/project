package com.jrmf.controller.systemrole;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import com.jrmf.controller.constant.AgainCalculateType;
import com.jrmf.controller.constant.CustomType;
import com.jrmf.domain.CustomProxySubCommission;
import org.apache.poi.hssf.record.DVALRecord;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.GearLaberType;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.Page;
import com.jrmf.domain.QbClearingAccounts;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.QbClearingAccountsService;
import com.jrmf.service.UserCommissionService;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.ExcelCopyUtil;
import com.jrmf.utils.ExcelFileGenerator;
import com.jrmf.utils.ExcelUtil;
import com.jrmf.utils.ListUtil;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;

@RestController
@RequestMapping("/clearAccounts")
public class ClearAccountsController extends BaseController{

	private static Logger logger = LoggerFactory.getLogger(ClearAccountsController.class);

	//商户信息service
	@Autowired
	private ChannelCustomService channelCustomService;
	@Autowired
	private QbClearingAccountsService qbClearingAccountsService;
	@Autowired
	private UserCommissionService userCommissionService;

	/**
	 * 查询清结算报表
	 * @param request
	 * @return
	 */
	@RequestMapping("/queryList")
	public ResponseEntity<?> queryList(HttpServletRequest request){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		Page page = new Page(request);
		//校验是否有权限
		boolean checkFlag = true;
		//获取登陆信息
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		Integer []allowCustomType = new Integer[]{6,7};
		checkFlag = channelCustomService.getCustomKeysByType(page.getParams(), allowCustomType, customLogin);
		if(checkFlag){
			//分页查询白名单信息
			PageHelper.startPage(page.getPageNo(),page.getPageSize());
			List<Map<String, Object>> relationList = qbClearingAccountsService.getClearAccountsByPage(page);
			//查询白名单总条数
			//int count = qbClearingAccountsService.getClearAccountsCount(page);
			PageInfo<Map<String,Object>> pageInfo = new PageInfo<>(relationList);
			result.put("total", pageInfo.getTotal());
			result.put("relationList", pageInfo.getList());
		}else{
			respstat = RespCode.DO_NOT_HAVE_APPROVAL_RIGHT;
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return new ResponseEntity<Object>(result,HttpStatus.OK);
	}

	/**
	 * 导出
	 * @param request
	 */
	@RequestMapping("/export")
	public void export(HttpServletRequest request,HttpServletResponse response){
		try{
			// 标题
			String filename = "清结算报表"; 
			Page page = new Page(request);
			//校验是否有权限
			boolean checkFlag = true;
			//获取登陆信息
			ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
			Integer []allowCustomType = new Integer[]{6,7};
			checkFlag = channelCustomService.getCustomKeysByType(page.getParams(), allowCustomType, customLogin);
			if(checkFlag){
				//分页查询白名单信息
				List<QbClearingAccounts> clearAccountList = qbClearingAccountsService.getClearAccountsNoPage(page);
				String excelpath="/excel/clearAccountTemplate.xls";
				HSSFSheet hssfSheet = ExcelUtil.getSheet(excelpath, 0,true);
				HSSFWorkbook wb = hssfSheet.getWorkbook();
				addalentDemands(hssfSheet,clearAccountList);
				ExcelUtil.exportExcel(response, filename, wb);
			}else{
				logger.info("权限不足，无法操作");
			}
		}catch(Exception e){
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * 重新发起结算单
	 * @param request
	 * @param againType
	 * @param merchantId
	 * @param month
	 * @return
	 */
	@RequestMapping("/againClearAccounts")
	public ResponseEntity<?> againClearAccounts(HttpServletRequest request,Integer againType,String merchantId,String month,String businessPlatformId){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		Page page = new Page(request);
		boolean checkFlag = false;
		boolean paramsCheck = false;
		int deleteFlag=0;
		//获取登陆信息
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		Integer []allowCustomType = new Integer[]{6,7};
		checkFlag = channelCustomService.getCustomKeysByType(page.getParams(), allowCustomType, customLogin);
		if (StringUtil.isEmpty(businessPlatformId)){
			businessPlatformId = page.getParams().get("businessPlatformId");
		}

		Map<String,Object> platformParams = new HashMap<>();
		if (!StringUtil.isEmpty(merchantId)){
			//判断选择的商户是否在当前平台下
			platformParams.put("businessPlatformId",businessPlatformId);
			platformParams.put("customkey",merchantId);
			List<ChannelCustom> channelCustomList = channelCustomService.getCustomByParam(platformParams);
			if (channelCustomList == null || channelCustomList.size() == 0){
				result.put(RespCode.RESP_STAT, RespCode.PLATFORM_NOT_MERCHANT);
				result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.PLATFORM_NOT_MERCHANT));
				return new ResponseEntity<Object>(result,HttpStatus.OK);
			}
		}
		//1.操作权限校验
		if(checkFlag){
			Map<String, String> params = new HashMap<String, String>();
			//根据businessPlatformId获取businessPlatform
			HashMap platformMap = channelCustomService.selectPlatformByCostomId(Integer.valueOf(businessPlatformId));
			params.put("businessPlatform", (String) platformMap.get("businessPlatform"));
			//2.参数校验
			if(againType!=null&&!StringUtil.isEmpty(month)){
				if(againType!=1){
					if(!StringUtil.isEmpty(merchantId)){
						paramsCheck=true;
					}
				}else{
					paramsCheck=true;
				}
			} 
			if(!paramsCheck){
				respstat = RespCode.error101;
			}else{
				String customKeys = null;
				platformParams.clear();
				platformParams.put("businessPlatformId",businessPlatformId);
				platformParams.put("customTypes", CustomType.CUSTOM.getCode() +"," + CustomType.GROUP.getCode());
				List<ChannelCustom> channelCustomList = channelCustomService.getCustomByParam(platformParams);
				if (channelCustomList == null || channelCustomList.size() == 0){
					result.put(RespCode.RESP_STAT, RespCode.PLATFORM_NOT_MERCHANT);
					result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.PLATFORM_NOT_MERCHANT));
					return new ResponseEntity<Object>(result,HttpStatus.OK);
				} else{
					List<String> list = new ArrayList<>();
					for (ChannelCustom channelCustom : channelCustomList) {
						list.add(channelCustom.getCustomkey());
					}
					customKeys = Joiner.on(",").join(list);
				}
				params.put("customKeys", customKeys);
				params.put("month", month);
				//3.删除原数据
				if(againType== AgainCalculateType.COMPANY.getCode()){
					params.put("companyId", merchantId);
					qbClearingAccountsService.deleteClearAccountAllByCompany(params);
					deleteFlag =qbClearingAccountsService.selectClearAccountAllByCompanyCount(params);
				}else if(againType==AgainCalculateType.CUSTOM_PROXY.getCode()){
					params.put("agentId", merchantId);
					qbClearingAccountsService.deleteClearAccountAllByAgent(params);
					deleteFlag = qbClearingAccountsService.selectClearAccountAllByAgentCount(params);
				}else if(againType==AgainCalculateType.CUSTOM.getCode()){
					params.put("merchantId", merchantId);
					qbClearingAccountsService.deleteClearAccountAllByMerchant(params);
					deleteFlag = qbClearingAccountsService.selectClearAccountAllByMerchantCount(params);
				}else{
					qbClearingAccountsService.deleteClearAccountAllByMonth(params);
					deleteFlag = qbClearingAccountsService.selectClearAccountAllByMonthCount(params);
				}
				if(deleteFlag==0){
					params.put("startMonth", month);
					params.put("endMonth", month);
					params.put("fenrun", "1");
					//4.获取新数据
					List<Map<String, String>> clearTermlist = new ArrayList<Map<String,String>>();
					clearTermlist = qbClearingAccountsService.groupClearTermMonthNew(params);
					//5.执行新增
					Map<String, List<Map<String, String>>> clearAccountMap = getClearAccountMonth(clearTermlist);
					if(!clearAccountMap.isEmpty()){
						//汇总对应条件累计金额
						List<QbClearingAccounts> clearingAccountList = new ArrayList<QbClearingAccounts>();
						for (String key : clearAccountMap.keySet()) {
							List<Map<String, String>> merchantTransList = clearAccountMap.get(key);
							setClearAccountInfo(merchantTransList,clearingAccountList);
						}
						//新增数据库
						for (QbClearingAccounts qbClearingAccounts : clearingAccountList) {
							qbClearingAccountsService.insert(qbClearingAccounts);
						}
					}else{
						//该月份不存在清结算记录
						respstat = RespCode.CLEARACCOUNTS_IS_NULL;
					}
				}else{
					//删除原数据失败
					respstat = RespCode.DELETE_FAIL;
				}
			}
		}else{
			respstat = RespCode.DO_NOT_HAVE_APPROVAL_RIGHT;
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return new ResponseEntity<Object>(result,HttpStatus.OK);
	}


	/**
	 * 第四个sheet页：人才需求情况调查表
	 */
	private static void addalentDemands(HSSFSheet talentDemandSheet, List<QbClearingAccounts> talentDemands)
			throws IllegalArgumentException, IllegalAccessException {
		Row talentDemandRow = talentDemandSheet.getRow(2);
		// 如果数据大于模板中的行数，插入行并复制第一行数据的格式
		if (talentDemands.size() > 5) {
			// 插入行，5是模板中已有的行数
			talentDemandSheet.shiftRows(3, talentDemandSheet.getLastRowNum(), talentDemands.size() - 5, true, false);
			HSSFRow sourceRow = talentDemandSheet.getRow(2);
			for (int i = 0; i < talentDemands.size() - 5; i++) {
				HSSFRow newRow = talentDemandSheet.createRow(2 + i + 1);
				newRow.setHeight(sourceRow.getHeight());
				for (int j = 0; j < sourceRow.getLastCellNum(); j++) {
					HSSFCell templateCell = sourceRow.getCell(j);
					if (templateCell != null) {
						HSSFCell newCell = newRow.createCell(j);
						ExcelCopyUtil.copyCell(templateCell, newCell);
					}
				}
			}
		}
		// 填充数据
		for (int i = 0; i < talentDemands.size(); i++) {
			talentDemandRow = talentDemandSheet.getRow(2 + i);
			talentDemandRow.getCell(0).setCellValue(talentDemands.get(i).getTransMonth());
			talentDemandRow.getCell(1).setCellValue(talentDemands.get(i).getMerchantName());
			talentDemandRow.getCell(2).setCellValue(talentDemands.get(i).getAgentName());
			talentDemandRow.getCell(3).setCellValue(talentDemands.get(i).getBusinessManager());
			talentDemandRow.getCell(4).setCellValue(talentDemands.get(i).getCompanyName());
			talentDemandRow.getCell(5).setCellValue(talentDemands.get(i).getGearLaber());
			talentDemandRow.getCell(6).setCellValue(talentDemands.get(i).getSmallAmountOne());
			talentDemandRow.getCell(7).setCellValue(talentDemands.get(i).getSmallAmountTwo());
			talentDemandRow.getCell(8).setCellValue(talentDemands.get(i).getBigAmount());
			talentDemandRow.getCell(9).setCellValue(talentDemands.get(i).getTotalAmount());
			talentDemandRow.getCell(10).setCellValue(talentDemands.get(i).getMerchantRateRule());
			talentDemandRow.getCell(11).setCellValue(talentDemands.get(i).getSmallAmountOneRate());
			talentDemandRow.getCell(12).setCellValue(talentDemands.get(i).getSmallAmountTwoRate());
			talentDemandRow.getCell(13).setCellValue(talentDemands.get(i).getBigAmountRate());

			talentDemandRow.getCell(14).setCellValue(talentDemands.get(i).getSmallAmountOneRateUpdate());
			talentDemandRow.getCell(15).setCellValue(talentDemands.get(i).getSmallAmountTwoRateUpdate());
			talentDemandRow.getCell(16).setCellValue(talentDemands.get(i).getBigAmountRateUpdate());

			talentDemandRow.getCell(17).setCellValue(talentDemands.get(i).getCompanySmallAmount());
			talentDemandRow.getCell(18).setCellValue(talentDemands.get(i).getCompanyBigAmount());
			talentDemandRow.getCell(19).setCellValue(talentDemands.get(i).getAgentSmallRate());
			talentDemandRow.getCell(20).setCellValue(talentDemands.get(i).getAgentBigRate());
			talentDemandRow.getCell(21).setCellValue(talentDemands.get(i).getAgentSmallRateUpdate());
			talentDemandRow.getCell(22).setCellValue(talentDemands.get(i).getAgentBigRateUpdate());

			talentDemandRow.getCell(23).setCellValue(talentDemands.get(i).getAgentSmallAmount());
			talentDemandRow.getCell(24).setCellValue(talentDemands.get(i).getAgentBigAmount());
			talentDemandRow.getCell(25).setCellValue(talentDemands.get(i).getAgentTotalAmount());
			talentDemandRow.getCell(26).setCellValue(talentDemands.get(i).getAgentCommission());
			talentDemandRow.getCell(27).setCellValue(talentDemands.get(i).getAgentFinalCommission());
			talentDemandRow.getCell(28).setCellValue(talentDemands.get(i).getRepairCommission());
		}
	}	

	/**
	 * 设置清结算报表数据
	 */
	public void setClearAccountInfo(List<Map<String, String>> merchantTransList,List<QbClearingAccounts> clearingAccountList){
		List<Map<String, String>> merchantTransOverList = new ArrayList<Map<String,String>>();
		String gearPositionDesc = "";
		String merchantRateRuleDesc = "";
		int smallCount = 1;
		int bigCount = 1;
		Map<String, String> merchantTrans = merchantTransList.get(0);
		String merchantName = merchantTrans.get("merchantName");
		String companyName = merchantTrans.get("companyName");
		String agentName = merchantTrans.get("agentName");
		String agentId = merchantTrans.get("businessChannel");
		String companyId = merchantTrans.get("companyId");
		String customkey = merchantTrans.get("originalId");
		String transMonth = merchantTrans.get("month");
		String businessManager = merchantTrans.get("businessManager");
		QbClearingAccounts clearingAccounts = new QbClearingAccounts();
		clearingAccounts.setTransMonth(transMonth);
		clearingAccounts.setCompanyId(companyId);
		clearingAccounts.setCompanyName(companyName);
		clearingAccounts.setCustomkey(customkey);
		clearingAccounts.setMerchantName(merchantName);
		clearingAccounts.setAgentId(agentId);
		clearingAccounts.setAgentName(agentName);
		clearingAccounts.setBusinessManager(businessManager);
		Map<String, String> rateIntervalMap = new HashMap<String, String>();
		for (int i = 0;i<merchantTransList.size();i++) {
			Map<String, String> merchantTranMap = merchantTransList.get(i);
			//设置挡位、费率、累加金额信息
			String rateInterval = merchantTranMap.get("gear");
			String merchantRate = merchantTranMap.get("customRate");
			if(rateIntervalMap.containsKey(rateInterval)){
				merchantTransOverList.add(merchantTranMap);
			}else{
				rateIntervalMap.put(rateInterval, merchantRate);
				String totalAmount = String.valueOf(merchantTranMap.get("amount"));
				String proxyFeeRate = ArithmeticUtil.mulStr(merchantTranMap.get("proxyFeeRate"),"100")+"%";
				String merchantRateRule = ArithmeticUtil.mulStr(merchantRate,"100")+"%";
				if(!StringUtil.isEmpty(String.valueOf(merchantTranMap.get("gearLabel")))){
				String gearLaber = GearLaberType.codeOf(Integer.parseInt(String.valueOf(merchantTranMap.get("gearLabel")))).getDesc();
					if(Integer.parseInt(String.valueOf(merchantTranMap.get("gearLabel")))==1){
						if(StringUtil.isEmpty(clearingAccounts.getSmallAmountOne())||StringUtil.isEmpty(clearingAccounts.getSmallAmountTwo())){
							gearPositionDesc+=rateInterval+",实发金额"+gearLaber+smallCount+"\n";
							merchantRateRuleDesc+=rateInterval+",实发金额"+gearLaber+smallCount+":"+merchantRateRule+"\n";
						}
					}else{
						if(StringUtil.isEmpty(clearingAccounts.getBigAmount())){
							gearPositionDesc+=rateInterval+",实发金额"+gearLaber+bigCount+"\n";
							merchantRateRuleDesc+=rateInterval+",实发金额"+gearLaber+bigCount+":"+merchantRateRule+"\n";
						}
					}
					if(Integer.parseInt(String.valueOf(merchantTranMap.get("gearLabel")))==1){
						clearingAccounts.setAgentSmallRate(proxyFeeRate);
						if(StringUtil.isEmpty(clearingAccounts.getSmallAmountOne())){
							//设置小额1
							clearingAccounts.setSmallAmountOne(totalAmount);
							clearingAccounts.setSmallAmountOneRate(merchantRateRule);
							String fee = ArithmeticUtil.mulStr(totalAmount, merchantRate, 2);
							clearingAccounts.setSmallAmountOneFee(fee);
							//判断是否商户存在变更费率，若存在获取生效日期类的下发金额，并计算手续费
							List<Map<String, String>> merRateUpdateList = qbClearingAccountsService.getMerRateUpdate(merchantTranMap);
							if(merRateUpdateList!=null && merRateUpdateList.size()>0){
								Map<String, String> differMap = qbClearingAccountsService.differSummary(merchantTranMap, merchantRate,merRateUpdateList,1);
								clearingAccounts.setSmallAmountOneRateUpdate(differMap.get("updateRateDesc"));
								clearingAccounts.setDifferSmallOneMerAmount(differMap.get("differAmount"));
								clearingAccounts.setDifferSmallOneMerAmountDesc(differMap.get("differAmountDesc"));
							}

							//判断是否代理存在变更费率，若存在获取生效日期类的下发金额，并计算手续费
							List<Map<String, String>> agentRateUpdateList = qbClearingAccountsService.getAgentRateUpdate(merchantTranMap);
							if(agentRateUpdateList!=null && agentRateUpdateList.size()>0){
								Map<String, String> differMap = qbClearingAccountsService.differSummary(merchantTranMap,merchantTranMap.get("proxyFeeRate"),agentRateUpdateList,2);
								clearingAccounts.setDifferAgentSmallAmount(differMap.get("differAmount"));
								clearingAccounts.setAgentSmallRateUpdate(differMap.get("updateRateDesc"));
								clearingAccounts.setDifferAgentSmallAmountDesc(differMap.get("differAmountDesc"));
							}

						}else{
							if(StringUtil.isEmpty(clearingAccounts.getSmallAmountTwo())){
								//设置小额2
								clearingAccounts.setSmallAmountTwo(totalAmount);
								clearingAccounts.setSmallAmountTwoRate(merchantRateRule);
								String fee = ArithmeticUtil.mulStr(totalAmount, merchantRate, 2);
								clearingAccounts.setSmallAmountTwoFee(fee);
								List<Map<String, String>> merRateUpdateList = qbClearingAccountsService.getMerRateUpdate(merchantTranMap);
								if(merRateUpdateList!=null && merRateUpdateList.size()>0){
									Map<String, String> differMap = qbClearingAccountsService.differSummary(merchantTranMap, merchantRate,merRateUpdateList,1);
									clearingAccounts.setSmallAmountTwoRateUpdate(differMap.get("updateRateDesc"));
									clearingAccounts.setDifferSmallTwoMerAmount(differMap.get("differAmount"));
									clearingAccounts.setDifferSmallTwoMerAmountDesc(differMap.get("differAmountDesc"));
								}
								//判断是否代理存在变更费率，若存在获取生效日期类的下发金额，并计算手续费
								List<Map<String, String>> agentRateUpdateList = qbClearingAccountsService.getAgentRateUpdate(merchantTranMap);
								if(agentRateUpdateList!=null && agentRateUpdateList.size()>0){
									Map<String, String> differMap = qbClearingAccountsService.differSummary(merchantTranMap,merchantTranMap.get("proxyFeeRate"),agentRateUpdateList,2);
									String differAmount = ArithmeticUtil.addStr(clearingAccounts.getDifferAgentSmallAmount(), differMap.get("differAmount"));
									clearingAccounts.setDifferAgentSmallAmount(differAmount);
									String []differAgentAmountArray = clearingAccounts.getDifferAgentSmallAmountDesc().split(";");
									String []differAgentAmountArrayTwo =  differMap.get("differAmountDesc").split(";");
									String differAgentAmountDesc = "";
									for(int j=0;j<differAgentAmountArray.length;j++){
										String differAgentAmount= differAgentAmountArray[j];
										String differAgentAmountTwo= differAgentAmountArrayTwo[j];
										String differAgentAmountTotal = ArithmeticUtil.addStr(differAgentAmount, differAgentAmountTwo);
										differAgentAmountDesc = differAgentAmountDesc+differAgentAmountTotal+";";
									}
									clearingAccounts.setDifferAgentSmallAmountDesc(differAgentAmountDesc);
								}
							}else{
								merchantTransOverList.add(merchantTranMap);
							}
						}
						smallCount++;
					}else{
						if(StringUtil.isEmpty(clearingAccounts.getBigAmount())){
							//设置大额
							clearingAccounts.setAgentBigRate(proxyFeeRate);
							clearingAccounts.setBigAmount(totalAmount);
							clearingAccounts.setBigAmountRate(merchantRateRule);
							clearingAccounts.setCompanyBigAmount(totalAmount);
							String fee = ArithmeticUtil.mulStr(totalAmount, merchantRate, 2);
							clearingAccounts.setBigAmountFee(fee);
							//判断是否商户存在变更费率，若存在获取生效日期类的下发金额，并计算手续费
							List<Map<String, String>> merRateUpdateList = qbClearingAccountsService.getMerRateUpdate(merchantTranMap);
							if(merRateUpdateList!=null && merRateUpdateList.size()>0){
								Map<String, String> differMap = qbClearingAccountsService.differSummary(merchantTranMap, merchantRate,merRateUpdateList,1);
								clearingAccounts.setBigAmountRateUpdate(differMap.get("updateRateDesc"));
								clearingAccounts.setDifferBigMerAmount(differMap.get("differAmount"));
								clearingAccounts.setDifferBigMerAmountDesc(differMap.get("differAmountDesc"));
							}

							//判断是否代理存在变更费率，若存在获取生效日期类的下发金额，并计算手续费
							List<Map<String, String>> agentRateUpdateList = qbClearingAccountsService.getAgentRateUpdate(merchantTranMap);
							if(agentRateUpdateList!=null && agentRateUpdateList.size()>0){
								Map<String, String> differMap = qbClearingAccountsService.differSummary(merchantTranMap,merchantTranMap.get("proxyFeeRate"),agentRateUpdateList,2);
								clearingAccounts.setAgentBigRateUpdate(differMap.get("updateRateDesc"));
								clearingAccounts.setDifferAgentBigAmount(differMap.get("differAmount"));
								clearingAccounts.setDifferAgentBigAmountDesc(differMap.get("differAmountDesc"));
							}
						}else{
							merchantTransOverList.add(merchantTranMap);
						}
						bigCount++;
					}
				}
			}
		}
		//设置商户费率规则
		clearingAccounts.setMerchantRateRule(merchantRateRuleDesc);
		//设置签约实发金额当位值
		clearingAccounts.setGearLaber(gearPositionDesc);
		//商户实发小额累计=商户实发小额1+商户实发小额2+商户实发大额
		String totalAmount = ArithmeticUtil.addStr(clearingAccounts.getSmallAmountOne(),clearingAccounts.getSmallAmountTwo());
		totalAmount = ArithmeticUtil.addStr(totalAmount, clearingAccounts.getBigAmount());
		clearingAccounts.setTotalAmount(totalAmount);
		//服务公司小额=商户实发小额1+商户实发小额2
		String companySmallAmount = ArithmeticUtil.addStr(clearingAccounts.getSmallAmountOne(), clearingAccounts.getSmallAmountTwo());
		clearingAccounts.setCompanySmallAmount(companySmallAmount);
		String agentSamllRate = clearingAccounts.getAgentSmallRate()==null?"0":ArithmeticUtil.divideStr2(clearingAccounts.getAgentSmallRate().replace("%", ""),"100");
		String agentBigRate = clearingAccounts.getAgentBigRate()==null?"0":ArithmeticUtil.divideStr2(clearingAccounts.getAgentBigRate().replace("%", ""),"100");
		String agentSmallAmount = ArithmeticUtil.mulStr(companySmallAmount, agentSamllRate,2);
		String agentBigAmount = ArithmeticUtil.mulStr(clearingAccounts.getCompanyBigAmount(), agentBigRate,2);
		String agentTotalAmount = ArithmeticUtil.addStr(agentSmallAmount, agentBigAmount);
		clearingAccounts.setAgentSmallAmount(agentSmallAmount);
		clearingAccounts.setAgentBigAmount(agentBigAmount);
		clearingAccounts.setAgentTotalAmount(agentTotalAmount);
		String merchantTotalAmount = ArithmeticUtil.addStr(clearingAccounts.getSmallAmountOneFee(), clearingAccounts.getSmallAmountTwoFee());
		merchantTotalAmount = ArithmeticUtil.addStr(merchantTotalAmount, clearingAccounts.getBigAmountFee());
		String agentCommission = ArithmeticUtil.subStr2(merchantTotalAmount, agentTotalAmount);
		clearingAccounts.setAgentCommission(agentCommission);
		clearingAccounts.setCreateTime(DateUtils.getNowDate());
		//商户费率变更小额档位差额
		String differSmallAmount = ArithmeticUtil.addStr(clearingAccounts.getDifferSmallOneMerAmount(), clearingAccounts.getDifferSmallTwoMerAmount());
		//商户费率变更大额档位差额
		String differBigAmount = clearingAccounts.getDifferBigMerAmount();
		String totalDifferMerAmount = ArithmeticUtil.addStr(differSmallAmount,differBigAmount);
		String totalDifferAgentAmount = ArithmeticUtil.addStr(clearingAccounts.getDifferAgentSmallAmount(), clearingAccounts.getDifferAgentBigAmount());
		String totalDifferAmount = ArithmeticUtil.subStr2(totalDifferMerAmount, totalDifferAgentAmount);
		clearingAccounts.setRepairCommission(totalDifferAmount);
		String agentFianlCommission = ArithmeticUtil.addStr(clearingAccounts.getAgentCommission(), clearingAccounts.getRepairCommission());
		clearingAccounts.setAgentFinalCommission(agentFianlCommission);
		clearingAccountList.add(clearingAccounts);

		boolean size = merchantTransOverList.size()>0;
		if(size){
			setClearAccountInfo(merchantTransOverList,clearingAccountList);
		}
	}

	/**
	 * 统计直客/渠道统计信息
	 * @param request
	 * @return
	 */
	@RequestMapping("/summaryInfoByMerchant")
	public ResponseEntity<?> summaryInfoByMerchant(HttpServletRequest request){
		HashMap<String, Object> result = new HashMap<>();
		Page page = new Page(request);
		//判断是否是平台商户
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		if (isPlatformAccount(customLogin)) {
			Integer integer = checkCustom(customLogin);
			page.getParams().put("businessPlatformId",String.valueOf(integer));
		}
		//判断是否有平台ID
		if (StringUtil.isEmpty(page.getParams().get("businessPlatformId"))) {
			result.put(RespCode.RESP_STAT, RespCode.error101);
			result.put(RespCode.RESP_MSG, RespCode.PARAMS_ERROR);
			return new ResponseEntity<Object>(result,HttpStatus.OK);
		}
		//根据businessPlatformId获取businessPlatform
		HashMap platformMap = channelCustomService.selectPlatformByCostomId(Integer.valueOf(page.getParams().get("businessPlatformId")));
		if (platformMap == null || platformMap.size() == 0){
			result.put(RespCode.RESP_STAT, RespCode.PLATFORM_NOT_EXIST);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.PLATFORM_NOT_EXIST));
			return new ResponseEntity<Object>(result,HttpStatus.OK);
		}
		page.getParams().put("businessPlatform", (String) platformMap.get("businessPlatform"));
		List<Map<String,Object>> summaryList = new ArrayList<Map<String,Object>>();
		Map<String, Object> merchantSummary = new HashMap<String, Object>();
		Map<String, Object> agentSummary = new HashMap<String, Object>();
		String summaryAmount = userCommissionService.summaryInfoByMerchant(page.getParams());
		String agentSummaryAmount = userCommissionService.summaryInfoByAgent(page.getParams());
		merchantSummary.put("dataName", "直客");
		merchantSummary.put("startTime", page.getParams().get("startTime"));
		merchantSummary.put("endTime", page.getParams().get("endTime"));
		merchantSummary.put("amount", summaryAmount==null?"0":summaryAmount);
		agentSummary.put("dataName", "渠道");
		agentSummary.put("startTime", page.getParams().get("startTime"));
		agentSummary.put("endTime", page.getParams().get("endTime"));
		agentSummary.put("amount", agentSummaryAmount==null?"0":agentSummaryAmount);
		summaryList.add(merchantSummary);
		summaryList.add(agentSummary);
		String merchantAmount = String.valueOf(merchantSummary.get("amount"));
		String agentAmount = String.valueOf(agentSummary.get("amount"));
		String totalAmount = ArithmeticUtil.addStr(merchantAmount, agentAmount);
		result.put("totalAmount", totalAmount);
		result.put("total", summaryList.size());
		result.put("relationList", summaryList);
		result.put(RespCode.RESP_STAT, RespCode.success);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));
		return new ResponseEntity<Object>(result,HttpStatus.OK);
	}

	/**
	 * 直客/渠道汇总详情
	 * @param request
	 * @return
	 */
	@RequestMapping("/summaryInfoByMerchantDetail")
	public ResponseEntity<?> summaryInfoByMerchantDetail(HttpServletRequest request){
		HashMap<String, Object> result = new HashMap<>();
		Page page = new Page(request);
		//判断是否是平台商户
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		if (isPlatformAccount(customLogin)) {
			Integer integer = checkCustom(customLogin);
			page.getParams().put("businessPlatformId",String.valueOf(integer));
		}
		//判断是否有平台ID
		if (StringUtil.isEmpty(page.getParams().get("businessPlatformId"))) {
			result.put(RespCode.RESP_STAT, RespCode.error101);
			result.put(RespCode.RESP_MSG, RespCode.PARAMS_ERROR);
			return new ResponseEntity<Object>(result,HttpStatus.OK);
		}
		//根据businessPlatformId获取businessPlatform
		HashMap platformMap = channelCustomService.selectPlatformByCostomId(Integer.valueOf(page.getParams().get("businessPlatformId")));
		if (platformMap == null || platformMap.size() == 0){
			result.put(RespCode.RESP_STAT, RespCode.PLATFORM_NOT_EXIST);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.PLATFORM_NOT_EXIST));
			return new ResponseEntity<Object>(result,HttpStatus.OK);
		}
		page.getParams().put("businessPlatform", (String) platformMap.get("businessPlatform"));
		List<Map<String, Object>> summaryList = new ArrayList<Map<String,Object>>();
		int total = 0;
		String totalAmount = "0";
		if(page.getParams().get("dataType")!=null){
			if(page.getParams().get("dataType").equals("1")){
				//查询直客商户分组统计数据
				summaryList = userCommissionService.summaryInfoGroupMerchant(page);
				total = userCommissionService.summaryInfoGroupMerchantCount(page);
				totalAmount = userCommissionService.summaryInfoByMerchant(page.getParams());
			}else{
				//查询渠道分组统计数据
				summaryList = userCommissionService.summaryInfoGroupAgent(page);
				total = userCommissionService.summaryInfoGroupAgentCount(page);
				totalAmount = userCommissionService.summaryInfoByAgent(page.getParams());
			}
			result.put(RespCode.RESP_STAT, RespCode.success);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));
			result.put("relationList", summaryList);
			result.put("total", total);
			result.put("totalAmount", totalAmount==null?"0":totalAmount);
		}else{
			result.put(RespCode.RESP_STAT, RespCode.error101);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.error101));
		}
		return new ResponseEntity<Object>(result,HttpStatus.OK);
	}

	/**
	 * 直客渠道数据统计导出
	 * @param request
	 * @param response
	 */
	@RequestMapping("/exportSummaryMerchant")
	@ResponseBody
	public void exportSummaryMerchant(HttpServletRequest request,HttpServletResponse response){
		// 标题
		String[] headers = new String[] {"数据名称", "交易总金额(元)","统计交易开始时间","统计交易结束时间"}; 
		String filename = "直客渠道数据统计"; 
		Page page = new Page(request);
		//判断是否是平台商户
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		if (isPlatformAccount(customLogin)) {
			Integer integer = checkCustom(customLogin);
			page.getParams().put("businessPlatformId",String.valueOf(integer));
		}
		//根据businessPlatformId获取businessPlatform
		HashMap platformMap = channelCustomService.selectPlatformByCostomId(Integer.valueOf(page.getParams().get("businessPlatformId")));
		page.getParams().put("businessPlatform", (String) platformMap.get("businessPlatform"));
		//查询直客/渠道汇总交易
		List<Map<String,Object>> summaryList = new ArrayList<Map<String,Object>>();
		Map<String, Object> merchantSummary = new HashMap<String, Object>();
		Map<String, Object> agentSummary = new HashMap<String, Object>();
		String summaryAmount = userCommissionService.summaryInfoByMerchant(page.getParams());
		String agentSummaryAmount = userCommissionService.summaryInfoByAgent(page.getParams());
		merchantSummary.put("dataName", "直客");
		merchantSummary.put("startTime", page.getParams().get("startTime"));
		merchantSummary.put("endTime", page.getParams().get("endTime"));
		merchantSummary.put("amount", summaryAmount==null?"0":summaryAmount);
		agentSummary.put("dataName", "渠道");
		agentSummary.put("startTime", page.getParams().get("startTime"));
		agentSummary.put("endTime", page.getParams().get("endTime"));
		agentSummary.put("amount",agentSummaryAmount==null?"0":agentSummaryAmount);
		summaryList.add(merchantSummary);
		summaryList.add(agentSummary);
		List<Map<String, Object>> data = new ArrayList<>();
		for (Map<String, Object> summary : summaryList) {
			Map<String, Object> dataMap = new HashMap<>(20);
			dataMap.put("1", summary.get("dataName"));
			dataMap.put("2", summary.get("amount"));
			dataMap.put("3", summary.get("startTime"));
			dataMap.put("4",summary.get("endTime"));
			data.add(sortMapByKey(dataMap));
		}
		ExcelFileGenerator.ExcelExport(response, headers, filename, data);
	}

	/**
	 * 直客渠道数据明细统计导出
	 * @param request
	 * @param response
	 */
	@RequestMapping("/exportSummaryMerchantDetail")
	@ResponseBody
	public void exportSummaryMerchantDetail(HttpServletRequest request,HttpServletResponse response){
		// 标题
		String[] headers = new String[] {"数据名称", "交易总金额(元)","统计交易开始时间","统计交易结束时间"}; 
		String filename = "直客渠道数据明细统计"; 
		Page page = new Page(request);
		//判断是否是平台商户
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		if (isPlatformAccount(customLogin)) {
			Integer integer = checkCustom(customLogin);
			page.getParams().put("businessPlatformId",String.valueOf(integer));
		}
		//根据businessPlatformId获取businessPlatform
		HashMap platformMap = channelCustomService.selectPlatformByCostomId(Integer.valueOf(page.getParams().get("businessPlatformId")));
		page.getParams().put("businessPlatform", (String) platformMap.get("businessPlatform"));
		List<Map<String, Object>> summaryList = new ArrayList<Map<String,Object>>();
		if(page.getParams().get("dataType")!=null){
			if(page.getParams().get("dataType").equals("1")){
				//查询直客商户分组统计数据
				summaryList = userCommissionService.summaryInfoGroupMerchantNoPage(page);
			}else{
				//查询渠道分组统计数据
				summaryList = userCommissionService.summaryInfoGroupAgentNoPage(page);
			}
		}
		List<Map<String, Object>> data = new ArrayList<>();

		for (Map<String, Object> summary : summaryList) {
			Map<String, Object> dataMap = new HashMap<>(20);
			dataMap.put("1", summary.get("companyName"));
			dataMap.put("2", summary.get("amount"));
			dataMap.put("3", summary.get("startTime"));
			dataMap.put("4",summary.get("endTime"));
			data.add(sortMapByKey(dataMap));
		}
		ExcelFileGenerator.ExcelExport(response, headers, filename, data);
	}


	/**
	 * 统计直客/渠道月度统计信息
	 * @param request
	 * @return
	 */
	@RequestMapping("/summaryInfoMonthByMerchant")
	public ResponseEntity<?> summaryInfoMonthByMerchant(HttpServletRequest request){
		HashMap<String, Object> result = new HashMap<>();
		Page page = new Page(request);
		//判断是否是平台商户
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		if (isPlatformAccount(customLogin)) {
			Integer integer = checkCustom(customLogin);
			page.getParams().put("businessPlatformId",String.valueOf(integer));
		}
		//判断是否有平台ID
		if (StringUtil.isEmpty(page.getParams().get("businessPlatformId"))) {
			result.put(RespCode.RESP_STAT, RespCode.error101);
			result.put(RespCode.RESP_MSG, RespCode.PARAMS_ERROR);
			return new ResponseEntity<Object>(result,HttpStatus.OK);
		}
		//根据businessPlatformId获取businessPlatform
		HashMap platformMap = channelCustomService.selectPlatformByCostomId(Integer.valueOf(page.getParams().get("businessPlatformId")));
		if (platformMap == null || platformMap.size() == 0){
			result.put(RespCode.RESP_STAT, RespCode.PLATFORM_NOT_EXIST);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.PLATFORM_NOT_EXIST));
			return new ResponseEntity<Object>(result,HttpStatus.OK);
		}
		page.getParams().put("businessPlatform", (String) platformMap.get("businessPlatform"));
		List<Map<String, Object>> relationList = userCommissionService.summaryInfoByMerchantMonth(page);
		int count = userCommissionService.summaryInfoByMerchantMonthCount(page);
		String amount = userCommissionService.summaryInfoByMonth(page);
		result.put("totalAmount", amount);
		result.put("total", count);
		result.put("relationList", relationList);
		result.put(RespCode.RESP_STAT, RespCode.success);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));
		return new ResponseEntity<Object>(result,HttpStatus.OK);
	}

	/**
	 * 直客/渠道月度汇总详情
	 * @param request
	 * @return
	 */
	@RequestMapping("/summaryInfoDetailByMerchantMonth")
	public ResponseEntity<?> summaryInfoDetailByMerchantMonth(HttpServletRequest request){
		HashMap<String, Object> result = new HashMap<>();
		Page page = new Page(request);
		//判断是否是平台商户
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		if (isPlatformAccount(customLogin)) {
			Integer integer = checkCustom(customLogin);
			page.getParams().put("businessPlatformId",String.valueOf(integer));
		}
		//判断是否有平台ID
		if (StringUtil.isEmpty(page.getParams().get("businessPlatformId"))) {
			result.put(RespCode.RESP_STAT, RespCode.error101);
			result.put(RespCode.RESP_MSG, RespCode.PARAMS_ERROR);
			return new ResponseEntity<Object>(result,HttpStatus.OK);
		}
		//根据businessPlatformId获取businessPlatform
		HashMap platformMap = channelCustomService.selectPlatformByCostomId(Integer.valueOf(page.getParams().get("businessPlatformId")));
		if (platformMap == null || platformMap.size() == 0){
			result.put(RespCode.RESP_STAT, RespCode.PLATFORM_NOT_EXIST);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.PLATFORM_NOT_EXIST));
			return new ResponseEntity<Object>(result,HttpStatus.OK);
		}
		page.getParams().put("businessPlatform", (String) platformMap.get("businessPlatform"));
		int total = 0;
		String totalAmount = "0";
		if(page.getParams().get("dataType")!=null){
			//查询直客商户分组统计数据
			List<Map<String, Object>> relationList = new ArrayList<Map<String,Object>>();
			if(page.getParams().get("dataType").equals("1")){
				//查询直客商户分组统计数据
				relationList = userCommissionService.summaryInfoGroupMerchantMonth(page);
				total = userCommissionService.summaryInfoGroupMerchantMonthCount(page);
			}else{
				relationList = userCommissionService.summaryInfoGroupAgentMonth(page);
				total = userCommissionService.summaryInfoGroupAgentMonthCount(page);
			}
			totalAmount = userCommissionService.summaryInfoByMonthDetail(page);
			result.put(RespCode.RESP_STAT, RespCode.success);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));
			result.put("relationList", relationList);
			result.put("total", total);
			result.put("totalAmount", totalAmount==null?"0":totalAmount);
		}else{
			result.put(RespCode.RESP_STAT, RespCode.error101);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.error101));
		}
		return new ResponseEntity<Object>(result,HttpStatus.OK);
	}

	/**
	 * 直客渠道数据月份统计导出
	 * @param request
	 * @param response
	 */
	@RequestMapping("/exportSummaryByMonth")
	@ResponseBody
	public void exportSummaryByMonth(HttpServletRequest request,HttpServletResponse response){
		// 标题
		String[] headers = new String[] {"数据名称","月份","小额实发金额(元)","大额实发金额(元)","交易总金额(元)"}; 
		String filename = "直客渠道数据明细月度统计"; 
		Page page = new Page(request);
		//判断是否是平台商户
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		if (isPlatformAccount(customLogin)) {
			Integer integer = checkCustom(customLogin);
			page.getParams().put("businessPlatformId",String.valueOf(integer));
		}
		//判断是否有平台ID
		if (StringUtil.isEmpty(page.getParams().get("businessPlatformId"))) {
			logger.info(RespCode.PARAMS_ERROR);
			return;
		}
		//根据businessPlatformId获取businessPlatform
		HashMap platformMap = channelCustomService.selectPlatformByCostomId(Integer.valueOf(page.getParams().get("businessPlatformId")));
		if (platformMap == null || platformMap.size() == 0){
			logger.info(RespCode.codeMaps.get(RespCode.PLATFORM_NOT_EXIST));
			return;
		}
		page.getParams().put("businessPlatform", (String) platformMap.get("businessPlatform"));
		List<Map<String, Object>> summaryList = new ArrayList<Map<String,Object>>();
		//查询直客商户分组统计数据
		summaryList = userCommissionService.summaryInfoByMerchantMonthNoPage(page);
		List<Map<String, Object>> data = new ArrayList<>();

		for (Map<String, Object> summary : summaryList) {
			Map<String, Object> dataMap = new HashMap<>(20);
			dataMap.put("1", summary.get("belongName"));
			dataMap.put("2", summary.get("month"));
			dataMap.put("3", summary.get("smallAmount"));
			dataMap.put("4", summary.get("bigAmount"));
			dataMap.put("5",summary.get("totalAmount"));
			data.add(sortMapByKey(dataMap));
		}
		ExcelFileGenerator.ExcelExport(response, headers, filename, data);
	}


	/**
	 * 直客渠道数据明细月份统计导出
	 * @param request
	 * @param response
	 */
	@RequestMapping("/exportSummaryDetailByMonth")
	@ResponseBody
	public void exportSummaryDetailByMonth(HttpServletRequest request,HttpServletResponse response){
		// 标题
		String[] headers = new String[] {"月份","代理商名称","商户名称","小额实发金额(元)","大额实发金额(元)","交易总金额(元)"}; 
		String filename = "直客渠道数据明细月度统计"; 
		Page page = new Page(request);
		//判断是否是平台商户
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		if (isPlatformAccount(customLogin)) {
			Integer integer = checkCustom(customLogin);
			page.getParams().put("businessPlatformId",String.valueOf(integer));
		}
		//判断是否有平台ID
		if (StringUtil.isEmpty(page.getParams().get("businessPlatformId"))) {
			logger.info(RespCode.PARAMS_ERROR);
			return;
		}
		//根据businessPlatformId获取businessPlatform
		HashMap platformMap = channelCustomService.selectPlatformByCostomId(Integer.valueOf(page.getParams().get("businessPlatformId")));
		if (platformMap == null || platformMap.size() == 0){
			logger.info(RespCode.codeMaps.get(RespCode.PLATFORM_NOT_EXIST));
			return;
		}
		page.getParams().put("businessPlatform", (String) platformMap.get("businessPlatform"));
		List<Map<String, Object>> summaryList = new ArrayList<Map<String,Object>>();
		//查询直客商户分组统计数据
		List<Map<String, Object>> data = new ArrayList<>();
		if(page.getParams().get("dataType").equals("1")){
			//查询直客商户分组统计数据
			summaryList = userCommissionService.summaryInfoGroupMerchantMonthNoPage(page);
			for (Map<String, Object> summary : summaryList) {
				Map<String, Object> dataMap = new HashMap<>(20);
				dataMap.put("1", summary.get("month"));
				dataMap.put("2", summary.get("agentName"));
				dataMap.put("3", summary.get("merchantName"));
				dataMap.put("4", summary.get("smallAmount"));
				dataMap.put("5",summary.get("bigAmount"));
				dataMap.put("6",summary.get("totalAmount"));
				data.add(sortMapByKey(dataMap));
			}
		}else{
			headers = new String[] {"月份","代理商名称","小额实发金额(元)","大额实发金额(元)","交易总金额(元)"}; 
			summaryList = userCommissionService.summaryInfoGroupAgentMonthNoPage(page);
			for (Map<String, Object> summary : summaryList) {
				Map<String, Object> dataMap = new HashMap<>(20);
				dataMap.put("1", summary.get("month"));
				dataMap.put("2", summary.get("agentName"));
				dataMap.put("3", summary.get("smallAmount"));
				dataMap.put("4",summary.get("bigAmount"));
				dataMap.put("5",summary.get("totalAmount"));
				data.add(sortMapByKey(dataMap));
			}
		}
		ExcelFileGenerator.ExcelExport(response, headers, filename, data);
	}

	@RequestMapping("/monthClearAccounts")
	public ResponseEntity<?> monthClearAccounts(HttpServletRequest request){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		Page page = new Page(request);
		boolean checkFlag = false;
		//获取登陆信息
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		Integer []allowCustomType = new Integer[]{6,7};
		checkFlag = channelCustomService.getCustomKeysByType(page.getParams(), allowCustomType, customLogin);
		//根据businessPlatformId获取businessPlatform
		String businessPlatformId = page.getParams().get("businessPlatformId");
		if (StringUtil.isEmpty(businessPlatformId)){
			respstat = RespCode.PLATFORM_NOT_EXIST;
			result.put(RespCode.RESP_STAT, respstat);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
			return new ResponseEntity<Object>(result,HttpStatus.OK);
		}else{
			HashMap platformMap = channelCustomService.selectPlatformByCostomId(Integer.valueOf(businessPlatformId));
			String businessPlatform = (String) platformMap.get("businessPlatform");
			page.getParams().put("businessPlatform",businessPlatform);
		}
		//1.操作权限校验
		if(checkFlag){
			//4.获取新数据
			List<Map<String, String>> clearTermlist = qbClearingAccountsService.groupClearTermMonthNew(page.getParams());
			Map<String, List<Map<String, String>>> clearAccountMap = getClearAccountMonth(clearTermlist);
			if(!clearAccountMap.isEmpty()){
				//汇总对应条件累计金额
				List<QbClearingAccounts> clearingAccountList = new LinkedList<QbClearingAccounts>();
				for (String key : clearAccountMap.keySet()) {
					List<Map<String, String>> merchantTransList = clearAccountMap.get(key);
					setClearAccountInfoNew(merchantTransList,clearingAccountList);
				}
				clearingAccountList = clearingAccountList.stream().sorted(Comparator.comparing(QbClearingAccounts::getTransMonth).thenComparing(QbClearingAccounts::getTotalAmount).reversed()).collect(Collectors.toList());
				List<String> listAmount = clearingAccountList.stream().map(QbClearingAccounts::getTotalAmount).collect(Collectors.toList());
				BigDecimal totalAmount = listAmount.stream().map(BigDecimal::new).reduce(BigDecimal.ZERO,BigDecimal::add);
				List clearingAccountPageList = ListUtil.startPage(clearingAccountList, page.getPageNo(), page.getPageSize());
				result.put("total", clearingAccountList.size());
				result.put("totalAmount", ArithmeticUtil.getScale(String.valueOf(totalAmount),2));
				result.put("relationList", clearingAccountPageList);
			}else{
				//该月份不存在清结算记录
				result.put("total", 0);
				result.put("totalAmount", "0");
				result.put("relationList", new ArrayList<QbClearingAccounts>());
			}
		}else{
			respstat = RespCode.DO_NOT_HAVE_APPROVAL_RIGHT;
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return new ResponseEntity<Object>(result,HttpStatus.OK);
	}

	/**
	 * 获取清结算数据
	 * @param clearTermlist
	 * @return
	 */
	public Map<String, List<Map<String, String>>> getClearAccountMonth(List<Map<String, String>> clearTermlist){
		Map<String, List<Map<String, String>>> clearAccountMap = new HashMap<String, List<Map<String,String>>>();
		for (Map<String, String> clearTerm: clearTermlist) {
			String mapKey = "";
			if(!StringUtil.isEmpty(clearTerm.get("businessChannel"))){
				mapKey = clearTerm.get("originalId")+clearTerm.get("companyId")+clearTerm.get("month")+clearTerm.get("businessChannel");
			}else{
				mapKey = clearTerm.get("originalId")+clearTerm.get("companyId")+clearTerm.get("month");
			}
			if(clearAccountMap.containsKey(mapKey)){
				clearAccountMap.get(mapKey).add(clearTerm);
			}else{
				List<Map<String, String>> merchantTransList = new ArrayList<Map<String,String>>();
				merchantTransList.add(clearTerm);
				clearAccountMap.put(mapKey, merchantTransList);
			}
		}
		return clearAccountMap;
	}

	/**
	 * 设置清结算报表数据
	 * @param merchantTransList
	 */
	public void setClearAccountInfoNew(List<Map<String, String>> merchantTransList,List<QbClearingAccounts> clearingAccountList){
		List<Map<String, String>> merchantTransOverList = new ArrayList<Map<String,String>>();
		String gearPositionDesc = "";
		String merchantRateRuleDesc = "";
		int smallCount = 1;
		int bigCount = 1;
		Map<String, String> merchantTrans = merchantTransList.get(0);
		String merchantName = merchantTrans.get("merchantName");
		String companyName = merchantTrans.get("companyName");
		String agentName = merchantTrans.get("agentName");
		String agentId = merchantTrans.get("businessChannel");
		String companyId = merchantTrans.get("companyId");
		String customkey = merchantTrans.get("originalId");
		String transMonth = merchantTrans.get("month");
		String businessManager = merchantTrans.get("businessManager");
		QbClearingAccounts clearingAccounts = new QbClearingAccounts();
		clearingAccounts.setTransMonth(transMonth);
		clearingAccounts.setCompanyId(companyId);
		clearingAccounts.setCompanyName(companyName);
		clearingAccounts.setCustomkey(customkey);
		clearingAccounts.setMerchantName(merchantName);
		clearingAccounts.setAgentId(agentId);
		clearingAccounts.setAgentName(agentName);
		clearingAccounts.setBusinessManager(businessManager);
		Map<String, String> rateIntervalMap = new HashMap<String, String>();
		for (int i = 0;i<merchantTransList.size();i++) {
			Map<String, String> merchantTranMap = merchantTransList.get(i);
			//设置挡位、费率、累加金额信息/
			String rateInterval = merchantTranMap.get("gear");
			String merchantRate = merchantTranMap.get("customRate");
			if(rateIntervalMap.containsKey(rateInterval)){
				merchantTransOverList.add(merchantTranMap);
			}else{
				rateIntervalMap.put(rateInterval, merchantRate);
				String totalAmount = String.valueOf(merchantTranMap.get("amount"));
				String proxyFeeRate = StringUtil.isEmpty(merchantTranMap.get("proxyFeeRate"))?"":ArithmeticUtil.mulStr(merchantTranMap.get("proxyFeeRate"),"100")+"%";
				if(!StringUtil.isEmpty(String.valueOf(merchantTranMap.get("gearLabel")))){
					String gearLaber = GearLaberType.codeOf(Integer.parseInt(String.valueOf(merchantTranMap.get("gearLabel")))).getDesc();
					String merchantRateRule = ArithmeticUtil.mulStr(merchantRate,"100")+"%";
					if(Integer.parseInt(String.valueOf(merchantTranMap.get("gearLabel")))==1){
						if(StringUtil.isEmpty(clearingAccounts.getSmallAmountOne())||StringUtil.isEmpty(clearingAccounts.getSmallAmountTwo())){
							gearPositionDesc+=rateInterval+",实发金额"+gearLaber+smallCount+"\n";
							merchantRateRuleDesc+=rateInterval+",实发金额"+gearLaber+smallCount+":"+merchantRateRule+"\n";
						}
					}else{
						if(StringUtil.isEmpty(clearingAccounts.getBigAmount())){
							gearPositionDesc+=rateInterval+",实发金额"+gearLaber+bigCount+"\n";
							merchantRateRuleDesc+=rateInterval+",实发金额"+gearLaber+bigCount+":"+merchantRateRule+"\n";
						}
					}
					if(Integer.parseInt(String.valueOf(merchantTranMap.get("gearLabel")))==1){
						clearingAccounts.setAgentSmallRate(proxyFeeRate);
						if(StringUtil.isEmpty(clearingAccounts.getSmallAmountOne())){
							//设置小额1
							clearingAccounts.setSmallAmountOne(totalAmount);
							clearingAccounts.setSmallAmountOneRate(merchantRateRule);
							//判断是否商户存在变更费率，若存在获取生效日期类的下发金额，并计算手续费
							List<Map<String, String>> merRateUpdateList = qbClearingAccountsService.getMerRateUpdate(merchantTranMap);
							if(merRateUpdateList!=null && merRateUpdateList.size()>0){
								Map<String, String> differMap = qbClearingAccountsService.differSummary(merchantTranMap, merchantRate,merRateUpdateList,1);
								clearingAccounts.setSmallAmountOneRateUpdate(differMap.get("updateRateDesc"));
								clearingAccounts.setDifferSmallOneMerAmount(differMap.get("differAmount"));
								clearingAccounts.setDifferSmallOneMerAmountDesc(differMap.get("differAmountDesc"));
							}

							//判断是否代理存在变更费率，若存在获取生效日期类的下发金额，并计算手续费
							List<Map<String, String>> agentRateUpdateList = qbClearingAccountsService.getAgentRateUpdate(merchantTranMap);
							if(agentRateUpdateList!=null && agentRateUpdateList.size()>0){
								Map<String, String> differMap = qbClearingAccountsService.differSummary(merchantTranMap,merchantTranMap.get("proxyFeeRate"),agentRateUpdateList,2);
								clearingAccounts.setDifferAgentSmallAmount(differMap.get("differAmount"));
								clearingAccounts.setAgentSmallRateUpdate(differMap.get("updateRateDesc"));
								clearingAccounts.setDifferAgentSmallAmountDesc(differMap.get("differAmountDesc"));
							}
						}else{
							if(StringUtil.isEmpty(clearingAccounts.getSmallAmountTwo())){
								//设置小额2
								clearingAccounts.setSmallAmountTwo(totalAmount);
								clearingAccounts.setSmallAmountTwoRate(merchantRateRule);
								List<Map<String, String>> merRateUpdateList = qbClearingAccountsService.getMerRateUpdate(merchantTranMap);
								if(merRateUpdateList!=null && merRateUpdateList.size()>0){
									Map<String, String> differMap = qbClearingAccountsService.differSummary(merchantTranMap, merchantRate,merRateUpdateList,1);
									clearingAccounts.setSmallAmountTwoRateUpdate(differMap.get("updateRateDesc"));
									clearingAccounts.setDifferSmallTwoMerAmount(differMap.get("differAmount"));
									clearingAccounts.setDifferSmallTwoMerAmountDesc(differMap.get("differAmountDesc"));
								}
								//判断是否代理存在变更费率，若存在获取生效日期类的下发金额，并计算手续费
								List<Map<String, String>> agentRateUpdateList = qbClearingAccountsService.getAgentRateUpdate(merchantTranMap);
								if(agentRateUpdateList!=null && agentRateUpdateList.size()>0){
									Map<String, String> differMap = qbClearingAccountsService.differSummary(merchantTranMap,merchantTranMap.get("proxyFeeRate"),agentRateUpdateList,2);
									String differAmount = ArithmeticUtil.addStr(clearingAccounts.getDifferAgentSmallAmount(), differMap.get("differAmount"));
									clearingAccounts.setDifferAgentSmallAmount(differAmount);
									String []differAgentAmountArray = clearingAccounts.getDifferAgentSmallAmountDesc().split(";");
									String []differAgentAmountArrayTwo =  differMap.get("differAmountDesc").split(";");
									String differAgentAmountDesc = "";
									for(int j=0;j<differAgentAmountArray.length;j++){
										String differAgentAmount= differAgentAmountArray[j];
										String differAgentAmountTwo= differAgentAmountArrayTwo[j];
										String differAgentAmountTotal = ArithmeticUtil.addStr(differAgentAmount, differAgentAmountTwo);
										differAgentAmountDesc = differAgentAmountDesc+differAgentAmountTotal+";";
									}
									clearingAccounts.setDifferAgentSmallAmountDesc(differAgentAmountDesc);
								}
							}else{
								merchantTransOverList.add(merchantTranMap);
							}
						}
						smallCount++;
					}else{
						if(StringUtil.isEmpty(clearingAccounts.getBigAmount())){
							//设置大额
							clearingAccounts.setAgentBigRate(proxyFeeRate);
							clearingAccounts.setBigAmount(totalAmount);
							clearingAccounts.setBigAmountRate(merchantRateRule);
							clearingAccounts.setCompanyBigAmount(totalAmount);
							//判断是否商户存在变更费率，若存在获取生效日期类的下发金额，并计算手续费
							List<Map<String, String>> merRateUpdateList = qbClearingAccountsService.getMerRateUpdate(merchantTranMap);
							if(merRateUpdateList!=null && merRateUpdateList.size()>0){
								Map<String, String> differMap = qbClearingAccountsService.differSummary(merchantTranMap, merchantRate,merRateUpdateList,1);
								clearingAccounts.setBigAmountRateUpdate(differMap.get("updateRateDesc"));
								clearingAccounts.setDifferBigMerAmount(differMap.get("differAmount"));
								clearingAccounts.setDifferBigMerAmountDesc(differMap.get("differAmountDesc"));
							}

							//判断是否代理存在变更费率，若存在获取生效日期类的下发金额，并计算手续费
							List<Map<String, String>> agentRateUpdateList = qbClearingAccountsService.getAgentRateUpdate(merchantTranMap);
							if(agentRateUpdateList!=null && agentRateUpdateList.size()>0){
								Map<String, String> differMap = qbClearingAccountsService.differSummary(merchantTranMap,merchantTranMap.get("proxyFeeRate"),agentRateUpdateList,2);
								clearingAccounts.setAgentBigRateUpdate(differMap.get("updateRateDesc"));
								clearingAccounts.setDifferAgentBigAmount(differMap.get("differAmount"));
								clearingAccounts.setDifferAgentBigAmountDesc(differMap.get("differAmountDesc"));
							}
						}else{
							merchantTransOverList.add(merchantTranMap);
						}
						bigCount++;
					}
				}
			}
		}
		//设置商户费率规则
		clearingAccounts.setMerchantRateRule(merchantRateRuleDesc);
		//设置签约实发金额当位值
		clearingAccounts.setGearLaber(gearPositionDesc);
		//商户实发小额累计=商户实发小额1+商户实发小额2+商户实发大额
		String totalAmount = ArithmeticUtil.addStr(clearingAccounts.getSmallAmountOne(),clearingAccounts.getSmallAmountTwo());
		totalAmount = ArithmeticUtil.addStr(totalAmount, clearingAccounts.getBigAmount());
		clearingAccounts.setTotalAmount(totalAmount);
		//服务公司小额=商户实发小额1+商户实发小额2
		String companySmallAmount = ArithmeticUtil.addStr(clearingAccounts.getSmallAmountOne(), clearingAccounts.getSmallAmountTwo());
		clearingAccounts.setCompanySmallAmount(companySmallAmount);
		String agentSamllRate = clearingAccounts.getAgentSmallRate()==null?"0":ArithmeticUtil.divideStr2(clearingAccounts.getAgentSmallRate().replace("%", ""),"100");
		String agentBigRate = clearingAccounts.getAgentBigRate()==null?"0":ArithmeticUtil.divideStr2(clearingAccounts.getAgentBigRate().replace("%", ""),"100");
		String agentSmallAmount = ArithmeticUtil.mulStr(companySmallAmount, agentSamllRate,2);
		String agentBigAmount = ArithmeticUtil.mulStr(clearingAccounts.getCompanyBigAmount(), agentBigRate,2);
		String agentTotalAmount = ArithmeticUtil.addStr(agentSmallAmount, agentBigAmount);
		clearingAccounts.setAgentSmallAmount(agentSmallAmount);
		clearingAccounts.setAgentBigAmount(agentBigAmount);
		clearingAccounts.setAgentTotalAmount(agentTotalAmount);
		String merchantSmallOneRate = clearingAccounts.getSmallAmountOneRate()==null?"0":ArithmeticUtil.divideStr2(clearingAccounts.getSmallAmountOneRate().replace("%", ""),"100");
		String merchantSmallTwoRate = clearingAccounts.getSmallAmountTwoRate()==null?"0":ArithmeticUtil.divideStr2(clearingAccounts.getSmallAmountTwoRate().replace("%", ""),"100");
		String merchantBigRate = clearingAccounts.getBigAmountRate()==null?"0":ArithmeticUtil.divideStr2(clearingAccounts.getBigAmountRate().replace("%", ""),"100");
		String smallAmountOne = ArithmeticUtil.mulStr(clearingAccounts.getSmallAmountOne(), merchantSmallOneRate,2);
		String smallAmountTwo = ArithmeticUtil.mulStr(clearingAccounts.getSmallAmountTwo(), merchantSmallTwoRate,2);
		String bigAmount = ArithmeticUtil.mulStr(clearingAccounts.getBigAmount(), merchantBigRate,2);
		String merchantTotalAmount = ArithmeticUtil.addStr(smallAmountOne, smallAmountTwo);
		merchantTotalAmount = ArithmeticUtil.addStr(merchantTotalAmount, bigAmount);
		String agentCommission = ArithmeticUtil.subStr2(merchantTotalAmount, agentTotalAmount);
		clearingAccounts.setAgentCommission(agentCommission);
		clearingAccounts.setCreateTime(DateUtils.getNowDate());
		clearingAccountList.add(clearingAccounts);

		boolean size = merchantTransOverList.size()>0;
		if(size){
			setClearAccountInfoNew(merchantTransOverList,clearingAccountList);
		}
	}

	/**
	 * 导出
	 * @param request
	 */
	@RequestMapping("/exportMonth")
	public void exportMonth(HttpServletRequest request,HttpServletResponse response){
		try{
			// 标题
			String filename = "月度综合报表"; 
			Page page = new Page(request);
			//校验是否有权限
			boolean checkFlag = true;
			//获取登陆信息
			ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
			Integer []allowCustomType = new Integer[]{6,7};
			checkFlag = channelCustomService.getCustomKeysByType(page.getParams(), allowCustomType, customLogin);
			//根据businessPlatformId获取businessPlatform
			String businessPlatformId = page.getParams().get("businessPlatformId");
			HashMap platformMap = channelCustomService.selectPlatformByCostomId(Integer.valueOf(businessPlatformId));
			String businessPlatform = (String) platformMap.get("businessPlatform");
			page.getParams().put("businessPlatform",businessPlatform);
			if(checkFlag){
				//4.获取新数据
				List<Map<String, String>> clearTermlist = qbClearingAccountsService.groupClearTermMonthNew(page.getParams());
				Map<String, List<Map<String, String>>> clearAccountMap = getClearAccountMonth(clearTermlist);
				if(!clearAccountMap.isEmpty()){
					//汇总对应条件累计金额
					List<QbClearingAccounts> clearingAccountList = new ArrayList<QbClearingAccounts>();
					for (String key : clearAccountMap.keySet()) {
						List<Map<String, String>> merchantTransList = clearAccountMap.get(key);
						setClearAccountInfoNew(merchantTransList,clearingAccountList);
					}
					//分页查询白名单信息
					String excelpath="/excel/clearAccountMonthTemplate.xls";
					HSSFSheet hssfSheet = ExcelUtil.getSheet(excelpath, 0,true);
					HSSFWorkbook wb = hssfSheet.getWorkbook();
					addalentDemandsMonth(hssfSheet,clearingAccountList);
					ExcelUtil.exportExcel(response, filename, wb);
				}
			}else{
				logger.info("权限不足，无法操作");
			}
		}catch(Exception e){
			logger.error(e.getMessage(), e);
		}
	}

	private static void addalentDemandsMonth(HSSFSheet talentDemandSheet, List<QbClearingAccounts> talentDemands)
			throws IllegalArgumentException, IllegalAccessException {
		Row talentDemandRow = talentDemandSheet.getRow(2);
		// 如果数据大于模板中的行数，插入行并复制第一行数据的格式
		if (talentDemands.size() > 5) {
			// 插入行，5是模板中已有的行数
			talentDemandSheet.shiftRows(3, talentDemandSheet.getLastRowNum(), talentDemands.size() - 5, true, false);
			HSSFRow sourceRow = talentDemandSheet.getRow(2);
			for (int i = 0; i < talentDemands.size() - 5; i++) {
				HSSFRow newRow = talentDemandSheet.createRow(2 + i + 1);
				newRow.setHeight(sourceRow.getHeight());
				for (int j = 0; j < sourceRow.getLastCellNum(); j++) {
					HSSFCell templateCell = sourceRow.getCell(j);
					if (templateCell != null) {
						HSSFCell newCell = newRow.createCell(j);
						ExcelCopyUtil.copyCell(templateCell, newCell);
					}
				}
			}
		}
		// 填充数据
		for (int i = 0; i < talentDemands.size(); i++) {
			talentDemandRow = talentDemandSheet.getRow(2 + i);
			talentDemandRow.getCell(0).setCellValue(talentDemands.get(i).getTransMonth());
			talentDemandRow.getCell(1).setCellValue(talentDemands.get(i).getMerchantName());
			talentDemandRow.getCell(2).setCellValue(talentDemands.get(i).getAgentName());
			talentDemandRow.getCell(3).setCellValue(talentDemands.get(i).getBusinessManager());
			talentDemandRow.getCell(4).setCellValue(talentDemands.get(i).getCompanyName());
			talentDemandRow.getCell(5).setCellValue(talentDemands.get(i).getSmallAmountOne());
			talentDemandRow.getCell(6).setCellValue(talentDemands.get(i).getSmallAmountTwo());
			talentDemandRow.getCell(7).setCellValue(talentDemands.get(i).getBigAmount());
			talentDemandRow.getCell(8).setCellValue(talentDemands.get(i).getTotalAmount());
			talentDemandRow.getCell(9).setCellValue(talentDemands.get(i).getSmallAmountOneRate());
			talentDemandRow.getCell(10).setCellValue(talentDemands.get(i).getSmallAmountTwoRate());
			talentDemandRow.getCell(11).setCellValue(talentDemands.get(i).getBigAmountRate());
			talentDemandRow.getCell(12).setCellValue(talentDemands.get(i).getSmallAmountOneRateUpdate());
			talentDemandRow.getCell(13).setCellValue(talentDemands.get(i).getSmallAmountTwoRateUpdate());
			talentDemandRow.getCell(14).setCellValue(talentDemands.get(i).getBigAmountRateUpdate());
			talentDemandRow.getCell(15).setCellValue(talentDemands.get(i).getAgentSmallRate());
			talentDemandRow.getCell(16).setCellValue(talentDemands.get(i).getAgentBigRate());
			talentDemandRow.getCell(17).setCellValue(talentDemands.get(i).getAgentSmallRateUpdate());
			talentDemandRow.getCell(18).setCellValue(talentDemands.get(i).getAgentBigRateUpdate());
		}
	}	

}
