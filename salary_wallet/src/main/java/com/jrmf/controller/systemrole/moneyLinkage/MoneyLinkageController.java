package com.jrmf.controller.systemrole.moneyLinkage;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jrmf.bankapi.LinkageTransHistoryPage;
import com.jrmf.common.CommonString;
import com.jrmf.controller.constant.CustomType;
import com.jrmf.controller.systemrole.merchant.payment.PaymentProxy;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.LinkageBaseConfig;
import com.jrmf.domain.LinkageQueryTranHistory;
import com.jrmf.domain.PaymentConfig;
import com.jrmf.payment.PaymentFactory;
import com.jrmf.payment.entity.Payment;
import com.jrmf.payment.util.PaymentReturn;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.LinkageCustomConfigService;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @Title: MoneyGangedController
 * @Description: 资金联动
 * @create 2020/3/9 14:25
 */
@RestController
@RequestMapping(value = "/moneyLinkage")
public class MoneyLinkageController {

    private static final Logger logger = LoggerFactory.getLogger(MoneyLinkageController.class);

    @Autowired
    private LinkageCustomConfigService linkageCustomConfigService;

    @Autowired
    private ChannelCustomService channelCustomService;

    @RequestMapping(value = "/accountQuery")
    public Map<String, Object> accountQuery(String companyName, String corporationAccountName,
                                            String startTime, String endTime, Integer pathType,
                                            String corporationAccount, String bankName,
                                            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                            @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                            HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>(7);
        result.put(RespCode.RESP_STAT, RespCode.success);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));

        Map<String, String> paramMap = new HashMap<>(11);
        paramMap.put("companyName", companyName);
        paramMap.put("corporationAccountName", corporationAccountName);
        paramMap.put("startTime", startTime);
        paramMap.put("endTime", endTime);
        paramMap.put("linkageType", pathType+"");
        paramMap.put("corporationAccount", corporationAccount);
        paramMap.put("bankName", bankName);

        //校验是否有权限
        boolean checkFlag = true;
        //获取登陆信息
        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
        Integer []allowCustomType = new Integer[]{CustomType.GROUP.getCode(),CustomType.CUSTOM.getCode(),6};
        checkFlag = channelCustomService.getCustomKeysByType(paramMap, allowCustomType, customLogin);
        if(checkFlag){
            PageHelper.startPage(pageNo, pageSize);
            List<LinkageBaseConfig> list = linkageCustomConfigService.getMoneyLinkageByParam(paramMap);
            PageInfo page = new PageInfo(list);
            result.put("total", page.getTotal());
            result.put("list", page.getList());
            return result;
        }
        result.put(RespCode.RESP_STAT, RespCode.DO_NOT_HAVE_APPROVAL_RIGHT);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.DO_NOT_HAVE_APPROVAL_RIGHT));
        return result;
    }

    /**
     * 余额查询
     * @return
     */
    @RequestMapping(value = "/balanceQuery")
    public Map<String, Object> balanceQuery(String customkey) {
        Map<String, Object> result = new HashMap<>(5);
        result.put(RespCode.RESP_STAT, RespCode.success);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));

        try {
            Optional.ofNullable(customkey)
                    .filter(s -> !StringUtil.isEmpty(customkey))
                    .orElseThrow(() -> new NullPointerException("参数异常"));

            LinkageBaseConfig baseConfig = linkageCustomConfigService.getLinkageConfigByCustomKey(customkey);

            Optional.ofNullable(baseConfig)
                    .map(LinkageBaseConfig::getPathNo)
                    .filter(s -> !StringUtil.isEmpty(s))
                    .orElseThrow(() -> new NullPointerException("账户不存在"));

            String balance = String.valueOf(this.paymentConfig(baseConfig).queryBalanceResult(null).getAttachment());
            result.put("balance", balance);
        } catch (Exception e) {
            logger.error("-----查询余额异常-----{}", e);
            result.put(RespCode.RESP_STAT, RespCode.error101);
            result.put(RespCode.RESP_MSG, "刷新余额失败，请联系管理员！");
            return result;
        }
        return result;
    }

    /**
     * 交易明细
     * @return
     */
    @RequestMapping(value = "/transactionDetail")
    public Map<String, Object> transactionDetail(String beginDate, String endDate, Integer pageNo, Integer pageSize, String id) {
        Map<String, Object> result = new HashMap<>(5);
        result.put(RespCode.RESP_STAT, RespCode.success);

        LinkageQueryTranHistory linkageQueryTranHistory = new LinkageQueryTranHistory();
        linkageQueryTranHistory.setStartDate(beginDate);
        linkageQueryTranHistory.setEndDate(endDate);
        linkageQueryTranHistory.setPageNo(pageNo);
        linkageQueryTranHistory.setPageSize(pageSize);
        linkageQueryTranHistory.setOrderMode("001");
        // 默认主账户
        linkageQueryTranHistory.setOpFlag("1");
        try {
            Optional.ofNullable(id)
                    .filter(s -> !StringUtil.isEmpty(s))
                    .orElseThrow(() -> new Exception("参数异常"));

            LinkageBaseConfig baseConfig = linkageCustomConfigService.getConfigById(id);
            Optional.ofNullable(baseConfig)
                    .map(LinkageBaseConfig::getPathNo)
                    .filter(s -> !StringUtil.isEmpty(s))
                    .orElseThrow(() -> new Exception("账户不存在"));

            Map<String, Object> result2 = new HashMap<>(5);
            PaymentReturn paymentReturn = this.paymentConfig(baseConfig).queryTransHistoryPage(linkageQueryTranHistory);
            if (!StringUtil.isEmpty(paymentReturn.getFailMessage())) {
                result2.put("status", "2");
                result2.put("list", new ArrayList<>());
                result2.put("total", 0);
                result2.put(RespCode.RESP_MSG, paymentReturn.getFailMessage().substring(0, 30));
                result.put("result",result2);
                return result;
            }
            LinkageTransHistoryPage attachment = (LinkageTransHistoryPage) paymentReturn.getAttachment();
            result2.put("status", "1");
            result2.put("hasNextPage", attachment.getTransHistoryRecords().size()==0?false:attachment.isHasNextPage());
            result2.put("list", attachment.getTransHistoryRecords());
            result2.put("total", attachment.getTransHistoryRecords().size());
            result.put("result",result2);
        } catch (Exception e) {
            logger.error("-----查看明细异常-----{}", e);
            result.put(RespCode.RESP_STAT, RespCode.error101);
            result.put(RespCode.RESP_MSG, "查看明细失败，请联系管理员！");
            return result;
        }
        return result;
    }

    /**
     * 调用支付通道工厂模式
     * @param baseConfig
     * @return
     */
    public Payment paymentConfig(LinkageBaseConfig baseConfig){
        PaymentConfig paymentConfig = new PaymentConfig(baseConfig);
        Payment<?, ?, ?> payment = PaymentFactory.paymentEntity(paymentConfig);
        PaymentProxy paymentProxy = new PaymentProxy(payment, CommonString.LIFETIME, null);
        Payment proxy = paymentProxy.getProxy();
        return proxy;
    }
}
