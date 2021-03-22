package com.jrmf.controller.systemrole.merchant.invoice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.jrmf.controller.constant.CustomType;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.Page;
import com.jrmf.domain.QbInvoiceVoucher;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.QbInvoiceVoucherService;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.baiduai.invoiceDiscernUtil;

@RestController
@RequestMapping("/invoiceVoucher")
public class InvoiceVoucherController {

	//商户信息service
	@Autowired
	private ChannelCustomService channelCustomService;
	//发票凭证服务
	@Autowired
	private QbInvoiceVoucherService qbInvoiceVoucherService;

	@RequestMapping("/queryList")
	public ResponseEntity<?> queryList(HttpServletRequest request){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		Page page = new Page(request);
		//校验是否有权限
		boolean checkFlag = true;
		//获取登陆信息
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		Integer []allowCustomType = new Integer[]{CustomType.COMPANY.getCode(),6};
		checkFlag = channelCustomService.getCustomKeysByType(page.getParams(), allowCustomType, customLogin);
		if(checkFlag){
			//分页查询白名单信息
			List<Map<String, Object>> relationList = qbInvoiceVoucherService.getInvoiceVoucherByPage(page);
			//查询白名单总条数
			int count = qbInvoiceVoucherService.getInvoiceVoucherCount(page);
			result.put("total", count);
			result.put("relationList", relationList);
		}else{
			respstat = RespCode.DO_NOT_HAVE_APPROVAL_RIGHT;
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return new ResponseEntity<Object>(result,HttpStatus.OK);
	}


	@RequestMapping("/queryListBySerialNo")
	public ResponseEntity<?> queryListBySerialNo(HttpServletRequest request,String invoiceSerialNo){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		Page page = new Page(request);
		//校验是否有权限
		boolean checkFlag = true;
		//获取登陆信息
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		Integer []allowCustomType = new Integer[]{CustomType.COMPANY.getCode(),6};
		checkFlag = channelCustomService.getCustomKeysByType(page.getParams(), allowCustomType, customLogin);
		if(checkFlag){
			//分页查询白名单信息
			List<QbInvoiceVoucher> relationList = qbInvoiceVoucherService.getInvoiceVoucherBySerialNo(invoiceSerialNo);
			result.put("relationList", relationList);
		}else{
			respstat = RespCode.DO_NOT_HAVE_APPROVAL_RIGHT;
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return new ResponseEntity<Object>(result,HttpStatus.OK);
	}

	/**
	 * 获取发票信息
	 * @param file
	 * @return
	 */
	@RequestMapping("/getInvoice")
	public ResponseEntity<?> getInvoice(MultipartFile file){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		try{
			Map<String, String> invoiceInfo = invoiceDiscernUtil.getInvoice(file.getBytes());
			if(invoiceInfo.get("code").equals("0000")){
				result.put("invoiceCode", invoiceInfo.get("invoiceCode"));
				result.put("invoiceNum", invoiceInfo.get("invoiceNum"));
				result.put("invoiceAmount", invoiceInfo.get("invoiceAmount"));
				result.put(RespCode.RESP_STAT, respstat);
				result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
			}else{
				result.put(RespCode.RESP_STAT, RespCode.GET_INVOICE_FAIL);
				result.put(RespCode.RESP_MSG, invoiceInfo.get("msg"));
			}
		}catch(Exception e){
			result.put(RespCode.RESP_STAT, RespCode.HAPPEND_EXCEPTION);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.HAPPEND_EXCEPTION));
		}
		return new ResponseEntity<Object>(result,HttpStatus.OK);
	}

}
