package com.jrmf.controller.systemrole.merchant.invoice;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jrmf.controller.BaseController;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.ChannelInvoice;
import com.jrmf.domain.ChannelRelated;
import com.jrmf.domain.UserCommission;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.ChannelInvoiceService;
import com.jrmf.service.ChannelRelatedService;
import com.jrmf.service.UserCommissionService;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.OrderNoUtil;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author guoto
 */
@Controller
@RequestMapping("/merchant/invoice")
public class MerchantInvoiceController extends BaseController {

    private static Logger logger = LoggerFactory.getLogger(MerchantInvoiceController.class);
    @Autowired
    private UserCommissionService commissionService;
    @Autowired
    private ChannelInvoiceService channelInvoiceService;
    @Autowired
    private ChannelRelatedService channelRelatedService;
    @Autowired
    private ChannelCustomService customService;
    @Autowired
    private OrderNoUtil orderNoUtil;
    /**
     * 发票详情
     */
    @RequestMapping(value = "/company/invoiceDetail", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> invoiceDetail(HttpServletRequest request, HttpServletResponse response) {
        int respstat = RespCode.success;

        Map<String, Object> result = new HashMap<>(5);
        // 商户标识
        String originalId = (String) request.getSession().getAttribute("customkey");
        String id = request.getParameter("id");
        logger.info("/company/invoiceDetail方法  传参： originalId=" + originalId);
        if (StringUtil.isEmpty(originalId) || StringUtil.isEmpty(id)) {
            respstat = RespCode.error101;
            result.put(RespCode.RESP_STAT, respstat);
            result.put(RespCode.RESP_MSG, "请求参数不全");
            return result;
        } else {
            try {
                ChannelCustom custom = customService.getCustomByCustomkey(originalId);
                ChannelInvoice invoice = channelInvoiceService.getChannelInvoiceById(id);
                result.put("invoice", invoice);
                result.put("custom", custom);
            } catch (Exception e) {
                respstat = RespCode.error107;
                result.put(RespCode.RESP_STAT, respstat);
                result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
                logger.error(e.getMessage(), e);
                return result;
            }
        }
        result.put(RespCode.RESP_STAT, respstat);
        result.put(RespCode.RESP_MSG, "成功");
        logger.info("返回结果：" + result);
        return result;
    }

    /**
     * 发票列表
     */
    @RequestMapping(value = "/invoice/listData", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> companyInvoice(HttpServletRequest request,
        @RequestParam(defaultValue = "1", required = false) Integer pageNo,
        @RequestParam(defaultValue = "10", required = false) Integer pageSize) {
        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<>(5);
        // 商户标识
        String customkey = (String) request.getSession().getAttribute("customkey");
//        String pageNo = request.getParameter("pageNo");
        String name = request.getParameter("name");
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
        String status = request.getParameter("status");
        String companyId = request.getParameter("companyId");
        logger.info("/company/invoice方法  传参： customkey=" + customkey);
        if (StringUtil.isEmpty(customkey)) {
            respstat = RespCode.error101;
            result.put(RespCode.RESP_STAT, respstat);
            result.put(RespCode.RESP_MSG, "请求参数不全");
            return result;
        } else {
            try {
                ChannelRelated related = channelRelatedService.getRelatedByCompAndOrig(customkey,companyId);
                if(related == null){
                    respstat = RespCode.error101;
                    result.put(RespCode.RESP_STAT, respstat);
                    result.put(RespCode.RESP_MSG, "商户配置信息不完整");
                    return result;
                }
                Map<String, Object> paramMap = new HashMap<>(15);
                paramMap.put("reCustomkey", related.getCompanyId());
                paramMap.put("customkey", customkey);
                paramMap.put("startTime", startTime);
                paramMap.put("endTime", endTime);
                paramMap.put("name", name);
                paramMap.put("status", status);
//                paramMap.put("pageNo", pageNo);
//                int total = channelInvoiceService.getChannelInvoiceByParam(paramMap).size();
//                int pageSize = 10;
//                if (!StringUtil.isEmpty(pageNo)) {
//                    paramMap.put("start", (Integer.parseInt(pageNo) - 1) * pageSize);
//                    paramMap.put("limit", pageSize);
//                }
                PageHelper.startPage(pageNo, pageSize);
                List<ChannelInvoice> invoiceList = channelInvoiceService.getChannelInvoiceByParam(paramMap);
                PageInfo<ChannelInvoice> pageInfo = new PageInfo<>(invoiceList);
                result.put("total", pageInfo.getTotal());
                result.put("invoiceList", pageInfo.getList());
            } catch (Exception e) {
                respstat = RespCode.error107;
                result.put(RespCode.RESP_STAT, respstat);
                result.put(RespCode.RESP_MSG, "接口查询失败，请联系管理员！");
                logger.error(e.getMessage(), e);
                return result;
            }
        }
        result.put(RespCode.RESP_STAT, respstat);
        result.put(RespCode.RESP_MSG, "成功");
        logger.info("返回结果：" + result);
        return result;
    }

    /**
     * 开票信息
     */
    @RequestMapping(value = "/invoice/invoiceMessage", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> invoiceMessage(HttpServletRequest request, HttpServletResponse response) {
        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<>(5);
        // 商户标识
        String customkey = (String) request.getSession().getAttribute("customkey");
        if (StringUtil.isEmpty(customkey)) {
            respstat = RespCode.error101;
            result.put(RespCode.RESP_STAT, respstat);
            result.put(RespCode.RESP_MSG, "请求参数不全");
            return result;
        } else {
            try {
                ChannelCustom custom = customService.getCustomByCustomkey(customkey);
                result.put("custom", custom);
            } catch (Exception e) {
                respstat = RespCode.error107;
                result.put(RespCode.RESP_STAT, respstat);
                result.put(RespCode.RESP_MSG, "接口查询失败，请联系管理员！");
                logger.error("", e);
                return result;
            }
        }
        result.put(RespCode.RESP_STAT, respstat);
        result.put(RespCode.RESP_MSG, "成功");
        logger.info("返回结果：" + result);
        return result;
    }

    /**
     * 开票邮寄
     */
    @RequestMapping(value = "/invoice/address", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> address(HttpServletRequest request, HttpServletResponse response) {
        int respstat = RespCode.success;
        Map<String, Object> result = new HashMap<>(5);
        String address =  request.getParameter("address");
        String phoneNo =request.getParameter("phoneNo");
        String receiverName = request.getParameter("receiverName");
        // 商户标识
        String customkey = (String) request.getSession().getAttribute("customkey");
        if (StringUtil.isEmpty(customkey)) {
            respstat = RespCode.error101;
            result.put(RespCode.RESP_STAT, respstat);
            result.put(RespCode.RESP_MSG, "请求参数不全");
            return result;
        } else {
            try {
                if (!StringUtil.isMobileNO(phoneNo)) {
                    respstat = RespCode.error101;
                    result.put(RespCode.RESP_MSG, "手机号格式不正确");
                    return result;
                }
                ChannelCustom custom = customService.getCustomByCustomkey(customkey);
                custom.setAddress(address);
                custom.setReceiverName(receiverName);
                custom.setPhoneNo(phoneNo);
                customService.updateCustomById(custom);
            } catch (Exception e) {
                respstat = RespCode.error107;
                result.put(RespCode.RESP_STAT, respstat);
                result.put(RespCode.RESP_MSG, "接口查询失败，请联系管理员！");
                logger.error(e.getMessage(), e);
                return result;
            }
        }
        result.put(RespCode.RESP_STAT, respstat);
        result.put(RespCode.RESP_MSG, "成功");
        logger.info("返回结果：" + result);
        return result;
    }

    /**
     * 开票申请
     */
    @RequestMapping(value = "/invoice/chooseInvoce", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> chooseInvoce(HttpServletRequest request, HttpServletResponse response,
        @RequestParam(defaultValue = "1", required = false) Integer pageNo,
        @RequestParam(defaultValue = "10", required = false) Integer pageSize) {
        int respstat = RespCode.success;
        // 商户标识
        String originalId = (String) request.getSession().getAttribute("customkey");
        Map<String, Object> result = new HashMap<String, Object>(5);
        String amount = request.getParameter("amount");
        String companyId = request.getParameter("companyId");
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
//        String pageNo = request.getParameter("pageNo");
        logger.info("/invoice/chooseInvoce.do 请求参数：amount:" + amount);
        if (StringUtil.isEmpty(originalId) || StringUtil.isEmpty(companyId)) {
            return returnFail(RespCode.error101,RespCode.codeMaps.get(RespCode.error101));
        } else {
            try {
                // 添加发票状态为未开票的
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("originalId", originalId);
                paramMap.put("companyId", companyId);
                paramMap.put("status", "1");
                paramMap.put("startTime", startTime);
                paramMap.put("endTime", endTime);
//                int total = commissionService.getUserCommissionToInvoice(paramMap).size();
//                int pageSize = 10;
//                if (!StringUtils.isEmpty(pageNo)) {
//                    paramMap.put("start", (Integer.parseInt(pageNo) - 1) * pageSize);
//                    paramMap.put("limit", pageSize);
//                }
                PageHelper.startPage(pageNo, pageSize);
                List<UserCommission> list = commissionService.getUserCommissionToInvoice(paramMap);
                PageInfo<UserCommission> pageInfo = new PageInfo<>(list);
                String amountSum = "0";
                int orderNum = 0;
                String orderAmount = "0";
                List<UserCommission> data = new ArrayList<>();
                StringBuilder ids = new StringBuilder();
                if (StringUtil.isEmpty(amount)) {
                    for (UserCommission commission : list) {
                        orderAmount = ArithmeticUtil.addStr(orderAmount, commission.getAmount());
                        orderNum++;
                        ids.append(commission.getId() + ",");
                    }
                    data = list;
                } else {
                    for (UserCommission commission : list) {
                        amountSum = ArithmeticUtil.addStr(amountSum, commission.getAmount());
                        // 目标金额小于总金额
                        if (ArithmeticUtil.compareTod(amount, amountSum) < 0) {
                            break;
                        } else {
                            ids.append(commission.getId() + ",");
                            data.add(commission);
                            orderNum++;
                            orderAmount = ArithmeticUtil.addStr(orderAmount, commission.getAmount());
                        }
                    }
                }
                result.put("orderNum", orderNum);
                result.put("orderAmount", orderAmount);
                result.put("list", data);
                result.put("ids", ids.toString());
                result.put("pageNo", pageNo);
                result.put("total", pageInfo.getTotal());

            } catch (Exception e) {
                respstat = RespCode.error107;
                result.put(RespCode.RESP_STAT, respstat);
                result.put(RespCode.RESP_MSG, "接口查询失败，请联系管理员！");
                logger.error(e.getMessage(), e);
                return result;
            }
        }
        result.put(RespCode.RESP_STAT, respstat);
        result.put(RespCode.RESP_MSG, "成功");
        logger.info("返回结果：" + result);
        return result;
    }

    /**
     * 新增发票
     */
    @RequestMapping(value = "/invoice/addData", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> companyAddInvoice(HttpServletRequest request) {
        int respstat = RespCode.success;
        // 商户标识
        String originalId = (String) request.getSession().getAttribute("customkey");
        Map<String, Object> result = new HashMap<>(5);
        String amount = request.getParameter("amount");
        // 公司名称
        String invoiceCompanyName = request.getParameter("invoiceCompanyName");
        // 统一社会信用代码
        String invoiceNo = request.getParameter("invoiceNo");
        // 备注
        String remark = request.getParameter("remark");
        // 手机号
        String mobileNo = request.getParameter("mobileNo");
        //服务公司ID
        String companyId = request.getParameter("companyId");
        // 地址
        String address = request.getParameter("address");
        String taxpayerType = request.getParameter("taxpayerType");
        String receiverName = request.getParameter("receiverName");
        String bankNameAndBankNo = customService.getCustomByCustomkey(originalId).getBankNameAndBankNo();
        String ids = request.getParameter("ids");
        ChannelRelated related = channelRelatedService.getRelatedByCompAndOrig(originalId,companyId);
        logger.info("/company/addData方法  传参： originalId=" + originalId + " amount=" + amount + " companyId=" + companyId
                + " invoiceCompanyName=" + invoiceCompanyName + " invoiceNo=" + invoiceNo + " remark=" + remark
                + " taxpayerType=" + taxpayerType + " address=" + address + " ids=" + ids + " bankNameAndBankNo="
                + bankNameAndBankNo + " mobileNo=" + mobileNo);
        if (StringUtil.isEmpty(originalId) || StringUtil.isEmpty(amount) || StringUtil.isEmpty(companyId)
                || StringUtil.isEmpty(invoiceCompanyName) || StringUtil.isEmpty(invoiceNo)
                || StringUtil.isEmpty(address) || StringUtil.isEmpty(mobileNo) || StringUtil.isEmpty(taxpayerType)
                || StringUtil.isEmpty(receiverName) || StringUtil.isEmpty(ids)) {
            respstat = RespCode.error101;
            result.put(RespCode.RESP_STAT, respstat);
            result.put(RespCode.RESP_MSG, "请求参数不全");
            return result;
        } else {
            try {
                String batchNo = orderNoUtil.getChannelSerialno();
                ChannelInvoice invoice = new ChannelInvoice();
                invoice.setAmount(amount);
                invoice.setCustomkey(originalId);
                invoice.setReCustomkey(companyId);
                invoice.setInvoiceNo(invoiceNo);
                invoice.setAddress(address);
                invoice.setRemark(remark);
                invoice.setNum(1);
                invoice.setMobileNo(mobileNo);
                invoice.setTaxpayerType(Integer.parseInt(taxpayerType));
                invoice.setReceiverName(receiverName);
                invoice.setOrderno(batchNo);
                invoice.setBankNameAndBankNo(bankNameAndBankNo);
                channelInvoiceService.addChannelInvoice(invoice);
                // 修改明细状态，添加发票批次订单号
                commissionService.updateUserCommissionByInvoice(batchNo, ids);
            } catch (Exception e) {
                respstat = RespCode.error107;
                result.put(RespCode.RESP_STAT, respstat);
                result.put(RespCode.RESP_MSG, "接口查询失败，请联系管理员！");
                logger.error(e.getMessage(), e);
                return result;
            }
        }
        result.put(RespCode.RESP_STAT, respstat);
        result.put(RespCode.RESP_MSG, "成功");
        logger.info("返回结果：" + result);
        return result;
    }
}
