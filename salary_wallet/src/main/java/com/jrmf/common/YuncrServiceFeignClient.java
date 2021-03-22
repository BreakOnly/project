package com.jrmf.common;

import com.alibaba.fastjson.JSONObject;
import com.jrmf.domain.dto.EconomicCategoryDTO;
import com.jrmf.domain.dto.YuncrPushProjectDTO;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "platform-yuncr-service")
public interface YuncrServiceFeignClient {

  @GetMapping("/yuncr/live/list/economic/category")
  public ServiceResponse<List<EconomicCategoryDTO>> getCategoryList(
      @RequestParam Integer level,
      @RequestParam(required = false) String levelCode);

  @PostMapping("/yuncr/project/open/item")
  public ServiceResponse openItem(@RequestBody YuncrPushProjectDTO yuncrPushProjectDTO);

  @ApiOperation("云控开放api - 企业审核")
  @PostMapping(value = "/yuncr/live/approval/qy")
  JSONObject approvalQy(@RequestBody JSONObject object);

  @ApiOperation("云控开放api - 身份认证")
  @PostMapping(value = "/yuncr/live/identity/auth")
  JSONObject sfc(@RequestBody JSONObject object);

  @ApiOperation("云控开放api - 实名认证")
  @GetMapping(value = "/yuncr/live/real/name/auth")
  JSONObject nameAuth(@RequestBody JSONObject object);

  @ApiOperation("云控开放api - 活体检测")
  @PostMapping(value = "/yuncr/live/video/auth")
  JSONObject videoAuth(@RequestBody JSONObject object);

  @ApiOperation("云控开放api - 注册工商户")
  @GetMapping(value = "/yuncr/live/business/license")
  JSONObject businessLicense(@RequestBody JSONObject object);


  @ApiOperation("云控开放api - 上传结算发票")
  @PostMapping(value = "/yuncr/project/open/settlement/upload/invoice")
  JSONObject openSettlement(@RequestBody Map<String,Object> params);


  @ApiOperation("云控开放api - 推送合同")
  @PostMapping(value = "/yuncr/project/open/contract")
  JSONObject pushContract(@RequestBody Map<String,Object> params);

  @ApiOperation("云控开放api - 推送结算")
  @PostMapping(value = "/yuncr/project/open/settlement")
  JSONObject pushSettlement(@RequestBody Map<String,Object> params);

  @ApiOperation("云控开放api - 上传结算单")
  @PostMapping(value = "/yuncr/project/open/settlement/upload")
  JSONObject pushSettlementUpload(@RequestBody Map<String,Object> params);
}



















