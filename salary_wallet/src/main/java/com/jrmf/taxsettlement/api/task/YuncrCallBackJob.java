package com.jrmf.taxsettlement.api.task;

import com.jrmf.service.UserAuthenticationService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: YJY
 * @date: 2020/12/4 15:37
 * @description: 个体工商户审核 检查用户回调情况
 */
@Component
public class YuncrCallBackJob {

  @Autowired
  UserAuthenticationService userAuthenticationService;


  @XxlJob("yuncrCallBackJob")
  private ReturnT<String> yuncrCallBackJob(String param) {

    userAuthenticationService.callBack(param);
    return ReturnT.SUCCESS;
  }
  }
