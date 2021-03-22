package com.jrmf.controller.systemrole.merchant.invoice;

import com.jrmf.common.CommonString;
import com.jrmf.controller.BaseController;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.Page;
import com.jrmf.domain.vo.CustomInvoiceInfoVO;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.CustomInvoiceService;
import com.jrmf.utils.RespCode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用途：
 * 作者：郭桐宁
 * 时间：2018/12/13 17:32
 * Version:1.0
 *
 * @author guoto
 */
@Controller
@RequestMapping("/custom/invoice/info")
public class CustomInvoiceInfoController extends BaseController {

    private final CustomInvoiceService customInvoiceService;
    
    @Autowired
    private ChannelCustomService customService;

    @Autowired
    public CustomInvoiceInfoController(CustomInvoiceService customInvoiceService) {
        this.customInvoiceService = customInvoiceService;
    }

    @RequestMapping(value = "/addOrUpdateInvoiceInfo", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> addCustomInvoiceInfo(HttpServletRequest request, CustomInvoiceInfoVO customInvoiceInfoVO) {
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		if(customLogin.getCustomType()==4&&customLogin.getMasterCustom()!=null){
			ChannelCustom masterChannelCustom = customService.getCustomByCustomkey(customLogin.getMasterCustom());
	        customInvoiceInfoVO.setCustomkey(masterChannelCustom.getCustomkey());
		}else{
			customInvoiceInfoVO.setCustomkey(customLogin.getCustomkey());
		}

        boolean isSuccess;
        if(customInvoiceInfoVO.getId() == null || customInvoiceInfoVO.getId() == 0){
            isSuccess = customInvoiceService.addCustomInvoiceInfo(customInvoiceInfoVO);
        }else{
            isSuccess = customInvoiceService.updateCustomInvoiceByParam(customInvoiceInfoVO);
        }
        if (isSuccess) {
            return returnSuccess();
        } else {
            return returnFail(RespCode.error101, RespCode.OPERATING_FAILED);
        }
    }

    @RequestMapping(value = "/deleteInvoiceInfo", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> deleteCustomInvoiceInfo(HttpSession session,Integer id) {
        boolean isSuccess = customInvoiceService.deleteCustomInvoiceInfo((String)session.getAttribute(CommonString.CUSTOMKEY), id);
        if (isSuccess) {
            return returnSuccess();
        } else {
            return returnFail(RespCode.error101, RespCode.DELETE_FAILED);
        }
    }

    @RequestMapping(value = "/listInvoiceInfo", method = RequestMethod.POST)
    public @ResponseBody Map<String, Object> listCustomInvoiceInfo(HttpServletRequest request) {
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		Page page = new Page(request);
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		if(customLogin.getCustomType()==4&&customLogin.getMasterCustom()!=null){
			ChannelCustom masterChannelCustom = customService.getCustomByCustomkey(customLogin.getMasterCustom());
			page.getParams().put("customkey", masterChannelCustom.getCustomkey());
		}else{
			page.getParams().put("customkey", customLogin.getCustomkey());
		}
		List<CustomInvoiceInfoVO> relationList = customInvoiceService.listCustomInvoiceInfoByPage(page);
		int total = customInvoiceService.listCustomInvoiceInfoCountByPage(page);
		result.put("total", total);
		result.put("relationList", relationList);
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
        return result;
    }

    @RequestMapping(value = "/setCurrentDefault", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> setCurrentDefault(HttpSession session, Integer id) {
        String customKey = (String) session.getAttribute(CommonString.CUSTOMKEY);
        boolean isSuccess = customInvoiceService.setCurrentDefault(customKey, id);
        if (isSuccess) {
            return returnSuccess();
        } else {
            return returnFail(RespCode.error101, RespCode.UPDATE_FAILED);
        }
    }

    @RequestMapping(value = "/listInvoiceInfoByParam", method = RequestMethod.POST)
    public @ResponseBody
    Map<String, Object> listCustomInvoiceInfoByParam(HttpSession session,
                                                     @RequestParam(required = false) String invoicePhone,
                                                     @RequestParam(required = false) String invoiceUserName,
                                                     @RequestParam(required = false) Integer status,
                                                     @RequestParam(required = false, defaultValue = "1") Integer pageNo,
                                                     @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        HashMap<String, Object> params = new HashMap<>(5);
        params.put("invoicePhone", invoicePhone);
        params.put("invoiceUserName", invoiceUserName);
        params.put(CommonString.CUSTOMKEY, session.getAttribute(CommonString.CUSTOMKEY));
        params.put("status", status);
        params.put("pageNo", (pageNo - 1) * pageSize);
        params.put("pageSize", pageSize);
        List<CustomInvoiceInfoVO> customInvoiceInfoVOS = customInvoiceService.listCustomInvoiceInfoByParams(params);
        return returnSuccess(customInvoiceInfoVOS);
    }
}
