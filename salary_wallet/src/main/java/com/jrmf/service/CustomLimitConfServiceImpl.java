package com.jrmf.service;

import com.jrmf.controller.constant.TempStatus;
import com.jrmf.domain.*;
import com.jrmf.persistence.CommissionTemporary2Dao;
import com.jrmf.persistence.CustomLimitConfDao;
import com.jrmf.persistence.CustomPaymentTotalAmountDao;
import com.jrmf.persistence.UserCommission2Dao;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chonglulu
 * 商户配置限额
 */
@Service
public class CustomLimitConfServiceImpl implements CustomLimitConfService {

	private static Logger logger = LoggerFactory.getLogger(CustomLimitConfServiceImpl.class);

	@Autowired
	private CommissionTemporary2Dao temporaryDao2;
	@Autowired
	private CustomLimitConfDao customLimitConfDao;
	@Autowired
	private UserCommission2Dao userCommissionDao2;
	@Autowired
	private CustomPaymentTotalAmountDao customPaymentTotalAmountDao;
	@Autowired
	private CompanyService companyService;
	@Autowired
	private CustomLdConfigService customLdConfigService;
	@Autowired
	private WhiteUserService whiteUserService;
	@Autowired
	private ChannelRelatedService channelRelatedService;
	@Autowired
    private UserCommissionService userCommissionService;

	@Value("${jrmfMonthAmountLimit}")
	private String jrmfMonthAmountLimit;

	@Override
	public CustomLimitConf getCustomLimitConf(Map<String, Object> param) {
		return customLimitConfDao.getCustomLimitConf(param);
	}

	/**
	 * 获取商户配置限额列表
	 *
	 * @param hashMap 入参
	 *                pageNo 页码
	 *                pageSize 单页条数
	 * @return list
	 */
	@Override
	public List<CustomLimitConf> listLimitConfByParams(Map<String, Object> hashMap) {
		return customLimitConfDao.listLimitConfByParams(hashMap);
	}

	/**
	 * 添加配置
	 *
	 * @param customLimitConf 新增配置
	 */
	@Override
	public void addConfig(CustomLimitConf customLimitConf) {
		customLimitConfDao.insertCustomLimitConf(customLimitConf);

	}

	/**
	 * 删除配置
	 *
	 * @param id 配置id
	 */
	@Override
	public void deleteConfig(String id) {

		customLimitConfDao.deleteConfig(id);
	}

	/**
	 * 修改配置
	 *
	 * @param customLimitConf 修改配置
	 */
	@Override
	public void updateConfig(CustomLimitConf customLimitConf) {
		customLimitConfDao.updateConfig(customLimitConf);

	}

	@Override
	public boolean autoSupplement(String companyId, String customkey) {

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("customkey", customkey);
		params.put("companyId", companyId);
		CustomLimitConf customLimitConf = getCustomLimitConf(params);

		boolean autoSupplementFlag;
		if (customLimitConf != null) {
			String unAutoCompensatable = customLimitConf.getUnAutoCompensatable();
			if ("Y".equals(unAutoCompensatable)) {//no no == yes
				autoSupplementFlag = false;
			} else {
				autoSupplementFlag = true;
			}
		} else {//如果不存在，默认则自动补差价
			autoSupplementFlag = true;
		}

		return autoSupplementFlag;
	}

