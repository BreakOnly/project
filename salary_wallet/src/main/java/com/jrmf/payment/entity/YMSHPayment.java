package com.jrmf.payment.entity;

import com.jrmf.service.CustomMenuService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.jrmf.bankapi.LinkageTransHistoryPage;
import com.jrmf.controller.constant.BaseInfo;
import com.jrmf.domain.AgreementTemplate;
import com.jrmf.domain.Company;
import com.jrmf.domain.LinkageQueryTranHistory;
import com.jrmf.domain.LinkageTransferRecord;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.UserCommission;
import com.jrmf.payment.util.PayRespCode;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.payment.util.TransStatus;
import com.jrmf.payment.ymyf.YFService;
import com.jrmf.payment.ymyf.entity.PaymentTransModel;
import com.jrmf.payment.ymyf.entity.PaymentTransQueryModle;
import com.jrmf.payment.ymyf.entity.PaymentTransQueryModle.QueryItems;
import com.jrmf.payment.ymyf.util.JsonUtils;
import com.jrmf.service.AgreementTemplateService;
import com.jrmf.service.CompanyService;
import com.jrmf.service.UserCommissionService;
import com.jrmf.utils.AmountConvertUtil;
import com.jrmf.utils.SpringContextUtil;
import com.jrmf.utils.StringUtil;
import com.jrmf.utils.exception.YmyfNormalExcepion;

public class YMSHPayment implements Payment<Map<String, Object>, Map<String, Object>, String>{

	private Logger logger = LoggerFactory.getLogger(YMSHPayment.class);

	public PaymentConfig payment;

	private AgreementTemplateService agreementTemplateService = SpringContextUtil.getBean(AgreementTemplateService.class);

	private UserCommissionService userCommissionService = SpringContextUtil.getBean(UserCommissionService.class);

	private CompanyService companyService = SpringContextUtil.getBean(CompanyService.class);

	private CustomMenuService customMenuService = SpringContextUtil.getBean(CustomMenuService.class);

	private BaseInfo baseInfo = SpringContextUtil.getBean(BaseInfo.class);

	public YMSHPayment(PaymentConfig payment) {
		super();
		this.payment = payment;
	}


	@Override
	public Map<String, Object> getTransferTemple(UserCommission userCommission) {
		return null;
	}

	@Override
	public PaymentReturn<String> paymentTransfer(UserCommission userCommission) {
		logger.info("订单号："+userCommission.getOrderNo()+"付款通道为溢美普通商户模式");
		PaymentReturn<String> transferReturn = null;
		try{
			Map<String, Object> agreementParam = new HashMap<>();
			agreementParam.put("originalId", userCommission.getOriginalId());
			agreementParam.put("companyId", userCommission.getCompanyId());
			//获取商户签约服务公司配置
//			List<AgreementTemplate> agreementTemplates = agreementTemplateService.getAgreementTemplateByParam(agreementParam);
//			if(agreementTemplates.size()>0){
//				Company serviceCompany = companyService.getCompanyByUserId(Integer.parseInt(userCommission.getCompanyId()));
				Company realServiceCompany = companyService.getCompanyByUserId(
						Integer.parseInt(userCommission.getRealCompanyId()));
//				AgreementTemplate agreementTemplate = agreementTemplates.get(0);
				//获取溢美项目id
				String projectId = customMenuService.getProjectIdByCustomKey(userCommission.getOriginalId());
				String reqInfo = getPrePaymentTrans(userCommission,payment.getThirdMerchId(),realServiceCompany.getServiceCompanyId(),projectId);
				//调用服务
				YFService yfService = new YFService(payment.getPreHost(), payment.getThirdMerchId(),
						payment.getPayPrivateKey(), payment.getPayPublicKey(), payment.getApiKey(), reqInfo, "UTF-8");
				//获取响应
				String respInfo = yfService.payment();
				Map<String, Object> respMap = JSONObject.parseObject(respInfo, Map.class);
				transferReturn = getTransferResult(respMap);
//			}else{
//				transferReturn = new PaymentReturn<String>(PayRespCode.RESP_CHECK_FAIL,"无签约模板配置",userCommission.getOrderNo());
//			}
		}catch(YmyfNormalExcepion e){
			logger.error(e.getMessage(),e);
			//YmyfNormalExcepion不能作为请求失败依据，这里人工介入或等待定时任务查询状态
			transferReturn = new PaymentReturn<>(PayRespCode.RESP_UNKNOWN,e.getMessage(),userCommission.getOrderNo());
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			transferReturn = new PaymentReturn<>(PayRespCode.RESP_UNKNOWN,PayRespCode.codeMaps.get(PayRespCode.RESP_UNKNOWN),userCommission.getOrderNo());
		}
		return transferReturn;
	}

