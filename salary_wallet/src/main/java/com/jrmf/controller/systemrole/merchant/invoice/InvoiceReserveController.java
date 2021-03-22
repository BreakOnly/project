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

import com.jrmf.controller.constant.CustomType;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.Page;
import com.jrmf.domain.QbInvoiceReserve;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.QbInvoiceReserveService;
import com.jrmf.service.QbInvoiceVoucherService;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.RespCode;

@RestController
@RequestMapping("/invoiceReserre")
public class InvoiceReserveController {

	//商户信息service
	@Autowired
	private ChannelCustomService channelCustomService;
	//发票库存service
	@Autowired
	private QbInvoiceReserveService qbInvoiceReserveService;
	//发票凭证服务
	@Autowired
	private QbInvoiceVoucherService qbInvoiceVoucherService;

	/**
	 * 发票库存维护管理列表
	 * @param request
	 * @return
	 */
	@RequestMapping("/queryList")
	public ResponseEntity<?> queryList(HttpServletRequest request){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		Page page = new Page(request);
		//校验是否有权限
		boolean checkFlag = true;
		//获取登陆信息
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		Integer []allowCustomType = new Integer[]{6};
		checkFlag = channelCustomService.getCustomKeysByType(page.getParams(), allowCustomType, customLogin);
		if(checkFlag){
			//分页查询白名单信息
			List<Map<String, Object>> relationList = qbInvoiceReserveService.getInvoiceReserveByPage(page);
			//查询白名单总条数
			int count = qbInvoiceReserveService.getInvoiceReserveCount(page);
			result.put("total", count);
			result.put("relationList", relationList);
		}else{
			respstat = RespCode.DO_NOT_HAVE_APPROVAL_RIGHT;
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return new ResponseEntity<Object>(result,HttpStatus.OK);
	}

	/**
	 * 维护发票库存信息
	 * @param invoiceReserve
	 * @return
	 */
	@RequestMapping("/saveInvoiceReserve")
	public ResponseEntity<?> saveInvoiceReserve(HttpServletRequest request,QbInvoiceReserve invoiceReserve){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		Page page = new Page(request);
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		//校验是否有权限
		boolean checkFlag = true;
		Integer []allowCustomType = new Integer[]{6};
		checkFlag = channelCustomService.getCustomKeysByType(page.getParams(), allowCustomType, customLogin);
		if(checkFlag){
			if(invoiceReserve.getId()==null){
				//添加
				int count = qbInvoiceReserveService.checkIsExist(invoiceReserve);
				if(count>0){
					respstat = RespCode.EXISTS_INVOICE_RESERVE;
				}else{
					invoiceReserve.setCreateTime(DateUtils.getNowDate());
					invoiceReserve.setAddUser(customLogin.getUsername());
					qbInvoiceReserveService.insert(invoiceReserve);
				}
			}else{
				//修改
				QbInvoiceReserve oldInvoiceReserve = qbInvoiceReserveService.selectByPrimaryKey(invoiceReserve.getId());
				int invoiceCount = qbInvoiceVoucherService.getAlreadyInvoiceCount(oldInvoiceReserve);
				if(invoiceCount>0&&!invoiceReserve.getInvoiceLimitAmout().equals(oldInvoiceReserve.getInvoiceLimitAmout())){
					respstat = RespCode.IS_USE_WRONG;
				}else{
					if(invoiceReserve.getInvoiceTotalNum()>=invoiceCount){
						invoiceReserve.setUpdateTime(DateUtils.getNowDate());
						qbInvoiceReserveService.updateByPrimaryKeySelective(invoiceReserve);
					}else{
						respstat = RespCode.INVOICE_NUM_WRONG;
					}	
				}

			}
		}else{
			respstat = RespCode.DO_NOT_HAVE_APPROVAL_RIGHT;
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return new ResponseEntity<Object>(result,HttpStatus.OK);
	}

	/**
	 * 删除
	 * @param id
	 * @return
	 */
	@RequestMapping("/deleteInvoiceReserve")
	public ResponseEntity<?> deleteInvoiceReserve(HttpServletRequest request,Integer id){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		Page page = new Page(request);
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		//校验是否有权限
		boolean checkFlag = true;
		Integer []allowCustomType = new Integer[]{6};
		checkFlag = channelCustomService.getCustomKeysByType(page.getParams(), allowCustomType, customLogin);
		if(checkFlag){
			QbInvoiceReserve invoiceReserve = qbInvoiceReserveService.selectByPrimaryKey(id);
			int invoiceCount = qbInvoiceVoucherService.getAlreadyInvoiceCount(invoiceReserve);
			if(invoiceCount<1){
				qbInvoiceReserveService.deleteByPrimaryKey(id);
			}else{
				respstat = RespCode.IS_USE_WRONG;
			}
		}else{
			respstat = RespCode.DO_NOT_HAVE_APPROVAL_RIGHT;
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return new ResponseEntity<Object>(result,HttpStatus.OK);
	}

}
