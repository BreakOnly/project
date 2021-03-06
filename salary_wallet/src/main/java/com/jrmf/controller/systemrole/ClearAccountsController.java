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

	//????????????service
	@Autowired
	private ChannelCustomService channelCustomService;
	@Autowired
	private QbClearingAccountsService qbClearingAccountsService;
	@Autowired
	private UserCommissionService userCommissionService;

	/**
	 * ?????????????????????
	 * @param request
	 * @return
	 */
	@RequestMapping("/queryList")
	public ResponseEntity<?> queryList(HttpServletRequest request){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		Page page = new Page(request);
		//?????????????????????
		boolean checkFlag = true;
		//??????????????????
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		Integer []allowCustomType = new Integer[]{6,7};
		checkFlag = channelCustomService.getCustomKeysByType(page.getParams(), allowCustomType, customLogin);
		if(checkFlag){
			//???????????????????????????
			PageHelper.startPage(page.getPageNo(),page.getPageSize());
			List<Map<String, Object>> relationList = qbClearingAccountsService.getClearAccountsByPage(page);
			//????????????????????????
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
	 * ??????
	 * @param request
	 */
	@RequestMapping("/export")
	public void export(HttpServletRequest request,HttpServletResponse response){
		try{
			// ??????
			String filename = "???????????????"; 
			Page page = new Page(request);
			//?????????????????????
			boolean checkFlag = true;
			//??????????????????
			ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
			Integer []allowCustomType = new Integer[]{6,7};
			checkFlag = channelCustomService.getCustomKeysByType(page.getParams(), allowCustomType, customLogin);
			if(checkFlag){
				//???????????????????????????
				List<QbClearingAccounts> clearAccountList = qbClearingAccountsService.getClearAccountsNoPage(page);
				String excelpath="/excel/clearAccountTemplate.xls";
				HSSFSheet hssfSheet = ExcelUtil.getSheet(excelpath, 0,true);
				HSSFWorkbook wb = hssfSheet.getWorkbook();
				addalentDemands(hssfSheet,clearAccountList);
				ExcelUtil.exportExcel(response, filename, wb);
			}else{
				logger.info("???????????????????????????");
			}
		}catch(Exception e){
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * ?????????????????????
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
		//??????????????????
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		Integer []allowCustomType = new Integer[]{6,7};
		checkFlag = channelCustomService.getCustomKeysByType(page.getParams(), allowCustomType, customLogin);
		if (StringUtil.isEmpty(businessPlatformId)){
			businessPlatformId = page.getParams().get("businessPlatformId");
		}

		Map<String,Object> platformParams = new HashMap<>();
		if (!StringUtil.isEmpty(merchantId)){
			//?????????????????????????????????????????????
			platformParams.put("businessPlatformId",businessPlatformId);
			platformParams.put("customkey",merchantId);
			List<ChannelCustom> channelCustomList = channelCustomService.getCustomByParam(platformParams);
			if (channelCustomList == null || channelCustomList.size() == 0){
				result.put(RespCode.RESP_STAT, RespCode.PLATFORM_NOT_MERCHANT);
				result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.PLATFORM_NOT_MERCHANT));
				return new ResponseEntity<Object>(result,HttpStatus.OK);
			}
		}
		//1.??????????????????
		if(checkFlag){
			Map<String, String> params = new HashMap<String, String>();
			//??????businessPlatformId??????businessPlatform
			HashMap platformMap = channelCustomService.selectPlatformByCostomId(Integer.valueOf(businessPlatformId));
			params.put("businessPlatform", (String) platformMap.get("businessPlatform"));
			//2.????????????
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
				//3.???????????????
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
					//4.???????????????
					List<Map<String, String>> clearTermlist = new ArrayList<Map<String,String>>();
					clearTermlist = qbClearingAccountsService.groupClearTermMonthNew(params);
					//5.????????????
					Map<String, List<Map<String, String>>> clearAccountMap = getClearAccountMonth(clearTermlist);
					if(!clearAccountMap.isEmpty()){
						//??????????????????????????????
						List<QbClearingAccounts> clearingAccountList = new ArrayList<QbClearingAccounts>();
						for (String key : clearAccountMap.keySet()) {
							List<Map<String, String>> merchantTransList = clearAccountMap.get(key);
							setClearAccountInfo(merchantTransList,clearingAccountList);
						}
						//???????????????
						for (QbClearingAccounts qbClearingAccounts : clearingAccountList) {
							qbClearingAccountsService.insert(qbClearingAccounts);
						}
					}else{
						//?????????????????????????????????
						respstat = RespCode.CLEARACCOUNTS_IS_NULL;
					}
				}else{
					//?????????????????????
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
	 * ?????????sheet?????????????????????????????????
	 */
	private static void addalentDemands(HSSFSheet talentDemandSheet, List<QbClearingAccounts> talentDemands)
			throws IllegalArgumentException, IllegalAccessException {
		Row talentDemandRow = talentDemandSheet.getRow(2);
		// ?????????????????????????????????????????????????????????????????????????????????
		if (talentDemands.size() > 5) {
			// ????????????5???????????????????????????
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
		// ????????????
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
	 * ???????????????????????????
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
			//??????????????????????????????????????????
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
							gearPositionDesc+=rateInterval+",????????????"+gearLaber+smallCount+"\n";
							merchantRateRuleDesc+=rateInterval+",????????????"+gearLaber+smallCount+":"+merchantRateRule+"\n";
						}
					}else{
						if(StringUtil.isEmpty(clearingAccounts.getBigAmount())){
							gearPositionDesc+=rateInterval+",????????????"+gearLaber+bigCount+"\n";
							merchantRateRuleDesc+=rateInterval+",????????????"+gearLaber+bigCount+":"+merchantRateRule+"\n";
						}
					}
					if(Integer.parseInt(String.valueOf(merchantTranMap.get("gearLabel")))==1){
						clearingAccounts.setAgentSmallRate(proxyFeeRate);
						if(StringUtil.isEmpty(clearingAccounts.getSmallAmountOne())){
							//????????????1
							clearingAccounts.setSmallAmountOne(totalAmount);
							clearingAccounts.setSmallAmountOneRate(merchantRateRule);
							String fee = ArithmeticUtil.mulStr(totalAmount, merchantRate, 2);
							clearingAccounts.setSmallAmountOneFee(fee);
							//?????????????????????????????????????????????????????????????????????????????????????????????????????????
							List<Map<String, String>> merRateUpdateList = qbClearingAccountsService.getMerRateUpdate(merchantTranMap);
							if(merRateUpdateList!=null && merRateUpdateList.size()>0){
								Map<String, String> differMap = qbClearingAccountsService.differSummary(merchantTranMap, merchantRate,merRateUpdateList,1);
								clearingAccounts.setSmallAmountOneRateUpdate(differMap.get("updateRateDesc"));
								clearingAccounts.setDifferSmallOneMerAmount(differMap.get("differAmount"));
								clearingAccounts.setDifferSmallOneMerAmountDesc(differMap.get("differAmountDesc"));
							}

							//?????????????????????????????????????????????????????????????????????????????????????????????????????????
							List<Map<String, String>> agentRateUpdateList = qbClearingAccountsService.getAgentRateUpdate(merchantTranMap);
							if(agentRateUpdateList!=null && agentRateUpdateList.size()>0){
								Map<String, String> differMap = qbClearingAccountsService.differSummary(merchantTranMap,merchantTranMap.get("proxyFeeRate"),agentRateUpdateList,2);
								clearingAccounts.setDifferAgentSmallAmount(differMap.get("differAmount"));
								clearingAccounts.setAgentSmallRateUpdate(differMap.get("updateRateDesc"));
								clearingAccounts.setDifferAgentSmallAmountDesc(differMap.get("differAmountDesc"));
							}

						}else{
							if(StringUtil.isEmpty(clearingAccounts.getSmallAmountTwo())){
								//????????????2
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
								//?????????????????????????????????????????????????????????????????????????????????????????????????????????
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
							//????????????
							clearingAccounts.setAgentBigRate(proxyFeeRate);
							clearingAccounts.setBigAmount(totalAmount);
							clearingAccounts.setBigAmountRate(merchantRateRule);
							clearingAccounts.setCompanyBigAmount(totalAmount);
							String fee = ArithmeticUtil.mulStr(totalAmount, merchantRate, 2);
							clearingAccounts.setBigAmountFee(fee);
							//?????????????????????????????????????????????????????????????????????????????????????????????????????????
							List<Map<String, String>> merRateUpdateList = qbClearingAccountsService.getMerRateUpdate(merchantTranMap);
							if(merRateUpdateList!=null && merRateUpdateList.size()>0){
								Map<String, String> differMap = qbClearingAccountsService.differSummary(merchantTranMap, merchantRate,merRateUpdateList,1);
								clearingAccounts.setBigAmountRateUpdate(differMap.get("updateRateDesc"));
								clearingAccounts.setDifferBigMerAmount(differMap.get("differAmount"));
								clearingAccounts.setDifferBigMerAmountDesc(differMap.get("differAmountDesc"));
							}

							//?????????????????????????????????????????????????????????????????????????????????????????????????????????
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
		//????????????????????????
		clearingAccounts.setMerchantRateRule(merchantRateRuleDesc);
		//?????????????????????????????????
		clearingAccounts.setGearLaber(gearPositionDesc);
		//????????????????????????=??????????????????1+??????????????????2+??????????????????
		String totalAmount = ArithmeticUtil.addStr(clearingAccounts.getSmallAmountOne(),clearingAccounts.getSmallAmountTwo());
		totalAmount = ArithmeticUtil.addStr(totalAmount, clearingAccounts.getBigAmount());
		clearingAccounts.setTotalAmount(totalAmount);
		//??????????????????=??????????????????1+??????????????????2
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
		//????????????????????????????????????
		String differSmallAmount = ArithmeticUtil.addStr(clearingAccounts.getDifferSmallOneMerAmount(), clearingAccounts.getDifferSmallTwoMerAmount());
		//????????????????????????????????????
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
	 * ????????????/??????????????????
	 * @param request
	 * @return
	 */
	@RequestMapping("/summaryInfoByMerchant")
	public ResponseEntity<?> summaryInfoByMerchant(HttpServletRequest request){
		HashMap<String, Object> result = new HashMap<>();
		Page page = new Page(request);
		//???????????????????????????
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		if (isPlatformAccount(customLogin)) {
			Integer integer = checkCustom(customLogin);
			page.getParams().put("businessPlatformId",String.valueOf(integer));
		}
		//?????????????????????ID
		if (StringUtil.isEmpty(page.getParams().get("businessPlatformId"))) {
			result.put(RespCode.RESP_STAT, RespCode.error101);
			result.put(RespCode.RESP_MSG, RespCode.PARAMS_ERROR);
			return new ResponseEntity<Object>(result,HttpStatus.OK);
		}
		//??????businessPlatformId??????businessPlatform
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
		merchantSummary.put("dataName", "??????");
		merchantSummary.put("startTime", page.getParams().get("startTime"));
		merchantSummary.put("endTime", page.getParams().get("endTime"));
		merchantSummary.put("amount", summaryAmount==null?"0":summaryAmount);
		agentSummary.put("dataName", "??????");
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
	 * ??????/??????????????????
	 * @param request
	 * @return
	 */
	@RequestMapping("/summaryInfoByMerchantDetail")
	public ResponseEntity<?> summaryInfoByMerchantDetail(HttpServletRequest request){
		HashMap<String, Object> result = new HashMap<>();
		Page page = new Page(request);
		//???????????????????????????
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		if (isPlatformAccount(customLogin)) {
			Integer integer = checkCustom(customLogin);
			page.getParams().put("businessPlatformId",String.valueOf(integer));
		}
		//?????????????????????ID
		if (StringUtil.isEmpty(page.getParams().get("businessPlatformId"))) {
			result.put(RespCode.RESP_STAT, RespCode.error101);
			result.put(RespCode.RESP_MSG, RespCode.PARAMS_ERROR);
			return new ResponseEntity<Object>(result,HttpStatus.OK);
		}
		//??????businessPlatformId??????businessPlatform
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
				//????????????????????????????????????
				summaryList = userCommissionService.summaryInfoGroupMerchant(page);
				total = userCommissionService.summaryInfoGroupMerchantCount(page);
				totalAmount = userCommissionService.summaryInfoByMerchant(page.getParams());
			}else{
				//??????????????????????????????
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
	 * ??????????????????????????????
	 * @param request
	 * @param response
	 */
	@RequestMapping("/exportSummaryMerchant")
	@ResponseBody
	public void exportSummaryMerchant(HttpServletRequest request,HttpServletResponse response){
		// ??????
		String[] headers = new String[] {"????????????", "???????????????(???)","????????????????????????","????????????????????????"}; 
		String filename = "????????????????????????"; 
		Page page = new Page(request);
		//???????????????????????????
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		if (isPlatformAccount(customLogin)) {
			Integer integer = checkCustom(customLogin);
			page.getParams().put("businessPlatformId",String.valueOf(integer));
		}
		//??????businessPlatformId??????businessPlatform
		HashMap platformMap = channelCustomService.selectPlatformByCostomId(Integer.valueOf(page.getParams().get("businessPlatformId")));
		page.getParams().put("businessPlatform", (String) platformMap.get("businessPlatform"));
		//????????????/??????????????????
		List<Map<String,Object>> summaryList = new ArrayList<Map<String,Object>>();
		Map<String, Object> merchantSummary = new HashMap<String, Object>();
		Map<String, Object> agentSummary = new HashMap<String, Object>();
		String summaryAmount = userCommissionService.summaryInfoByMerchant(page.getParams());
		String agentSummaryAmount = userCommissionService.summaryInfoByAgent(page.getParams());
		merchantSummary.put("dataName", "??????");
		merchantSummary.put("startTime", page.getParams().get("startTime"));
		merchantSummary.put("endTime", page.getParams().get("endTime"));
		merchantSummary.put("amount", summaryAmount==null?"0":summaryAmount);
		agentSummary.put("dataName", "??????");
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
	 * ????????????????????????????????????
	 * @param request
	 * @param response
	 */
	@RequestMapping("/exportSummaryMerchantDetail")
	@ResponseBody
	public void exportSummaryMerchantDetail(HttpServletRequest request,HttpServletResponse response){
		// ??????
		String[] headers = new String[] {"????????????", "???????????????(???)","????????????????????????","????????????????????????"}; 
		String filename = "??????????????????????????????"; 
		Page page = new Page(request);
		//???????????????????????????
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		if (isPlatformAccount(customLogin)) {
			Integer integer = checkCustom(customLogin);
			page.getParams().put("businessPlatformId",String.valueOf(integer));
		}
		//??????businessPlatformId??????businessPlatform
		HashMap platformMap = channelCustomService.selectPlatformByCostomId(Integer.valueOf(page.getParams().get("businessPlatformId")));
		page.getParams().put("businessPlatform", (String) platformMap.get("businessPlatform"));
		List<Map<String, Object>> summaryList = new ArrayList<Map<String,Object>>();
		if(page.getParams().get("dataType")!=null){
			if(page.getParams().get("dataType").equals("1")){
				//????????????????????????????????????
				summaryList = userCommissionService.summaryInfoGroupMerchantNoPage(page);
			}else{
				//??????????????????????????????
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
	 * ????????????/????????????????????????
	 * @param request
	 * @return
	 */
	@RequestMapping("/summaryInfoMonthByMerchant")
	public ResponseEntity<?> summaryInfoMonthByMerchant(HttpServletRequest request){
		HashMap<String, Object> result = new HashMap<>();
		Page page = new Page(request);
		//???????????????????????????
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		if (isPlatformAccount(customLogin)) {
			Integer integer = checkCustom(customLogin);
			page.getParams().put("businessPlatformId",String.valueOf(integer));
		}
		//?????????????????????ID
		if (StringUtil.isEmpty(page.getParams().get("businessPlatformId"))) {
			result.put(RespCode.RESP_STAT, RespCode.error101);
			result.put(RespCode.RESP_MSG, RespCode.PARAMS_ERROR);
			return new ResponseEntity<Object>(result,HttpStatus.OK);
		}
		//??????businessPlatformId??????businessPlatform
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
	 * ??????/????????????????????????
	 * @param request
	 * @return
	 */
	@RequestMapping("/summaryInfoDetailByMerchantMonth")
	public ResponseEntity<?> summaryInfoDetailByMerchantMonth(HttpServletRequest request){
		HashMap<String, Object> result = new HashMap<>();
		Page page = new Page(request);
		//???????????????????????????
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		if (isPlatformAccount(customLogin)) {
			Integer integer = checkCustom(customLogin);
			page.getParams().put("businessPlatformId",String.valueOf(integer));
		}
		//?????????????????????ID
		if (StringUtil.isEmpty(page.getParams().get("businessPlatformId"))) {
			result.put(RespCode.RESP_STAT, RespCode.error101);
			result.put(RespCode.RESP_MSG, RespCode.PARAMS_ERROR);
			return new ResponseEntity<Object>(result,HttpStatus.OK);
		}
		//??????businessPlatformId??????businessPlatform
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
			//????????????????????????????????????
			List<Map<String, Object>> relationList = new ArrayList<Map<String,Object>>();
			if(page.getParams().get("dataType").equals("1")){
				//????????????????????????????????????
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
	 * ????????????????????????????????????
	 * @param request
	 * @param response
	 */
	@RequestMapping("/exportSummaryByMonth")
	@ResponseBody
	public void exportSummaryByMonth(HttpServletRequest request,HttpServletResponse response){
		// ??????
		String[] headers = new String[] {"????????????","??????","??????????????????(???)","??????????????????(???)","???????????????(???)"}; 
		String filename = "????????????????????????????????????"; 
		Page page = new Page(request);
		//???????????????????????????
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		if (isPlatformAccount(customLogin)) {
			Integer integer = checkCustom(customLogin);
			page.getParams().put("businessPlatformId",String.valueOf(integer));
		}
		//?????????????????????ID
		if (StringUtil.isEmpty(page.getParams().get("businessPlatformId"))) {
			logger.info(RespCode.PARAMS_ERROR);
			return;
		}
		//??????businessPlatformId??????businessPlatform
		HashMap platformMap = channelCustomService.selectPlatformByCostomId(Integer.valueOf(page.getParams().get("businessPlatformId")));
		if (platformMap == null || platformMap.size() == 0){
			logger.info(RespCode.codeMaps.get(RespCode.PLATFORM_NOT_EXIST));
			return;
		}
		page.getParams().put("businessPlatform", (String) platformMap.get("businessPlatform"));
		List<Map<String, Object>> summaryList = new ArrayList<Map<String,Object>>();
		//????????????????????????????????????
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
	 * ??????????????????????????????????????????
	 * @param request
	 * @param response
	 */
	@RequestMapping("/exportSummaryDetailByMonth")
	@ResponseBody
	public void exportSummaryDetailByMonth(HttpServletRequest request,HttpServletResponse response){
		// ??????
		String[] headers = new String[] {"??????","???????????????","????????????","??????????????????(???)","??????????????????(???)","???????????????(???)"}; 
		String filename = "????????????????????????????????????"; 
		Page page = new Page(request);
		//???????????????????????????
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		if (isPlatformAccount(customLogin)) {
			Integer integer = checkCustom(customLogin);
			page.getParams().put("businessPlatformId",String.valueOf(integer));
		}
		//?????????????????????ID
		if (StringUtil.isEmpty(page.getParams().get("businessPlatformId"))) {
			logger.info(RespCode.PARAMS_ERROR);
			return;
		}
		//??????businessPlatformId??????businessPlatform
		HashMap platformMap = channelCustomService.selectPlatformByCostomId(Integer.valueOf(page.getParams().get("businessPlatformId")));
		if (platformMap == null || platformMap.size() == 0){
			logger.info(RespCode.codeMaps.get(RespCode.PLATFORM_NOT_EXIST));
			return;
		}
		page.getParams().put("businessPlatform", (String) platformMap.get("businessPlatform"));
		List<Map<String, Object>> summaryList = new ArrayList<Map<String,Object>>();
		//????????????????????????????????????
		List<Map<String, Object>> data = new ArrayList<>();
		if(page.getParams().get("dataType").equals("1")){
			//????????????????????????????????????
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
			headers = new String[] {"??????","???????????????","??????????????????(???)","??????????????????(???)","???????????????(???)"}; 
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
		//??????????????????
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		Integer []allowCustomType = new Integer[]{6,7};
		checkFlag = channelCustomService.getCustomKeysByType(page.getParams(), allowCustomType, customLogin);
		//??????businessPlatformId??????businessPlatform
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
		//1.??????????????????
		if(checkFlag){
			//4.???????????????
			List<Map<String, String>> clearTermlist = qbClearingAccountsService.groupClearTermMonthNew(page.getParams());
			Map<String, List<Map<String, String>>> clearAccountMap = getClearAccountMonth(clearTermlist);
			if(!clearAccountMap.isEmpty()){
				//??????????????????????????????
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
				//?????????????????????????????????
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
	 * ?????????????????????
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
	 * ???????????????????????????
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
			//??????????????????????????????????????????/
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
							gearPositionDesc+=rateInterval+",????????????"+gearLaber+smallCount+"\n";
							merchantRateRuleDesc+=rateInterval+",????????????"+gearLaber+smallCount+":"+merchantRateRule+"\n";
						}
					}else{
						if(StringUtil.isEmpty(clearingAccounts.getBigAmount())){
							gearPositionDesc+=rateInterval+",????????????"+gearLaber+bigCount+"\n";
							merchantRateRuleDesc+=rateInterval+",????????????"+gearLaber+bigCount+":"+merchantRateRule+"\n";
						}
					}
					if(Integer.parseInt(String.valueOf(merchantTranMap.get("gearLabel")))==1){
						clearingAccounts.setAgentSmallRate(proxyFeeRate);
						if(StringUtil.isEmpty(clearingAccounts.getSmallAmountOne())){
							//????????????1
							clearingAccounts.setSmallAmountOne(totalAmount);
							clearingAccounts.setSmallAmountOneRate(merchantRateRule);
							//?????????????????????????????????????????????????????????????????????????????????????????????????????????
							List<Map<String, String>> merRateUpdateList = qbClearingAccountsService.getMerRateUpdate(merchantTranMap);
							if(merRateUpdateList!=null && merRateUpdateList.size()>0){
								Map<String, String> differMap = qbClearingAccountsService.differSummary(merchantTranMap, merchantRate,merRateUpdateList,1);
								clearingAccounts.setSmallAmountOneRateUpdate(differMap.get("updateRateDesc"));
								clearingAccounts.setDifferSmallOneMerAmount(differMap.get("differAmount"));
								clearingAccounts.setDifferSmallOneMerAmountDesc(differMap.get("differAmountDesc"));
							}

							//?????????????????????????????????????????????????????????????????????????????????????????????????????????
							List<Map<String, String>> agentRateUpdateList = qbClearingAccountsService.getAgentRateUpdate(merchantTranMap);
							if(agentRateUpdateList!=null && agentRateUpdateList.size()>0){
								Map<String, String> differMap = qbClearingAccountsService.differSummary(merchantTranMap,merchantTranMap.get("proxyFeeRate"),agentRateUpdateList,2);
								clearingAccounts.setDifferAgentSmallAmount(differMap.get("differAmount"));
								clearingAccounts.setAgentSmallRateUpdate(differMap.get("updateRateDesc"));
								clearingAccounts.setDifferAgentSmallAmountDesc(differMap.get("differAmountDesc"));
							}
						}else{
							if(StringUtil.isEmpty(clearingAccounts.getSmallAmountTwo())){
								//????????????2
								clearingAccounts.setSmallAmountTwo(totalAmount);
								clearingAccounts.setSmallAmountTwoRate(merchantRateRule);
								List<Map<String, String>> merRateUpdateList = qbClearingAccountsService.getMerRateUpdate(merchantTranMap);
								if(merRateUpdateList!=null && merRateUpdateList.size()>0){
									Map<String, String> differMap = qbClearingAccountsService.differSummary(merchantTranMap, merchantRate,merRateUpdateList,1);
									clearingAccounts.setSmallAmountTwoRateUpdate(differMap.get("updateRateDesc"));
									clearingAccounts.setDifferSmallTwoMerAmount(differMap.get("differAmount"));
									clearingAccounts.setDifferSmallTwoMerAmountDesc(differMap.get("differAmountDesc"));
								}
								//?????????????????????????????????????????????????????????????????????????????????????????????????????????
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
							//????????????
							clearingAccounts.setAgentBigRate(proxyFeeRate);
							clearingAccounts.setBigAmount(totalAmount);
							clearingAccounts.setBigAmountRate(merchantRateRule);
							clearingAccounts.setCompanyBigAmount(totalAmount);
							//?????????????????????????????????????????????????????????????????????????????????????????????????????????
							List<Map<String, String>> merRateUpdateList = qbClearingAccountsService.getMerRateUpdate(merchantTranMap);
							if(merRateUpdateList!=null && merRateUpdateList.size()>0){
								Map<String, String> differMap = qbClearingAccountsService.differSummary(merchantTranMap, merchantRate,merRateUpdateList,1);
								clearingAccounts.setBigAmountRateUpdate(differMap.get("updateRateDesc"));
								clearingAccounts.setDifferBigMerAmount(differMap.get("differAmount"));
								clearingAccounts.setDifferBigMerAmountDesc(differMap.get("differAmountDesc"));
							}

							//?????????????????????????????????????????????????????????????????????????????????????????????????????????
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
		//????????????????????????
		clearingAccounts.setMerchantRateRule(merchantRateRuleDesc);
		//?????????????????????????????????
		clearingAccounts.setGearLaber(gearPositionDesc);
		//????????????????????????=??????????????????1+??????????????????2+??????????????????
		String totalAmount = ArithmeticUtil.addStr(clearingAccounts.getSmallAmountOne(),clearingAccounts.getSmallAmountTwo());
		totalAmount = ArithmeticUtil.addStr(totalAmount, clearingAccounts.getBigAmount());
		clearingAccounts.setTotalAmount(totalAmount);
		//??????????????????=??????????????????1+??????????????????2
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
	 * ??????
	 * @param request
	 */
	@RequestMapping("/exportMonth")
	public void exportMonth(HttpServletRequest request,HttpServletResponse response){
		try{
			// ??????
			String filename = "??????????????????"; 
			Page page = new Page(request);
			//?????????????????????
			boolean checkFlag = true;
			//??????????????????
			ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
			Integer []allowCustomType = new Integer[]{6,7};
			checkFlag = channelCustomService.getCustomKeysByType(page.getParams(), allowCustomType, customLogin);
			//??????businessPlatformId??????businessPlatform
			String businessPlatformId = page.getParams().get("businessPlatformId");
			HashMap platformMap = channelCustomService.selectPlatformByCostomId(Integer.valueOf(businessPlatformId));
			String businessPlatform = (String) platformMap.get("businessPlatform");
			page.getParams().put("businessPlatform",businessPlatform);
			if(checkFlag){
				//4.???????????????
				List<Map<String, String>> clearTermlist = qbClearingAccountsService.groupClearTermMonthNew(page.getParams());
				Map<String, List<Map<String, String>>> clearAccountMap = getClearAccountMonth(clearTermlist);
				if(!clearAccountMap.isEmpty()){
					//??????????????????????????????
					List<QbClearingAccounts> clearingAccountList = new ArrayList<QbClearingAccounts>();
					for (String key : clearAccountMap.keySet()) {
						List<Map<String, String>> merchantTransList = clearAccountMap.get(key);
						setClearAccountInfoNew(merchantTransList,clearingAccountList);
					}
					//???????????????????????????
					String excelpath="/excel/clearAccountMonthTemplate.xls";
					HSSFSheet hssfSheet = ExcelUtil.getSheet(excelpath, 0,true);
					HSSFWorkbook wb = hssfSheet.getWorkbook();
					addalentDemandsMonth(hssfSheet,clearingAccountList);
					ExcelUtil.exportExcel(response, filename, wb);
				}
			}else{
				logger.info("???????????????????????????");
			}
		}catch(Exception e){
			logger.error(e.getMessage(), e);
		}
	}

	private static void addalentDemandsMonth(HSSFSheet talentDemandSheet, List<QbClearingAccounts> talentDemands)
			throws IllegalArgumentException, IllegalAccessException {
		Row talentDemandRow = talentDemandSheet.getRow(2);
		// ?????????????????????????????????????????????????????????????????????????????????
		if (talentDemands.size() > 5) {
			// ????????????5???????????????????????????
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
		// ????????????
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
