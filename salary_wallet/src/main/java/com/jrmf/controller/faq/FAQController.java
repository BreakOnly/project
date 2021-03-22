package com.jrmf.controller.faq;

import com.jrmf.controller.BaseController;
import com.jrmf.domain.FAQ;
import com.jrmf.service.FAQService;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/faq")
public class FAQController extends BaseController {
    final FAQService faqService;

    @Autowired
    public FAQController(FAQService faqService) {
        this.faqService = faqService;
    }

    @RequestMapping("/add")
    public Map<String, Object> addFAQ(FAQ faq) {
        boolean isSuccess;
        if (faq.getId() == null || faq.getId().equals(0)) {
            //add
            if (StringUtil.isEmpty(faq.getTitle())) {
                return returnFail(RespCode.error101, RespCode.TITLENOTNULL);
            }
            isSuccess = faqService.addFAQ(faq);
        } else {
            isSuccess = faqService.updateFAQ(faq);
        }
        return isSuccess ? returnSuccess(null) : returnFail(RespCode.error101, RespCode.OPERATING_FAILED);
    }

    @RequestMapping("/delete")
    public Map<String, Object> deleteFAQ(Integer id) {
        return faqService.deleteFAQ(id) ? returnSuccess(null) : returnFail(RespCode.error101, RespCode.OPERATING_FAILED);
    }

    @RequestMapping("/search")
    public Map<String, Object> searchFAQ(String keyWords, @RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> params = new HashMap<>();
        params.put("keyWords", keyWords);
        int total = faqService.listFAQ(params).size();
        params.put("start", (pageNo - 1) * pageSize);
        params.put("limit", pageSize);
        List<FAQ> faqList = faqService.listFAQ(params);
        return returnSuccess(faqList, total);
    }

}