	@Override
	public void updateCustomPaymentTotalAmount(String companyId,
			String customkey,
			String identityNo,
			String transAmount,
			boolean signFlay) {

		Map<String, Object> params = new HashMap<>(10);
		params.put("companyId", companyId);
		params.put("originalId", customkey);
		params.put("identityNo", identityNo);
		CustomPaymentTotalAmount customPaymentTotalAmount = customPaymentTotalAmountDao.queryCustomPaymentTotalAmount(params);
		params.clear();

		if (customPaymentTotalAmount == null) {
			customPaymentTotalAmount = new CustomPaymentTotalAmount();
			customPaymentTotalAmount.setCompanyId(companyId);
			customPaymentTotalAmount.setOriginalId(customkey);
			customPaymentTotalAmount.setIdentityNo(identityNo);
			customPaymentTotalAmount.setLastDayTotal(0);
			customPaymentTotalAmount.setTodayTotal(0);
			customPaymentTotalAmount.setLastMonthTotal(0);
			customPaymentTotalAmount.setCurrentMonthTotal(0);
			customPaymentTotalAmount.setCurrentQuarterTotal(0);
			customPaymentTotalAmount.setLastQuarterTotal(0);
			customPaymentTotalAmountDao.initCustomPaymentTotalAmount(customPaymentTotalAmount);
		}

		BigDecimal amountDecimal = null;
		BigDecimal transAmountDecimal = new BigDecimal(transAmount);

		if (signFlay) {
			amountDecimal = transAmountDecimal.multiply(new BigDecimal(100));
		} else {
			amountDecimal = transAmountDecimal.multiply(new BigDecimal(-100));
		}
		int amount = amountDecimal.intValue();
		params.put("todayTotal", amount);
		params.put("currentMonthTotal", amount);
		params.put("currentQuarterTotal", amount);
		params.put("companyId", companyId);
		params.put("originalId", customkey);
		params.put("identityNo", identityNo);
		customPaymentTotalAmountDao.updateCustomPaymentTotalAmount(params);

	}

	@Override
	public CustomPaymentTotalAmount queryCustomPaymentTotalAmount(String companyId,
			String customkey,
			String identityNo) {

		Map<String, Object> params = new HashMap<>(10);
		params.put("companyId", companyId);
		params.put("originalId", customkey);
		params.put("identityNo", identityNo);
		CustomPaymentTotalAmount customPaymentTotalAmount = customPaymentTotalAmountDao.queryCustomPaymentTotalAmount(params);

		if (customPaymentTotalAmount == null) {
			customPaymentTotalAmount = new CustomPaymentTotalAmount();
			customPaymentTotalAmount.setCompanyId(companyId);
			customPaymentTotalAmount.setOriginalId(customkey);
			customPaymentTotalAmount.setIdentityNo(identityNo);
			customPaymentTotalAmount.setLastDayTotal(0);
			customPaymentTotalAmount.setTodayTotal(0);
			customPaymentTotalAmount.setLastMonthTotal(0);
			customPaymentTotalAmount.setCurrentMonthTotal(0);
			customPaymentTotalAmount.setCurrentQuarterTotal(0);
			customPaymentTotalAmount.setLastQuarterTotal(0);
			customPaymentTotalAmountDao.initCustomPaymentTotalAmount(customPaymentTotalAmount);

			customPaymentTotalAmount = customPaymentTotalAmountDao.queryCustomPaymentTotalAmount(params);
		}

		return customPaymentTotalAmount;
	}

	@Override
	public void initCustomPaymentTotalAmount(String companyId,
			String customkey,
			String identityNo) {
		CustomPaymentTotalAmount customPaymentTotalAmount = new CustomPaymentTotalAmount();
		customPaymentTotalAmount.setCompanyId(companyId);
		customPaymentTotalAmount.setOriginalId(customkey);
		customPaymentTotalAmount.setIdentityNo(identityNo);
		customPaymentTotalAmount.setLastDayTotal(0);
		customPaymentTotalAmount.setTodayTotal(0);
		customPaymentTotalAmount.setLastMonthTotal(0);
		customPaymentTotalAmount.setCurrentMonthTotal(0);
		customPaymentTotalAmount.setCurrentQuarterTotal(0);
		customPaymentTotalAmount.setLastQuarterTotal(0);
		customPaymentTotalAmountDao.initCustomPaymentTotalAmount(customPaymentTotalAmount);
	}

	@Override
	public List<CustomPaymentTotalAmount> listCustomPaymentTotalAmountByParam(Map<String, Object> param) {
		return customPaymentTotalAmountDao.listCustomPaymentTotalAmountByParam(param);
	}

	@Override
	public int initDayMonthPaymentTotalAmount(Map<String, Object> param) {
		return customPaymentTotalAmountDao.initDayMonthPaymentTotalAmount(param);
	}

	@Override
	public CustomPaymentTotalAmount queryCompanyPaymentTotalAmount(String companyId, String identityNo) {
		return customPaymentTotalAmountDao.queryCompanyPaymentTotalAmount(companyId, identityNo);
	}

