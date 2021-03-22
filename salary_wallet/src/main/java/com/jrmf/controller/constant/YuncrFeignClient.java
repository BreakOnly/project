package com.jrmf.controller.constant;

import com.alibaba.fastjson.JSONObject;
import com.jrmf.common.RespResult;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

//@FeignClient(name = "platform-yuncr-service", url = "http://localhost:8051")
@FeignClient(name = "platform-yuncr-service")
public interface YuncrFeignClient {

  @PostMapping(value = "/yuncr/project/open/firm")
  ResponseEntity<RespResult<Map<String, String>>> openFirm(
      @RequestBody JSONObject yuncrPushCustomerFirm);

  @PostMapping(value = "/yuncr/project/open/contract")
  ResponseEntity<RespResult<Map<String, String>>> openContract(
      @RequestBody JSONObject yuncrPushContract);

  @PostMapping(value = "/yuncr/live/save/bank")
  ResponseEntity<RespResult<Map<String, String>>> saveBankInfo(
      @RequestBody JSONObject yuncrUserBank);
}
