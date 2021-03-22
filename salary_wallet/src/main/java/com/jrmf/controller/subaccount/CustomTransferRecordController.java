package com.jrmf.controller.subaccount;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.jrmf.common.CommonString;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.ConfirmStatus;
import com.jrmf.controller.constant.CustomTransferRecordType;
import com.jrmf.controller.constant.CustomType;
import com.jrmf.controller.constant.LoginRole;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.Page;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.CustomTransferRecordService;
import com.jrmf.utils.ExcelFileGenerator;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;

/**
 * 子账户交易记录管理
 * @author 孙春辉
 *
 */
@RestController
@RequestMapping("/subTransRecord")
class CustomTransferRecordController extends BaseController{

	//日志记录
	private static Logger logger = LoggerFactory.getLogger(CustomTransferRecordController.class);

	//子账户交易service
	@Autowired
	private CustomTransferRecordService customTransferRecordService;

	//商户信息service
	@Autowired
	private ChannelCustomService channelCustomService;

	/**
	 * 子账户交易记录列表
	 * @param request
	 * @return
	 */
	@RequestMapping("/queryList")
	public ResponseEntity<?> queryList(HttpServletRequest request){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		//校验是否有权限
		boolean checkFlag = true;
		//获取分页参数
		Page page = new Page(request);
		//获取用户登陆信息
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		if(StringUtil.isEmpty(page.getParams().get("companyId"))){
			if(customLogin.getCustomType()==4&&customLogin.getMasterCustom()!=null){
				ChannelCustom masterChannelCustom = channelCustomService.getCustomByCustomkey(customLogin.getMasterCustom());
				if((CommonString.ROOT.equals(masterChannelCustom.getCustomkey()) || (CustomType.ROOT.getCode() == masterChannelCustom.getCustomType() && CommonString.ROOT.equals(masterChannelCustom.getMasterCustom()) && LoginRole.ADMIN_ACCOUNT.getCode() == masterChannelCustom.getLoginRole()))){
					//超管
				}else if(masterChannelCustom.getCustomType() == CustomType.COMPANY.getCode()){
					//下发公司
					page.getParams().put("companyId", masterChannelCustom.getCustomkey());
				}else{
					//其他角色身份无权限
					checkFlag=false;
				}
			}else {
				if((CommonString.ROOT.equals(customLogin.getCustomkey()) || (CustomType.ROOT.getCode() == customLogin.getCustomType() && CommonString.ROOT.equals(customLogin.getMasterCustom()) && LoginRole.ADMIN_ACCOUNT.getCode() == customLogin.getLoginRole()))){
					//超管
				}else if(customLogin.getCustomType() == CustomType.COMPANY.getCode()){
					//下发公司
					page.getParams().put("companyId", customLogin.getCustomkey());
				}else{
					//其他角色身份无权限
					checkFlag=false;
				}
			}
		}
		if(checkFlag){
			//根据当前登陆角色查询子账户交易记录
			PageHelper.startPage(page.getPageNo(), page.getPageSize());
			List<Map<String, Object>> relationList = customTransferRecordService.getSubTransRecordListByPage(page);
			PageInfo<Map<String, Object>> pageInfo = new PageInfo(relationList);
			//获取子账户交易记录总数
			result.put("total", pageInfo.getTotal());
			result.put("relationList", pageInfo.getList());
		}else{
			respstat=RespCode.DO_NOT_HAVE_APPROVAL_RIGHT;
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return new ResponseEntity<Object>(result, HttpStatus.OK);
	}

	/**
	 * 导出excel
	 */
	@RequestMapping("/export")
	public void export(HttpServletResponse response,HttpServletRequest request){
		try{
			// 标题
			String[] headers = new String[] {"商户名称","子账户别名(公司名称)", "交易时间","子账号","借贷标识","交易金额","交易类型","对方账号",
					"对方账户名","对方银行名称","交易备注","对方银行联行号","银行流水号","交易状态","充值记录订单号","自动确认时间","主账号","主账号户名"};
			String filename = "子商户交易记录"; 
			//校验是否有权限
			boolean checkFlag = true;
			//获取分页参数
			Page page = new Page(request);
			//获取用户登陆信息
			ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
			if(StringUtil.isEmpty(page.getParams().get("companyId"))){
				if(customLogin.getCustomType()==4&&customLogin.getMasterCustom()!=null){
					ChannelCustom masterChannelCustom = channelCustomService.getCustomByCustomkey(customLogin.getMasterCustom());
					if((CommonString.ROOT.equals(masterChannelCustom.getCustomkey()) || (CustomType.ROOT.getCode() == masterChannelCustom.getCustomType() && CommonString.ROOT.equals(masterChannelCustom.getMasterCustom()) && LoginRole.ADMIN_ACCOUNT.getCode() == masterChannelCustom.getLoginRole()))){
						//超管
					}else if(masterChannelCustom.getCustomType() == CustomType.COMPANY.getCode()){
						//下发公司
						page.getParams().put("companyId", masterChannelCustom.getCustomkey());
					}else{
						//其他角色身份无权限
						checkFlag=false;
					}
				}else {
					if((CommonString.ROOT.equals(customLogin.getCustomkey()) || (CustomType.ROOT.getCode() == customLogin.getCustomType() && CommonString.ROOT.equals(customLogin.getMasterCustom()) && LoginRole.ADMIN_ACCOUNT.getCode() == customLogin.getLoginRole()))){
						//超管
					}else if(customLogin.getCustomType() == CustomType.COMPANY.getCode()){
						//下发公司
						page.getParams().put("companyId", customLogin.getCustomkey());
					}else{
						//其他角色身份无权限
						checkFlag=false;
					}
				}
			}
			if(checkFlag){
				//根据当前登陆角色查询子账户交易记录
				List<Map<String, Object>> relationList=customTransferRecordService.getSubTransRecordListNoPage(page);
				List<Map<String, Object>> data = new ArrayList<>();
				for (Map<String, Object> subTrans : relationList) {
					Map<String, Object> dataMap = new HashMap<>(20);
					dataMap.put("1",subTrans.get("companyName"));
					dataMap.put("2",subTrans.get("contractCompanyName"));
					dataMap.put("3",subTrans.get("tranTime"));
					dataMap.put("4",subTrans.get("subAccount"));
					dataMap.put("5",String.valueOf(subTrans.get("flag")).equals("C")?"借记":"贷记");
					dataMap.put("6",subTrans.get("tranAmount"));
					dataMap.put("7",CustomTransferRecordType.codeOf(Integer.parseInt(String.valueOf(subTrans.get("tranType")))).getDesc());
					dataMap.put("8",subTrans.get("oppAccountNo"));
					dataMap.put("9",subTrans.get("oppAccountName"));
					dataMap.put("10",subTrans.get("oppBankName"));
					dataMap.put("11",subTrans.get("remark"));
					dataMap.put("12",subTrans.get("oppBankNo"));
					dataMap.put("13",subTrans.get("bizFlowNo"));
					dataMap.put("14",ConfirmStatus.codeOf(Integer.parseInt(String.valueOf(subTrans.get("isConfirm")))).getDesc());
					dataMap.put("15",subTrans.get("confirmOrderNo"));
					dataMap.put("16",subTrans.get("confirmDate"));
					dataMap.put("17",subTrans.get("mainAccount"));
					dataMap.put("18",subTrans.get("mainAccountName"));
					data.add(sortMapByKey(dataMap));
				}
				ExcelFileGenerator.ExcelExport(response, headers, filename, data);
			}else{
				logger.error("操作无权限");
			}
		}catch(Exception e){
			logger.error("子账户交易导出异常....");
			logger.error(e.getMessage(), e);
		}
	}
}
