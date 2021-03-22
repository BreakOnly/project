package com.jrmf.controller;

import com.jrmf.domain.AgreementTemplate;
import com.jrmf.domain.UsersAgreement;
import com.jrmf.persistence.AgreementTemplateDao;
import com.jrmf.persistence.UsersAgreementDao;
import com.jrmf.service.UserSerivce;
import com.jrmf.service.UsersAgreementService;
import com.jrmf.taxsettlement.api.APIDockingException;
import com.jrmf.taxsettlement.api.service.CommonRetCodes;
import com.jrmf.utils.StringUtil;
import com.jrmf.utils.threadpool.ThreadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 种路路
 * @create 2019-08-07 10:23
 * @desc
 **/
@Controller
public class TestDownload {
    @Autowired
    private UsersAgreementDao usersAgreementDao;
    @Autowired
    private AgreementTemplateDao agreementTemplateDao;
    @Autowired
    private UsersAgreementService usersAgreementService;

    @RequestMapping("aa")
    @ResponseBody
    public Map<String, Object> downLoad() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("customKeys", "j09I92fXyzLeK4Arci8Y,I1f17lMz33D0ws265137,RX747U355tWC8AhIFi5d");
        params.put("signStatus", 5);
        List<UsersAgreement> agreements = usersAgreementDao.getUsersAgreementsByParamsForCopy(params);
        for (UsersAgreement agreement : agreements) {
            params.remove("signStatus");
            params.put("certId", agreement.getCertId());
            List<UsersAgreement> agreementList = usersAgreementDao.getUsersAgreementsByParams(params);
            for (UsersAgreement usersAgreement : agreementList) {
                ThreadUtil.pdfThreadPool.execute(new Thread(() -> {

                    if (usersAgreement.getSignStatus() == 5 && !StringUtil.isEmpty(usersAgreement.getImageURLA())) {
                        return;
                    }
                    String templateId = usersAgreement.getAgreementTemplateId();
                    AgreementTemplate template = agreementTemplateDao.getAgreementTemplateById(templateId);
                    if (template == null) {
                        return;
                    }
                    try {
                        usersAgreementService.copyAgreement(agreement, template);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }));
            }
        }
        System.out.println(agreements.size());
        return null;

    }

}
