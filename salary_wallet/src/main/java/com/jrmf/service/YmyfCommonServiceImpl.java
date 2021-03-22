package com.jrmf.service;

import com.jrmf.controller.constant.CustomThirdPaymentConfigTypeEnum;
import com.jrmf.domain.CustomThirdPaymentConfig;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.payment.PaymentFactory;
import com.jrmf.utils.exception.YmyfNormalExcepion;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.jrmf.domain.ChannelInterimBatch;
import com.jrmf.domain.CommissionTemporary;
import com.jrmf.domain.Company;
import com.jrmf.oldsalarywallet.service.ChannelInterimBatchService;
import com.jrmf.payment.ymyf.YFService;
import com.jrmf.payment.ymyf.entity.PaymentTransModel;
import com.jrmf.payment.ymyf.entity.PaymentTransQueryModle;
import com.jrmf.payment.ymyf.entity.SmsModel;
import com.jrmf.payment.ymyf.entity.PaymentTransQueryModle.QueryItems;
import com.jrmf.payment.ymyf.util.JsonUtils;
import com.jrmf.persistence.CommissionTemporary2Dao;
import com.jrmf.utils.AmountConvertUtil;
import com.jrmf.utils.StringUtil;
import com.jrmf.utils.exception.NoBatchException;

@Service
public class YmyfCommonServiceImpl implements YmyfCommonService{

	private static Logger logger = LoggerFactory.getLogger(YmyfCommonServiceImpl.class);

	@Autowired
	private CompanyService companyService;
	@Autowired
	private CommissionTemporary2Dao temporaryDao2;
	@Autowired
	private AgreementTemplateService agreementTemplateService;
	@Autowired
	private ChannelInterimBatchService channelInterimBatchService;
	@Autowired
	private CustomMenuService customMenuService;
	@Autowired
	private CustomThirdPaymentConfigService customThirdPaymentConfigService;

	/**
	 * 溢美优付预下单调用
	 * @param interimBatch
	 * @param phone
	 * @return
	 */
	@Override
	public Map<String, String> prePay(ChannelInterimBatch interimBatch, String phone){
		Map<String, String> result = new HashMap<String, String>();
		result.put("phoneNo", phone);
		try {
			Map<String, Object> param = new HashMap<>();
			param.put("originalId", interimBatch.getCustomkey());
			param.put("batchIds", interimBatch.getOrderno());
			param.put("status", "1");

			//获取成功临时下发明细
			List<CommissionTemporary> commissionTempList = temporaryDao2.getCommissionedByBatchIdsAndParam(param);
			if(commissionTempList.size()>0){

				Integer configType = CustomThirdPaymentConfigTypeEnum.CUSTOM_KEY_AND_MERCHANTID.getCode();

				String customKey = interimBatch.getCustomkey();
				//转包下发
				if (!interimBatch.getRealCompanyId().equals(interimBatch.getRecCustomkey())) {
					customKey = interimBatch.getRecCustomkey();
					configType = CustomThirdPaymentConfigTypeEnum.REALCOMPANY_KEY.getCode();
				}

				//获取商户签约服务公司配置
				PaymentConfig paymentConfig = customThirdPaymentConfigService
						.getConfigByCustomKeyAndTypeAndPathNo(customKey,
								configType,
								interimBatch.getRealCompanyId(),
								PaymentFactory.YMFWSPAY);

				if(paymentConfig != null){
					Company serviceCompany = companyService.getCompanyByUserId(Integer.parseInt(interimBatch.getRealCompanyId()));

					//获取溢美项目id
					String projectId = customMenuService
							.getProjectIdByMenu(interimBatch.getMenuId(), interimBatch.getCustomkey());

					//封装请求信息
					String reqInfo = getPrePaymentTrans(interimBatch,commissionTempList,paymentConfig.getThirdMerchId(),phone,serviceCompany.getServiceCompanyId(),projectId);
					//调用服务
					YFService yfService = new YFService(paymentConfig.getPreHost(), paymentConfig.getThirdMerchId(),
							paymentConfig.getPayPrivateKey(), paymentConfig.getPayPublicKey(), paymentConfig.getApiKey(), reqInfo, "UTF-8");
					//获取响应
					String respInfo = yfService.paymentConfirm();
					Map<String, String> respUpInfo = JSONObject.parseObject(respInfo, Map.class);
					//请求成功
					result.put("code", "0000");
					result.put("reqId", respUpInfo.get("reqId"));
					interimBatch.setYmReqId(respUpInfo.get("reqId"));
					interimBatch.setYmBatchNo(respUpInfo.get("batchNo"));
					channelInterimBatchService.updateYmInfoByBatch(interimBatch);
				}else{
					result.put("code", "1013");
					result.put("msg", "无商户秘钥配置");
				}
			}else{
				result.put("code", "1012");
				result.put("msg", "无校验成功下发明细");
			}
		} catch (Exception e) {
			//发生异常
			logger.error(e.getMessage(), e);
			result.put("code", "1009");
			result.put("msg", e.getMessage());
		}
		return result;
	}

