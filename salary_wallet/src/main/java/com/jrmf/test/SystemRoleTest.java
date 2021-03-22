package com.jrmf.test;

import com.jrmf.service.OrganizationTreeService;
import com.jrmf.service.ReceiptService;
import com.jrmf.taxsettlement.util.cache.UtilCacheManager;
import com.jrmf.utils.OrderNoUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * 用途：
 * 作者：郭桐宁
 * 时间：2019/1/13 21:34
 * Version:1.0
 *
 * @author guoto
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SystemRoleTest {
    private static final Logger logger = LoggerFactory.getLogger(SystemRoleTest.class);
    @Autowired
    private OrganizationTreeService organizationTreeService;
    @Autowired
    private ReceiptService receiptDao;
    @Autowired
    private ReceiptService receiptService;
    @Autowired
    private OrderNoUtil orderNoUtil;
    @Autowired
    private UtilCacheManager utilCacheManager;

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
        boolean group = organizationTreeService.addGroup("AAAAAAA");
        System.out.println(group);
    }

    @Test
    public void test2() {
        organizationTreeService.addParentGroup("AAAAAAA", "HqOtv1n5xoLMd56U3e0m");
        organizationTreeService.addParentGroup("Qzv33o3r20pVeI76S3Z7", "Q193G73r9cdp08T42T8W");
    }

    @Test
    public void test3() {
        organizationTreeService.addProxy("PROXYTEST");
    }

    @Test
    public void test4() {
        organizationTreeService.addParentProxy("PROXYTEST", "BBBBBBB");
    }

    @Test
    public void test() {
        organizationTreeService.addParentGroup("1inO66h8YHo9N092CkO4", "CCCCCCC");
    }

    @Test
    public void test5() {
        List<String> aNull = organizationTreeService.queryNodeCusotmKey(2, "NULL", 4575);
        for (String s : aNull) {
            System.out.println(s);
        }
    }

}
