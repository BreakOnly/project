package com.jrmf.controller.systemrole;

import com.jrmf.common.CommonString;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.BestSignConfig;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.OemConfig;
import com.jrmf.service.OemConfigService;
import com.jrmf.utils.FtpTool;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author 种路路
 * @create 2019-03-12 14:52
 * @desc oem商户
 **/
@Controller
public class OemController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(OemController.class);

    private final OemConfigService oemConfigService;

    private final BestSignConfig bestSignConfig;

    @Value("${ftppath}")
    private String domainName;

    @Autowired
    public OemController(OemConfigService oemConfigService, BestSignConfig bestSignConfig) {
        this.oemConfigService = oemConfigService;
        this.bestSignConfig = bestSignConfig;
    }


    /**
     * 获取OEM配置
     *
     * @param request      获取request
     * @param portalDomain 域名
     * @return oem配置信息
     */
    @RequestMapping("/oem")
    @ResponseBody
    public Map<String, Object> oemInfo(HttpServletRequest request,
                                       @RequestParam(name = "domain", required = false) String portalDomain) {
        if (StringUtil.isEmpty(portalDomain)) {
            portalDomain = request.getServerName();
        }
        Map<String, Object> map = new HashMap<>(2);
        map.put("portalDomain", portalDomain);
        OemConfig oemConfig = oemConfigService.getOemByParam(map);

        map.clear();
        map.put("oemConfig", oemConfig);
        return returnSuccess(map);
    }

    /**
     * 清除缓存
     *
     * @return 成功
     */
    @RequestMapping("/oem/cache")
    @ResponseBody
    public Map<String, Object> oemCache() {
        oemConfigService.reloadCache();
        return returnSuccess(null);
    }


    /**
     * 查询OEM配置
     * @param pageNo
     * @param pageSize
     * @param oemName
     * @param portalDomain
     * @param clientDomain
     * @return
     */
    @RequestMapping(value = "/oem/getOemConfig")
    @ResponseBody
    public Map<String, Object> getOemConfig(@RequestParam(value = "pageNo", required = false) String pageNo,
                                            @RequestParam(value = "pageSize", required = false, defaultValue = "10") String pageSize,
                                            @RequestParam(value = "oemName", required = false) String oemName,
                                            @RequestParam(value = "portalDomain", required = false) String portalDomain,
                                            @RequestParam(value = "clientDomain", required = false) String clientDomain) {
        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<String, Object>(7);
        Map<String, Object> paramMap = new HashMap<>(5);
        result.put(RespCode.RESP_STAT, respstat);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
        try {
            paramMap.put("oemName", oemName);
            paramMap.put("portalDomain", portalDomain);
            paramMap.put("clientDomain", clientDomain);
            int total = oemConfigService.getOemConfig(paramMap).size();
            if (!StringUtil.isEmpty(pageNo)) {
                paramMap.put("start", getFirst(pageNo, pageSize));
                paramMap.put("limit", Integer.parseInt(pageSize));
            }
            List<OemConfig> list = oemConfigService.getOemConfig(paramMap);
            result.put("list", list);
            result.put("total", total);
            logger.info("查询OEM配置返回结果：" + result);
        } catch (Exception e) {
            logger.error("查询OEM配置失败{}", e);
            return returnFail(RespCode.error107, "查询OEM配置失败，请联系管理员！");
        }
        return result;
    }

    /**
     * 新增/修改OEM信息
     * @param oemConfig
     * @param request
     * @param portalLogoFile
     * @param welcomePictureFile
     * @param clientLogoFile
     * @return
     */
    @RequestMapping("/oem/configOem")
    @ResponseBody
    public Map<String, Object> configOem(OemConfig oemConfig, HttpServletRequest request,
                                         @RequestParam(value = "portalLogoFile", required = false) MultipartFile portalLogoFile,
                                         @RequestParam(value = "welcomePictureFile", required = false) MultipartFile welcomePictureFile,
                                         @RequestParam(value = "clientLogoFile", required = false) MultipartFile clientLogoFile) {

        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute("customLogin");
        if (!CommonString.ROOT.equals(loginUser.getCustomkey()) && !CommonString.ROOT.equals(loginUser.getMasterCustom())) {
            return returnFail(RespCode.error101, "权限不足！");
        }

        String protalUrl = "/oem/logo/";
        String welcomeUrl = "/oem/welcome/";
        String clientUrl = "/oem/client/banner/";
        String welcomePictureFileType = "";
        String portalLogoFileType = "";
        String clientLogoFileType = "";

        Map<String, Object> map = new HashMap<>(2);
        String[] strs = domainName.split("//");
        map.put("portalDomain", strs[1]);
        OemConfig config = oemConfigService.getOemByParam(map);
        String fileName = UUID.randomUUID().toString().replace("-", "");

        if (StringUtil.isEmpty(oemConfig.getOemName())) {
            oemConfig.setOemName(config.getOemName());
            oemConfig.setPortalTitle(config.getOemName());
        } else {
            oemConfig.setPortalTitle(oemConfig.getOemName());
        }

        if (!StringUtil.isEmpty(oemConfig.getId() + "")) {
            // 修改
            OemConfig oc = oemConfigService.getOemConfigById(String.valueOf(oemConfig.getId()));
            if ((!StringUtil.isEmpty(oc.getPortalLogo()) && portalLogoFile != null) || (StringUtil.isEmpty(oc.getPortalLogo()) && portalLogoFile != null)) {
                String portalLogoFilename = portalLogoFile.getOriginalFilename();
                portalLogoFileType = portalLogoFilename.substring(portalLogoFilename.lastIndexOf(".") + 1);
                logger.info("portalLogoFileType文件原名:{}", portalLogoFilename);
                logger.info("portalLogoFileType文件类型:{}", portalLogoFileType);
                if (!"png".equals(portalLogoFileType)) {
                    return returnFail(RespCode.error116, "B端首页LOGO图片类型错误！");
                }
                oemConfig.setPortalLogo(domainName + protalUrl + fileName + "." + portalLogoFileType);
            } else if (StringUtil.isEmpty(oc.getPortalLogo()) && portalLogoFile == null) {
                oemConfig.setPortalLogo(config.getPortalLogo());
            }

            if ((!StringUtil.isEmpty(oc.getWelcomePicture()) && welcomePictureFile != null) || (StringUtil.isEmpty(oc.getWelcomePicture()) && welcomePictureFile != null)) {
                String welcomePictureFilename = welcomePictureFile.getOriginalFilename();
                welcomePictureFileType = welcomePictureFilename.substring(welcomePictureFilename.lastIndexOf(".") + 1);
                logger.info("welcomePictureFileType文件原名:{}", welcomePictureFilename);
                logger.info("welcomePictureFileType文件类型:{}", welcomePictureFileType);
                if (!"jpg".equals(welcomePictureFileType)) {
                    return returnFail(RespCode.error116, "B端欢迎页图片类型错误！");
                }
                oemConfig.setWelcomePicture(domainName + welcomeUrl + fileName + "." + welcomePictureFileType);
            } else if (StringUtil.isEmpty(oc.getWelcomePicture()) && welcomePictureFile == null) {
                oemConfig.setWelcomePicture(config.getWelcomePicture());
            }

            if ((!StringUtil.isEmpty(oc.getClientLogo()) && clientLogoFile != null) || (StringUtil.isEmpty(oc.getClientLogo()) && clientLogoFile != null)) {
                String clientLogoFilename = clientLogoFile.getOriginalFilename();
                clientLogoFileType = clientLogoFilename.substring(clientLogoFilename.lastIndexOf(".") + 1);
                logger.info("clientLogoFileType文件原名:{}", clientLogoFilename);
                logger.info("clientLogoFileType文件类型:{}", clientLogoFileType);
                if (!"jpg".equals(clientLogoFileType)) {
                    return returnFail(RespCode.error116, "C端首页LOGO图片类型错误！");
                }
                oemConfig.setClientLogo(domainName + clientUrl + fileName + "." + clientLogoFileType);
            } else if (StringUtil.isEmpty(oc.getClientLogo()) && clientLogoFile == null) {
                oemConfig.setClientLogo(config.getClientLogo());
            }

        } else {
            // 新增
            if (portalLogoFile != null) {
                String portalLogoFilename = portalLogoFile.getOriginalFilename();
                portalLogoFileType = portalLogoFilename.substring(portalLogoFilename.lastIndexOf(".") + 1);
                logger.info("portalLogoFileType文件原名{}", portalLogoFilename);
                logger.info("portalLogoFileType文件类型:{}", portalLogoFileType);
                if (!"png".equals(portalLogoFileType)) {
                    return returnFail(RespCode.error116, "B端首页LOGO图片类型错误！");
                }
                oemConfig.setPortalLogo(domainName + protalUrl + fileName + "." + portalLogoFileType);
            } else {
                oemConfig.setPortalLogo(config.getPortalLogo());
            }

            if (welcomePictureFile != null) {
                String welcomePictureFilename = welcomePictureFile.getOriginalFilename();
                welcomePictureFileType = welcomePictureFilename.substring(welcomePictureFilename.lastIndexOf(".") + 1);
                logger.info("portalLogoFileType文件原名{}", welcomePictureFilename);
                logger.info("portalLogoFileType文件类型:{}", welcomePictureFileType);
                if (!"jpg".equals(welcomePictureFileType)) {
                    return returnFail(RespCode.error116, "B端欢迎页图片类型错误！");
                }
                oemConfig.setWelcomePicture(domainName + welcomeUrl + fileName + "." + welcomePictureFileType);
            } else {
                oemConfig.setWelcomePicture(config.getWelcomePicture());
            }

            if (clientLogoFile != null) {
                String clientLogoFilename = clientLogoFile.getOriginalFilename();
                clientLogoFileType = clientLogoFilename.substring(clientLogoFilename.lastIndexOf(".") + 1);
                logger.info("portalLogoFileType文件原名{}", clientLogoFilename);
                logger.info("portalLogoFileType文件类型:{}", clientLogoFileType);
                if (!"jpg".equals(clientLogoFileType)) {
                    return returnFail(RespCode.error116, "C端首页LOGO图片类型错误！");
                }
                oemConfig.setClientLogo(domainName + clientUrl + fileName + "." + clientLogoFileType);
            } else {
                oemConfig.setClientLogo(config.getClientLogo());
            }
        }

        if (StringUtil.isEmpty(oemConfig.getSmsSignature())) {
            oemConfig.setSmsSignature(config.getSmsSignature());
        }

        if (StringUtil.isEmpty(oemConfig.getServiceHotline())) {
            oemConfig.setServiceHotline(config.getServiceHotline());
        }

        if (StringUtil.isEmpty(oemConfig.getSmsStatus() + "")) {
            oemConfig.setSmsStatus(config.getSmsStatus());
        }


        if (portalLogoFile != null) {
            try {
                byte[] bytes = portalLogoFile.getBytes();
                InputStream fileInputStream = new ByteArrayInputStream(bytes);
                FtpTool.uploadFile(bestSignConfig.getFtpURL(), 21, protalUrl, fileName + "." + portalLogoFileType, fileInputStream, bestSignConfig.getUsername(), bestSignConfig.getPassword());
            } catch (IOException e) {
                logger.error("上传B端首页LOGO图片错误{}", e);
                return returnFail(RespCode.error101, "上传B端首页LOGO图片失败，请联系管理员！");
            }
        }

        if (welcomePictureFile != null) {
            try {
                byte[] bytes = welcomePictureFile.getBytes();
                InputStream fileInputStream = new ByteArrayInputStream(bytes);
                FtpTool.uploadFile(bestSignConfig.getFtpURL(), 21, welcomeUrl, fileName + "." + welcomePictureFileType, fileInputStream, bestSignConfig.getUsername(), bestSignConfig.getPassword());
            } catch (IOException e) {
                logger.error("上传B端欢迎页图片错误{}", e);
                return returnFail(RespCode.error101, "上传B端欢迎页图片失败，请联系管理员！");
            }
        }

        if (clientLogoFile != null) {
            try {
                byte[] bytes = clientLogoFile.getBytes();
                InputStream fileInputStream = new ByteArrayInputStream(bytes);
                FtpTool.uploadFile(bestSignConfig.getFtpURL(), 21, clientUrl, fileName + "." + clientLogoFileType, fileInputStream, bestSignConfig.getUsername(), bestSignConfig.getPassword());
            } catch (IOException e) {
                logger.error("上传C端首页LOGO图片错误{}", e);
                return returnFail(RespCode.error101, "上传C端首页LOGO图片失败，请联系管理员！");
            }
        }

        try {
            if (!StringUtil.isEmpty(oemConfig.getId() + "")) {
                oemConfigService.updateOemConfig(oemConfig);
            } else {
                oemConfigService.insertOemConfig(oemConfig);
            }
            oemConfigService.reloadCache();
        } catch (Exception e) {
            logger.error("配置OEM失败{}", e);
            return returnFail(RespCode.error101, "配置OEM失败，请联系管理员！");
        }

        return returnSuccess();
    }

    /**
     * 删除OEM信息
     * @param id
     * @param request
     * @param portalLogo
     * @param welcomePicture
     * @param clientLogo
     * @return
     */
    @RequestMapping("/oem/deleteOem")
    @ResponseBody
    public Map<String, Object> deleteOem(@RequestParam(value = "id") String id, HttpServletRequest request,
                                         @RequestParam(value = "portalLogo", required = false) String portalLogo,
                                         @RequestParam(value = "welcomePicture", required = false) String welcomePicture,
                                         @RequestParam(value = "clientLogo", required = false) String clientLogo) {

        ChannelCustom loginUser = (ChannelCustom) request.getSession().getAttribute("customLogin");
        if (!CommonString.ROOT.equals(loginUser.getCustomkey()) && !CommonString.ROOT.equals(loginUser.getMasterCustom())) {
            return returnFail(RespCode.error101, "权限不足！");
        }

        OemConfig oemConfig = oemConfigService.getOemConfigById(id);
        if (oemConfig == null) {
            return returnFail(RespCode.error101, "该记录不存在，请刷新页面！");
        }

        OemConfig oc = new OemConfig();
        oc.setId(Integer.parseInt(id));
        if (!StringUtil.isEmpty(portalLogo) || !StringUtil.isEmpty(welcomePicture) || !StringUtil.isEmpty(clientLogo)) {
            oc.setPortalLogo(portalLogo);
            oc.setWelcomePicture(welcomePicture);
            oc.setClientLogo(clientLogo);
            oemConfigService.updateOemConfigIsNull(oc);
            return returnSuccess();
        }

        try {
            oemConfigService.deleteOemConfig(id);
            oemConfigService.reloadCache();
        } catch (Exception e) {
            logger.error("删除OEM配置失败：", e);
            return returnFail(RespCode.DELETE_FAIL, "删除OEM配置失败，请联系管理员！");
        }
        return returnSuccess();
    }


    /**
     * 查询OEM图片信息
     * @param id
     * @param request
     * @return
     */
    @RequestMapping(value = "/oem/getOemLogo")
    @ResponseBody
    public Map<String, Object> getOemLogo(@RequestParam(value = "id") String id) {
        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<String, Object>(7);
        result.put(RespCode.RESP_STAT, respstat);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
        try {
            OemConfig oemConfig = oemConfigService.getOemConfigById(id);
            result.put("portalLogo", oemConfig.getPortalLogo());
            result.put("welcomePicture", oemConfig.getWelcomePicture());
            result.put("clientLogo", oemConfig.getClientLogo());
        } catch (Exception e) {
            logger.error("查询OEM图片信息失败{}", e);
            return returnFail(RespCode.error107, "查询OEM图片信息失败，请联系管理员！");
        }
        return result;
    }
}