	@Override
	public Map<String, Object> queryCompanyPaymentTotalAmountByRealCompany(
			String realCompanyId, String certificateNo) {
		return customPaymentTotalAmountDao.queryCompanyPaymentTotalAmountByRealCompany(realCompanyId,certificateNo);
	}

	@Override
	public int listCustomPaymentTotalAmountByParamCount(Map<String, Object> param) {
		return customPaymentTotalAmountDao.listCustomPaymentTotalAmountByParamCount(param);
	}


	/**
	 * 优化原有customAmountLimit方法
	 * @author linsong
	 * @date 2020/3/27
	 */
	@Override
	public void customAmountLimit2(String companyId, String customkey, String batchId) {

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("customkey", customkey);
		params.put("companyId", companyId);

		List<CommissionGroup> commissionGroupList = temporaryDao2.getCommissionGroupByCertId(batchId, "1", null);


		CustomLdConfig conLdConfig = customLdConfigService.getCustomLdConfigByMer(params);
		logger.info("------下发限额判断--联动--- customkey:{},companyId:{},conLdConfig:{}", customkey, companyId, conLdConfig);
		if (conLdConfig == null) {

			Company company = companyService.getCompanyByUserId(Integer.parseInt(companyId));
			String companySingleMonthLimit = company.getSingleMonthLimit();
			String companySingleQuarterLimit = company.getSingleQuarterLimit();
			//获取商户下发公司关联关系
			ChannelRelated channelRelated = channelRelatedService.getRelatedByCompAndOrig(customkey, companyId);
			//实际下发公司id
			String realCompanyId = temporaryDao2.selectRealCompanyIdByBatchId(batchId);
			if (StringUtil.isEmpty(realCompanyId)){
				realCompanyId =company.getRealCompanyId();
			}

			Map<String, String> realConpanyMounthSumAmonuts = new HashMap<>();
			String realCompanySingleMonthLimit = null;

			if (!StringUtil.isEmpty(realCompanyId) && (channelRelated.getRealCompanyOperate() == null || channelRelated.getRealCompanyOperate() != 1)) {
				//查询实际服务公司的当月个人下发额
				realConpanyMounthSumAmonuts = userCommissionService.getRealCommissionMounthSumAmonut(null, realCompanyId, batchId, "1");
				Company realCompany = companyService.getCompanyByUserId(Integer.parseInt(realCompanyId));
				realCompanySingleMonthLimit = realCompany.getSingleMonthLimit();
			}

			//查询服务公司的当月个人下发额
			Map<String, String> mounthSumAmonuts = userCommissionService.getCommissionMounthSumAmonut(null, companyId, batchId, "1");
			//查询服务公司的当季个人下发额
			Map<String, String> quarterSumAmonuts = userCommissionService.getCommissionQuarterSumAmonut(null, companyId, batchId, "1");

			for (CommissionGroup commission : commissionGroupList) {

				WhiteUser whiteUser = new WhiteUser();
				whiteUser.setCertId(commission.getCertId());
				whiteUser.setDocumentType(1);
				whiteUser.setCustomkey(customkey);
				whiteUser.setCompanyId(companyId);
				Integer isWhiteUser = whiteUserService.checkIsWhiteUser(whiteUser);
				//UserRelated userRelated = userRelatedService.selectIsWhiteList(customkey, companyId, commission.getCertId());
				//非白名单用户进行限额校验
				if (isWhiteUser < 1) {

					List<TempCommission> tempList = commission.getCommissionList();

					//服务公司当月限额校验
					if (!StringUtil.isEmpty(companySingleMonthLimit)) {

//						String companyMonthSumAmonut = userCommissionDao2.getSumAmountOfMonthByCertId(commission.getCertId(), null, companyId);
						String companyMonthSumAmonut = mounthSumAmonuts.get(commission.getCertId());
						logger.info("-----服务公司月累计限额判断----服务公司当月累计companyMonthSumAmonut：" + companyMonthSumAmonut);


						for (TempCommission temp : tempList) {

							if (temp.getState() == 0) {
								continue;
							}

							String amout = temp.getSourceAmount();
							companyMonthSumAmonut = ArithmeticUtil.addStr(companyMonthSumAmonut, amout);
							if (ArithmeticUtil.compareTod(companyMonthSumAmonut, companySingleMonthLimit) > 0) {
								CommissionTemporary commissionFail = new CommissionTemporary();
								commissionFail.setId(temp.getId());
								commissionFail.setStatus(2);
								commissionFail.setStatusDesc("超过服务公司月累计限额（" + companySingleMonthLimit + "）");
								commissionFail.setSumFee("0.00");
								commissionFail.setCalculationRates("0.00");
								commissionFail.setSupplementAmount("0.00");
								commissionFail.setSupplementFee("0.00");
								temporaryDao2.updateCommissionTemporary(commissionFail);

								//校验不通过的明细id直接变更为0,防止下面的判断重复校验失败明细
								temp.setState(0);
								logger.info("----服务公司月累计限额判断----id:" + temp.getId() + "----金额：" + amout + "---超过-服务公司月累计限额：" + companySingleMonthLimit);
							}
							logger.info("-----服务公司月累计限额判断---id:" + temp.getId() + "----姓名：" + temp.getUserName() + "--金额：" + amout);
						}

					}

					//实际下发公司限额校验
					if (!StringUtil.isEmpty(realCompanyId)  && (channelRelated.getRealCompanyOperate() == null || channelRelated.getRealCompanyOperate() != 1)) {
						logger.info("进入实际下发公司限额校验");
						//进入实际下发公司限额校验
						CustomPaymentTotalAmount realCompanyPaymentTotalAmount = queryCompanyPaymentTotalAmount(realCompanyId, commission.getCertId());
						if (realCompanyPaymentTotalAmount == null) {
							initCustomPaymentTotalAmount(realCompanyId, customkey, commission.getCertId());
//							realCompanyPaymentTotalAmount = queryCompanyPaymentTotalAmount(realCompanyId, commission.getCertId());
						}

//						String currentMonthTotal = userCommissionDao2.getSumAmountOfMonthByCertId(commission.getCertId(), null, realCompanyId);
//						String currentMonthTotal = realConpanyMounthSumAmonuts.get(commission.getCertId());
						//获取使用实际下发公司的累计额度
//						Map<String, Object> userRealCompanyPaymentTotalAmount = queryCompanyPaymentTotalAmountByRealCompany(realCompanyId, commission.getCertId());
//						String realCompanyMonthTotal = String.valueOf(currentMonthTotal);
//						String useRealCompanyMonthTotal = String.valueOf(userRealCompanyPaymentTotalAmount.get("currentMonthTotalStr"));
						String realAlreadyCompanyMonthTotal = realConpanyMounthSumAmonuts.get(commission.getCertId());

						logger.info("-----实际服务公司当月累计限额判断----实际服务公司当月所有累计companyMonthSumAmonut：" + realAlreadyCompanyMonthTotal);
						//获取实际下发公司累计额度
						for (TempCommission temp : tempList) {

							if (temp.getState() == 0) {
								continue;
							}

							String realAllCompanyMonthTotal = ArithmeticUtil.addStr(realAlreadyCompanyMonthTotal, temp.getSourceAmount());
							if (ArithmeticUtil.compareTod(realAllCompanyMonthTotal, realCompanySingleMonthLimit) > 0) {
								//抛出超出实际下发公司限额异常
								CommissionTemporary commissionFail = new CommissionTemporary();
								commissionFail.setId(temp.getId());
								commissionFail.setStatus(2);
								commissionFail.setStatusDesc("超过实际服务公司月累计限额（" + realCompanySingleMonthLimit + "）");
								commissionFail.setSumFee("0.00");
								commissionFail.setCalculationRates("0.00");
								commissionFail.setSupplementAmount("0.00");
								commissionFail.setSupplementFee("0.00");
								temporaryDao2.updateCommissionTemporary(commissionFail);

								//校验不通过的明细id直接变更为0,防止下面的判断重复校验失败明细
								temp.setState(0);
							}
						}
					}

					//服务公司季度限额校验
					if (!StringUtil.isEmpty(companySingleQuarterLimit)) {

//						String companyQuarterSumAmonut = userCommissionDao2.getSumAmountOfQuarterByCertId(commission.getCertId(), null, companyId);
						String companyQuarterSumAmonut = quarterSumAmonuts.get(commission.getCertId());
						logger.info("-----服务公司季度累计限额判断----服务公司季度累计companyQuarterSumAmonut：" + companyQuarterSumAmonut);

						for (TempCommission temp : tempList) {

							if (temp.getState() == 0) {
								continue;
							}

							String amout = temp.getSourceAmount();
							companyQuarterSumAmonut = ArithmeticUtil.addStr(companyQuarterSumAmonut, amout);
							if (ArithmeticUtil.compareTod(companyQuarterSumAmonut, companySingleQuarterLimit) > 0) {
								CommissionTemporary commissionFail = new CommissionTemporary();
								commissionFail.setId(temp.getId());
								commissionFail.setStatus(2);
								commissionFail.setStatusDesc("超过服务公司季度累计限额（" + companySingleQuarterLimit + "）");
								commissionFail.setSumFee("0.00");
								commissionFail.setCalculationRates("0.00");
								commissionFail.setSupplementAmount("0.00");
								commissionFail.setSupplementFee("0.00");
								temporaryDao2.updateCommissionTemporary(commissionFail);

								//校验不通过的明细id直接变更为0,防止下面的判断重复校验失败明细
								temp.setState(0);

								logger.info("----服务公司季度累计限额判断----id:" + temp.getId() + "----金额：" + amout + "---超过-服务公司季度累计限额：" + companySingleQuarterLimit);
							}
							logger.info("-----服务公司季度累计限额判断---id:" + temp.getId() + "----姓名：" + temp.getUserName() + "--金额：" + amout);
						}
					}
				}
			}
		}

		CustomLimitConf customLimitConf = getCustomLimitConf(params);

		if (customLimitConf != null) {
			String singleOrderLimit = customLimitConf.getSingleOrderLimit();
			String singleDayLimit = customLimitConf.getSingleDayLimit();
			String singleMonthLimit = customLimitConf.getSingleMonthLimit();
			String singleQuarterLimit = customLimitConf.getSingleQuarterLimit();

			logger.info("----商户限额----companyId:" + companyId + "---customkey:" + customkey + "---batchId:" + batchId + "---商户单笔限额：" + singleOrderLimit + "---商户单日限额：" + singleDayLimit + "---商户单月限额：" + singleMonthLimit);

			//查询商户+服务公司的当日个人下发额
			Map<String, String> daySumAmonuts = userCommissionService.getCommissionDaySumAmonut(customkey, companyId, batchId, "1");
			//查询商户+服务公司的当月个人下发额
			Map<String, String> mounthSumAmonuts = userCommissionService.getCommissionMounthSumAmonut(customkey, companyId, batchId, "1");
			//查询商户+服务公司的当季个人下发额
			Map<String, String> quarterSumAmonuts = userCommissionService.getCommissionQuarterSumAmonut(customkey, companyId, batchId, "1");

			for (CommissionGroup commission : commissionGroupList) {
				WhiteUser whiteUser = new WhiteUser();
				whiteUser.setCertId(commission.getCertId());
				whiteUser.setDocumentType(1);
				whiteUser.setCustomkey(customkey);
				whiteUser.setCompanyId(companyId);
				Integer isWhiteUser = whiteUserService.checkIsWhiteUser(whiteUser);
				//UserRelated userRelated = userRelatedService.selectIsWhiteList(customkey, companyId, commission.getCertId());
				//白名单用户不进行限额校验
				if (isWhiteUser < 1) {

					List<TempCommission> tempList = commission.getCommissionList();

					if (ArithmeticUtil.compareTod(singleOrderLimit, "0") > 0) {

						for (TempCommission temp : tempList) {

							if (temp.getState() == 0) {
								continue;
							}

							String amout = temp.getSourceAmount();
							if (ArithmeticUtil.compareTod(amout, singleOrderLimit) > 0) {
								CommissionTemporary commissionFail = new CommissionTemporary();
								commissionFail.setId(temp.getId());
								commissionFail.setStatus(2);
								commissionFail.setStatusDesc("超过商户单笔限额（" + singleOrderLimit + "）");
								commissionFail.setSumFee("0.00");
								commissionFail.setCalculationRates("0.00");
								commissionFail.setSupplementAmount("0.00");
								commissionFail.setSupplementFee("0.00");
								temporaryDao2.updateCommissionTemporary(commissionFail);

								//校验不通过的明细id直接变更为0,防止下面的判断重复校验失败明细
								temp.setState(0);
								logger.info("----单笔限额判断----id:" + temp.getId() + "----金额：" + amout + "---超过-商户单笔限额：" + singleMonthLimit);
							}
							logger.info("-----单笔限额判断---id:" + temp.getId() + "----姓名：" + temp.getUserName() + "--金额：" + amout);
						}
					}

					if (ArithmeticUtil.compareTod(singleDayLimit, "0") > 0) {

//						String daySumAmonut = userCommissionDao2.getSumAmountOfDayByCertId(commission.getCertId(), customkey, companyId);
						String daySumAmonut = daySumAmonuts.get(commission.getCertId());
						logger.info("-----单日限额判断----单日下发金额daySumAmonut：" + daySumAmonut);

						for (TempCommission temp : tempList) {

							if (temp.getState() == 0) {
								continue;
							}

							String amout = temp.getSourceAmount();
							daySumAmonut = ArithmeticUtil.addStr(daySumAmonut, amout);
							if (ArithmeticUtil.compareTod(daySumAmonut, singleDayLimit) > 0) {
								CommissionTemporary commissionFail = new CommissionTemporary();
								commissionFail.setId(temp.getId());
								commissionFail.setStatus(2);
								commissionFail.setStatusDesc("超过单日限额（" + singleDayLimit + "）");
								commissionFail.setSumFee("0.00");
								commissionFail.setCalculationRates("0.00");
								commissionFail.setSupplementAmount("0.00");
								commissionFail.setSupplementFee("0.00");
								temporaryDao2.updateCommissionTemporary(commissionFail);

								//校验不通过的明细id直接变更为0,防止下面的判断重复校验失败明细
								temp.setState(0);
								logger.info("----单笔限额判断----id:" + temp.getId() + "----金额：" + amout + "---超过-商户单日限额：" + daySumAmonut);
							}
							logger.info("-----单日限额判断---id:" + temp.getId() + "----姓名：" + temp.getUserName() + "--金额：" + amout);
						}
					}

					if (ArithmeticUtil.compareTod(singleMonthLimit, "0") > 0) {

//						String mounthSumAmonut = userCommissionDao2.getSumAmountOfMonthByCertId(commission.getCertId(), customkey, companyId);
						String mounthSumAmonut = mounthSumAmonuts.get(commission.getCertId());
						logger.info("-----单月限额判断----单月下发金额mounthSumAmonut：" + mounthSumAmonut);


						for (TempCommission temp : tempList) {

							if (temp.getState() == 0) {
								continue;
							}

							String amout = temp.getSourceAmount();
							mounthSumAmonut = ArithmeticUtil.addStr(mounthSumAmonut, amout);
							if (ArithmeticUtil.compareTod(mounthSumAmonut, singleMonthLimit) > 0) {
								CommissionTemporary commissionFail = new CommissionTemporary();
								commissionFail.setId(temp.getId());
								commissionFail.setStatus(2);
								commissionFail.setStatusDesc("超过月累计额度（" + singleMonthLimit + "）");
								commissionFail.setSumFee("0.00");
								commissionFail.setCalculationRates("0.00");
								commissionFail.setSupplementAmount("0.00");
								commissionFail.setSupplementFee("0.00");
								temporaryDao2.updateCommissionTemporary(commissionFail);

								//校验不通过的明细id直接变更为0,防止下面的判断重复校验失败明细
								temp.setState(0);
								logger.info("----单月限额判断----id:" + temp.getId() + "----金额：" + amout + "---超过-商户单月限额：" + mounthSumAmonut);
							}
							logger.info("-----单月限额判断---id:" + temp.getId() + "----姓名：" + temp.getUserName() + "--金额：" + amout);
						}
					}

					if (ArithmeticUtil.compareTod(singleQuarterLimit, "0") > 0) {
//						String quarterSumAmonut = userCommissionDao2.getSumAmountOfMonthByCertId(commission.getCertId(), customkey, companyId);
						String quarterSumAmonut = quarterSumAmonuts.get(commission.getCertId());
						logger.info("-----季度限额判断----季度下发金额mounthSumAmonut：" + quarterSumAmonut);

						for (TempCommission temp : tempList) {

							if (temp.getState() == 0) {
								continue;
							}

							String amout = temp.getSourceAmount();
							quarterSumAmonut = ArithmeticUtil.addStr(quarterSumAmonut, amout);
							if (ArithmeticUtil.compareTod(quarterSumAmonut, singleQuarterLimit) > 0) {
								CommissionTemporary commissionFail = new CommissionTemporary();
								commissionFail.setId(temp.getId());
								commissionFail.setStatus(2);
								commissionFail.setStatusDesc("超过季度累计额度（" + singleQuarterLimit + "）");
								commissionFail.setSumFee("0.00");
								commissionFail.setCalculationRates("0.00");
								commissionFail.setSupplementAmount("0.00");
								commissionFail.setSupplementFee("0.00");
								temporaryDao2.updateCommissionTemporary(commissionFail);

								//校验不通过的明细id直接变更为0,防止下面的判断重复校验失败明细
								temp.setState(0);
								logger.info("----季度限额判断----id:" + temp.getId() + "----金额：" + amout + "---超过-商户季度限额：" + quarterSumAmonut);
							}
							logger.info("-----季度限额判断---id:" + temp.getId() + "----姓名：" + temp.getUserName() + "--金额：" + amout);
						}
					}

				}
			}
		}
	}

