package com.jrmf.controller.receipt;

import com.jrmf.controller.constant.BaseInfo;
import com.jrmf.domain.ReceiptCommission;
import com.jrmf.payment.openapi.OpenApiClient;
import com.jrmf.payment.openapi.model.request.deliver.ReceiptRequestParam;
import com.jrmf.payment.openapi.model.response.BaseResponseResult;
import com.jrmf.payment.openapi.model.response.OpenApiBaseResponse;
import com.jrmf.service.ReceiptService;
import com.jrmf.utils.DateUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用途：
 * 作者：郭桐宁
 * 时间：2019/1/28 21:43
 * Version:1.0
 */
public class UpdateReceiptCommission implements Runnable {
    private Integer beforeDayCount;
    private Integer payType;
    private Integer companyId;
    private ReceiptService receiptService;
    private BaseInfo baseInfo;

    public UpdateReceiptCommission(Integer beforeDayCount, Integer payType, Integer companyId, ReceiptService receiptService, BaseInfo baseInfo) {
        this.beforeDayCount = beforeDayCount;
        this.payType = payType;
        this.companyId = companyId;
        this.receiptService = receiptService;
        this.baseInfo = baseInfo;
    }

    @Override
    public void run() {
        OpenApiClient client;

        String priKeyString = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCcdJ+nqtInOaHn/fZTbxdj5oTMFU6AJKD3DWmoYqUGcMCebtpdKhT/BbAb/t8Rsp1QLv1r4h+nf7s98LZkV9J6dSa5iHDIqCGT12byaTftKrYZlf3Hr8SFDkzBPTSBz56LRnYCRaYlrMQgZgY9U/T5UIwKVhJEordPvwbH2MqueKxUdk9vD4S8Dj7rEgor31Ifc3E/qRivKcRCncWBpiAtOTNSPLtsx0TIxizPXYicjL8Tr2+eLnuUxjKhWnFxvpRMC2ghLVrsWiVU0oW/0a6Qbv+TtB4/xR1JWV+pn4fqQhiJAssMHN4kEn7JxPpXB+/MzWUfrrGsYnSlNwpxPojzAgMBAAECggEAYJNEsfSpwHi8zj1fveTHJW1375n/WO5DRfzLiZtKjo0u+R0oQXXme/0A1mcfPwdoP8ShveRY8cXQyM07aPkk/V4vRztHkzTldSLzcxMr6IQC4AxMGOUQg6luC6JCNRb5oLMfyQtBIeRhNDaGB3k5sGPd7ctvf1qJmPorr1TM16C9vW8MdF4YjOfxpvm2g5AuaFHB3O9jTd6+tN3oAiusC3TwjtvhfQDmnPqfJOdp2x2NZAf47luU7J/eiEvyEPkjfmwSIQKvXfzIw0uu2rLX+52OmtJlJ8DYF8Ep9iCSJJMLiHhxYkSkNuwyB5/5oVZmRGj8mZVNdfIXyCaFXnEw0QKBgQDzZFAYCg+QRQZx9fdWwM4mSeHRCeZpQ9QBGjwNN0yWF+XioNtnMIr/pZYV/25biVVF5Pc/20r/8ZSBCShgR3u1ePuoaxzU907lT3R7tBoqaIfvjEBaZuOQcaGJqvNNJnySmhP1PnsjD5x9wWwer7nJLvl8UqmgbED0gKoClfWopwKBgQCkj2ypjvo6fHg19huw96y7CaYQjljWslY6GsWzE1WqSVy0xJb6JEzARZe38XhXPiAeg9mhlyUfkum8LJaYGe1JA5a2c9qhUOPfFeGM6PblGcF1jlMZYLv8shhxkjeWQA/kJngbW6lCHQBbc2G/W7irUC+PbkZkknAcYkYlQE3a1QKBgD9fVxtrQzIlRtBVYtlLymFdy1ZKZZvy9Th0RD6Mr3xFLK4dhAMSOJ7n1nRT1cAvuexA+b++sYCCvk/6unCXLDbMEXqAqTkqS3iZf5LWChoQrZRJyFfBgm8RpyXZRRBJfRYO2DN62UT/w5dazXQP/SfM+1jLjS8gAKmo9ptFwHjxAoGAK3y3g4uENwaDogb6xGZ/YCIpn4Bum7YfMVW33x4B6nFerWqyV0JWgg0iDfsjCTMiu82uKpTNu61QVWkXFvTrDvuCzY6KPU0qGt8mbt11uY9334AQF8nHg/zwlrrEM9GUIX/FB73OWeleGczBDRfJEoSrPOUwdw130RhrXxbCPE0CgYEAvXFClSExFaNvOz5eowwn09mOtgWEJEU6TFBEJNVoCaqavQlpelIyLGMA/+KP6Xg3ZvbfPTf1iTQhX3nLhOBEV4DanUo+9SH+96j0petltFgzkogf7vw3NHDf/bAuYAvyecrhRXAnRSy/YIAqF/p3nZDird3vSVgvdkjKtAUGrsc=";
        boolean isTest = true;
        String appId = "bjylmfay";
        if (!isTest) {
            client = new OpenApiClient.Builder().appId(appId).privateKey(priKeyString).useSandbox().build();
        } else {
            client = new OpenApiClient.Builder().appId(appId).privateKey(priKeyString).build();
        }

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("receiptTime", DateUtils.getBeforeDayString(beforeDayCount));
        params.put("payType", payType);
        params.put("companyId", companyId);
        params.put("merchantId", "aiyuangong");

        List<ReceiptCommission> listReceipt = receiptService.listReceiptCommission(params);
        for (ReceiptCommission commission : listReceipt) {
            ReceiptRequestParam param = new ReceiptRequestParam();
            String reqNo = commission.getOrderNo();
            param.setReqNo(reqNo);
            param.setNotifyUrl(baseInfo.getDomainName() + "/receipt/receiptAsyncNotify.do");
            param.setOutOrderNo(commission.getOrderNo());
            param.setAttach("回单");
            OpenApiBaseResponse<BaseResponseResult<String>> response = client.execute(param);

            String aygReceiptStatus;
            String aygCode = response.getCode();
            if (OpenApiBaseResponse.SUCCESS_CODE.equals(aygCode)) {
                aygReceiptStatus = "1";
            } else if ("2002".equals(aygCode) || "2101".equals(aygCode)) {
                aygReceiptStatus = "2";
            } else {
                aygReceiptStatus = "3";
            }

            params.clear();
            params.put("aygRreceiptStatus", aygReceiptStatus);
            params.put("accountDate", commission.getAccountDate());
            params.put("receiptNo", commission.getReceiptNo());
            receiptService.updateReceiptCommissionByReceiptNo(params);

        }
    }
}
