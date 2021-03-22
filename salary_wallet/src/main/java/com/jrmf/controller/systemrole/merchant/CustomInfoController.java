package com.jrmf.controller.systemrole.merchant;

import com.alibaba.fastjson.JSONObject;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.WebChannel;
import com.jrmf.domain.WebCusotmInfo;
import com.jrmf.service.WebCustomInfoService;
import com.jrmf.utils.AddressUtil;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RequestMapping("/custom")
@Controller
public class CustomInfoController extends BaseController {
    private static Logger logger = LoggerFactory.getLogger(CustomInfoController.class);
    private final WebCustomInfoService webCustomInfoService;

    @Autowired
    public CustomInfoController(WebCustomInfoService webCusotmInfoService) {
        this.webCustomInfoService = webCusotmInfoService;
    }

    @ApiOperation(value = "免费预约顾问咨询接口", httpMethod = "POST")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "phoneNo", value = "手机号", dataType = "String", paramType = "query"),
        @ApiImplicitParam(name = "userName", value = "姓名", dataType = "String", paramType = "query"),
        @ApiImplicitParam(name = "companyName", value = "企业名称", dataType = "String", paramType = "query"),
        @ApiImplicitParam(name = "email", value = "邮箱", dataType = "String", paramType = "query")})
    @PostMapping("/add")
    @ResponseBody
    public Map<String, Object> addCustomInfo(HttpServletRequest request, String phoneNo,
        String userName, String companyName, String email) {
        WebCusotmInfo webCusotmInfo = new WebCusotmInfo();
        webCusotmInfo.setPhoneNo(phoneNo);
        webCusotmInfo.setUserName(userName);
        webCusotmInfo.setCompanyName(companyName);
        webCusotmInfo.setEmail(email);

        // 根据跨域请求 获取用户的IP地址
        String userIP = AddressUtil.getIp2(request);
        logger.info("web端用户信息采集 userIP={}", userIP);
        String addresses = AddressUtil.getAddresses2(userIP);
        webCusotmInfo.setUserIPAddress(userIP);
        // 根据用户的IP地址获取用户所在的物理地址
//        addresses = getUserAddress(addresses);
        logger.info("web端用户信息采集 addresses={}", addresses);
        webCusotmInfo.setUserAddress(addresses);
        // 根据用户所在渠道号获取用户信息来源渠道名称
        webCusotmInfo.setChannel("来自网页");
        // 将用户添加到数据库
        if (webCustomInfoService.addWebCustomInfo(webCusotmInfo)) {
            logger.info("用户添加成功 user:{}", webCusotmInfo);
            return returnSuccess();
        } else {
            logger.info("用户添加失败 user:{}", webCusotmInfo);
            return returnFail(RespCode.error107, RespCode.CONNECTION_ERROR);
        }
    }

    private String getUserAddress(String addresses) {
        JSONObject jsStr = JSONObject.parseObject(addresses);
        if (jsStr == null) {
            return "未知地区";
        }
        JSONObject data = JSONObject.parseObject(jsStr.getString("data"));
        String country = data.getString("country");
        String region = data.getString("region");
        String city = data.getString("city");
        String isp = data.getString("isp");
        addresses = country + region + city + isp;
        return addresses;
    }

    public static String getIp2(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (!StringUtil.isEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
            //多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = ip.indexOf(",");
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        }
        ip = request.getHeader("X-Real-IP");
        if (!StringUtil.isEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }

    @RequestMapping("/list")
    public Map<String, Object> listCustomInfo(@RequestParam(defaultValue = "1") Integer pageNO, @RequestParam(defaultValue = "10") Integer pageSize,
                                              String userName) {
        Map<String, Object> params = new HashMap<>();
        int total = webCustomInfoService.listCustomInfo(params).size();
        params.put("userName", userName);
        params.put("start", (pageNO - 1) * pageSize);
        params.put("limit", pageSize);
        List<WebCusotmInfo> customInfoList = webCustomInfoService.listCustomInfo(params);
        return returnSuccess(customInfoList, total);
    }

}