	@Override
	public void platformAmountLimit(String batchId) {
		List<CommissionGroup> commissionGroupList = temporaryDao2.getCommissionGroupByCertId(batchId, "1", null);
		Map<String, String> monthSumAmonuts = userCommissionService.getCommissionMounthSumAmonutByPlatformId(batchId);

		for (CommissionGroup commission : commissionGroupList) {

//            UserRelated userRelated = userRelatedService.selectIsWhiteList(customkey, companyId, commission.getCertId());
//            //白名单用户不进行档位校验
//            if (!(userRelated != null && userRelated.getIsWhiteList() == 1)) {

			//计算本批次一个证件号的下发金额之和
			String batchAmout = "0.0";

			for (TempCommission tempCommission : commission.getCommissionList()) {
				batchAmout = ArithmeticUtil.addStr(tempCommission.getSourceAmount(), batchAmout);
			}
			logger.info("-----用户:{},本批次总金额batchAmout：{}", commission.getCertId(), batchAmout);

			//本证件号月累计总额
			String monthSumAmonut = monthSumAmonuts.get(commission.getCertId());

			logger.info("-----用户:{},本月魔方下发金额monthSumAmonut：{}", commission.getCertId(), monthSumAmonut);

			String monthbatchsum = ArithmeticUtil.addStr(monthSumAmonut, batchAmout);

			if (ArithmeticUtil.compareTod(monthbatchsum, jrmfMonthAmountLimit) > 0) {
				logger.info("-------用户:{}当月下发金额累加本次下发金额:{},超过魔方平台当月限额:{}----------", commission.getCertId(),
						monthbatchsum, jrmfMonthAmountLimit);
				for (TempCommission tempCommission : commission.getCommissionList()) {
					CommissionTemporary commissionFail = new CommissionTemporary();
					commissionFail.setId(tempCommission.getId());
					commissionFail.setStatus(TempStatus.FAILURE.getCode());
					String laveAmount = ArithmeticUtil.subStr(jrmfMonthAmountLimit, monthSumAmonut, 2);
					commissionFail
							.setStatusDesc("超系统" + jrmfMonthAmountLimit + "元限额，限额余额为" + laveAmount + "元");
					commissionFail.setSumFee("0.00");
					commissionFail.setCalculationRates("0.00");
					commissionFail.setSupplementAmount("0.00");
					commissionFail.setSupplementFee("0.00");
					temporaryDao2.updateCommissionTemporary(commissionFail);
				}
			}
		}
	}
}
