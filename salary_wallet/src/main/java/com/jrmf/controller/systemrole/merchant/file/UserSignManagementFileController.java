package com.jrmf.controller.systemrole.merchant.file;

import com.google.common.base.Joiner;
import com.jrmf.common.CommonString;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.CertType;
import com.jrmf.controller.constant.CustomType;
import com.jrmf.controller.constant.QueryType;
import com.jrmf.controller.constant.UsersAgreementSignType;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.OrganizationTreeService;
import com.jrmf.service.UsersAgreementService;
import com.jrmf.utils.ExcelFileGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用途：
 * 作者：郭桐宁
 * 时间：2018/11/20 16:16
 * Version:1.0
 *
 * @author guoto
 */
@Controller
@RequestMapping("/merchant/user/management/file")
public class UserSignManagementFileController extends BaseController {
    private static Logger logger = LoggerFactory.getLogger(UserSignManagementFileController.class);
    private final UsersAgreementService usersAgreementService;
    private final ChannelCustomService channelCustomService;
    private final OrganizationTreeService organizationTreeService;

    @Autowired
    public UserSignManagementFileController(UsersAgreementService usersAgreementService, ChannelCustomService channelCustomService, OrganizationTreeService organizationTreeService) {
        this.usersAgreementService = usersAgreementService;
        this.channelCustomService = channelCustomService;
        this.organizationTreeService = organizationTreeService;
    }

    /**
     * Author Nicholas-Ning
     * Description //TODO 用户签约协议查看导出
     * Date 15:16 2018/11/23
     * Param [response, request, userName, certId, signDateStart, signDateEnd, userNo, companyId, signStatus, customName]
     * return void
     **/
    @RequestMapping(value = "/sign/list/user/agreements")
    public void users(HttpServletResponse response,HttpServletRequest request,
                      @RequestParam(required = false) String userName, @RequestParam(required = false) String certId,
                      @RequestParam(required = false) String signDateStart, @RequestParam(required = false) String signDateEnd,
                      @RequestParam(required = false) String userNo, @RequestParam(required = false) String companyId,
                      @RequestParam(required = false) String remark,
                      @RequestParam(required = false) String signStatus,@RequestParam(required = false) String customName,
                      @RequestParam(required = false) String userType) {
        Map<String, Object> params = new HashMap<>(15);
        String customkey = (String) request.getSession().getAttribute(CommonString.CUSTOMKEY);
        if(!CommonString.ROOT.equals(customkey)){
            params.put("originalId", customkey);
        }
        ChannelCustom channelCustom = channelCustomService.getCustomByCustomkey(customkey);
        if(channelCustom.getCustomType() == CustomType.GROUP.getCode()){
            int id = organizationTreeService.queryNodeIdByCustomKey(channelCustom.getCustomkey());
            List<String> stringList = organizationTreeService.queryNodeCusotmKey(CustomType.GROUP.getCode(), QueryType.QUERY_CURRENT_AND_CHILDREN, id);
            String originalIds = Joiner.on(",").join(stringList);
            params.put("originalId", originalIds);
        }
        if(channelCustom.getCustomType() == CustomType.COMPANY.getCode()){
            params.put("originalId", null);
            companyId = channelCustom.getCustomkey();
        }
        params.put("userName", userName);
        params.put("customName", customName);
        params.put("certId", certId);
        params.put("signDateStart", signDateStart);
        params.put("signDateEnd", signDateEnd);
        params.put("userNo", userNo);
        params.put("companyId", companyId);
        params.put("signStatus", signStatus);
        params.put("remark", remark);
        params.put("userType", userType);
        logger.info("用户签约协议查看导出 params{}", params);
        List<Map<String, Object>> agreements = usersAgreementService.getAgreementsByParams(params);
        List<Map<String, Object>> data = new ArrayList<>();
        String[] colunmName = new String[]{"签约协议号", "商户用户编号", "用户名称", "证件类型", "证件号", "签约状态","手机号", "签名状态描述", "商户名称","服务公司名称",
                "签约模板名称", "签约模板描述", "签约时间","备注"};
        String filename = "用户签约协议表";
        for (Map<String, Object> dataResult : agreements) {
            Map<String, Object> dataMap = new HashMap<>(20);
            dataMap.put("1", dataResult.get("agreementNo"));
            dataMap.put("2", dataResult.get("userNo"));
            dataMap.put("3", dataResult.get("userName"));
            int documentType = (Integer)dataResult.get("documentType");
            String desc = CertType.codeOf(documentType).getDesc();
            dataMap.put("4", desc);
            dataMap.put("5", dataResult.get("certId"));
            int status = (Integer)dataResult.get("signStatus");
            UsersAgreementSignType agreementSignType = UsersAgreementSignType.codeOf(status);
            dataMap.put("6", agreementSignType.getDesc());
            dataMap.put("7", dataResult.get("mobileNo"));
            dataMap.put("8", dataResult.get("signStatusDes"));
            dataMap.put("9", dataResult.get("companyName"));
            dataMap.put("10", dataResult.get("payCompanyName"));
            dataMap.put("11", dataResult.get("agreementName"));
            dataMap.put("12", dataResult.get("thirdTemplateDes"));
            dataMap.put("13", dataResult.get("signTime"));
            dataMap.put("14", dataResult.get("remark"));
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }
}
