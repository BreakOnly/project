package com.jrmf.controller.systemrole;

import com.jrmf.controller.BaseController;
import com.jrmf.domain.CustomPaymentTotalAmount;
import com.jrmf.service.CustomLimitConfService;
import com.jrmf.utils.RespCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/totalamount")
public class CustomLimitController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(CustomLimitController.class);
    private final CustomLimitConfService customLimitConfService;

    @Autowired
    public CustomLimitController(CustomLimitConfService customLimitConfService) {
        this.customLimitConfService = customLimitConfService;
    }

    @RequestMapping("/list")
    @ResponseBody
    public Map<String, Object> listCustomLimit(
    		String companyId,
    		String companyName,
    		String identityNo,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {

    	int respstat = RespCode.success;
    	HashMap<String, Object> result = new HashMap<String, Object>();

		try {

			result.put(RespCode.RESP_STAT, respstat);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));

	        Map<String, Object> param = new HashMap<>();
	        param.put("companyId", companyId);
	        param.put("companyName", companyName);
	        param.put("identityNo", identityNo);
	        param.put("status", 1);
	        int total = customLimitConfService.listCustomPaymentTotalAmountByParamCount(param);
	        param.put("start", (pageNo - 1) * pageSize);
	        param.put("limit", pageSize);
	        result.put("total", total);

	        List<CustomPaymentTotalAmount> customTotalList = customLimitConfService.listCustomPaymentTotalAmountByParam(param);
			result.put("customTotalList", customTotalList);
	        return returnSuccess(result);

		} catch (Exception e) {
			result.put(RespCode.RESP_STAT, RespCode.error107);
			result.put(RespCode.RESP_MSG, "接口查询失败，请联系管理员！");
			logger.error(e.getMessage(),e);
			return result;
		}

    }

}