	/**
	 * 预下单参数
	 *
	 * @return
	 */
	private String getPrePaymentTrans(UserCommission userCommission,String thirdMerchId,Long serviceCompanyId,String projectId) {
		PaymentTransModel pt = new PaymentTransModel();
		//商户号
		pt.setMerId(thirdMerchId);
		//总金额
		pt.setTotalAmt(AmountConvertUtil.changeY2F(userCommission.getAmount()));
		//总笔数
		pt.setTotalCount("1");
		//批次号
		pt.setMerBatchId(userCommission.getOrderNo());
		pt.setProjectId(projectId);
		List<PaymentTransModel.PayItems> list = new ArrayList<>();
		PaymentTransModel.PayItems param = param(userCommission,serviceCompanyId);
		list.add(param);
		pt.setPayItems(list);
		String json = JsonUtils.toJson(pt);
		return json;
	}

	/**
	 * 预下单参数
	 * @param userCommission
	 * @param serviceCompanyId
	 * @return
	 */
	private PaymentTransModel.PayItems param(UserCommission userCommission,Long serviceCompanyId) {
		PaymentTransModel.PayItems pi1 = new PaymentTransModel.PayItems();
		//交易金额
		pi1.setAmt(Long.parseLong(AmountConvertUtil.changeY2F(userCommission.getAmount())));
		//身份证
		pi1.setIdCard(userCommission.getCertId());
		//备注
		pi1.setMemo(userCommission.getRemark());
		//订单号
		pi1.setMerOrderId(userCommission.getOrderNo());
		//手机号
		pi1.setMobile(userCommission.getPhoneNo());
		//银行卡
		pi1.setPayeeAcc(userCommission.getAccount());
		//户名
		pi1.setPayeeName(userCommission.getUserName());
		//代付方式 0：银行卡，1：支付宝，2：微信
		pi1.setPaymentType(0);
		//代付类型 0：实时 1：工作日
		pi1.setPayType(0);
		//优地ID
		pi1.setLevyId(serviceCompanyId);
		pi1.setAccType(1);
		pi1.setNotifyUrl(baseInfo.getDomainName()+"/ymshNotify.do");
		return pi1;
	}

	@Override
	public PaymentReturn<String> getTransferResult(Map<String, Object> result) {
		String orderNo = "";
		//受理成功
		logger.info("付款受理成功");
		String code = PayRespCode.RESP_SUCCESS;
		String message = PayRespCode.codeMaps.get(PayRespCode.RESP_SUCCESS);
		PaymentReturn<String> transferReturn = new PaymentReturn<String>(code,message,orderNo);
		return transferReturn;
	}

