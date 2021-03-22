package com.jrmf.controller.systemrole.merchant.file;

import com.jrmf.bankapi.TransHistoryRecord;
import com.jrmf.common.CommonString;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.CertType;
import com.jrmf.controller.constant.CommissionStatus;
import com.jrmf.controller.constant.PayType;
import com.jrmf.controller.constant.QueryType;
import com.jrmf.controller.constant.RechargeStatusType;
import com.jrmf.controller.constant.RechargeType;
import com.jrmf.controller.constant.ServiceFeeType;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.ChannelHistory;
import com.jrmf.domain.ChannelRelated;
import com.jrmf.domain.CommissionTemporary;
import com.jrmf.domain.CustomMenu;
import com.jrmf.domain.User;
import com.jrmf.domain.UserCommission;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.ChannelHistoryService;
import com.jrmf.service.ChannelInterimBatchService2;
import com.jrmf.service.ChannelRelatedService;
import com.jrmf.service.CustomCompanyRateConfService;
import com.jrmf.service.OrganizationTreeService;
import com.jrmf.service.PingAnBankService;
import com.jrmf.service.UserCommissionService;
import com.jrmf.service.UserSerivce;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.ExcelFileGenerator;
import com.jrmf.utils.FtpTool;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Author Nicholas-lulu
 * Description //TODO商户查询功能页面的导出
 * Date 11:23 2018/11/27
 * Param
 * return
 **/
@Controller
@RequestMapping("/merchant/file")
public class MerchantFileController extends BaseController {

    private static Logger logger = LoggerFactory.getLogger(MerchantFileController.class);
    @Autowired
    protected UserSerivce userSerivce;
    @Autowired
    private UserCommissionService commissionService;
    @Autowired
    private ChannelHistoryService channelHistoryService;
    @Autowired
    private ChannelCustomService customService;
    @Autowired
    private ChannelInterimBatchService2 batchService2;
    @Autowired
    private PingAnBankService pinganBankService;
    @Autowired
    private ChannelInterimBatchService2 channelInterimBatchService2;
    @Autowired
    private ChannelRelatedService channelRelatedService;
    @Autowired
    private OrganizationTreeService organizationTreeService;
    @Autowired
    private CustomCompanyRateConfService rateConfService;
    @Autowired
    ChannelCustomService channelCustomService;

