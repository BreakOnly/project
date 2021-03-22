package com.jrmf.test;

import com.alibaba.fastjson.JSONObject;
import com.jrmf.controller.constant.InvoiceOrderStatus2;
import com.jrmf.domain.ChannelHistory;
import com.jrmf.domain.CommissionInvoice;
import com.jrmf.domain.SplitOrderConf;
import com.jrmf.domain.UsersAgreement;
import com.jrmf.domain.WebCusotmInfo;
import com.jrmf.oldsalarywallet.dao.UserCommissionDao;
import com.jrmf.persistence.ChannelHistoryDao;
import com.jrmf.persistence.UserCommission2Dao;
import com.jrmf.persistence.UsersAgreementDao;
import com.jrmf.service.SplitOrderConfService;
import com.jrmf.service.UserCommissionService;
import com.jrmf.service.WebCustomInfoService;
import com.jrmf.utils.AddressUtil;
import java.util.function.Consumer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author Nicholas-Ning
 * Description //TODO 测试一切
 * Date 14:48 2018/12/7
 * Param
 * return
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestTest {

    private static final Logger logger = LoggerFactory.getLogger(TestTest.class);

    @Autowired
    private WebCustomInfoService webCustomInfoService;
    @Autowired
    private ChannelHistoryDao channelHistoryDao;
    @Autowired
    private SplitOrderConfService splitOrderConfService;
    @Autowired
    private UsersAgreementDao usersAgreementDao;

    @Before
    public void init() {
        logger.info("-----------------开始测试-----------------");
    }

    @After
    public void after() {
        logger.info("-----------------测试结束-----------------");
    }

    @Test
    public void test1() {
        WebCusotmInfo webCusotmInfo = new WebCusotmInfo();
        webCusotmInfo.setChannel("121");
        webCusotmInfo.setCompanyName("111");
        webCusotmInfo.setEmail("111");
        webCusotmInfo.setPhoneNo("111");
        webCusotmInfo.setUserAddress("1211");
        webCusotmInfo.setUserIPAddress("111");
        webCusotmInfo.setUserName("111");
        boolean b = webCustomInfoService.addWebCustomInfo(webCusotmInfo);
        System.out.println(b ? "success" : "failure");
    }

    @Test
    public void test2() {
        AddressUtil addressUtils = new AddressUtil();
        try {
            String addresses = addressUtils.getAddresses("ip=122.49.20.247", "utf-8");
            System.out.println("地址：" + addresses);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test3() {
        ChannelHistory channelHistory = new ChannelHistory();
        channelHistory.setAccountName("6666666");
        int count = channelHistoryDao.addChannelHistory(channelHistory);
        System.out.println("影响的条数：" + count);
        System.out.println("返回的主键：" + channelHistory.getId());
    }

    @Test
    public void test4() {
        Map<String, Object> params = new HashMap<>();
        params.put("customKey", "9IwEH3rN8jV45nCV8i2w");
        params.put("customName", "");
        params.put("timeStart", "2019-03-04");
        params.put("timeEnd", "2019-03-07");
        List<SplitOrderConf> confByCustomKey = splitOrderConfService.getConfByCustomKey(params);
        for (SplitOrderConf splitOrderConf : confByCustomKey) {
            System.out.println(splitOrderConf);
        }
    }

    @Test
    public void test5() {
        Map<String, Object> params = new HashMap<>();
        params.put("customKeys", "j09I92fXyzLeK4Arci8Y,I1f17lMz33D0ws265137,RX747U355tWC8AhIFi5d");
        params.put("signStatus", 5);
        List<UsersAgreement> agreements = usersAgreementDao.getUsersAgreementsByParams(params);
        System.out.println(agreements.size());


    }

    @Autowired
    UserCommissionService userCommissionService;
    @Autowired
    UserCommission2Dao userCommission2Dao;

    @Autowired
    UserCommissionDao userCommissionDao;
    @Test
    public void testInvoiceCustomInfos(){
        CommissionInvoice commissionInvoice = new CommissionInvoice();
        commissionInvoice.setStartMonth("2020-03");
        commissionInvoice.setEndMonth("2020-06");
        List<Map<String, Object>> invoiceCustomInfos = userCommissionService.getInvoiceCustomInfos(commissionInvoice);
        if (invoiceCustomInfos != null && invoiceCustomInfos.size() > 0){
            invoiceCustomInfos.forEach(new Consumer<Map<String, Object>>() {
                @Override
                public void accept(Map<String, Object> stringObjectMap) {
                    String month = (String) stringObjectMap.get("month");
                    String originalId = (String) stringObjectMap.get("originalId");
                    String companyId = (String) stringObjectMap.get("companyId");
                    String invoiceStatus2 = (String) stringObjectMap.get("invoiceStatus2");
                    logger.info("month:"+month+",originalId:"+originalId+",companyId:"+companyId+",invoiceStatus2:"+invoiceStatus2);
                }
            });
        }
    }

    @Test
    public void testUpdateCommission(){
        Map<String, Object> updateParams = new HashMap<>();
        updateParams.put("invoiceSerialNo", "P2020072400081591542");
        updateParams.put("invoiceStatus2", InvoiceOrderStatus2.FAIL_TYPE.getCode());
        updateParams.put("individualTax", "0");
        updateParams.put("taxRate", "0");
        updateParams.clear();
        updateParams.put("invoiceSerialNo2", "P2020072400081591542");
        updateParams.put("individualBackTax", "1");
        userCommission2Dao.updateCommissionInvoiceInfoByInvoiceSerialNo(updateParams);
    }

    @Test
    public void queryUserCommission(){
        Map<String, Object> params = new HashMap<>();
        params.put("companyId","28617");
        params.put("customName","魔方正式下发（测试环境专用");
        List<HashMap<String, Object>> commissions = userCommissionDao
            .getCompanyCommissions(params);

        System.out.println(JSONObject.toJSON(commissions).toString());
    }
}
















