package com.jrmf.controller.subcontract;

import com.jrmf.common.APIResponse;
import com.jrmf.controller.constant.PayType;
import com.jrmf.domain.SubcontractRouter;
import com.jrmf.domain.dto.SubcontractRouterQueryDTO;
import com.jrmf.domain.vo.PayTypeVO;
import com.jrmf.service.ChannelInterimBatchService2;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/subcontract/")
@Api(tags = "转包服务")
public class SubcontractController {

  @Autowired
  ChannelInterimBatchService2 channelInterimBatchService2;

  //根据商户查询是否有转包多个完税服务公司配置
  @ApiOperation("根据商户查询是否有转包多个完税服务公司")
  @GetMapping("company/subcontract")
  public APIResponse listSubcontractCompany(@RequestParam String customKey,
      @RequestParam Integer companyId) {
    SubcontractRouterQueryDTO subcontractRouterQueryDTO = new SubcontractRouterQueryDTO();
    subcontractRouterQueryDTO.setCompanyId(companyId);
    subcontractRouterQueryDTO.setCustomKey(customKey);
    List<SubcontractRouter> subcontractRouterList = channelInterimBatchService2
        .listSubcontractRouter(subcontractRouterQueryDTO);
    return APIResponse.successResponse(subcontractRouterList);
  }


  @ApiOperation("查询服务公司默认支付通道的支付方式")
  @GetMapping("company/paytypes")
  public APIResponse listPayTypesOfCompanyDefaultPayChannel(@RequestParam Integer companyId) {
    List<String> payTypes = channelInterimBatchService2
        .listPayTypesOfCompanyDefaultPayChannel(companyId);

    List<PayTypeVO> payTypeVoList = payTypes.stream().map(payTypeStr -> {
      int payTypeCode = Integer.valueOf(payTypeStr);
      PayTypeVO payTypeVO = new PayTypeVO(payTypeCode, PayType.codeOf(payTypeCode).getDesc());
      return payTypeVO;
    }).collect(Collectors.toList());
    return APIResponse.successResponse(payTypeVoList);
  }

}
