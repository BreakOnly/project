package com.jrmf.controller.systemrole.merchant.linkconfig;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jrmf.common.CommonString;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.*;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.LinkageTransferRecord;
import com.jrmf.service.LinkageTransferRecordService;
import com.jrmf.utils.ExcelFileGenerator;
import com.jrmf.utils.RespCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/linkageTransfer")
public class LinkageTransferController extends BaseController {

    private static Logger logger = LoggerFactory.getLogger(LinkageTransferController.class);

    @Autowired
    private LinkageTransferRecordService linkageTransferRecordService;

    @PostMapping("/recordList")
    public Map<String, Object> queryList(HttpServletRequest request,
                                         LinkageTransferRecord record,
                                         @RequestParam(required = false, defaultValue = "1") Integer pageNo,
                                         @RequestParam(required = false, defaultValue = "10") Integer pageSize) {

        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);
        if (!isMFKJAccount(customLogin)) {
            return returnFail(RespCode.error101, RespCode.PERMISSIONERROR);
        }

        HashMap<String, Object> result = new HashMap<>();

        try {

            PageHelper.startPage(pageNo, pageSize);
            List<LinkageTransferRecord> list = linkageTransferRecordService.getList(record);
            PageInfo page = new PageInfo(list);

            result.put("total", page.getTotal());
            result.put("list", page.getList());

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return returnFail(RespCode.error101, RespCode.CONNECTION_ERROR);
        }

        return returnSuccess(result);
    }


    @PostMapping("/recordList/export")
    public void export(HttpServletRequest request,
                       HttpServletResponse response,
                       LinkageTransferRecord record) {

        ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute(CommonString.CUSTOMLOGIN);

        try {


            if (!isMFKJAccount(customLogin)) {
                response.setCharacterEncoding("utf-8");
                response.setContentType("application/json; charset=utf-8");
                PrintWriter writer = response.getWriter();
                writer.write(returnFail(RespCode.error101, RespCode.PERMISSIONERROR).toString());
            }

            // 标题
            String[] headers = new String[]{"商户名称", "业务类型", "状态", "状态描述", "交易金额", "交易时间",
                    "付款账户", "付款账号", "付款银行", "交易通道", "收款账户", "收款账号", "收款银行", "交易备注", "业务订单号",
                    "调用外部订单号", "外部系统订单号", "商户key", "更新时间"};
            String filename = "联动交易登记记录";


            //分页查询资金出入金记录信息
            List<LinkageTransferRecord> list = linkageTransferRecordService.getList(record);

            List<Map<String, Object>> data = new ArrayList<>();
            for (LinkageTransferRecord transferRecord : list) {
                Map<String, Object> dataMap = new HashMap<>(20);
                dataMap.put("1", transferRecord.getCustomName());
                dataMap.put("2", LinkageTranType.codeOf(transferRecord.getTranType()));
                dataMap.put("3", LinkageTranStatus.codeOf(transferRecord.getStatus()));
                dataMap.put("4", transferRecord.getStatusDesc());
                dataMap.put("5", transferRecord.getTranAmount());
                dataMap.put("6", transferRecord.getTranTime());
                dataMap.put("7", transferRecord.getPayAccountName());
                dataMap.put("8", transferRecord.getPayAccountNo());
                dataMap.put("9", transferRecord.getPayBankName());
                dataMap.put("10", transferRecord.getPathNo());
                dataMap.put("11", transferRecord.getInAccountName());
                dataMap.put("12", transferRecord.getInAccountNo());
                dataMap.put("13", transferRecord.getInBankName());
                dataMap.put("14", transferRecord.getTranRemark());
                dataMap.put("15", transferRecord.getOrderNo());
                dataMap.put("16", transferRecord.getPathOrderNo());
                dataMap.put("17", transferRecord.getSelOrderNo());
                dataMap.put("18", transferRecord.getCustomKey());
                dataMap.put("19", transferRecord.getUpdateTime());
                data.add(sortMapByKey(dataMap));
            }
            ExcelFileGenerator.ExcelExport(response, headers, filename, data);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }
}