package com.jrmf.controller.common;

import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.JunkInfoType;
import com.jrmf.domain.CustomCompanyRateConf;
import com.jrmf.domain.JunkInfo;
import com.jrmf.domain.UserCommission;
import com.jrmf.service.CustomCompanyRateConfService;
import com.jrmf.service.JunkInfoService;
import com.jrmf.service.UserCommissionService;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/util")
public class UtilsController extends BaseController {

    private Logger logger = LoggerFactory.getLogger(UtilsController.class);

    @Autowired
    private UserCommissionService commissionService;
    @Autowired
    private CustomCompanyRateConfService customCompanyRateConfService;
    @Autowired
    private JunkInfoService junkInfoService;

    @PostMapping(value = "/updateRateInterval")
    public Map<String, Object> updateRateInterval(String month, String customKey, String companyId) {

        if (StringUtil.isEmpty(month)) {
            return returnFail(RespCode.error101, "请指定初始化档位信息的年月份");
        }

        Map<String, Object> params = new HashMap<>(20);
        params.put("originalId", customKey);
        params.put("companyId", companyId);
        params.put("tradeStartTime", month + "-01");
        params.put("tradeEndTime", month + "-31");

        logger.info("--------批量更新下发记录档位信息接口访问,{}", params);

        try {
            List<UserCommission> userList = commissionService.getSuccessUserCommission(params);

            int successCount = 0;
            int errorCount = 0;
            int noUpdateCount = 0;
            int totalCount = 0;
            if (userList != null && userList.size() > 0) {
                totalCount = userList.size();
                logger.info("--------批量更新下发记录档位信息开始,month:{},customKey:{},companyId:{},totalCount{}-----------", month, customKey, companyId, totalCount);

                for (UserCommission commission : userList) {

                    if (StringUtil.isEmpty(commission.getRateInterval()) || StringUtil.isEmpty(commission.getCalculationRates()) || ArithmeticUtil.compareTod(commission.getCalculationRates(), "0") == 0) {
                        params.put("originalId", commission.getOriginalId());
                        params.put("companyId", commission.getCompanyId());
                        params.put("commissionId", commission.getId());
                        params.put("certId", commission.getCertId());
                        String monthTotalAmount = commissionService.getSuccessAmount(params);
                        params.put("sumAmount", monthTotalAmount);
                        CustomCompanyRateConf rateConf = customCompanyRateConfService.getCustomCompanyRateConf(params);

                        if (rateConf != null) {
                            commission.setCalculationRates(rateConf.getCustomRate());
                            commission.setRateInterval(rateConf.getAmountStart() + rateConf.getOperator() + rateConf.getAmountEnd());

                            commissionService.updateUserCommissionRate(commission);
                            successCount++;
                            logger.error("下发记录:{},当月累计下发{},更新档位信息或费率成功,更新后档位{}费率{}", commission.getOrderNo(), monthTotalAmount, commission.getRateInterval(), commission.getCalculationRates());
                        } else {
                            JunkInfo junkInfo = new JunkInfo();
                            junkInfo.setType(JunkInfoType.RATE.getCode());
                            junkInfo.setParameter1(commission.getOrderNo());
                            junkInfoService.insert(junkInfo);

                            errorCount++;
                            logger.error("下发记录:{},当月累计下发{},初始化档位信息或费率时未获取到对应档位", commission.getOrderNo(), monthTotalAmount);
                        }
                    } else {
                        noUpdateCount++;
                    }

                }
            }

            HashMap<String, Object> result = new HashMap<>(5);
            result.put("totalCount", totalCount);
            result.put("noUpdateCount", noUpdateCount);
            result.put("successCount", successCount);
            result.put("errorCount", errorCount);
            return returnSuccess(result);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
        }
    }


}
