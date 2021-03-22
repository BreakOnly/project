package com.jrmf.controller.systemrole.merchant.linkconfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.AccountTransStatus;
import com.jrmf.controller.constant.AccountTransType;
import com.jrmf.controller.constant.CustomType;
import com.jrmf.controller.constant.IsSubAccount;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.Page;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.LinkAccountTransService;
import com.jrmf.utils.ExcelFileGenerator;
import com.jrmf.utils.RespCode;

@RestController
@RequestMapping("/linkAccountTrans")
public class LinkAccountTransController extends BaseController {
	
	//商户信息service
	@Autowired
	private ChannelCustomService channelCustomService;
	@Autowired
	private LinkAccountTransService linkAccountTransService;
	
	@RequestMapping("/queryList")
	public ResponseEntity<?> queryList(HttpServletRequest request){
		HashMap<String, Object> result = new HashMap<>();
		Page page = new Page(request);
		//校验是否有权限
		boolean checkFlag = true;
		//获取登陆信息
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		Integer []allowCustomType = new Integer[]{CustomType.GROUP.getCode(),CustomType.CUSTOM.getCode(),6};
		checkFlag = channelCustomService.getCustomKeysByType(page.getParams(), allowCustomType, customLogin);
		if(checkFlag){
			List<Map<String, Object>> list = linkAccountTransService.getLinkAccountTransList(page);
			int count = linkAccountTransService.getLinkAccountTransListCount(page);
			result.put("total", count);
			result.put("relationList", list);
			result.put(RespCode.RESP_STAT, RespCode.success);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));
			return new ResponseEntity<Object>(result, HttpStatus.OK);
		}
		result.put(RespCode.RESP_STAT, RespCode.DO_NOT_HAVE_APPROVAL_RIGHT);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.DO_NOT_HAVE_APPROVAL_RIGHT));
		return new ResponseEntity<Object>(result, HttpStatus.OK);
	}
	
	
	/**
	 * 联动账户资金出入金记录导出
	 * @param request
	 * @param response
	 */
	@RequestMapping("/export")
	public void export(HttpServletRequest request,HttpServletResponse response){
		Page page = new Page(request);
		// 标题
		String[] headers = new String[] {"商户名称", "业务类型","状态","状态描述","交易金额","交易时间",
				"付款账户","付款账号","付款银行","交易通道","收款账户","收款账号","收款银行","主账号","是否存管子账户",
				"交易备注","业务订单号","调用外部订单号","外部系统订单号","更新时间"}; 
		String filename = "联动账户平台出入金记录"; 
		//校验是否有权限
		boolean checkFlag = true;
		//获取登陆信息
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		Integer []allowCustomType = new Integer[]{CustomType.GROUP.getCode(),CustomType.CUSTOM.getCode(),6};
		checkFlag = channelCustomService.getCustomKeysByType(page.getParams(), allowCustomType, customLogin);
		if(checkFlag){
			//分页查询资金出入金记录信息
			List<Map<String, String>> linkAccountTransList = linkAccountTransService.getLinkAccountTransListNoPage(page);
			List<Map<String, Object>> data = new ArrayList<>();
			for (Map<String, String> linkAccountTrans : linkAccountTransList) {
				Map<String, Object> dataMap = new HashMap<>(20);
				dataMap.put("1", linkAccountTrans.get("merchantName"));
				dataMap.put("2", AccountTransType.codeOf(Integer.parseInt(String.valueOf(linkAccountTrans.get("tranType")))).getDesc());
				dataMap.put("3", AccountTransStatus.codeOf(Integer.parseInt(String.valueOf(linkAccountTrans.get("status")))));
				dataMap.put("4",linkAccountTrans.get("msg"));
				dataMap.put("5",linkAccountTrans.get("tranAmount"));
				dataMap.put("6",linkAccountTrans.get("tranTime"));
				dataMap.put("7",linkAccountTrans.get("payAccount"));
				dataMap.put("8",linkAccountTrans.get("payAccountNo"));
				dataMap.put("10",linkAccountTrans.get("payBank"));
				dataMap.put("11",linkAccountTrans.get("pathName"));
				dataMap.put("12",linkAccountTrans.get("receiveAccount"));
				dataMap.put("13",linkAccountTrans.get("receiveAccountNo"));
				dataMap.put("14",linkAccountTrans.get("receiveBank"));
				dataMap.put("15",linkAccountTrans.get("mainAccount"));
				dataMap.put("16",IsSubAccount.codeOf(Integer.parseInt(String.valueOf(linkAccountTrans.get("isSubAccountTrans")))).getDesc());
				dataMap.put("17",linkAccountTrans.get("remark"));
				dataMap.put("18",linkAccountTrans.get("orderNo"));
				dataMap.put("19",linkAccountTrans.get("reqChannelNo"));
				dataMap.put("20",linkAccountTrans.get("channelNo"));
				dataMap.put("21",linkAccountTrans.get("updateTime"));
				data.add(sortMapByKey(dataMap));
			}
			ExcelFileGenerator.ExcelExport(response, headers, filename, data);
		}
	}

}