	/**
	 * 溢美优付短信付款
	 * @param interimBatch
	 * @param smsNo
	 * @return
	 */
	@Override
	public Map<String, Object> smsPay(ChannelInterimBatch interimBatch, String smsNo){
		Map<String, Object> result = new HashMap<>();
		try {

			String customKey = interimBatch.getCustomkey();
			Integer configType = CustomThirdPaymentConfigTypeEnum.CUSTOM_KEY_AND_MERCHANTID.getCode();
			//转包下发
			if (!interimBatch.getRealCompanyId().equals(interimBatch.getRecCustomkey())) {
				customKey = interimBatch.getRecCustomkey();
				configType = CustomThirdPaymentConfigTypeEnum.REALCOMPANY_KEY.getCode();
			}

			//获取商户签约服务公司配置
			PaymentConfig paymentConfig = customThirdPaymentConfigService.getConfigByCustomKeyAndTypeAndPathNo(customKey,
					configType,interimBatch.getRealCompanyId(),
					PaymentFactory.YMFWSPAY);
			if(paymentConfig!=null){
				//封装请求信息
				String reqInfo = getSmsPaymentTrans(interimBatch,smsNo);
				//调用服务
				YFService yfService = new YFService(paymentConfig.getPreHost(), paymentConfig.getThirdMerchId(),
						paymentConfig.getPayPrivateKey(), paymentConfig.getPayPublicKey(), paymentConfig.getApiKey(), reqInfo, "UTF-8");
				//获取响应
				String respInfo = yfService.paymentSms();
				logger.info("响应信息："+respInfo);
				//请求成功
				result.put("code", "0000");
			}else{
				result.put("code", "1013");
				result.put("msg", "无商户秘钥配置");
			}
		} catch (YmyfNormalExcepion e) {
			//请求溢美服务非成功响应
			logger.error(e.getMessage(), e);
			result.put("code", "1010");
			result.put("msg", e.getMessage());
			return result;
		} catch (Exception e) {
			//发生异常
			logger.error(e.getMessage(), e);
			result.put("code", "1009");
			result.put("msg", e.getMessage());
		}
		return result;
	}


