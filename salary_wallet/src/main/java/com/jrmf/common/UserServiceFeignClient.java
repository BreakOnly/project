package com.jrmf.common;

import com.alibaba.fastjson.JSONObject;
import com.jrmf.domain.dto.EconomicCategoryDTO;
import com.jrmf.domain.dto.EsignContractDTO;
import com.jrmf.domain.dto.YuncrPushProjectDTO;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "platform-user-service")
public interface UserServiceFeignClient {

  @ApiOperation("创建合同")
  @PostMapping(value = "/esign/contract/new")
  ServiceResponse createContract(@RequestBody EsignContractDTO esignContractDTO);
}