	@Override
	public PaymentReturn<TransStatus> queryTransferResult(String orderNo) {
		String code = "";
		String message = "";
		String transCode = "";
		String transMsg = "";
		PaymentReturn<TransStatus> paymentReturn = null;
		try{
			UserCommission userCommission = userCommissionService.getUserCommission(orderNo);
			Map<String, Object> agreementParam = new HashMap<>();
			agreementParam.put("originalId", userCommission.getOriginalId());
			agreementParam.put("companyId", userCommission.getCompanyId());
			//获取商户签约服务公司配置
//			List<AgreementTemplate> agreementTemplates = agreementTemplateService.getAgreementTemplateByParam(agreementParam);
//			if(agreementTemplates.size()>0){
//				AgreementTemplate agreementTemplate = agreementTemplates.get(0);
				//封装请求信息
				String reqInfo = getPayResultQueryJson(orderNo,payment.getThirdMerchId());
				//调用服务
				YFService yfService = new YFService(payment.getPreHost(), payment.getThirdMerchId(),
						payment.getPayPrivateKey(), payment.getPayPublicKey(), payment.getApiKey(), reqInfo, "UTF-8");
				//获取响应
				String respInfo = yfService.paymentQuery();
				code = PayRespCode.RESP_SUCCESS;
				message = PayRespCode.codeMaps.get(PayRespCode.RESP_SUCCESS);
				PaymentTransQueryModle queryModle = JsonUtils.fromJson(respInfo, PaymentTransQueryModle.class);
				List<QueryItems>queryItems = queryModle.getQueryItems();
				for (QueryItems queryItem : queryItems) {
					if(queryItem.getMerOrderId().equals(orderNo)){
						//封装响应结果
						if(queryItem.getState()==3){
							//交易成功
							logger.info("订单号："+orderNo+"交易成功");
							transCode = PayRespCode.RESP_TRANSFER_SUCCESS;
							transMsg = PayRespCode.codeMaps.get(PayRespCode.RESP_SUCCESS);
						}else if(queryItem.getState()==4){
							//交易失败
							logger.info("订单号："+orderNo+"交易失败："+queryItem.getResMsg());
							transCode = PayRespCode.RESP_TRANSFER_FAILURE;
							transMsg = queryItem.getResMsg();
						}else{
							//待付款/汇款处理中
							transCode = PayRespCode.RESP_TRANSFER_UNKNOWN;
							transMsg = PayRespCode.codeMaps.get(PayRespCode.RESP_TRANSFER_UNKNOWN);
						}
					}
				}
//			}else{
//				transCode = PayRespCode.RESP_FAILURE;
//				transMsg = "无签约模板配置";
//			}
			TransStatus transStatus = new TransStatus(orderNo,transCode,transMsg);
			paymentReturn = new PaymentReturn<TransStatus>(code,message,transStatus);
		}catch(Exception e){
			code = PayRespCode.RESP_FAILURE;
			message = PayRespCode.codeMaps.get(PayRespCode.RESP_FAILURE);
			paymentReturn = new PaymentReturn<TransStatus>(code,message,null);
		}
		return paymentReturn;
	}

	/**
	 * 下单查询结果参数
	 * @param orderNo
	 * @param thirdMerchId
	 * @return
	 */
	private String getPayResultQueryJson(String orderNo,String thirdMerchId){
		PaymentTransQueryModle ptq = new PaymentTransQueryModle();
		ptq.setMerBatchId(orderNo);
		ptq.setMerId(thirdMerchId);
		List<PaymentTransQueryModle.QueryItems> list = new ArrayList<PaymentTransQueryModle.QueryItems>();
		PaymentTransQueryModle.QueryItems pi1 = new PaymentTransQueryModle.QueryItems();
		pi1.setMerOrderId(orderNo);
		list.add(pi1);
		ptq.setQueryItems(list);
		String json=JsonUtils.toJson(ptq);
		return json;
	}


	@Override
	public PaymentReturn<String> queryBalanceResult(String type) {
		return null;
	}

	@Override
	public PaymentReturn<String> linkageTransfer(
			LinkageTransferRecord transferRecord) {
		return null;
	}

	@Override
	public PaymentReturn<LinkageTransHistoryPage> queryTransHistoryPage(
			LinkageQueryTranHistory queryParams) {
		return null;
	}

}
