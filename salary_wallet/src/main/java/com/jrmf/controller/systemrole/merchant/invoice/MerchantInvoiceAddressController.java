package com.jrmf.controller.systemrole.merchant.invoice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.jrmf.controller.BaseController;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.Page;
import com.jrmf.domain.vo.CustomInvoiceInfoVO;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.CustomInvoiceService;
import com.jrmf.utils.ExcelFileGenerator;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;

/**
 * 新版发票地址管理
 * @author 孙春辉
 *
 */
@RestController
@RequestMapping("/invoiceAddress")
public class MerchantInvoiceAddressController extends BaseController{

	//日志
	private static Logger logger = LoggerFactory.getLogger(MerchantInvoiceAddressController.class);

	//发票地址service
	@Autowired
	private  CustomInvoiceService customInvoiceService;
	//商户信息service

	@Autowired
	private ChannelCustomService customService;

	/**
	 * 获取发票邮寄地址列表
	 * @param request
	 * @return
	 */
	@RequestMapping("/listAddress")
	public ResponseEntity<?> listAddress(HttpServletRequest request){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		//获取分页参数
		Page page = new Page(request);
		//获取用户登陆信息
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		if (customLogin.getCustomType() == 4 && customLogin.getMasterCustom() != null) {
			customLogin = customService.getCustomByCustomkey(customLogin.getMasterCustom());
		}
		//根据登陆商户类型获取关联商户
		if(StringUtil.isEmpty(page.getParams().get("customkey"))){
			String customKeys = customService.getCustomKeysByLoginMerchant(customLogin);
			if(!StringUtil.isEmpty(customKeys)){
				page.getParams().put("customkey", customKeys + "," + customLogin.getCustomkey());
			}		
		}
		//分页查询发票邮寄地址信息
		List<Map<String, Object>> relationList = customInvoiceService.getMerchantInvoiceAddressByPage(page);
		//查询发票邮寄地址总条数
		int total = customInvoiceService.getMerchantInvoiceAddressCount(page);
		result.put("total", total);
		result.put("relationList", relationList);
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return  new ResponseEntity<Object>(result, HttpStatus.OK);
	}