	/**
	 * 溢美优付短信付款结果查询
	 * @param interimBatch
	 * @param orderNo
	 * @return
	 */
	@Override
	public Map<String, Object> smsPayResultQuery(ChannelInterimBatch interimBatch,String orderNo){
		Map<String, Object> result = new HashMap<>();
		try {

			String customKey = interimBatch.getCustomkey();
			Integer configType = CustomThirdPaymentConfigTypeEnum.CUSTOM_KEY_AND_MERCHANTID.getCode();
			//转包下发
			if (!interimBatch.getRealCompanyId().equals(interimBatch.getRecCustomkey())) {
				customKey = interimBatch.getRecCustomkey();
				configType = CustomThirdPaymentConfigTypeEnum.REALCOMPANY_KEY.getCode();
			}

			PaymentConfig paymentConfig = customThirdPaymentConfigService.getConfigByCustomKeyAndTypeAndPathNo(customKey,
					configType,interimBatch.getRealCompanyId(),
					PaymentFactory.YMFWSPAY);
			if(paymentConfig!=null){
				//封装请求信息
				String reqInfo = getPayResultQueryJson(interimBatch.getOrderno(),orderNo,paymentConfig.getThirdMerchId());
				//调用服务
				YFService yfService = new YFService(paymentConfig.getPreHost(), paymentConfig.getThirdMerchId(),
						paymentConfig.getPayPrivateKey(), paymentConfig.getPayPublicKey(), paymentConfig.getApiKey(), reqInfo, "UTF-8");
				//获取响应
				String respInfo = yfService.paymentQuery();
				PaymentTransQueryModle queryModle = JsonUtils.fromJson(respInfo, PaymentTransQueryModle.class);
				List<QueryItems>queryItems = queryModle.getQueryItems();
				if (queryItems!=null && queryItems.size()>0){
					result.put("code","0000");
				}
				for (QueryItems queryItem : queryItems) {
					if(queryItem.getMerOrderId().equals(orderNo)){
						//封装响应结果
						if(queryItem.getState()==0){
							//付款待发放
							result.put("state", "0");
						}else if(queryItem.getState()==1){
							//付款处理中
							result.put("state", "3");
						}else if(queryItem.getState()==3){
							//付款成功
							result.put("state", "1");
						}else if(queryItem.getState()==4){
							//付款失败
							result.put("state", "2");
							result.put("msg", queryItem.getResMsg());
						}else{
							result.put("state", "7");
							logger.info("其他状态,状态值为："+queryItem.getState());
						}
						break;
					}

				}
			}else{
				result.put("code", "1013");
				result.put("msg", "无商户秘钥配置");
			}
		}catch (NoBatchException e) {
			//批次信息未查到
			logger.error(e.getMessage(), e);
			result.put("code", "1010");
			result.put("msg", e.getMessage());
		}catch (Exception e) {
			//发生异常
			logger.error(e.getMessage(), e);
			result.put("code", "1009");
			result.put("msg", e.getMessage());
		}
		return result;
	}

	/**
	 * 溢美优付短信付款结果查询
	 * @param interimBatch
	 * @return
	 */
	@Override
	public Map<String, Object> smsPayResultBatchQuery(ChannelInterimBatch interimBatch){
		Map<String, Object> result = new HashMap<>();

		logger.info("---------------调用溢美批次查询结果接口{}---------------------", interimBatch);

		try {
			String customKey = interimBatch.getCustomkey();
			Integer configType = CustomThirdPaymentConfigTypeEnum.CUSTOM_KEY_AND_MERCHANTID.getCode();
			//转包下发
			if (!interimBatch.getRealCompanyId().equals(interimBatch.getRecCustomkey())) {
				customKey = interimBatch.getRecCustomkey();
				configType = CustomThirdPaymentConfigTypeEnum.REALCOMPANY_KEY.getCode();
			}
			PaymentConfig paymentConfig = customThirdPaymentConfigService.getConfigByCustomKeyAndTypeAndPathNo(customKey,
					configType,interimBatch.getRealCompanyId(),
					PaymentFactory.YMFWSPAY);
			//获取商户签约服务公司配置
			if(paymentConfig!=null){
				//封装请求信息
				String reqInfo = getPayResultBatchQueryJson(interimBatch.getOrderno(),paymentConfig.getThirdMerchId());
				//调用服务
				YFService yfService = new YFService(paymentConfig.getPreHost(), paymentConfig.getThirdMerchId(),
						paymentConfig.getPayPrivateKey(), paymentConfig.getPayPublicKey(), paymentConfig.getApiKey(), reqInfo, "UTF-8");
				//获取响应
				String respInfo = yfService.paymentQuery();
				PaymentTransQueryModle queryModle = JsonUtils.fromJson(respInfo, PaymentTransQueryModle.class);
				List<QueryItems>queryItems = queryModle.getQueryItems();
				result.put("code", "0000");
				result.put("msg", "成功");
				result.put("orderList", queryItems);
			}else{
				result.put("code", "1013");
				result.put("msg", "无商户秘钥配置");
			}
		}catch (NoBatchException e) {
			//批次信息未查到
			logger.error(e.getMessage(), e);
			result.put("code", "1010");
			result.put("msg", e.getMessage());
		}catch (Exception e) {
			//发生异常
			logger.error(e.getMessage(), e);
			result.put("code", "1009");
			result.put("msg", e.getMessage());
		}
		return result;
	}