    /**
     * Author Nicholas-Ning
     * Description //TODO 商户余额列表导出
     * Date 15:35 2018/12/6
     * Param [request, response]
     * return void
     **/
    @RequestMapping(value = "/commission/balance/export")
    public void balanceExport(Integer customType, Integer nodeId, HttpServletResponse response) {
        List<String> customKeys = organizationTreeService.queryNodeCusotmKey(customType, QueryType.QUERY_CURRENT_AND_CHILDREN, nodeId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (String customKey : customKeys) {
            ChannelCustom custom = customService.getCustomByCustomkey(customKey);
            Map<String, Object> params1 = new HashMap<>(5);
            params1.put(CommonString.CUSTOMKEY, customKey);
            List<ChannelRelated> relateds = channelRelatedService.queryRelatedList(params1);
            for (ChannelRelated related : relateds) {
                ChannelCustom company = customService.getCustomByCustomkey(related.getCompanyId());
                Map<String, Object> data = new HashMap<>(15);
                data.put("customName", custom.getCompanyName());
                data.put("companyName", company.getCompanyName());
                for (PayType payType : PayType.values()) {
                    String balance = channelHistoryService.getBalance(related.getOriginalId(), related.getCompanyId(), String.valueOf(payType.getCode()));
                    data.put(payType.getEnglishDesc() + "balance", balance);
                }
                //商户在某个服务公司下发总余额
                data.put("balanceSum", channelHistoryService.getBalance(customKey, related.getCompanyId(), ""));
                data.put("customId", custom.getId());
                data.put("companyId", company.getCustomkey());
                //查询商户待确认充值金额
                Map<String, Object> params = new HashMap<>(10);
                params.put(CommonString.CUSTOMKEY, related.getOriginalId());
                params.put(CommonString.COMPANYID, related.getCompanyId());
                params.put(CommonString.TRANSFERTYPE, "1");
                params.put(CommonString.STATUS, "0");
                List<ChannelHistory> channelHistorys = channelHistoryService.getChannelHistoryByParam(params);
                String waitConfirmedBalance = "0.00";
                for (ChannelHistory channelHistory : channelHistorys) {
                    waitConfirmedBalance = ArithmeticUtil.addStr(waitConfirmedBalance, channelHistory.getAmount());
                }
                data.put("waitConfirmedBalance", waitConfirmedBalance);

                Map<String, Object> customRateConf = rateConfService.getCustomRateConf(related.getOriginalId(), related.getCompanyId());
                if (customRateConf != null) {
                    data.putAll(customRateConf);
                } else {

                }
                result.add(data);
            }
        }
        String[] colunmName = new String[]{"商户名称", "签约服务服务公司", "打款待确认金额", "充值账户汇总可用余额", "银行卡余额", "支付宝余额", "微信余额", "服务费率", "服务费收取方式"};
        String filename = "商户余额统计表";
        List<Map<String, Object>> data = new ArrayList<>();
        for (Map<String, Object> stringObjectMap : result) {
            Map<String, Object> dataMap = new HashMap<>(20);
            dataMap.put("1", stringObjectMap.get("customName"));
            dataMap.put("2", stringObjectMap.get("companyName"));
            dataMap.put("3", stringObjectMap.get("waitConfirmedBalance"));
            dataMap.put("4", stringObjectMap.get("balanceSum"));
            dataMap.put("5", stringObjectMap.get("bankcardbalance"));
            dataMap.put("6", stringObjectMap.get("alipaybalance"));
            dataMap.put("7", stringObjectMap.get("wechatbalance"));
            if (stringObjectMap.containsKey("customRate")) {
                dataMap.put("8", stringObjectMap.get("customRate"));
            } else {
                dataMap.put("8", "未配置");
            }

            if (stringObjectMap.containsKey("serviceFeeType")) {
                dataMap.put("9", ServiceFeeType.codeOf((Integer) stringObjectMap.get("serviceFeeType")).getDesc());
            } else {
                dataMap.put("9", "未配置");
            }
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }

    /**
     * Author Nicholas-Ning
     * Description //TODO 重复数据导出
     * Date 11:07 2018/11/28
     * Param [ids]
     * return void
     **/
    @RequestMapping(value = "/commission/repeat")
    public void exportCustomManage(HttpServletRequest request, HttpServletResponse response) {
        String ids = request.getParameter("ids");
        String[] split = ids.split(",");
        Integer[] commIds = new Integer[split.length];
        for (int i = 0; i < split.length; i++) {
            commIds[i] = Integer.parseInt(split[i]);
        }
        String[] colunmName = new String[]{"姓名", "证件号", "卡号", "金额(元)"};
        String filename = "下发重复数据表";
        List<CommissionTemporary> commissionList = channelInterimBatchService2.getCommissionByIds(commIds);
        List<Map<String, Object>> data = new ArrayList<>();
        for (CommissionTemporary commissionTemporary : commissionList) {
            Map<String, Object> dataMap = new HashMap<>(20);
            dataMap.put("1", commissionTemporary.getUserName());
            dataMap.put("2", commissionTemporary.getIdCard());
            dataMap.put("3", commissionTemporary.getBankCardNo());
            dataMap.put("4", commissionTemporary.getAmount());
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }

    /**
     * Author Nicholas-Ning
     * Description //TODO 商户充值记录导出
     * Date 9:50 2018/11/27
     * Param [model, startTime, endTime, amount, payType, request, response]
     * return void
     **/
    @RequestMapping(value = "/custom/exportCustomData")
    public void exportCustomManage(@RequestParam(required = false) String startTime,
                                   @RequestParam(required = false) String endTime,
                                   @RequestParam(required = false) String rechargeAmount,
                                   @RequestParam(required = false) String status,
                                   @RequestParam(required = false) String payType,
                                   @RequestParam(required = false) String companyId,
                                   @RequestParam(required = false) String customName,
                                   @RequestParam(required = false) Integer nodeId,
                                   @RequestParam(required = false) Integer customType,
                                   @RequestParam(required = false) String orderNo,
                                   @RequestParam(required = false) Integer rechargeType,
                                   HttpServletResponse response) {
        // 商户标识
        List<String> originalIds = organizationTreeService.queryNodeCusotmKey(customType, QueryType.QUERY_CURRENT_AND_CHILDREN, nodeId);
        StringBuffer originalIdStr = new StringBuffer();
        for (String originalId : originalIds) {
            originalIdStr.append(originalId).append(",");
        }
        if (customType == QueryType.COMPANY){
            ChannelCustom custom = channelCustomService.getCustomById(nodeId);
            String customkey = custom.getCustomkey();
            originalIdStr.append(customkey).append(",");
        }
        Map<String, Object> paramMap = new HashMap<>(10);
        paramMap.put("customkey", originalIdStr.toString());
        paramMap.put("startTime", startTime);
        paramMap.put("endTime", endTime);
        paramMap.put("rechargeAmount", rechargeAmount);
        paramMap.put("status", status);
        paramMap.put("payType", payType);
        paramMap.put("companyId", companyId);
        paramMap.put("customName", customName);
        paramMap.put("orderNo", orderNo);
        paramMap.put("rechargeType", rechargeType);

        List<Map<String, Object>> list = channelHistoryService.geCustomChargeDetail(paramMap);

        String[] colunmName = new String[]{"商户名称", "打款金额", "充值状态", "退款金额", "时间",
                "充值下发通道", "备注描述", "可用余额", "手续费收取方式", "服务费率", "预扣手续费", "操作帐号",
                "收款下发公司", "收款账号", "收款账户银行", "付款账户名称", "付款账号", "付款账号银行","充值流水","类别"};
        String filename = "充值记录表";
        List<Map<String, Object>> data = new ArrayList<>();
        for (Map<String, Object> stringObjectMap : list) {
            Map<String, Object> dataMap = new HashMap<>(25);
            dataMap.put("1", stringObjectMap.get("customName"));
            dataMap.put("2", stringObjectMap.get("rechargeAmount"));
            dataMap.put("3", RechargeStatusType.codeOf((Integer) stringObjectMap.get("status")).getDesc());
            dataMap.put("4", stringObjectMap.get("refundAmount"));
            dataMap.put("5", stringObjectMap.get("createTime"));
            dataMap.put("6", PayType.codeOf((Integer) stringObjectMap.get("payType")).getDesc());
            dataMap.put("7", stringObjectMap.get("remark"));
            dataMap.put("8", stringObjectMap.get("amount"));

            Integer rechargeTypeCode = (Integer) stringObjectMap.get("rechargeType");

            if (rechargeTypeCode == RechargeType.AMOUNT.getCode()) {
                dataMap.put("9", ServiceFeeType.codeOf((Integer) stringObjectMap.get("serviceFeeType")).getDesc());
            } else {
                dataMap.put("9", "");
            }

            String serviceFeeRate = (String) stringObjectMap.get("serviceFeeRate");
            if (!StringUtil.isEmpty(serviceFeeRate)) {
                dataMap.put("10", ArithmeticUtil.mulStr(serviceFeeRate, "100", 2) + "%");
            } else {
                dataMap.put("10", "0%");
            }

            dataMap.put("11", stringObjectMap.get("serviceFee"));
            dataMap.put("12", stringObjectMap.get("operatorName"));
            dataMap.put("13", stringObjectMap.get("companyName"));
            dataMap.put("14", stringObjectMap.get("inAccountNo"));
            dataMap.put("15", stringObjectMap.get("inAccountBankName"));
            dataMap.put("16", stringObjectMap.get("customName"));
            dataMap.put("17", stringObjectMap.get("payAccountNo"));
            dataMap.put("18", stringObjectMap.get("payAccountBankName"));
            dataMap.put("19", stringObjectMap.get("orderNo"));
            dataMap.put("20", RechargeType.codeOf(rechargeTypeCode).getDesc());

            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }

    /**
     * 用户列表导出
     *
     * @param model
     * @param startTime
     * @param endTime
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/user/exportUserData")
    public void exportUserData(ModelMap model, String startTime, String endTime, String status, String userType,
                               String userName, HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 渠道名称
        String customkey = (String) request.getSession().getAttribute("customkey");
        Map<String, Object> paramMap = new HashMap<>(15);
        paramMap.put("userType", userType);
        paramMap.put("startTime", startTime);
        paramMap.put("endTime", endTime);
        paramMap.put("userName", userName);
        paramMap.put("originalId", customkey);
        paramMap.put("status", status);
        List<User> list = userSerivce.getUserRelatedByParam(paramMap);
        String today = DateUtils.getNowDay();
        ArrayList<String> dataStr = new ArrayList<String>();
        for (int i = 0; i < list.size(); i++) {
            User user = list.get(i);
            StringBuffer strBuff = new StringBuffer();
            String role = "";
            String payStatus = "";
            int UserType = user.getUserType();
            if (UserType == 1) {
                role = "已开户";
            } else {
                role = "未开户";
            }

            String type = user.getStatus();
            if ("1".equals(type)) {
                payStatus = "签约";
            } else {
                payStatus = "未签约";
            }
            strBuff.append(user.getCreateTime() == null ? "" : user.getCreateTime()).append(",")
                    .append(user.getUserNo() == null ? "" : user.getUserNo()).append(",")
                    .append(user.getUserName() == null ? "" : user.getUserName()).append(",")
                    .append(user.getCertId() == null ? "" : user.getCertId()).append(",")
                    .append(user.getBankNo() == null ? "" : user.getBankNo()).append(",")
                    .append(user.getMobilePhone() == null ? "" : user.getMobilePhone()).append(",").append(role)
                    .append(",").append(payStatus);

            dataStr.add(strBuff.toString());
        }
        ArrayList<String> fieldName = new ArrayList<String>();
        fieldName.add("申请时间");
        fieldName.add("渠道用户ID");
        fieldName.add("姓名");
        fieldName.add("身份证号码");
        fieldName.add("银行卡号");
        fieldName.add("手机号");
        fieldName.add("银行电子户");
        fieldName.add("是否签约");
        String filename = today + "用户列表";
        ExcelFileGenerator.exportExcel(response, fieldName, dataStr, filename);
    }

    /**
     * 开户模板下载
     *
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/download/userTemplate")
    public ResponseEntity<byte[]> userTemplate(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String path = "/excel/userTemplate.xls";// Excel模板所在的路径。
        String fileName = path.substring(path.lastIndexOf("/")+1);
        byte[] bytes = FtpTool.downloadFtpFile(path.substring(0, path.lastIndexOf("/")), path.substring(path.lastIndexOf("/")+1));
        HttpHeaders headers = new HttpHeaders(); 
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);    
		fileName = new String(fileName.getBytes("gbk"), "iso8859-1");// 防止中文乱码
		headers.add("Content-Disposition", "attachment;filename=" + fileName);
		headers.setContentDispositionFormData("attachment", fileName);    
		return new ResponseEntity<byte[]>(bytes,    
				headers, HttpStatus.OK); 
    }

    /**
     * 徽商下发模板
     *
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/download/commissionTemplate")
    public ResponseEntity<byte[]> commissionTemplate(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String path = "/excel/commissionTemplate.xlsx";// Excel模板所在的路径。
        String fileName = path.substring(path.lastIndexOf("/")+1);
        byte[] bytes = FtpTool.downloadFtpFile(path.substring(0, path.lastIndexOf("/")), path.substring(path.lastIndexOf("/")+1));
        HttpHeaders headers = new HttpHeaders(); 
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);    
		fileName = new String(fileName.getBytes("gbk"), "iso8859-1");// 防止中文乱码
		headers.add("Content-Disposition", "attachment;filename=" + fileName);
		headers.setContentDispositionFormData("attachment", fileName);    
		return new ResponseEntity<byte[]>(bytes,    
				headers, HttpStatus.OK); 
    }

    /**
     * 银企直联下发模板
     *
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/download/bankPayTemplate")
    public ResponseEntity<byte[]> bankPayTemplate(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String path = "/excel/bankPayTemplate.xlsx";// Excel模板所在的路径。
        String fileName = path.substring(path.lastIndexOf("/")+1);
        byte[] bytes = FtpTool.downloadFtpFile(path.substring(0, path.lastIndexOf("/")), path.substring(path.lastIndexOf("/")+1));
        HttpHeaders headers = new HttpHeaders(); 
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);    
		fileName = new String(fileName.getBytes("gbk"), "iso8859-1");// 防止中文乱码
		headers.add("Content-Disposition", "attachment;filename=" + fileName);
		headers.setContentDispositionFormData("attachment", fileName);    
		return new ResponseEntity<byte[]>(bytes,    
				headers, HttpStatus.OK); 
    }

    /**
     * 支付宝下发模板
     *
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/download/aliCommissionTemplate")
    public ResponseEntity<byte[]> aliCommissionTemplate(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String path = "/excel/aliCommissionTemplate.xlsx";// Excel模板所在的路径。
        String fileName = path.substring(path.lastIndexOf("/")+1);
        byte[] bytes = FtpTool.downloadFtpFile(path.substring(0, path.lastIndexOf("/")), path.substring(path.lastIndexOf("/")+1));
        HttpHeaders headers = new HttpHeaders(); 
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);    
		fileName = new String(fileName.getBytes("gbk"), "iso8859-1");// 防止中文乱码
		headers.add("Content-Disposition", "attachment;filename=" + fileName);
		headers.setContentDispositionFormData("attachment", fileName);    
		return new ResponseEntity<byte[]>(bytes,    
				headers, HttpStatus.OK); 
    }

    /**
     * 导出批次用户信息
     *
     * @param model
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/batch/exportUserDetailData")
    public void exportUserDetailData(ModelMap model, String batcheId, String userType, String userName,
                                     HttpServletRequest request, HttpServletResponse response) throws Exception {

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("userType", userType);
        paramMap.put("batcheId", batcheId);
        paramMap.put("userName", userName);
        List<User> userList = userSerivce.getUserRelatedByParam(paramMap);

        String today = DateUtils.getNowDay();
        ArrayList<String> dataStr = new ArrayList<String>();
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            StringBuffer strBuff = new StringBuffer();
            String statusDesc = "";

            int status1 = user.getUserType();
            if (status1 == 11) {
                statusDesc = "待开户";
            } else if (status1 == 1) {
                statusDesc = "开户成功";
            } else if (status1 == 0) {
                statusDesc = "开户失败-" + user.getRemark();
            } else if (status1 == 12) {
                statusDesc = "开户失败-" + user.getRemark();
            }
            strBuff.append(user.getCreateTime() == null ? "" : user.getCreateTime()).append(",")
                    .append(user.getUserName() == null ? "" : user.getUserName()).append(",")
                    .append(user.getCertId() == null ? "" : user.getCertId()).append(",")
                    .append(user.getBankNo() == null ? "" : user.getBankNo()).append(",")
                    .append(user.getMobilePhone() == null ? "" : user.getMobilePhone()).append(",").append(statusDesc);

            dataStr.add(strBuff.toString());
        }
        ArrayList<String> fieldName = new ArrayList<String>();
        fieldName.add("申请时间");
        fieldName.add("姓名");
        fieldName.add("身份证号码");
        fieldName.add("银行卡号");
        fieldName.add("手机号");
        fieldName.add("状态");
        String filename = today + "银行电子户批次详情";
        ExcelFileGenerator.exportExcel(response, fieldName, dataStr, filename);
    }

    /**
     * 交易流水--导出
     *
     * @throws Exception
     */
    @RequestMapping(value = "/user/commissionDataExport")
    public void commissionDataExport(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String customkey = (String) request.getSession().getAttribute("customkey");// 薪税服务公司标识
        String name = (String) request.getParameter("name");
        String startTime = (String) request.getParameter("startTime");
        String endTime = (String) request.getParameter("endTime");
        String status = (String) request.getParameter("status");
        logger.info("/user/commissionData 方法  传参： customkey=" + customkey);
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("originalId", customkey);
        paramMap.put("name", name);
        paramMap.put("status", status);
        paramMap.put("startTime", startTime);
        paramMap.put("endTime", endTime);
        List<UserCommission> list = commissionService.getUserCommissionByParam(paramMap);
        String today = DateUtils.getNowDay();
        ArrayList<String> dataStr = new ArrayList<String>();
        for (int i = 0; i < list.size(); i++) {
            UserCommission commission = list.get(i);
            StringBuffer strBuff = new StringBuffer();

            strBuff.append(commission.getCustomName() == null ? "" : commission.getCustomName()).append(",")
                    .append(commission.getCreatetime() == null ? "" : commission.getCreatetime()).append(",")
                    .append(commission.getBatchId() == null ? "" : commission.getBatchId()).append(",")
                    .append(commission.getOrderNo() == null ? "" : commission.getOrderNo()).append(",")
                    .append(commission.getUserName() == null ? "" : commission.getUserName()).append(",")
                    .append(commission.getPhoneNo() == null ? "" : commission.getPhoneNo()).append(",")
                    .append(commission.getStatusDesc() == null ? "" : commission.getStatusDesc()).append(",")
                    .append(commission.getAmount() == null ? "" : commission.getAmount()).append(",")
                    .append(commission.getUpdatetime() == null ? "" : commission.getUpdatetime());

            dataStr.add(strBuff.toString());
        }
        ArrayList<String> fieldName = new ArrayList<String>();
        fieldName.add("商户名称");
        fieldName.add("提交时间");
        fieldName.add("批次号");
        fieldName.add("订单号");
        fieldName.add("姓名");
        fieldName.add("手机号");
        fieldName.add("状态");
        fieldName.add("下发金额");
        fieldName.add("到账时间");
        String filename = today + "交易流水";
        ExcelFileGenerator.exportExcel(response, fieldName, dataStr, filename);
    }

    /**
     * 批次交易结果查询---导出批次
     *
     * @throws Exception
     */
    @RequestMapping(value = "/user/batchDataExport")
    public void batchDataExport(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String operatorName = null;
        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
        if (customLogin.getLoginRole() == 2) {//如果为操作员，只能查看自己数据
            operatorName = customLogin.getUsername();
        }
        String nodeCustomKey = customLogin.getCustomkey();

        String batchName = request.getParameter("batchName");
        String batchDesc = request.getParameter("batchDesc");
        String status = request.getParameter("status");
        String batchAmount = request.getParameter("amount");
        batchAmount = ArithmeticUtil.formatDecimals(batchAmount);
        String payType = request.getParameter("payType");
        String contentName = request.getParameter("contentName");
        String fileName = request.getParameter("fileName");
        String recCustomkey = request.getParameter("recCustomkey");
        String submitTimeStart = request.getParameter("submitTimeStart");
        String submitTimeEnd = request.getParameter("submitTimeEnd");
        String completeTimeStart = request.getParameter("completeTimeStart");
        String completeTimeEnd = request.getParameter("completeTimeEnd");
        String menuId = request.getParameter("menuId");
        String menuIds = "";
        menuIds = getMenuIds(menuId, menuIds);

        Map<String, Object> param = new HashMap<>(24);
        param.put("customkey", nodeCustomKey);
        param.put("batchName", !StringUtil.isEmpty(batchName) ? batchName : "");
        param.put("batchDesc", !StringUtil.isEmpty(batchDesc) ? batchDesc : "");
        param.put("status", !StringUtil.isEmpty(status) ? Integer.parseInt(status) : null);
        param.put("batchAmount", !StringUtil.isEmpty(batchAmount) ? batchAmount : "");
        param.put("payType", !StringUtil.isEmpty(payType) ? Integer.parseInt(payType) : null);
        param.put("contentName", !StringUtil.isEmpty(contentName) ? contentName : "");
        param.put("fileName", !StringUtil.isEmpty(fileName) ? fileName : "");
        param.put("recCustomkey", !StringUtil.isEmpty(recCustomkey) ? recCustomkey : "");
        param.put("submitTimeStart", !StringUtil.isEmpty(submitTimeStart) ? submitTimeStart : "");
        param.put("submitTimeEnd", !StringUtil.isEmpty(submitTimeEnd) ? submitTimeEnd : "");
        param.put("completeTimeStart", !StringUtil.isEmpty(completeTimeStart) ? completeTimeStart : "");
        param.put("completeTimeEnd", !StringUtil.isEmpty(completeTimeEnd) ? completeTimeEnd : "");
        param.put("menuIds", menuIds);
        param.put("operatorName", operatorName);

        List<Map<String, Object>> channelHistoryList = channelHistoryService.batchResultQuery(param);
        /**
         * 3-->定义导出文件的列名
         */
        String[] colunmName = new String[]{"商户名称", "批次名称", "批次说明", "批次状态", "下发通道", "提交时间"
                , "批次总金额", "批次总笔数", "服务费", "批次成功总金额", "批次成功总笔数"
                , "批次失败总金额", "批次失败总笔数", "到账时间", "代付项目名称", "服务公司", "操作账号", "复核账号"
                , "批次导入文件名称"};
        /**
         * 4-->定义导出文件文件名
         */
        String filename = "批次交易统计表";
        List<Map<String, Object>> data = new ArrayList<>();
        /**
         * 5-->组装列值集合 按照列名的顺序，将对应的列值放进map中，key值是顺序。确保了本来无序的map有序化。
         */
        for (Map<String, Object> map : channelHistoryList) {
            Map<String, Object> dataMap = new HashMap<>(20);
            dataMap.put("0", map.get("companyName"));
            dataMap.put("1", map.get("batchName"));
            dataMap.put("2", map.get("batchDesc"));
            dataMap.put("3", (int) map.get("status") == 1 ? "发放成功"
                    : (int) map.get("status") == 2 ? "全部失败" : (int) map.get("status") == 5 ? "部分失败" : "批次未完成");
            dataMap.put("4", (int) map.get("payType") == 1 ? "银行电子户"
                    : (int) map.get("payType") == 2 ? "支付宝" : (int) map.get("payType") == 3 ? "微信" : "银行卡");
            dataMap.put("5", map.get("createTime"));
            dataMap.put("6", map.get("batchAmount"));
            dataMap.put("7", map.get("batchNum"));
            dataMap.put("8", map.get("serviceFee"));
            dataMap.put("9", map.get("amount"));
            dataMap.put("10", map.get("passNum"));
            dataMap.put("11", map.get("failedAmount"));
            dataMap.put("12", map.get("failedNum"));
            dataMap.put("13", map.get("provideTime"));
            dataMap.put("14", map.get("contentName"));
            dataMap.put("15", map.get("companyName"));
            dataMap.put("16", map.get("operatorName"));
            dataMap.put("17", map.get("reviewName"));
            dataMap.put("18", map.get("fileName"));
            data.add(sortMapByKey(dataMap));
        }
        /**
         * 6-->调用桐宁桑封装好的方法，把刚才搞得参传进去，用户就可以得到一个想要的Excel表格
         */
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }

    @RequestMapping(value = "/user/new/batchDataExport")
    public void batchDataExportNew(HttpServletRequest request, HttpServletResponse response,
                                   @RequestParam(required = false, defaultValue = "0") Integer nodeId,
                                   @RequestParam(required = false, defaultValue = "0") Integer customType) throws Exception {
        String operatorName = null;

        String customKeyStr = "";
        List<String> customKeyList = organizationTreeService.queryNodeCusotmKey(customType, "G", nodeId);
        for (String ckey : customKeyList) {
            customKeyStr = customKeyStr + "," + ckey;
        }
        if (customKeyStr.lastIndexOf(",") >= 0) {
            customKeyStr = customKeyStr.substring(1);
        }

        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
        if (customLogin.getLoginRole() == 2) {//如果为操作员，只能查看自己数据
            operatorName = customLogin.getUsername();
        }

        String batchName = request.getParameter("batchName");
        String batchDesc = request.getParameter("batchDesc");
        String status = request.getParameter("status");
        String batchAmount = request.getParameter("amount");
        batchAmount = ArithmeticUtil.formatDecimals(batchAmount);
        String payType = request.getParameter("payType");
        String contentName = request.getParameter("contentName");
        String fileName = request.getParameter("fileName");
        String recCustomkey = request.getParameter("recCustomkey");
        String submitTimeStart = request.getParameter("submitTimeStart");
        String submitTimeEnd = request.getParameter("submitTimeEnd");
        String completeTimeStart = request.getParameter("completeTimeStart");
        String completeTimeEnd = request.getParameter("completeTimeEnd");
        String menuId = request.getParameter("menuId");
        String menuIds = "";
        menuIds = getMenuIds(menuId, menuIds);

        Map<String, Object> param = new HashMap<>(24);
        param.put("customkey", customKeyStr);
        param.put("batchName", !StringUtil.isEmpty(batchName) ? batchName : "");
        param.put("batchDesc", !StringUtil.isEmpty(batchDesc) ? batchDesc : "");
        param.put("status", !StringUtil.isEmpty(status) ? Integer.parseInt(status) : null);
        param.put("batchAmount", !StringUtil.isEmpty(batchAmount) ? batchAmount : "");
        param.put("payType", !StringUtil.isEmpty(payType) ? Integer.parseInt(payType) : null);
        param.put("contentName", !StringUtil.isEmpty(contentName) ? contentName : "");
        param.put("fileName", !StringUtil.isEmpty(fileName) ? fileName : "");
        param.put("recCustomkey", !StringUtil.isEmpty(recCustomkey) ? recCustomkey : "");
        param.put("submitTimeStart", !StringUtil.isEmpty(submitTimeStart) ? submitTimeStart : "");
        param.put("submitTimeEnd", !StringUtil.isEmpty(submitTimeEnd) ? submitTimeEnd : "");
        param.put("completeTimeStart", !StringUtil.isEmpty(completeTimeStart) ? completeTimeStart : "");
        param.put("completeTimeEnd", !StringUtil.isEmpty(completeTimeEnd) ? completeTimeEnd : "");
        param.put("menuIds", menuIds);
        param.put("operatorName", operatorName);

        List<Map<String, Object>> channelHistoryList = channelHistoryService.batchResultQuery(param);
        /**
         * 3-->定义导出文件的列名
         */
        String[] colunmName = new String[]{"商户名称", "批次名称", "批次说明", "批次状态", "下发通道", "提交时间"
                , "批次总金额", "批次总笔数", "服务费", "批次成功总金额", "批次成功总笔数"
                , "批次失败总金额", "批次失败总笔数", "到账时间", "代付项目名称", "服务公司", "操作账号", "复核账号"
                , "批次导入文件名称"};
        /**
         * 4-->定义导出文件文件名
         */
        String filename = "批次交易统计表";
        List<Map<String, Object>> data = new ArrayList<>();
        /**
         * 5-->组装列值集合 按照列名的顺序，将对应的列值放进map中，key值是顺序。确保了本来无序的map有序化。
         */
        for (Map<String, Object> map : channelHistoryList) {
            Map<String, Object> dataMap = new HashMap<>(20);
            dataMap.put("0", map.get("merchantName"));
            dataMap.put("1", map.get("batchName"));
            dataMap.put("2", map.get("batchDesc"));
            dataMap.put("3", (int) map.get("status") == 1 ? "发放成功"
                    : (int) map.get("status") == 2 ? "全部失败" : (int) map.get("status") == 5 ? "部分失败" : "批次未完成");
            dataMap.put("4", (int) map.get("payType") == 1 ? "银行电子户"
                    : (int) map.get("payType") == 2 ? "支付宝" : (int) map.get("payType") == 3 ? "微信" : "银行卡");
            dataMap.put("5", map.get("createTime"));
            dataMap.put("6", map.get("batchAmount"));
            dataMap.put("7", map.get("batchNum"));
            dataMap.put("8", map.get("serviceFee"));
            dataMap.put("9", map.get("amount"));
            dataMap.put("10", map.get("passNum"));
            dataMap.put("11", map.get("failedAmount"));
            dataMap.put("12", map.get("failedNum"));
            dataMap.put("13", map.get("provideTime"));
            dataMap.put("14", map.get("contentName"));
            dataMap.put("15", map.get("companyName"));
            dataMap.put("16", map.get("operatorName"));
            dataMap.put("17", map.get("reviewName"));
            dataMap.put("18", map.get("fileName"));
            data.add(sortMapByKey(dataMap));
        }
        /**
         * 6-->调用郭桐宁封装好的方法，把刚才搞得参传进去，用户就可以得到一个想要的Excel表格
         */
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }

    /**
     * 批次交易结果查询---导出批次明细
     *
     * @throws Exception
     */
    @RequestMapping(value = "/user/userCommissionDataExport")
    public void userCommissionDataExport(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String batchId = request.getParameter("batchId");
        // 用户名
        String userName = request.getParameter("userName");
        // 身份证号
        String certId = request.getParameter("certId");
        // 收款账号
        String account = request.getParameter("account");
        String amount = request.getParameter("amount");
        // 金额
        amount = ArithmeticUtil.formatDecimals(amount);
        // 订单状态
        String status = request.getParameter("status");
        Map<String, Object> param = new HashMap<>(15);
        param.put("batchId", batchId);
        param.put("userName", userName);
        param.put("certId", certId);
        param.put("account", account);
        param.put("amount", amount);
        param.put("status", status);
        logger.info("批次交易明细结果数据导出 params:{}", param);
        List<UserCommission> commissionList = commissionService.commissionResultQuery(param);
        List<Map<String, Object>> data = new ArrayList<>();
        String filename = "批次交易明细统计表";
        String[] colunmName = new String[]{"订单ID", "收款人姓名", "证件类型", "证件号", "手机号", "收款账号","交易金额"
                , "到账金额", "服务费率", "服务费", "补差价交易金额", "补差价服务费"
                , "服务费计算规则", "订单状态", "状态描述", "订单备注", "账号所属金融机构", "交易结果描述"
                , "最后更新时间"};
        for (UserCommission userCommission : commissionList) {
            Map<String, Object> dataMap = new HashMap<>(20);
            // 订单ID
            dataMap.put("1", userCommission.getOrderNo());
            // 收款人姓名
            dataMap.put("2", userCommission.getUserName());
            //证件类型
            dataMap.put("3", CertType.codeOf(userCommission.getDocumentType()).getDesc());
            // 证件号
            dataMap.put("4", userCommission.getCertId());
            dataMap.put("5", userCommission.getPhoneNo());
            // 收款账号
            dataMap.put("6", userCommission.getAccount());
            // 交易金额
            dataMap.put("7", userCommission.getSourceAmount());
            //到账金额
            dataMap.put("8", userCommission.getAmount());
            // 服务费率
            dataMap.put("9", userCommission.getCalculationRates());
            // 服务费
            dataMap.put("10", userCommission.getSumFee());
            //补差价交易金额
            dataMap.put("11", userCommission.getSupplementAmount());
            //补差价服务费
            dataMap.put("12", userCommission.getSupplementFee());
            //服务费计算规则
            dataMap.put("13", userCommission.getFeeRuleType());
            //订单状态
            dataMap.put("14", CommissionStatus.codeOf(userCommission.getStatus()).getDesc());
            // 订单状态说明
            dataMap.put("15", userCommission.getStatusDesc());
            // 订单备注
            dataMap.put("16", userCommission.getSourceRemark());
            // 账号所属金融机构
            dataMap.put("17", userCommission.getBankName());
            // 交易结果描述
            dataMap.put("18", userCommission.getStatusDesc());
            // 最后更新时间
            dataMap.put("19", userCommission.getUpdatetime());
            Map<String, Object> sortMapByKey = sortMapByKey(dataMap);
            data.add(sortMapByKey);
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }

    /**
     * 批次交易明细信息查询---导出
     *
     * @throws Exception
     */
    @RequestMapping(value = "/user/batchCommissionDataExport")
    public void batchCommissionDataExport(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String originalId = (String) request.getSession().getAttribute("customkey");
        String userName = request.getParameter("userName");
        String batchName = request.getParameter("batchName");
        String menuId = request.getParameter("menuId");
        String certId = request.getParameter("certId");
        String createTimeStart = request.getParameter("createTimeStart");
        String createTimeEnd = request.getParameter("createTimeEnd");
        String batchDesc = request.getParameter("batchDesc");
        String contentName = request.getParameter("contentName");
        String account = request.getParameter("account");
        String payType = request.getParameter("payType");
        String companyId = request.getParameter("companyId");
        String status = request.getParameter("status");
        String amountStart = request.getParameter("amountStart");
        String amountEnd = request.getParameter("amountEnd");
        String menuIds = "";
        menuIds = getMenuIds(menuId, menuIds);
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("originalId", originalId);
        param.put("userName", userName);
        param.put("batchName", batchName);
        param.put("menuIds", menuIds);
        param.put("certId", certId);
        param.put("createTimeStart", createTimeStart);
        param.put("createTimeEnd", createTimeEnd);
        param.put("batchDesc", batchDesc);
        param.put("contentName", contentName);
        param.put("account", account);
        param.put("payType", payType);
        param.put("companyId", companyId);
        param.put("status", status);
        param.put("amountStart", amountStart);
        param.put("amountEnd", amountEnd);
        logger.info("批次明细交易数据导出 params:" + param);
        List<UserCommission> commissionDetailResult = commissionService.commissionDetailResult(param);
        List<Map<String, Object>> data = new ArrayList<>();
        String[] colunmName = new String[]{"项目名称", "订单ID", "收款人姓名", "证件类型", "证件号", "手机号", "收款账号","交易金额", "到账金额"
                , "服务费率", "服务费(包含补差价)", "补差价交易金额", "补差价服务费", "服务费计算规则", "订单状态", "状态描述", "订单备注"
                , "下发通道", "服务公司", "账号所属金融机构", "交易时间", "批次名称", "批次说明"
                , "交易结果描述", "最后更新时间"};
        String filename = "批次明细信息交易统计表";
        for (UserCommission userCommission : commissionDetailResult) {
            Map<String, Object> dataMap = new HashMap<>();
            // 项目名称
            dataMap.put("1", userCommission.getContentName());
            // 订单ID
            dataMap.put("2", userCommission.getOrderNo());
            // 收款人姓名
            dataMap.put("3", userCommission.getUserName());
            dataMap.put("4", CertType.codeOf(userCommission.getDocumentType()).getDesc());
            // 证件号
            dataMap.put("5", userCommission.getCertId());
            dataMap.put("6", userCommission.getPhoneNo());
            // 收款账号
            dataMap.put("7", userCommission.getAccount());
            // 交易金额
            dataMap.put("8", userCommission.getSourceAmount());
            //到账金额
            dataMap.put("9", userCommission.getAmount());
            // 服务费
            dataMap.put("10", userCommission.getCalculationRates());
            dataMap.put("11", userCommission.getSumFee());
            dataMap.put("12", userCommission.getSupplementAmount());
            dataMap.put("13", userCommission.getSupplementFee());
            //服务费计算规则
            dataMap.put("14", userCommission.getFeeRuleType());
            dataMap.put("15", CommissionStatus.codeOf(userCommission.getStatus()).getDesc());
            // 状态描述
            dataMap.put("16", userCommission.getStatusDesc());
            // 订单备注
            dataMap.put("17", userCommission.getRemark());
            dataMap.put("18", PayType.codeOf(userCommission.getPayType()).getDesc());
            // 服务公司
            dataMap.put("19", userCommission.getCompanyName());
            // 账号所属金融机构
            dataMap.put("20", userCommission.getBankName());
            // 交易时间
            dataMap.put("21", userCommission.getCreatetime());
            // 批次名称
            dataMap.put("22", userCommission.getBatchName());
            // 批次说明
            dataMap.put("23", userCommission.getBatchDesc());
            // 交易结果描述
            dataMap.put("24", userCommission.getStatusDesc());
            // 最后更新时间
            dataMap.put("25", userCommission.getUpdatetime());
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }

    @RequestMapping(value = "/user/new/batchCommissionDataExport")
    public void batchCommissionDataExportNew(HttpServletRequest request, HttpServletResponse response,
                                             @RequestParam(required = false, defaultValue = "0") Integer nodeId,
                                             @RequestParam(required = false, defaultValue = "0") Integer customType) throws Exception {

        String customKeyStr = "";
        List<String> customKeyList = organizationTreeService.queryNodeCusotmKey(customType, "G", nodeId);
        for (String ckey : customKeyList) {
            customKeyStr = customKeyStr + "," + ckey;
        }
        if (customKeyStr.lastIndexOf(",") >= 0) {
            customKeyStr = customKeyStr.substring(1);
        }

        String operatorName = null;
        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
        if (customLogin.getLoginRole() == 2) {//如果为操作员，只能查看自己数据
            operatorName = customLogin.getUsername();
        }

        String userName = request.getParameter("userName");
        String batchName = request.getParameter("batchName");
        String menuId = request.getParameter("menuId");
        String certId = request.getParameter("certId");
        String createTimeStart = request.getParameter("createTimeStart");
        String createTimeEnd = request.getParameter("createTimeEnd");
        String batchDesc = request.getParameter("batchDesc");
        String contentName = request.getParameter("contentName");
        String account = request.getParameter("account");
        String payType = request.getParameter("payType");
        String companyId = request.getParameter("companyId");
        String status = request.getParameter("status");
        String amountStart = request.getParameter("amountStart");
        String amountEnd = request.getParameter("amountEnd");
        String menuIds = "";
        menuIds = getMenuIds(menuId, menuIds);
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("originalId", customKeyStr);
        param.put("userName", userName);
        param.put("batchName", batchName);
        param.put("menuIds", menuIds);
        param.put("certId", certId);
        param.put("createTimeStart", createTimeStart);
        param.put("createTimeEnd", createTimeEnd);
        param.put("batchDesc", batchDesc);
        param.put("contentName", contentName);
        param.put("account", account);
        param.put("payType", payType);
        param.put("companyId", companyId);
        param.put("status", status);
        param.put("operatorName", operatorName);
        param.put("amountStart", amountStart);
        param.put("amountEnd", amountEnd);
        logger.info("批次明细交易数据导出 params:" + param);
        List<UserCommission> commissionDetailResult = commissionService.commissionDetailResult2(param);
        List<Map<String, Object>> data = new ArrayList<>();
        String[] colunmName = new String[]{"项目名称", "订单ID", "收款人姓名", "证件类型", "证件号", "手机号", "收款账号", "交易金额"
                , "服务费率", "服务费(包含补差价)", "补差价交易金额", "补差价服务费", "服务费计算规则", "订单状态", "状态描述", "订单备注"
                , "下发通道", "服务公司", "账号所属金融机构", "交易时间", "批次名称", "批次说明"
                , "交易结果描述", "最后更新时间"};
        String filename = "批次明细信息交易统计表";
        for (UserCommission userCommission : commissionDetailResult) {
            Map<String, Object> dataMap = new HashMap<>();
            // 项目名称
            dataMap.put("1", userCommission.getContentName());
            // 订单ID
            dataMap.put("2", userCommission.getOrderNo());
            // 收款人姓名
            dataMap.put("3", userCommission.getUserName());
            dataMap.put("4", CertType.codeOf(userCommission.getDocumentType()).getDesc());
            // 证件号
            dataMap.put("5", userCommission.getCertId());
            dataMap.put("6", userCommission.getCertId());
            // 收款账号
            dataMap.put("7", userCommission.getAccount());
            // 交易金额
            dataMap.put("8", userCommission.getAmount());
            // 服务费
            dataMap.put("9", userCommission.getCalculationRates());
            dataMap.put("10", userCommission.getSumFee());
            dataMap.put("11", userCommission.getSupplementAmount());
            dataMap.put("12", userCommission.getSupplementFee());
            //服务费计算规则
            dataMap.put("13", userCommission.getFeeRuleType());
            dataMap.put("14", CommissionStatus.codeOf(userCommission.getStatus()).getDesc());
            // 状态描述
            dataMap.put("15", userCommission.getStatusDesc());
            // 订单备注
            dataMap.put("16", userCommission.getRemark());
            dataMap.put("17", PayType.codeOf(userCommission.getPayType()).getDesc());
            // 服务公司
            dataMap.put("18", userCommission.getCompanyName());
            // 账号所属金融机构
            dataMap.put("19", userCommission.getBankName());
            // 交易时间
            dataMap.put("20", userCommission.getCreatetime());
            // 批次名称
            dataMap.put("21", userCommission.getBatchName());
            // 批次说明
            dataMap.put("22", userCommission.getBatchDesc());
            // 交易结果描述
            dataMap.put("23", userCommission.getStatusDesc());
            // 最后更新时间
            dataMap.put("24", userCommission.getUpdatetime());
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }

    /**
     * 批次交易明细信息查询（超级管理员）---导出
     *
     * @throws Exception
     */
    @RequestMapping(value = "/user/root/batchCommissionDataExport")
    public void rootBatchCommissionDataExport(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String userName = request.getParameter("userName");
        String batchName = request.getParameter("batchName");
        String certId = request.getParameter("certId");
        String createTimeStart = request.getParameter("createTimeStart");
        String createTimeEnd = request.getParameter("createTimeEnd");
        String batchDesc = request.getParameter("batchDesc");
        String contentName = request.getParameter("contentName");
        String account = request.getParameter("account");
        String payType = request.getParameter("payType");
        String companyId = request.getParameter("companyId");
        String status = request.getParameter("status");
        String amountStart = request.getParameter("amountStart");
        String amountEnd = request.getParameter("amountEnd");
        String customName = request.getParameter("customName");
        Map<String, Object> param = new HashMap<>(20);
        param.put("userName", userName);
        param.put("batchName", batchName);
        param.put("certId", certId);
        param.put("createTimeStart", createTimeStart);
        param.put("createTimeEnd", createTimeEnd);
        param.put("batchDesc", batchDesc);
        param.put("contentName", contentName);
        param.put("account", account);
        param.put("payType", payType);
        param.put("companyId", companyId);
        param.put("status", status);
        param.put("amountStart", amountStart);
        param.put("amountEnd", amountEnd);
        param.put("customName", customName);
        logger.info("超管查询数据导出 params:" + param);
        List<UserCommission> commissionDetailResult = commissionService.commissionDetailResult(param);
        List<Map<String, Object>> data = new ArrayList<>();
        String[] colunmName = new String[]{"商户名称", "项目名称", "订单ID", "收款人姓名", "证件类型"
                , "证件号", "手机号", "收款账号", "交易金额","到账金额", "服务费率", "服务费(包含补差价)"
                , "补差价交易金额", "补差价服务费", "服务费计算规则", "订单状态", "订单备注"
                , "下发通道", "服务公司", "完税服务公司", "账号所属金融机构", "交易时间", "批次名称"
                , "批次说明", "订单状态描述", "业务所属客户经理","业务所属运营经理","业务所属平台", "业务所属渠道", "商户标签", "最后更新时间"};
        String filename = "批次明细信息交易统计表";
        for (UserCommission userCommission : commissionDetailResult) {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("0", userCommission.getCustomName());// 商户名称
            dataMap.put("1", userCommission.getContentName());// 项目名称
            dataMap.put("2", userCommission.getOrderNo());// 订单ID
            dataMap.put("3", userCommission.getUserName());// 收款人姓名
            dataMap.put("4", CertType.codeOf(userCommission.getDocumentType()).getDesc());// 证件类型
            dataMap.put("5", userCommission.getCertId());// 证件号
            dataMap.put("6", userCommission.getPhoneNo());// 证件号
            dataMap.put("7", userCommission.getAccount());// 收款账号
            dataMap.put("8", userCommission.getSourceAmount());// 交易金额
            dataMap.put("9", userCommission.getAmount());// 到账金额
            dataMap.put("10", userCommission.getCalculationRates());// 服务费率
            dataMap.put("11", userCommission.getSumFee());// 服务费
            dataMap.put("12", userCommission.getSupplementAmount());// 补差价交易金额
            dataMap.put("13", userCommission.getSupplementFee());// 补差价服务费
            dataMap.put("14", userCommission.getFeeRuleType());// 服务费计算规则
            dataMap.put("15", CommissionStatus.codeOf(userCommission.getStatus()).getDesc());// 订单状态
            dataMap.put("16", userCommission.getSourceRemark());// 订单备注
            dataMap.put("17", PayType.codeOf(userCommission.getPayType()).getDesc());// 下发通道
            dataMap.put("18", userCommission.getCompanyName());// 服务公司
            dataMap.put("19", userCommission.getRealCompanyName());// 完税服务公司
            dataMap.put("20", userCommission.getBankName());// 账号所属金融机构
            dataMap.put("21", userCommission.getCreatetime());// 交易时间
            dataMap.put("22", userCommission.getBatchName());// 批次名称
            dataMap.put("23", userCommission.getBatchDesc());// 批次说明
            dataMap.put("24", userCommission.getStatusDesc());// 交易结果描述
            dataMap.put("25", userCommission.getBusinessManager());// 业务所属客户经理
            dataMap.put("26", userCommission.getOperationsManager());// 业务所属运营经理
            dataMap.put("27", userCommission.getBusinessPlatform());// 业务所属平台
            dataMap.put("28", userCommission.getBusinessChannel());// 业务所属渠道
            dataMap.put("29", userCommission.getCustomLabel());// 商户标签
            dataMap.put("30", userCommission.getUpdatetime());// 最后更新时间
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }

    /**
     * 收款用户交易统计页面---用户交易统计
     *
     * @throws Exception
     */
    @RequestMapping(value = "/user/dealRecordDataExceport")
    public void dealRecordDataExceport(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String customkey = (String) request.getSession().getAttribute("customkey");
        String certId = request.getParameter("certId");// 身份证号
        String batchId = request.getParameter("batchId");// 批次编号
        String userName = request.getParameter("userName");// 用户姓名
        String tradeTimeStart = request.getParameter("tradeTimeStart");// 交易时间开始
        String tradeTimeEnd = request.getParameter("tradeTimeEnd");// 交易时间结束
        String batchDesc = request.getParameter("batchDesc");// 批次说明
        String payType = request.getParameter("payType");// 下发通道
        String companyId = request.getParameter("companyId");// 服务公司key
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("customkey", customkey);
        param.put("certId", StringUtils.isEmpty(certId) == false ? certId : "");
        param.put("batchId", StringUtils.isEmpty(batchId) == false ? batchId : "");
        param.put("userName", StringUtils.isEmpty(userName) == false ? userName : "");
        param.put("tradeTimeStart", StringUtils.isEmpty(tradeTimeStart) == false ? tradeTimeStart : "");
        param.put("tradeTimeEnd", StringUtils.isEmpty(tradeTimeEnd) == false ? tradeTimeEnd : "");
        param.put("batchDesc", StringUtils.isEmpty(batchDesc) == false ? batchDesc : "");
        param.put("payType", StringUtils.isEmpty(payType) == false ? Integer.parseInt(payType) : null);
        param.put("companyId", StringUtils.isEmpty(companyId) == false ? companyId : "");
        List<UserCommission> userDealRecord = commissionService.getUserDealRecord(param);
        List<Map<String, Object>> data = new ArrayList<>();
        String[] colunmName = new String[]{"用户证件号", "用户姓名", "手机号", "证件类型", "成功交易总金额", "成功交易总笔数", "总服务费", "用户创建时间"};
        String filename = "用户交易统计表";
        for (UserCommission userCommission : userDealRecord) {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("1", userCommission.getCertId());// 用户证件号
            dataMap.put("2", userCommission.getUserName());// 用户姓名
            dataMap.put("3", userCommission.getPhoneNo());// 用户姓名
            dataMap.put("4",
                    userCommission.getDocumentType() == 1 ? "身份证"
                            : userCommission.getDocumentType() == 2 ? "港澳台通行证"
                            : userCommission.getDocumentType() == 3 ? "护照"
                            : userCommission.getDocumentType() == 4 ? "军官证" : "无法识别");// 证件类型
            dataMap.put("5", userCommission.getAmount());// 成功交易总金额
            dataMap.put("6", userCommission.getPassNum());// 成功交易总笔数
            dataMap.put("7", userCommission.getSumFee());// 总服务费
            dataMap.put("8", userCommission.getCreatetime());// 用户创建时间
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }

    /**
     * 收款用户交易统计页面---用户交易统计详情
     *
     * @throws Exception
     */
    @RequestMapping(value = "/user/dealDetailDataExceport")
    public void dealDetailDataExceport(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<String, Object>();
        result.put(RespCode.RESP_STAT, respstat);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
        String customkey = (String) request.getSession().getAttribute("customkey");
        String userId = request.getParameter("userId");// 用户id
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("customkey", customkey);
        param.put("userId", userId);
        List<UserCommission> userDealDetail = commissionService.getUserDealDetail(param);
        List<Map<String, Object>> data = new ArrayList<>();
        String filename = "用户交易明细统计表";
        String[] colunmName = new String[]{"订单ID", "收款人姓名", "证件类型", "证件号", "手机号", "收款账号", "交易金额", "服务费率", "服务费(包含补差价)", "补差价交易金额", "补差价服务费", "服务费计算公式", "订单状态", "订单备注",
                "账号所属金融机构", "下发通道", "服务公司", "交易时间", "项目名称", "批次名称", "批次说明", "最后更新时间"};
        for (UserCommission userCommission : userDealDetail) {
            Map<String, Object> dataMap = new HashMap<>(40);
            dataMap.put("1", userCommission.getOrderNo());// 订单ID
            dataMap.put("2", userCommission.getUserName());// 收款人姓名
            dataMap.put("3",
                    userCommission.getDocumentType() == 1 ? "身份证"
                            : userCommission.getDocumentType() == 2 ? "港澳台通行证"
                            : userCommission.getDocumentType() == 3 ? "护照"
                            : userCommission.getDocumentType() == 4 ? "军官证" : "无法识别");// 证件类型
            dataMap.put("4", userCommission.getCertId());// 证件号
            dataMap.put("5", userCommission.getPhoneNo());// 证件号
            dataMap.put("6", userCommission.getAccount());// 收款账号
            dataMap.put("7", userCommission.getAmount());// 交易金额
            dataMap.put("8", userCommission.getCalculationRates());// 服务费率
            dataMap.put("9", userCommission.getSumFee());// 服务费
            dataMap.put("10", userCommission.getSupplementAmount());// 补差价交易金额
            dataMap.put("11", userCommission.getSupplementFee());// 补差价服务费
            dataMap.put("12", userCommission.getFeeRuleType());// 服务费计算公式
            dataMap.put("13",
                    userCommission.getStatus() == 1 ? "发放成功" : userCommission.getStatus() == 2 ? "发放失败" : "待处理");// 订单状态
            dataMap.put("14", userCommission.getStatusDesc());// 订单备注
            dataMap.put("15", userCommission.getBankName());// 账号所属金融机构
            dataMap.put("16", userCommission.getPayType() == 1 ? "银行电子户"
                    : userCommission.getPayType() == 2 ? "支付宝" : userCommission.getPayType() == 3 ? "微信" : "银行卡");// 下发通道
            dataMap.put("17", userCommission.getCompanyName());// 服务公司
            dataMap.put("18", userCommission.getCreatetime());// 交易时间
            dataMap.put("19", userCommission.getContentName());// 项目名称
            dataMap.put("20", userCommission.getBatchName());// 批次名称
            dataMap.put("21", userCommission.getBatchDesc());// 批次说明
            dataMap.put("22", userCommission.getUpdatetime());// 最后更新时间
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }

    /**
     * 交易统计---项目维度导出
     *
     * @throws Exception
     */
    @RequestMapping("/commissionByContentExport")
    public void commissionByContentExport(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<>(5);
        result.put(RespCode.RESP_STAT, respstat);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
        String originalId = (String) request.getSession().getAttribute("customkey");
        String batchName = request.getParameter("batchName");
        String menuId = request.getParameter("menuId");
        String createTimeStart = request.getParameter("createTimeStart");
        String createTimeEnd = request.getParameter("createTimeEnd");
        String batchDesc = request.getParameter("batchDesc");
        String contentName = request.getParameter("contentName");
        String payType = request.getParameter("payType");
        String companyId = request.getParameter("companyId");
        try {
            String menuIds = "";
            menuIds = getMenuIds(menuId, menuIds);
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("originalId", originalId);
            param.put("batchName", batchName);
            param.put("menuIds", menuIds);
            param.put("tradeTimeStart", createTimeStart);
            param.put("tradeTimeEnd", createTimeEnd);
            param.put("batchDesc", batchDesc);
            param.put("contentName", contentName);
            param.put("payType", payType);
            param.put("companyId", companyId);
            List<UserCommission> list = commissionService.commissionByMemuResult(param);
            String today = DateUtils.getNowDay();
            ArrayList<String> dataStr = new ArrayList<String>();
            for (int i = 0; i < list.size(); i++) {
                UserCommission commission = list.get(i);
                StringBuffer strBuff = new StringBuffer();

                strBuff.append(commission.getMenuId()).append(",")
                        .append(commission.getContentName() == null ? "" : commission.getContentName()).append(",")
                        .append(commission.getAmount() == null ? "" : commission.getAmount()).append(",")
                        .append(commission.getPassNum() == null ? "" : commission.getPassNum()).append(",")
                        .append(commission.getSumFee() == null ? "" : commission.getSumFee())
                        .append(",").append(commission.getBatchNum() == null ? "" : commission.getBatchNum()).append(",")
                        .append(commission.getUserNum() == null ? "" : commission.getUserNum());

                dataStr.add(strBuff.toString());
            }
            ArrayList<String> fieldName = new ArrayList<>();
            fieldName.add("项目编号");
            fieldName.add("项目名称");
            fieldName.add("成功交易总金额");
            fieldName.add("成功交易总笔数");
            fieldName.add("总服务费");
            fieldName.add("总批次数");
            fieldName.add("总用户数");
            fieldName.add("到账时间");
            String filename = today + "项目交易统计";
            ExcelFileGenerator.exportExcel(response, fieldName, dataStr, filename);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 交易统计---项目维度--详情
     *
     * @throws Exception
     */
    @RequestMapping(value = "/commissionDetailByContentExport")
    public void commissionDetailByContentExport(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<String, Object>();
        result.put(RespCode.RESP_STAT, respstat);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
        String originalId = (String) request.getSession().getAttribute("customkey");
        String userName = request.getParameter("userName");
        String menuId = request.getParameter("menuId");
        String certId = request.getParameter("certId");
        String account = request.getParameter("account");
        String payType = request.getParameter("payType");
        String batchDesc = request.getParameter("batchDesc");
        String amount = request.getParameter("amount");
        String batchName = request.getParameter("batchName");
        String createTimeStart = request.getParameter("createTimeStart");
        String createTimeEnd = request.getParameter("createTimeEnd");
        String contentName = request.getParameter("contentName");
        String companyId = request.getParameter("companyId");
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("originalId", originalId);
        param.put("menuId", menuId);
        param.put("userName", userName);
        param.put("certId", certId);
        param.put("account", account);
        param.put("payType", payType);
        param.put("batchDesc", batchDesc);
        param.put("amount", amount);
        param.put("batchName", batchName);
        param.put("tradeTimeStart", createTimeStart);
        param.put("tradeTimeEnd", createTimeEnd);
        param.put("contentName", contentName);
        param.put("companyId", companyId);
        List<UserCommission> list = commissionService.commissionByMemuDetail(param);
        String today = DateUtils.getNowDay();
        ArrayList<String> dataStr = new ArrayList<String>();
        for (int i = 0; i < list.size(); i++) {
            UserCommission commission = list.get(i);
            StringBuffer strBuff = new StringBuffer();

            int documentType = commission.getDocumentType();
            String documentDesc = "";
            if (documentType == 1) {
                documentDesc = "身份证号码";
            } else if (documentType == 2) {
                documentDesc = "港澳台通行证";
            } else if (documentType == 3) {
                documentDesc = "军官证";
            } else if (documentType == 4) {
                documentDesc = "护照";
            }

            int paytype = commission.getPayType();
            String payTypeDesc = "";
            if (paytype == 1) {
                payTypeDesc = "银行电子户";
            } else if (paytype == 2) {
                payTypeDesc = "支付宝";
            } else if (paytype == 3) {
                payTypeDesc = "微信";
            } else if (paytype == 4) {
                payTypeDesc = "银行卡";
            }

            strBuff.append(commission.getContentName() == null ? "" : commission.getContentName()).append(",")
                    .append(commission.getOrderNo() == null ? "" : commission.getOrderNo()).append(",")
                    .append(commission.getUserName() == null ? "" : commission.getUserName()).append(",")
                    .append(documentDesc).append(",")
                    .append(commission.getCertId() == null ? "" : commission.getCertId()).append(",")
                    .append(commission.getPhoneNo() == null ? "" : commission.getPhoneNo()).append(",")
                    .append(commission.getAccount() == null ? "" : commission.getAccount()).append(",")
                    .append(commission.getSourceAmount() == null ? "" : commission.getSourceAmount()).append(",")
                    .append(commission.getAmount() == null ? "" : commission.getAmount()).append(",")
                    .append(commission.getCalculationRates() == null ? "" : commission.getCalculationRates()).append(",")
                    .append(commission.getSumFee() == null ? "" : commission.getSumFee()).append(",")
                    .append(commission.getSupplementAmount() == null ? "" : commission.getSupplementAmount()).append(",")
                    .append(commission.getSupplementFee() == null ? "" : commission.getSupplementFee()).append(",")
                    .append(commission.getFeeRuleType() == null ? "" : commission.getFeeRuleType()).append(",")
                    .append(commission.getStatusDesc() == null ? "" : commission.getStatusDesc()).append(",")
                    .append(commission.getRemark() == null ? "" : commission.getRemark()).append(",")
                    .append(payTypeDesc).append(",")
                    .append(commission.getCompanyName() == null ? "" : commission.getCompanyName()).append(",")
                    .append(commission.getBankName() == null ? "" : commission.getBankName()).append(",")
                    .append(commission.getCreatetime() == null ? "" : commission.getCreatetime()).append(",")
                    .append(commission.getBatchName() == null ? "" : commission.getBatchName()).append(",")
                    .append(commission.getBatchDesc() == null ? "" : commission.getBatchDesc()).append(",")
                    .append(commission.getUpdatetime() == null ? "" : commission.getUpdatetime());

            dataStr.add(strBuff.toString());
        }
        ArrayList<String> fieldName = new ArrayList<String>();
        fieldName.add("项目名称");
        fieldName.add("订单ID");
        fieldName.add("收款人姓名");
        fieldName.add("证件人类型");
        fieldName.add("证件号");
        fieldName.add("手机号");
        fieldName.add("收款账号");
        fieldName.add("交易金额");
        fieldName.add("到账金额");
        fieldName.add("服务费率");
        fieldName.add("服务费(包含补差价)");
        fieldName.add("补差价交易金额");
        fieldName.add("补差价服务费");
        fieldName.add("服务费计算规则");
        fieldName.add("订单状态");
        fieldName.add("订单备注");
        fieldName.add("下发通道");
        fieldName.add("服务公司");
        fieldName.add("账户所属金融机构");
        fieldName.add("交易时间");
        fieldName.add("批次名称");
        fieldName.add("批次说明");
        fieldName.add("最后更新时间");
        String filename = today + "项目交易明细统计";
        ExcelFileGenerator.exportExcel(response, fieldName, dataStr, filename);
    }

    /**
     * 批次数据准备--下载批次校验失败或成功文件
     *
     * @throws Exception
     */
    @RequestMapping(value = "/validateFailFile")
    public void validateFailFile(HttpServletRequest request, HttpServletResponse response) {
        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<>(5);
        result.put(RespCode.RESP_STAT, respstat);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
        String batchId = request.getParameter("batchId");
        String status = request.getParameter("status");
        Map<String, Object> param = new HashMap<>(10);
        param.put("status", status);
        param.put("batchId", batchId);
        List<CommissionTemporary> list = batchService2.getCommissionedByParam(param);
        List<Map<String, Object>> data = new ArrayList<>();
        String name = Integer.parseInt(status) == 1 ? "成功" : "失败";
        String filename = "批次校验" + name + "文件";
        String[] colunmName = new String[]{"收款人姓名", "证件类型", "证件号", "手机号", "收款账号", "所属金融机构", "交易金额", "备注", "校验描述"};
        for (CommissionTemporary temporary : list) {
            Map<String, Object> dataMap = new HashMap<>(15);
            dataMap.put("1", temporary.getUserName());
            dataMap.put("2", CertType.codeOf(temporary.getDocumentType()).getDesc());
            dataMap.put("3", temporary.getIdCard());
            dataMap.put("4", temporary.getPhoneNo());
            dataMap.put("5", temporary.getBankCardNo());
            dataMap.put("6", temporary.getBankName());
            dataMap.put("7", temporary.getAmount());
            dataMap.put("8", temporary.getRemark());
            dataMap.put("9", temporary.getStatusDesc());
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }

    /**
     * 银企直连交易记录导出
     *
     * @throws Exception
     */
    @RequestMapping(value = "/ExportYQData")
    public void ExportYQData(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<String, Object>();
        result.put(RespCode.RESP_STAT, respstat);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
        String timeStart = request.getParameter("timeStart");
        String timeEnd = request.getParameter("timeEnd");
        String pageNo = request.getParameter("pageNo");
        List<Map<String, Object>> data = new ArrayList<>();
        String filename = "银企直连交易记录";
        String[] colunmName = new String[]{"付款方户名", "付款方账号", "付款行名称", "交易金额", "账户余额", "摘要", "用途", "收款方户名",
                "收款方账号", "收款方行名", "银行记账日期", "交易时间", "凭证号"};
        List<TransHistoryRecord> queryTransHistoryPage = pinganBankService.queryTransHistoryPage(timeStart, timeEnd,
                Integer.parseInt(pageNo));

        for (TransHistoryRecord temporary : queryTransHistoryPage) {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("2", temporary.getOutAccountName());
            dataMap.put("3", temporary.getOutAccountNo());
            dataMap.put("4", temporary.getOutAccountBankName());
            dataMap.put("5", temporary.getTransAmount());
            dataMap.put("6", temporary.getAccountBalance());
            dataMap.put("7", temporary.getRemark());
            dataMap.put("8", temporary.getPurpose());
            dataMap.put("9", temporary.getInAccountName());
            dataMap.put("10", temporary.getInAccountNo());
            dataMap.put("11", temporary.getInAccountBankName());
            dataMap.put("12", temporary.getAccountingDate());
            dataMap.put("13", temporary.getTransTime());
            dataMap.put("14", temporary.getBankTransSerialNo());
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }

    /**
     * 批次明细复核导出
     *
     * @throws Exception
     */
    @RequestMapping(value = "/exportReviewedCommission")
    public void exportReviewedCommission(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String customkey = (String) request.getSession().getAttribute("customkey");
        // 批次号
        String orderNo = request.getParameter("orderNo");
        // 姓名
        String userName = request.getParameter("userName");
        // 证件号
        String certId = request.getParameter("certId");
        // 金额
        String amount = request.getParameter("amount");
        // 订单状态
        String status = request.getParameter("status");
        // 订单状态
        String account = request.getParameter("account");
        List<Map<String, Object>> data = new ArrayList<>();
        String filename = "批次明细复核统计表";
        String[] colunmName = new String[]{"订单ID", "收款人姓名", "证件类型", "证件号", "手机号", "收款账号","交易金额", "到账金额", "服务费率",
                "服务费(包含补差价)", "补差价交易金额", "补差价服务费", "服务费计算规则", "订单状态", "状态描述",
                "订单备注", "账号所属金融机构", "最后更新时间"};
        Map<String, Object> param = new HashMap<>(15);
        param.put("customkey", customkey);
        param.put("batchId", orderNo);
        param.put("userName", userName);
        param.put("idCard", certId);
        param.put("amount", amount);
        param.put("account", account);
        param.put("status", status);
        List<CommissionTemporary> commissionedByParam = channelInterimBatchService2.getCommissionedByParam(param);
        for (CommissionTemporary temporary : commissionedByParam) {
            Map<String, Object> dataMap = new HashMap<>(15);
            dataMap.put("1", temporary.getOrderNo());
            dataMap.put("2", temporary.getUserName());
            dataMap.put("3", CertType.codeOf(temporary.getDocumentType()).getDesc());
            dataMap.put("4", temporary.getIdCard());
            dataMap.put("5", temporary.getPhoneNo());
            dataMap.put("6", temporary.getBankCardNo());
            dataMap.put("7", temporary.getSourceAmount());
            dataMap.put("8", temporary.getAmount());
            //本次服务费率
            dataMap.put("9", temporary.getCalculationRates());
            //服务费（包含补差价）
            dataMap.put("10", temporary.getSumFee());
            dataMap.put("11", temporary.getSupplementAmount());
            dataMap.put("12", temporary.getSupplementFee());
            int status2 = temporary.getStatus();
            dataMap.put("13", temporary.getFeeRuleType());
            dataMap.put("14", status2 == 1 ? "验证通过" : status2 == 2 ? "验证失败" : status2 == 3 ? "已打款" : "已删除");
            dataMap.put("15", temporary.getStatusDesc());
            dataMap.put("16", temporary.getRemark());
            dataMap.put("17", temporary.getBankName());
            dataMap.put("18", temporary.getUpdateTime());
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }

    @RequestMapping(value = "/new/exportReviewedCommission")
    public void exportReviewedCommissionNew(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 批次号
        String orderNo = request.getParameter("orderNo");
        // 姓名
        String userName = request.getParameter("userName");
        // 证件号
        String certId = request.getParameter("certId");
        // 金额
        String amount = request.getParameter("amount");
        // 订单状态
        String status = request.getParameter("status");
        // 订单状态
        String account = request.getParameter("account");
        List<Map<String, Object>> data = new ArrayList<>();
        String filename = "批次明细复核统计表";
        String[] colunmName = new String[]{"订单ID", "收款人姓名", "证件类型", "证件号", "手机号", "收款账号","交易金额","到账金额", "服务费率",
                "服务费(包含补差价)", "补差价交易金额", "补差价服务费", "服务费计算规则", "订单状态", "状态描述",
                "订单备注", "账号所属金融机构", "最后更新时间"};
        Map<String, Object> param = new HashMap<>(15);
        param.put("batchId", orderNo);
        param.put("userName", userName);
        param.put("idCard", certId);
        param.put("amount", amount);
        param.put("account", account);
        param.put("status", status);
        List<CommissionTemporary> commissionedByParam = channelInterimBatchService2.getCommissionedByParam(param);
        for (CommissionTemporary temporary : commissionedByParam) {
            Map<String, Object> dataMap = new HashMap<>(15);
            dataMap.put("1", temporary.getOrderNo());
            dataMap.put("2", temporary.getUserName());
            dataMap.put("3", CertType.codeOf(temporary.getDocumentType()).getDesc());
            dataMap.put("4", temporary.getIdCard());
            dataMap.put("5", temporary.getPhoneNo());
            dataMap.put("6", temporary.getBankCardNo());
            dataMap.put("7", temporary.getSourceAmount());
            dataMap.put("8", temporary.getAmount());
            //本次服务费率
            dataMap.put("9", temporary.getCalculationRates());
            //服务费（包含补差价）
            dataMap.put("10", temporary.getSumFee());
            dataMap.put("11", temporary.getSupplementAmount());
            dataMap.put("12", temporary.getSupplementFee());
            int status2 = temporary.getStatus();
            dataMap.put("13", temporary.getFeeRuleType());
            dataMap.put("14", status2 == 1 ? "验证通过" : status2 == 2 ? "验证失败" : status2 == 3 ? "已打款" : "已删除");
            dataMap.put("15", temporary.getStatusDesc());
            dataMap.put("16", temporary.getRemark());
            dataMap.put("17", temporary.getBankName());
            dataMap.put("18", temporary.getUpdateTime());
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }

    @RequestMapping(value = "/new/exportBatchReviewedCommission")
    public void exportBatchReviewedCommission(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 批次号
        String orderNo = request.getParameter("batchIds");
        // 姓名
        String userName = request.getParameter("userName");
        // 证件号
        String certId = request.getParameter("certId");
        // 金额
        String amount = request.getParameter("amount");
        // 订单状态
        String status = request.getParameter("status");
        // 订单状态
        String account = request.getParameter("account");
        //商户名称
        String customName = request.getParameter("customName");
        //批次名称
        String batchName = request.getParameter("batchName");
        List<Map<String, Object>> data = new ArrayList<>();
        String filename = "批次明细复核统计表";
        String[] colunmName = new String[]{"商户名称", "批次名称", "批次说明", "订单ID", "收款人姓名", "证件类型", "证件号", "手机号", "收款账号", "交易金额", "服务费率",
                "服务费(包含补差价)", "补差价交易金额", "补差价服务费", "服务费计算规则", "订单状态", "状态描述",
                "订单备注", "账号所属金融机构", "最后更新时间"};
        Map<String, Object> param = new HashMap<>(15);
        param.put("batchIds", orderNo);
        param.put("userName", userName);
        param.put("idCard", certId);
        param.put("amount", amount);
        param.put("account", account);
        param.put("status", status);
        param.put("customName", customName);
        param.put("batchName", batchName);
        List<CommissionTemporary> commissionedByParam = channelInterimBatchService2.getCommissionedByBatchIdsAndParam(param);
        for (CommissionTemporary temporary : commissionedByParam) {
            Map<String, Object> dataMap = new HashMap<>(15);
            dataMap.put("1", temporary.getCustomName());
            dataMap.put("2", temporary.getBatchName());
            dataMap.put("3", temporary.getBatchDesc());
            dataMap.put("4", temporary.getOrderNo());
            dataMap.put("5", temporary.getUserName());
            dataMap.put("6", CertType.codeOf(temporary.getDocumentType()).getDesc());
            dataMap.put("7", temporary.getIdCard());
            dataMap.put("8", temporary.getPhoneNo());
            dataMap.put("9", temporary.getBankCardNo());
            dataMap.put("10", temporary.getAmount());
            //本次服务费率
            dataMap.put("11", temporary.getCalculationRates());
            //服务费（包含补差价）
            dataMap.put("12", temporary.getSumFee());
            dataMap.put("13", temporary.getSupplementAmount());
            dataMap.put("14", temporary.getSupplementFee());
            int status2 = temporary.getStatus();
            dataMap.put("15", temporary.getFeeRuleType());
            dataMap.put("16", status2 == 1 ? "验证通过" : status2 == 2 ? "验证失败" : status2 == 3 ? "已打款" : "已删除");
            dataMap.put("17", temporary.getStatusDesc());
            dataMap.put("18", temporary.getRemark());
            dataMap.put("19", temporary.getBankName());
            dataMap.put("20", temporary.getUpdateTime());
            data.add(sortMapByKey(dataMap));
        }
        ExcelFileGenerator.ExcelExport(response, colunmName, filename, data);
    }


    private String getMenuIds(String menuId, String menuIds) {
        if (!StringUtil.isEmpty(menuId)) {
            Map<String, Object> param = new HashMap<String, Object>();
            CustomMenu customMenuById = customService.getCustomMenuById(Integer.parseInt(menuId));
            if (customMenuById == null) {
                menuIds = menuId;
            } else {
                param.put("levelCode", customMenuById.getLevelCode());
                List<CustomMenu> nodeTree = customService.getNodeTree(param);
                for (CustomMenu customMenu : nodeTree) {
                    menuIds += customMenu.getId() + ",";
                }
            }
        }
        return menuIds;
    }

}

