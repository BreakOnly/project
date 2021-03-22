package com.jrmf.controller.notify;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import com.jrmf.controller.constant.CommissionStatus;
import com.jrmf.controller.constant.LdCommissionBusinessTypeEnum;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.UserCommission;
import com.jrmf.payment.ymyf.entity.PayNotifyModel;
import com.jrmf.payment.ymyf.util.Base64Utils;
import com.jrmf.payment.ymyf.util.DESUtils;
import com.jrmf.payment.ymyf.util.JsonUtils;
import com.jrmf.service.*;
import com.jrmf.taxsettlement.api.service.CommonRetCodes;
import com.jrmf.taxsettlement.api.service.transfer.TransferDealStatusNotifier;
import com.jrmf.taxsettlement.api.service.transfer.TransferStatus;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class YMSHNotifyController {
	
	private static final Logger logger = LoggerFactory.getLogger(YMSHNotifyController.class);

	@Autowired
	CustomThirdPaymentConfigService customThirdPaymentConfigService;
	@Autowired
	UserCommissionService userCommissionService;
	@Autowired
	private UserCommissionService commissionService;
	@Autowired
	private UserSerivce userSerivce;
	@Autowired
	private TransferDealStatusNotifier transferDealStatusNotifier;
	@Autowired
	LdOrderStepService ldOrderStepService;

	private String RET_FAIL = "FAIL";
	private String RET_SUCCESS = "SUCCESS";

	/**
	 * {
	 *     "merchantId":"89900000064215707625",
	 *     "merbath":"batch1573624062496",
	 *     "merNo":order15736240625131,
	 *     "amount":1,
	 *     "status":"3",
	 *     "successDate":"2019-11-13 14:36:00",
	 *     "createDate":"2019-11-13 14:36:00",
	 * 	   "channelOrder":2019111313474200027141,
	 * }
	 * @param request
	 * @param reqBody
	 * @return
	 */
	@RequestMapping(value = "/ymshNotify",method = RequestMethod.POST)
	public String zxNotify(HttpServletRequest request, @RequestBody String reqBody){
		logger.info("接收到溢美优付下发通知数据:"+reqBody);
		PayNotifyModel payNotifyModel = JsonUtils.fromJson(reqBody, PayNotifyModel.class);
		//获取商户秘钥配置
		PaymentConfig paymentConfig = customThirdPaymentConfigService.getConfigByMerchId(payNotifyModel.getMerchantId());
		if (paymentConfig == null){
			logger.info("获取溢美商户秘钥信息失败:"+payNotifyModel.getMerchantId());
			return RET_FAIL;
		}
		//解密数据
		byte[] base64bs = Base64Utils.decode(payNotifyModel.getResData());
		byte[] debs = null;
		try {
			debs = DESUtils.decrypt(base64bs, paymentConfig.getApiKey());
		} catch (Exception e) {
			logger.error("解密溢美下发通知数据异常:",e);
			return RET_FAIL;
		}
		if (debs == null || debs.length == 0){
			logger.info("溢美优付下发通知获取ResData失败");
			return RET_FAIL;
		}
		String detailData = new String(debs, StandardCharsets.UTF_8);
		logger.info("溢美优付下发通知解密数据："+detailData);

		//获取本地下发记录
		//{"amount":101,"channelOrder":"2020122417321800914499","createDate":"2020-12-24 17:32:18","merId":"89900000125115783648","merNo":"2020122400000017",
		// "merbath":"2020122400000017","msg":"三要素银行卡验证认证失败","status":"4","sucAmount":0,"successDate":"2020-12-24 17:32:19"}
		Map<String, Object> respMap = JSONObject.parseObject(detailData, Map.class);
		String merNo = (String) respMap.get("merNo");
		String status = (String) respMap.get("status");
		if (!"3".equals(status)){
			logger.info("返回的订单状态不是成功：{}",merNo);
			return RET_SUCCESS;
		}
		UserCommission userCommission = userCommissionService.getUserCommission(merNo);
		if (userCommission == null){
			logger.info("溢美优付下发通知获取本地 UserCommission 信息失败,OrderNo:{}",merNo);
			return RET_SUCCESS;
		}
		if (userCommission.getStatus() != 3){
			logger.info("溢美优付下发通知订单状态异常,OrderNo:{}",merNo);
			return RET_SUCCESS;
		}
		//判断下发的类型
		String batchId = userCommission.getBatchId();
		Integer isSplit = userCommission.getIsSplit();
		String businessType = userCommission.getBusinessType();
		if (StringUtil.isEmpty(batchId)){
			if (isSplit != null && isSplit == 1){//-API拆单
				if (LdCommissionBusinessTypeEnum.B2CSPLIT.getCode().equals(businessType)){
					//调用 ExecuteSplitOrderLdQuery 处理
					handleApiSplitPay(userCommission);
				}
			}else{//-API下发
				//调用 ApiTaskImpl 处理
				handleApiPay(userCommission);
			}
		}else{
			//-WEB批次下发
			if (isSplit == null || isSplit != 1){
				//调用 ExecuteBatchGrantQuery 处理
				handleWebBatchPay(userCommission);
			}
		}
		return RET_SUCCESS;
	}

	private void handleApiPay(UserCommission userCommission){
		userCommission.setStatus(1);
		userCommission.setStatusDesc("成功");
		Map<String, Object> stringObjectMap = userSerivce.addUserInfo(
				userCommission.getUserName(),
				userCommission.getDocumentType(),
				userCommission.getCertId(),
				userCommission.getUserNo(),
				userCommission.getPhoneNo(),
				userCommission.getOriginalId(),
				userCommission.getMerchantId(), "");
		userCommission.setUserId(stringObjectMap.get("userId") + "");
		userCommission.setPaymentTime(DateUtils.getNowDate());
		commissionService.updateUserCommissionById(userCommission);
		transferDealStatusNotifier.notify(userCommission.getOrderNo(), TransferStatus.TRANSFER_DONE, CommonRetCodes.ACTION_DONE.getCode(), CommonRetCodes.ACTION_DONE.getDesc());
		logger.info("溢美优付下发通知API交易成功！系统内部回调成功！");
	}

	private void handleApiSplitPay(UserCommission commission){
		String orderNo = commission.getOrderNo();
		//根据拆单订单判断主账户状态
		int totalCount = ldOrderStepService.getCountByOrderNo(orderNo);
		int successCount = ldOrderStepService.getCountSuccessByOrderNo(orderNo);
		if (successCount == totalCount) {
			logger.info("溢美优付下发通知API拆单交易明细订单号：" + commission.getOrderNo() + "明细步骤全部为成功，更新交易明细订单为成功");
			commission.setPaymentTime(DateUtils.getNowDate());
			commission.setStatus(CommissionStatus.SUCCESS.getCode());
			commission.setStatusDesc(CommissionStatus.SUCCESS.getDesc());

			//添加用户
			Map<String, Object> stringObjectMap = userSerivce.addUserInfo(
					commission.getUserName(),
					commission.getDocumentType(),
					commission.getCertId(),
					commission.getUserNo(),
					commission.getPhoneNo(),
					commission.getOriginalId(),
					commission.getMerchantId(), "");
			commission.setUserId(stringObjectMap.get("userId") + "");

			int updateCount = commissionService.updateUserCommissionById(commission);
			//api下发进行回调
			if (StringUtil.isEmpty(commission.getBatchId()) && updateCount == 1) {
				transferDealStatusNotifier.notify(commission.getOrderNo(), TransferStatus.TRANSFER_DONE,
						CommonRetCodes.ACTION_DONE.getCode(), CommonRetCodes.ACTION_DONE.getDesc());
				logger.info("溢美优付下发通知API拆单交易明细订单号：" + commission.getOrderNo() + "交易成功！系统内部回调成功！");
			}
		}
	}

	private void handleWebBatchPay(UserCommission userCommission){
		userCommission.setStatus(1);
		userCommission.setStatusDesc("成功");
		userCommission.setPaymentTime(DateUtils.getNowDate());
		commissionService.updateUserCommissionById(userCommission);
		logger.info("溢美优付下发通知WEB批次下发更新成功 orderNo[{}]", userCommission.getOrderNo());

		if (CommissionStatus.SUCCESS.getCode() == userCommission.getStatus()) {
			//更新userrelated表，用户在某一商户下的手机号
			userSerivce.updateUserRelated(userCommission.getUserId(), userCommission.getPhoneNo(), userCommission.getOriginalId());
		}
	}

	public static void main(String[] args) {
		String detailData = "{\"amount\":101,\"channelOrder\":\"2020122417321800914499\",\"createDate\":\"2020-12-24 17:32:18\",\"merId\":\"89900000125115783648\",\"merNo\":\"2020122400000017\",\"merbath\":\"2020122400000017\",\"msg\":\"三要素银行卡验证认证失败\",\"status\":\"4\",\"sucAmount\":0,\"successDate\":\"2020-12-24 17:32:19\"}";
		Map<String, Object> respMap = JSONObject.parseObject(detailData, Map.class);
		System.out.println(respMap);
		String merNo = (String) respMap.get("merNo");
		Integer status = (Integer) respMap.get("status");
		System.out.println("merNo:"+merNo +",status:"+status);
	}

}




















