package com.jrmf.payment.mybankapi.service;

import com.alibaba.fastjson.JSON;
import com.jrmf.payment.mybankapi.common.util.MyBankHttpClientUtil;
import com.jrmf.payment.mybankapi.request.ImitatePaymentTransferRequest;
import com.jrmf.payment.mybankapi.request.ModifyEnterpriseInfoRequest;
import com.jrmf.payment.mybankapi.request.PaymentTransferRequest;
import com.jrmf.payment.mybankapi.request.QueryAccountBalanceRequest;
import com.jrmf.payment.mybankapi.request.QueryEnterpriseInfoRequest;
import com.jrmf.payment.mybankapi.request.QueryTransHistoryInfoRequest;
import com.jrmf.payment.mybankapi.request.QueryTransferResultRequest;
import com.jrmf.payment.mybankapi.request.RegisterEnterpriseInfoRequest;
import com.jrmf.payment.mybankapi.request.SubAccountSubmitTransferRequest;
import com.jrmf.payment.mybankapi.response.MyBankBaseResponse;
import com.jrmf.payment.mybankapi.response.QueryAccountBalanceResponse;
import com.jrmf.payment.mybankapi.response.QueryTransferResultResponse;
import com.jrmf.payment.mybankapi.response.RegisterEnterpriseInfoResponse;
import com.jrmf.utils.StringUtil;


public class MyBankService {

  private String reqUrl;
  private String configFilePath;
  private String keyStoreName;
  private String partnerId;

  public MyBankService(String reqUrl, String configFilePath, String keyStoreName,
      String partnerId) {
    this.reqUrl = reqUrl;
    this.configFilePath = configFilePath;
    this.keyStoreName = keyStoreName;
    this.partnerId = partnerId;
  }

  public MyBankBaseResponse registerEnterpriseInfo(RegisterEnterpriseInfoRequest request) {
    request.setPartner_id(partnerId);

    String result = MyBankHttpClientUtil
        .doHttpClientPost(this.reqUrl, this.keyStoreName, this.configFilePath, request);

    MyBankBaseResponse response = null;

    if (!StringUtil.isEmpty(result)) {
      response = JSON.parseObject(result, RegisterEnterpriseInfoResponse.class);
    }

    return response;
  }

  public MyBankBaseResponse modifyEnterpriseInfo(ModifyEnterpriseInfoRequest request) {
    request.setPartner_id(partnerId);

    String result = MyBankHttpClientUtil
        .doHttpClientPost(this.reqUrl, this.keyStoreName, this.configFilePath, request);

    MyBankBaseResponse response = null;

    if (!StringUtil.isEmpty(result)) {
      response = JSON.parseObject(result, MyBankBaseResponse.class);
    }

    return response;
  }


  public MyBankBaseResponse queryEnterpriseInfo(QueryEnterpriseInfoRequest request) {
    request.setPartner_id(partnerId);

    String result = MyBankHttpClientUtil
        .doHttpClientPost(this.reqUrl, this.keyStoreName, this.configFilePath, request);

    MyBankBaseResponse response = null;

    if (!StringUtil.isEmpty(result)) {
      response = JSON.parseObject(result, MyBankBaseResponse.class);
    }

    return response;
  }


  public MyBankBaseResponse queryAccountBaLance(QueryAccountBalanceRequest request) {
    request.setPartner_id(partnerId);

    String result = MyBankHttpClientUtil
        .doHttpClientPost(this.reqUrl, this.keyStoreName, this.configFilePath, request);

    MyBankBaseResponse response = null;

    if (!StringUtil.isEmpty(result)) {
      response = JSON.parseObject(result, QueryAccountBalanceResponse.class);
    }

    return response;
  }


  public MyBankBaseResponse imitatePaymentTransfer(ImitatePaymentTransferRequest request) {
    request.setPartner_id(partnerId);

    String result = MyBankHttpClientUtil
        .doHttpClientPost(this.reqUrl, this.keyStoreName, this.configFilePath, request);

    MyBankBaseResponse response = null;

    if (!StringUtil.isEmpty(result)) {
      response = JSON.parseObject(result, MyBankBaseResponse.class);
    }

    return response;
  }


  public MyBankBaseResponse paymentTransfer(PaymentTransferRequest request) {
    request.setPartner_id(partnerId);

    String result = MyBankHttpClientUtil
        .doHttpClientPost(this.reqUrl, this.keyStoreName, this.configFilePath, request);

    MyBankBaseResponse response = null;

    if (!StringUtil.isEmpty(result)) {
      response = JSON.parseObject(result, MyBankBaseResponse.class);
    }

    return response;
  }


  public MyBankBaseResponse queryTransferResult(QueryTransferResultRequest request) {
    request.setPartner_id(partnerId);

    String result = MyBankHttpClientUtil
        .doHttpClientPost(this.reqUrl, this.keyStoreName, this.configFilePath, request);

    MyBankBaseResponse response = null;

    if (!StringUtil.isEmpty(result)) {
      response = JSON.parseObject(result, QueryTransferResultResponse.class);
    }

    return response;
  }


  public MyBankBaseResponse queryTransHistoryInfo(QueryTransHistoryInfoRequest request) {
    request.setPartner_id(partnerId);

    String result = MyBankHttpClientUtil
        .doHttpClientPost(this.reqUrl, this.keyStoreName, this.configFilePath, request);

    MyBankBaseResponse response = null;

    if (!StringUtil.isEmpty(result)) {
      response = JSON.parseObject(result, MyBankBaseResponse.class);
    }

    return response;
  }

  public MyBankBaseResponse subAccountSubmitTransfer(SubAccountSubmitTransferRequest request) {
    request.setPartner_id(partnerId);
    String result = MyBankHttpClientUtil
        .doHttpClientPost(this.reqUrl, this.keyStoreName, this.configFilePath, request);

    MyBankBaseResponse response = null;

    if (!StringUtil.isEmpty(result)) {
      response = JSON.parseObject(result, MyBankBaseResponse.class);
    }

    return response;
  }

  public static class Builder {

    private String reqUrl;
    private String configFilePath;
    private String keyStoreName;
    private String partnerId;

    public MyBankService.Builder reqUrl(String reqUrl) {
      this.reqUrl = reqUrl;
      return this;
    }

    public MyBankService.Builder configFilePath(String configFilePath) {
      this.configFilePath = configFilePath;
      return this;
    }


    public MyBankService.Builder keyStoreName(String keyStoreName) {
      this.keyStoreName = keyStoreName;
      return this;
    }

    public MyBankService.Builder partnerId(String partnerId) {
      this.partnerId = partnerId;
      return this;
    }

    public MyBankService build() {
      return new MyBankService(reqUrl, configFilePath, keyStoreName, partnerId);
    }
  }

}