	/**
	 * 短信付款参数
	 * @param interimBatch
	 * @param smsNo
	 * @return
	 */
	private static String getSmsPaymentTrans(ChannelInterimBatch interimBatch,String smsNo) {
		SmsModel smsModel = new SmsModel();
		smsModel.setBatchNo(interimBatch.getYmBatchNo());
		smsModel.setReqId(interimBatch.getYmReqId());
		smsModel.setSmsCode(smsNo);
		String json = JsonUtils.toJson(smsModel);
		return json;
	}

	/**
	 * 预下单参数
	 *
	 * @return
	 */
	private String getPrePaymentTrans(ChannelInterimBatch interimBatch,List<CommissionTemporary> commissionTempList,String thirdMerchId,String phone,Long serviceCompanyId,String projectId) {
		PaymentTransModel pt = new PaymentTransModel();
		//商户号
		pt.setMerId(thirdMerchId);
		//手机号
		pt.setMobile(phone);
		//总金额
		pt.setTotalAmt(AmountConvertUtil.changeY2F(interimBatch.getAmount()));
		//总笔数
		pt.setTotalCount(String.valueOf(interimBatch.getPassNum()));
		//批次号
		pt.setMerBatchId(interimBatch.getOrderno());
		//溢美projectId
		pt.setProjectId(projectId);
		List<PaymentTransModel.PayItems> list = new ArrayList<>();
		for (CommissionTemporary commissionTemporary : commissionTempList) {
			PaymentTransModel.PayItems param = param(commissionTemporary,serviceCompanyId);
			list.add(param);
		}
		pt.setPayItems(list);
		String json = JsonUtils.toJson(pt);
		return json;
	}

	/**
	 * 预下单参数
	 * @param commissionTemporary
	 * @param serviceCompanyId
	 * @return
	 */
	private PaymentTransModel.PayItems param(CommissionTemporary commissionTemporary,Long serviceCompanyId) {
		PaymentTransModel.PayItems pi1 = new PaymentTransModel.PayItems();
		//交易金额
		pi1.setAmt(Long.parseLong(AmountConvertUtil.changeY2F(commissionTemporary.getAmount())));
		//身份证
		pi1.setIdCard(commissionTemporary.getIdCard());
		//备注
		pi1.setMemo(commissionTemporary.getRemark());
		//订单号
		pi1.setMerOrderId(commissionTemporary.getOrderNo());
		//手机号
		pi1.setMobile(commissionTemporary.getPhoneNo());
		//银行卡
		pi1.setPayeeAcc(commissionTemporary.getBankCardNo());
		//户名
		pi1.setPayeeName(commissionTemporary.getUserName());
		//代付方式 0：银行卡，1：支付宝，2：微信
		pi1.setPaymentType(0);
		//代付类型 0：实时 1：工作日
		pi1.setPayType(0);
		//优地ID
		pi1.setLevyId(serviceCompanyId);
		pi1.setAccType(1);
		return pi1;
	}

	/**
	 * 下单查询结果参数
	 * @param batchId
	 * @param orderNo
	 * @param thirdMerchId
	 * @return
	 */
	private String getPayResultQueryJson(String batchId,String orderNo,String thirdMerchId){
		PaymentTransQueryModle ptq = new PaymentTransQueryModle();
		ptq.setMerBatchId(batchId);
		ptq.setMerId(thirdMerchId);
		List<PaymentTransQueryModle.QueryItems> list = new ArrayList<PaymentTransQueryModle.QueryItems>();
		if(!StringUtil.isEmpty(orderNo)){
			PaymentTransQueryModle.QueryItems pi1 = new PaymentTransQueryModle.QueryItems();
			pi1.setMerOrderId(orderNo);
			list.add(pi1);
		}
		ptq.setQueryItems(list);
		String json=JsonUtils.toJson(ptq);
		return json;
	}

	/**
	 * 下单查询结果参数
	 * @param batchId
	 * @param thirdMerchId
	 * @return
	 */
	private String getPayResultBatchQueryJson(String batchId,String thirdMerchId){
		PaymentTransQueryModle ptq = new PaymentTransQueryModle();
		ptq.setMerBatchId(batchId);
		ptq.setMerId(thirdMerchId);
		List<PaymentTransQueryModle.QueryItems> list = new ArrayList<PaymentTransQueryModle.QueryItems>();
		ptq.setQueryItems(list);
		String json=JsonUtils.toJson(ptq);
		return json;
	}


}