	/**
	 * 新增/修改发票地址信息
	 * @param customInvoiceInfoVO
	 * @return
	 */
	@PostMapping("/saveAddress")
	public ResponseEntity<?> saveAddress(HttpServletRequest request,CustomInvoiceInfoVO customInvoiceInfoVO){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		if(customInvoiceInfoVO.getId()==null){
			//必填参数校验
			if(StringUtil.isEmpty(customInvoiceInfoVO.getCustomkey())||StringUtil.isEmpty(customInvoiceInfoVO.getInvoiceUserName())||
					StringUtil.isEmpty(customInvoiceInfoVO.getInvoiceAddress())||StringUtil.isEmpty(customInvoiceInfoVO.getInvoicePhone())){
				result.put(RespCode.RESP_STAT, RespCode.REQUIRED_PARAMS_ISNULL);
				result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.REQUIRED_PARAMS_ISNULL));
				return new ResponseEntity<Object>(result,HttpStatus.OK);
			}
			//添加发票地址信息
			customInvoiceInfoVO.setAddUser(customLogin.getUsername());
			boolean isSuccess = customInvoiceService.addCustomInvoiceInfo(customInvoiceInfoVO);
			if(isSuccess){
				//添加成功
				respstat = RespCode.success;
			}else{
				//添加失败
				respstat = RespCode.INSERT_FAIL;
			}
		}else{
			//修改发票地址信息
			boolean isSuccess = customInvoiceService.updateCustomInvoiceByParam(customInvoiceInfoVO);
			if(isSuccess){
				respstat = RespCode.success;
				//修改成功
			}else{
				//修改失败
				respstat = RespCode.UPDATE_FAIL;
			}
		}
		result.put(RespCode.RESP_STAT, respstat); 
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return new ResponseEntity<Object>(result,HttpStatus.OK);
	}

	/**
	 * 删除发票邮寄地址
	 * @param id
	 * @return
	 */
	@RequestMapping("/deleteOneAddress")
	public ResponseEntity<?> deleteOneAddress(String customkey,Integer id){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		try{
			if(id==null){
				//必填参数校验
				respstat = RespCode.REQUIRED_PARAMS_ISNULL;
			}else{
				//删除地址发票邮寄地址
				boolean isSuccess = customInvoiceService.deleteCustomInvoiceInfo(customkey, id);
				if(isSuccess){
					respstat = RespCode.success;
				}else{
					//删除失败
					respstat = RespCode.DELETE_FAIL;
				}
			}
		}catch(Exception e){
			logger.error("删除发票邮寄地址异常");
			logger.error(e.getMessage(), e);
			respstat = RespCode.HAPPEND_EXCEPTION;
		}
		result.put(RespCode.RESP_STAT, respstat); 
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return new ResponseEntity<Object>(result,HttpStatus.OK);
	}

	/**
	 * 设置默认地址
	 * @param session
	 * @param id
	 * @param customkey
	 * @return
	 */
	@RequestMapping(value = "/setCurrentDefault", method = RequestMethod.POST)
	public ResponseEntity<?> setCurrentDefault(Integer id,String customkey) {
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		try{
			if(id==null||StringUtil.isEmpty(customkey)){
				//必填参数校验
				respstat = RespCode.REQUIRED_PARAMS_ISNULL;
			}else{
				//执行设置
				boolean isSuccess = customInvoiceService.setCurrentDefault(customkey, id);
				if (isSuccess) {
					//设置成功
					respstat = RespCode.success;
				} else {
					//设置失败
					respstat = RespCode.UPDATE_FAIL;
				}
			}
		}catch (Exception e) {
			logger.error("设置邮寄默认地址异常");
			logger.error(e.getMessage(), e);
			respstat = RespCode.HAPPEND_EXCEPTION;
		}
		result.put(RespCode.RESP_STAT, respstat); 
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return new ResponseEntity<Object>(result,HttpStatus.OK);
	}

	/**
	 * 导出excel
	 */
	@RequestMapping("/export")
	public void export(HttpServletResponse response,HttpServletRequest request){
		try{
			// 标题
			String[] headers = new String[] {"商户名称", "收件人","联系人手机","收递地址","固定电话","电子邮箱","是否默认"};
			String filename = "商户邮寄地址"; 
			Page page = new Page(request);
			//获取用户登陆信息
			ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
			if (customLogin.getCustomType() == 4 && customLogin.getMasterCustom() != null) {
				customLogin = customService.getCustomByCustomkey(customLogin.getMasterCustom());
			}
			//根据登陆商户类型获取关联商户
			if(StringUtil.isEmpty(page.getParams().get("customkey"))){
				String customKeys = customService.getCustomKeysByLoginMerchant(customLogin);
				if(!StringUtil.isEmpty(customKeys)){
					page.getParams().put("customkey", customKeys + "," + customLogin.getCustomkey());
				}
			}
			List<Map<String, Object>> relationList = customInvoiceService.getMerchantInvoiceAddressNoPage(page);
			List<Map<String, Object>> data = new ArrayList<>();
			for (Map<String, Object> address : relationList) {
				Map<String, Object> dataMap = new HashMap<>(20);
				dataMap.put("1", address.get("merchantName"));
				dataMap.put("2", address.get("invoiceUserName"));
				dataMap.put("3", address.get("invoicePhone"));
				dataMap.put("4", address.get("invoiceAddress"));
				dataMap.put("5", address.get("fixedTelephone"));
				dataMap.put("6",address.get("email"));
				dataMap.put("7",Integer.parseInt(String.valueOf(address.get("isDefault")))==-1?"否":"是");
				data.add(sortMapByKey(dataMap));
			}
			ExcelFileGenerator.ExcelExport(response, headers, filename, data);
		}catch(Exception e){
			logger.error("邮寄默认地址导出异常");
			logger.error(e.getMessage(), e);
		}
	}


}
