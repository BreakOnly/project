package com.jrmf.controller.systemrole.userregisterrecord;

import com.jrmf.controller.BaseController;
import com.jrmf.domain.PageVisitRecord;
import com.jrmf.domain.UserRegisterRecord;
import com.jrmf.service.UserRegisterRecordService;
import com.jrmf.utils.AddressUtil;
import com.jrmf.utils.ExcelFileGenerator;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @Title: UserRegisterRecordController
 * @Description: 用户登记咨询记录
 * @create 2020/2/21 14:08
 */
@RestController
@RequestMapping(value = "/userRegisterRecord")
public class UserRegisterRecordController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(UserRegisterRecordController.class);

    @Autowired
    private UserRegisterRecordService userRegisterRecordService;


    /**
     * 用户登记咨询记录查询
     *
     * @param userName
     * @param registerType
     * @param phoneNo
     * @param companyName
     * @param startTime
     * @param endTime
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/queryRecord")
    public Map<String, Object> queryRecord(@RequestParam(value = "userName", required = false) String userName,
                                           @RequestParam(value = "registerType", required = false) String registerType,
                                           @RequestParam(value = "phoneNo", required = false) String phoneNo,
                                           @RequestParam(value = "companyName", required = false) String companyName,
                                           @RequestParam(value = "startTime", required = false) String startTime,
                                           @RequestParam(value = "endTime", required = false) String endTime,
                                           @RequestParam(value = "pageNo", required = false) String pageNo,
                                           @RequestParam(value = "pageSize", required = false, defaultValue = "10") String pageSize) {
        Map<String, Object> result = new HashMap<>(7);
        result.put(RespCode.RESP_STAT, RespCode.success);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));

        Map<String, Object> paramMap = new HashMap<>(12);
        paramMap.put("userName", userName);
        paramMap.put("registerType", registerType);
        paramMap.put("phoneNo", phoneNo);
        paramMap.put("companyName", companyName);
        paramMap.put("startTime", startTime);
        paramMap.put("endTime", endTime);
        paramMap.put("pageNo", pageNo);
        paramMap.put("pageSize", pageSize);

        int total = userRegisterRecordService.queryRecordListCount(paramMap);
        if (!StringUtil.isEmpty(pageNo)) {
            paramMap.put("start", getFirst(pageNo, pageSize));
            paramMap.put("limit", Integer.parseInt(pageSize));
        }
        List<UserRegisterRecord> list = userRegisterRecordService.queryRecordList(paramMap);
        result.put("list", list);
        result.put("total", total);
        return result;
    }

    /**
     * 用户登记咨询记录导出
     *
     * @param userName
     * @param registerType
     * @param phoneNo
     * @param companyName
     * @param startTime
     * @param endTime
     * @param response
     */
    @RequestMapping(value = "/exportRecord")
    public void exportRecord(@RequestParam(value = "userName", required = false) String userName,
                             @RequestParam(value = "registerType", required = false) String registerType,
                             @RequestParam(value = "phoneNo", required = false) String phoneNo,
                             @RequestParam(value = "companyName", required = false) String companyName,
                             @RequestParam(value = "startTime", required = false) String startTime,
                             @RequestParam(value = "endTime", required = false) String endTime,
                             HttpServletResponse response) {

        Map<String, Object> paramMap = new HashMap<>(9);
        paramMap.put("userName", userName);
        paramMap.put("registerType", registerType);
        paramMap.put("phoneNo", phoneNo);
        paramMap.put("companyName", companyName);
        paramMap.put("startTime", startTime);
        paramMap.put("endTime", endTime);

        List<UserRegisterRecord> list = userRegisterRecordService.queryRecordList(paramMap);

        String[] colunmName = new String[]{"登记姓名", "登记手机号", "登记来源", "公司名称", "职位",
                "用户请求IP", "请求IP所属区", "登记时间"};

        List<Map<String, Object>> data = new ArrayList<>();
        for (UserRegisterRecord userRegisterRecord : list) {
            Map<String, Object> dataMap = new HashMap<>(12);
            dataMap.put("1", userRegisterRecord.getUserName());
            dataMap.put("2", userRegisterRecord.getPhoneNo());
            dataMap.put("3", userRegisterRecord.getRegisterType());
            dataMap.put("4", userRegisterRecord.getCompanyName());
            dataMap.put("5", userRegisterRecord.getPosition());
            dataMap.put("6", userRegisterRecord.getUserIPAddress());
            dataMap.put("7", userRegisterRecord.getUserAddress());
            dataMap.put("8", userRegisterRecord.getCreateTime());
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, "用户登记咨询记录表", data);

    }

    /**
     * 新增用户咨询记录
     * @param request
     * @param userRegisterRecord
     * @return
     */
    @RequestMapping(value = "/insertUserRecord")
    public Map<String, Object> addCustomInfo(HttpServletRequest request, UserRegisterRecord userRegisterRecord) {
        // 根据跨域请求 获取用户的IP地址
        String userIP = AddressUtil.getIp2(request);
        logger.info("H5 用户登记咨询采集 userIP={}", userIP);
        userRegisterRecord.setUserIPAddress(userIP);

        // 根据用户的IP地址获取用户所在的物理地址
        String addresses = AddressUtil.getAddresses2(userIP);
        logger.info("H5 用户登记咨询采集 addresses={}", addresses);
        userRegisterRecord.setUserAddress(addresses);

        // 根据用户所在渠道号获取用户信息来源渠道名称
        userRegisterRecord.setChannel("财税工具栏目");

        try {
            logger.info("添加用户信息 userRegisterRecord:{}", userRegisterRecord.toString());
            Optional.ofNullable(userRegisterRecord)
                    .filter(r -> userRegisterRecordService.selectUserRegisterRecord(r.getUserName(),r.getPhoneNo()) == 0)
                    .orElseThrow(() -> new Exception("用户信息已经被注册"));
            userRegisterRecordService.insertUserRegisterRecord(userRegisterRecord);
        } catch (Exception e) {
            logger.error("添加用户信息失败 userRegisterRecord:{}", e);
            return returnFail(RespCode.error107, e.getMessage());
        }

        return returnSuccess();

    }

    /**
     * H5页面 增加访问记录和数量
     * @param pageVisitRecord
     * @return
     */
    @RequestMapping(value = "/pageVisitRecord")
    public Map<String, Object> pageVisitRecord(PageVisitRecord pageVisitRecord, HttpServletRequest request) {

        // 根据跨域请求 获取用户的IP地址
        String userIP = AddressUtil.getIp2(request);
        logger.info("H5 用户信息采集 userIP={}", userIP);
        pageVisitRecord.setUserIp(userIP);

        // 根据用户的IP地址获取用户所在的物理地址
        String addresses = AddressUtil.getAddresses2(userIP);
        logger.info("H5 用户信息采集 addresses={}", addresses);
        pageVisitRecord.setUserIpAddress(addresses);

        userRegisterRecordService.insertPageVisitRecord(pageVisitRecord);
        return returnSuccess();
    }
}
