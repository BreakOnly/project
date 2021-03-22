package com.jrmf.controller.receipt;

import com.jrmf.controller.BaseController;
import com.jrmf.service.UmfCommissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author chonglulu
 */
@RestController
@RequestMapping("/umf")
public class UMFController extends BaseController {

    private final UmfCommissionService umfCommissionService;

    @Autowired
    public UMFController(UmfCommissionService umfCommissionService) {
        this.umfCommissionService = umfCommissionService;
    }

    /**
     * 重新下载文件
     * @param time 下载日期
     * @return 返回成功
     */
    @RequestMapping(value = "/redownload")
    public Map<String, Object> receiptBatchCreate(@RequestParam("merId") String merId,@RequestParam("time") String time) {
        umfCommissionService.downloadUserCommission(merId,time);
        return returnSuccess();
    }


}
