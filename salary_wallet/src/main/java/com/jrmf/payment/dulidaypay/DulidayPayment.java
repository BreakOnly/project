package com.jrmf.payment.dulidaypay;

import com.alibaba.fastjson.JSON;
import com.duliday.openapi.OpenApiClient;
import com.duliday.openapi.param.QueryAmountParam;
import com.duliday.openapi.param.SinglePayResultParam;
import com.duliday.openapi.param.SinglePaymentParam;
import com.duliday.openapi.response.OpenApiBaseResponse;
import com.duliday.openapi.result.QueryAmountResult;
import com.duliday.openapi.result.SinglePayResultResult;
import com.duliday.openapi.result.SinglePaymentResult;
import com.jrmf.bankapi.LinkageTransHistoryPage;
import com.jrmf.domain.LinkageQueryTranHistory;
import com.jrmf.domain.LinkageTransferRecord;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.domain.UserCommission;
import com.jrmf.payment.entity.Payment;
import com.jrmf.payment.util.ClientMapUtil;
import com.jrmf.payment.util.PayRespCode;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.payment.util.TransStatus;
import com.jrmf.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DulidayPayment implements Payment<SinglePaymentParam, OpenApiBaseResponse<SinglePaymentResult>, String> {

    private Logger logger = LoggerFactory.getLogger(DulidayPayment.class);

    public PaymentConfig payment;

    public DulidayPayment(PaymentConfig payment) {
        super();
        this.payment = payment;
    }

    @Override
    public SinglePaymentParam getTransferTemple(UserCommission userCommission) {
        SinglePaymentParam param = new SinglePaymentParam();
        param.setOutOrderNo(userCommission.getOrderNo());
        param.setPayeeAccount(userCommission.getAccount());
        param.setChannel("duliday");

        String phone = userCommission.getPhoneNo();
        if (StringUtil.isEmpty(phone)) {
            phone = "17701393451";
        }
        param.setPhone(phone);

        param.setAmount(userCommission.getAmount());
        param.setIdentity(userCommission.getCertId());
        param.setPayeeRealName(userCommission.getUserName());
        param.setRemark(userCommission.getRemark() == null ? "-" : userCommission.getRemark());
        return param;
    }

    @Override
    public PaymentReturn<String> paymentTransfer(UserCommission userCommission) {

        OpenApiClient client = getClient(payment);

        SinglePaymentParam param = getTransferTemple(userCommission);
        logger.info("---------独立日 请求参数对象 SinglePaymentParam ------------:{}", JSON.toJSONString(param));
        OpenApiBaseResponse<SinglePaymentResult> result = client.execute(param);

        logger.info("---------独立日 返回参数对象 OpenApiBaseResponse<SinglePaymentResult> ------------:{}", JSON.toJSONString(result));

        return getTransferResult(result);
    }

    @Override
    public PaymentReturn<String> getTransferResult(OpenApiBaseResponse<SinglePaymentResult> result) {

        PaymentReturn<String> transferReturn;
        if (OpenApiBaseResponse.SUCCESS_CODE.equals(result.getCode())) {
            transferReturn = new PaymentReturn<String>(result.getCode(),
                    result.getMsg(),
                    result.getData().getOutOrderNo());
        } else {
            transferReturn = new PaymentReturn<>(result.getCode(),
                    result.getMsg(),
                    "");
        }


        logger.info("独立日支付下发返回参数：{}", JSON.toJSONString(transferReturn));

        return transferReturn;
    }

    @Override
    public PaymentReturn<TransStatus> queryTransferResult(String orderNo) {

        OpenApiClient client = getClient(payment);

        SinglePayResultParam param = new SinglePayResultParam();
        param.setOutOrderNo(orderNo);
        logger.info("---------独立日 请求参数对象 SinglePayResultParam ------------:" + JSON.toJSONString(param));
        OpenApiBaseResponse<SinglePayResultResult> result = client.execute(param);
        logger.info("---------独立日 返回参数对象 OpenApiBaseResponse<SinglePayResultResult> ------------:" + JSON.toJSONString(result));

        String code;
        String massage;
        String transCode = "";
        String transMsg = "";
        String transOrderNo = "";

        if (OpenApiBaseResponse.SUCCESS_CODE.equals(result.getCode())) {
            code = PayRespCode.RESP_SUCCESS;
            if ("30".equals(result.getData().getCode())) {
                transCode = PayRespCode.RESP_TRANSFER_SUCCESS;
            } else if ("40".equals(result.getData().getCode())) {
                transCode = PayRespCode.RESP_TRANSFER_FAILURE;
            } else if ("2002".equals(result.getData().getCode())) {
                transCode = PayRespCode.RESP_TRANSFER_UNKNOWN;
            } else {
                transCode = PayRespCode.RESP_TRANSFER_UNKNOWN;
            }
        } else {
            code = PayRespCode.RESP_FAILURE;
        }

        massage = result.getMsg();
        SinglePayResultResult queryResult = result.getData();
        if (queryResult != null) {
            transMsg = queryResult.getMsg();
            transOrderNo = queryResult.getOutOrderNo();
        }

        TransStatus transStatus = new TransStatus(transOrderNo,
                transCode,
                transMsg);

        PaymentReturn<TransStatus> paymentReturn = new PaymentReturn<TransStatus>(code,
                massage,
                transStatus);
        logger.info("独立日支付查询返回参数：{}", paymentReturn.toString());

        return paymentReturn;
    }

    @Override
    public PaymentReturn<String> queryBalanceResult(String type) {
        OpenApiClient client = getClient(payment);

        QueryAmountParam param = new QueryAmountParam();
        OpenApiBaseResponse<QueryAmountResult> result = client.execute(param);

        String balance = result.getData().getBalance();

        PaymentReturn<String> transferReturn = new PaymentReturn<>(result.getCode(),
                result.getMsg(),
                balance);

        logger.info("独立日查询余额返回参数：" + transferReturn.toString());

        return transferReturn;
    }

    @Override
    public PaymentReturn<String> linkageTransfer(LinkageTransferRecord transferRecord) {
        return null;
    }

    @Override
    public PaymentReturn<LinkageTransHistoryPage> queryTransHistoryPage(LinkageQueryTranHistory queryParams) {
        return null;
    }


    private OpenApiClient getClient(PaymentConfig payment) {

        logger.info("---------独立日appid------------:" + payment.getAppIdAyg());
        logger.info("---------独立日privateKey------------:" + payment.getPayPrivateKey());

        OpenApiClient client = ClientMapUtil.dulidayHttpClient.get(payment.getAppIdAyg());
        if (client == null) {
            synchronized (ClientMapUtil.dulidayHttpClient) {
                client = ClientMapUtil.dulidayHttpClient.get(payment.getAppIdAyg());
                if (client == null) {
                    client = new OpenApiClient.Builder().appId(payment.getAppIdAyg()).privateKey(payment.getPayPrivateKey()).build();
                    ClientMapUtil.dulidayHttpClient.putIfAbsent(payment.getAppIdAyg(), client);
                }
            }
        }

        return client;
    }
}
