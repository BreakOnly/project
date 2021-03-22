package com.jrmf.test;

import com.jrmf.domain.FAQ;
import com.jrmf.service.FAQService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FAQTest {

    private static final Logger logger = LoggerFactory.getLogger(FAQTest.class);
    @Autowired
    private FAQService faqService;

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
        logger.info("-----------------测试添加-----------------");
        FAQ faq = new FAQ();
        faq.setTitle("修改商户登陆账号");
        faq.setDescription("用户经常想修改账号或者我们运营需要修改账号什么的。");
        faq.setContext("问题解决方案模拟说明。\n问题解决方案模拟说明。\n问题解决方案模拟说明。\n问题解决方案模拟说明。\n问题解决方案模拟说明。\n问题解决方案模拟说明。");
        boolean b = faqService.addFAQ(faq);
        System.out.println(getReaultDetail(b));
    }

    @Test
    public void test2() {
        logger.info("-----------------测试列表-----------------");
        Map<String, Object> params = new HashMap<>();
        params.put("keyWords", "舒服舒服");
        params.put("start", 1);
        params.put("limit", 10);
        List<FAQ> faqs = faqService.listFAQ(params);
        for (FAQ faq : faqs) {
            System.out.println(faq);
        }
    }

    @Test
    public void test3() {
        logger.info("-----------------测试修改-----------------");
        Map<String, Object> params = new HashMap<>();
        params.put("keyWords", "我们运营需要");
        params.put("start", 1);
        params.put("limit", 10);
        List<FAQ> faqs = faqService.listFAQ(params);
        for (FAQ faq : faqs) {
            logger.info("before update faq={}", faq);
            faq.setTitle("修改后的title");
            faq.setDescription("修改后的description");
            faq.setContext("修改后的内容。\n修改后的内容。\n修改后的内容。\n修改后的内容。\n修改后的内容。\n");
            faqService.updateFAQ(faq);
        }
        List<FAQ> faqs1 = faqService.listFAQ(params);
        for (FAQ faq : faqs1) {
            logger.info("after update faq={}", faq);
        }
    }

    @Test
    public void test4() {
        logger.info("-----------------测试删除-----------------");
        Map<String, Object> params = new HashMap<>();
        params.put("start", 0);
        params.put("limit", 10);
        List<FAQ> faqs = faqService.listFAQ(params);
        for (FAQ faq : faqs) {
            boolean b = faqService.deleteFAQ(faq.getId());
            logger.info(getReaultDetail(b));
        }
    }

    public String getReaultDetail(boolean flag) {
        return flag ? "操作成功" : "操作失败";
    }
}
