package com.jrmf.common;


import com.alibaba.fastjson.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;


/**
 * @author: YJY
 * @date: 2020/10/21 15:00
 * @description:
 */
@FeignClient(name = "little-bee")
public interface LittleBeeFeignClient {

  @PostMapping(value = "/yuncr/open/api/upload/file")
  JSONObject uploadFile(JSONObject jsonObject);

  @PostMapping(value = "/yuncr/open/api/video/info")
  JSONObject videoInfo();

  @PostMapping(value = "/yuncr/open/api/true/name")
  JSONObject trueName(JSONObject jsonObject);

  @PostMapping(value = "/yuncr/open/api/individual/register")
  JSONObject individualRegister(JSONObject jsonObject);


  @PostMapping(value = "/yuncr/open/api/approval/status")
  JSONObject approvalStatus(JSONObject jsonObject);

  @PostMapping(value = "/yuncr/open/api/bank/card")
  JSONObject bankCard(JSONObject jsonObject);

  @PostMapping(value = "/yuncr/open/api/business/license/download")
  JSONObject businessLicenseDownload(JSONObject jsonObject);
}
