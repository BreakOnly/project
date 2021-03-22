package com.jrmf.controller.systemrole.merchant.user;

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
import com.jrmf.controller.constant.CertType;
import com.jrmf.controller.constant.CustomType;
import com.jrmf.controller.constant.LoginRole;
import com.jrmf.controller.constant.QueryType;
import com.jrmf.controller.constant.WhiteUserStatusType;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.Page;
import com.jrmf.domain.WhiteUser;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.OrganizationTreeService;
import com.jrmf.service.WhiteUserService;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.ExcelFileGenerator;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;

/**
 * 白名单管理
 * @author 孙春辉
 *
 */
@RestController
@RequestMapping("/whiteUser")
public class WhiteUserController extends BaseController {

	private static Logger logger = LoggerFactory.getLogger(WhiteUserController.class);

	//商户用户白名单service
	@Autowired
	private WhiteUserService whiteUserService;

	//商户信息service
	@Autowired
	private ChannelCustomService channelCustomService;

	//商户组织service
	@Autowired
	private OrganizationTreeService organizationTreeService;

	/**
	 * 白名单列表查询
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
		Integer []allowCustomType = new Integer[]{CustomType.CUSTOM.getCode(),CustomType.COMPANY.getCode(),CustomType.GROUP.getCode(),6};
		checkFlag = getCustomKeysByType(page.getParams(), allowCustomType, customLogin);
		if(checkFlag){
			//分页查询白名单信息
			List<Map<String, Object>> relationList = whiteUserService.getWhiteUsersByPage(page);
			//查询白名单总条数
			int count = whiteUserService.getWhiteUsersCount(page);
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
	 * 导出
	 * @param request
	 */
	@RequestMapping("/export")
	public void export(HttpServletRequest request,HttpServletResponse response){
		// 标题
		String[] headers = new String[] {"商户名称", "用户名称","证件类型","证件号","状态","下发公司","用户创建时间","备注","最后更新时间"};
		String filename = "商户白名单"; 
		Page page = new Page(request);
		//校验是否有权限
		boolean checkFlag = true;
		//获取登陆信息
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		Integer []allowCustomType = new Integer[]{CustomType.CUSTOM.getCode(),CustomType.COMPANY.getCode(),CustomType.GROUP.getCode(),6};
		checkFlag = getCustomKeysByType(page.getParams(), allowCustomType, customLogin);
		if(checkFlag){
			//分页查询白名单信息
			List<Map<String, Object>> relationList = whiteUserService.getWhiteUsersNoPage(page);
			List<Map<String, Object>> data = new ArrayList<>();
			for (Map<String, Object> whiteUser : relationList) {
				Map<String, Object> dataMap = new HashMap<>(20);
				dataMap.put("1",whiteUser.get("merchantName"));
				dataMap.put("2",whiteUser.get("userName"));
				dataMap.put("3",CertType.codeOf(Integer.parseInt(String.valueOf(whiteUser.get("documentType")))).getDesc());
				dataMap.put("4",whiteUser.get("certId"));
				dataMap.put("5",WhiteUserStatusType.codeOf(Integer.parseInt(String.valueOf(whiteUser.get("status")))).getDesc());
				dataMap.put("6",whiteUser.get("companyName"));
				dataMap.put("7",whiteUser.get("createTime"));
				dataMap.put("8",whiteUser.get("remark"));
				dataMap.put("9",whiteUser.get("updateTime"));
				data.add(sortMapByKey(dataMap));
			}
			ExcelFileGenerator.ExcelExport(response, headers, filename, data);
		}else{
			logger.info("权限不足，无法操作");
		}
	}

	/**
	 * 保存白名单用户
	 * @param whiteUser
	 * @return
	 */
	@RequestMapping("/saveWhiteUser")
	public ResponseEntity<?> saveWhiteUser(HttpServletRequest request, WhiteUser whiteUser){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		try{
			//校验是否有权限
			boolean checkFlag = false;
			//获取登陆信息
			ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
			if(customLogin.getCustomType()==4&&customLogin.getMasterCustom()!=null){
				ChannelCustom masterChannelCustom = channelCustomService.getCustomByCustomkey(customLogin.getMasterCustom());
				if(masterChannelCustom.getCustomType()==1||masterChannelCustom.getCustomType()==5){
					//只有商户/集团商户有增加、修改、删除权限
					checkFlag = true;
				}
			}else{
				if(customLogin.getCustomType()==1||customLogin.getCustomType()==5){
					//只有商户/集团商户有增加、修改、删除权限
					checkFlag = true;
				}
			}
			if(checkFlag){
				if(whiteUser.getId()==null){
					//校验是否已经存在当前白名单用户
					int count = whiteUserService.checkIsExists(whiteUser);
					if(count<=0){
						whiteUser.setStatus(0);
						whiteUser.setCreateTime(DateUtils.getNowDate());
						whiteUser.setAddUser(customLogin.getUsername());
						int respFlag = whiteUserService.insert(whiteUser);
						if(respFlag>0){
							//新增成功
							respstat = RespCode.success;
						}else{
							//新增失败
							respstat = RespCode.INSERT_FAIL;
						}
					}else{
						respstat = RespCode.error103;
					}
				}else{
					int respFlag=0;
					WhiteUser whiteUserOld = whiteUserService.getOne(whiteUser.getId());
					if(whiteUserOld.getDocumentType()==whiteUser.getDocumentType()&&whiteUserOld.getCompanyId().equals(whiteUser.getCompanyId())&&whiteUserOld.getCustomkey().equals(whiteUser.getCustomkey())&&whiteUserOld.getCertId().equals(whiteUser.getCertId())){
						//修改
						whiteUser.setUpdateTime(DateUtils.getNowDate());
						whiteUser.setAddUser(customLogin.getUsername());
						whiteUser.setStatus(0);
						respFlag = whiteUserService.update(whiteUser);
						if(respFlag>0){
							//修改成功
							respstat = RespCode.success;
						}else{
							//修改失败
							respstat = RespCode.INSERT_FAIL;
						}
					}else{
						int count = whiteUserService.checkIsExists(whiteUser);
						if(count>0){
							respstat = RespCode.error103;
						}else{
							//修改
							whiteUser.setUpdateTime(DateUtils.getNowDate());
							whiteUser.setAddUser(customLogin.getUsername());
							whiteUser.setStatus(0);
							respFlag = whiteUserService.update(whiteUser);
							if(respFlag>0){
								//修改成功
								respstat = RespCode.success;
							}else{
								//修改失败
								respstat = RespCode.INSERT_FAIL;
							}
						}	
					}

				}
			}else{
				//没有权限
				respstat = RespCode.DO_NOT_HAVE_APPROVAL_RIGHT;
			}
		}catch(Exception e){
			logger.error("白名单新增或者修改异常", e);
			respstat = RespCode.HAPPEND_EXCEPTION;
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return new ResponseEntity<Object>(result,HttpStatus.OK);
	}

	/**
	 * 删除白名单用户
	 * @param qBlackUsers
	 * @param request
	 * @return
	 */
	@RequestMapping("/deleteWhiteUser")
	public ResponseEntity<?> deleteWhiteUser(Integer id,HttpServletRequest request){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		try{
			if(id==null){
				//必填参数校验
				respstat = RespCode.REQUIRED_PARAMS_ISNULL;
			}else{
				//校验是否有权限
				boolean checkFlag = false;
				//获取登陆信息
				ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
				if(customLogin.getCustomType()==4&&customLogin.getMasterCustom()!=null){
					ChannelCustom masterChannelCustom = channelCustomService.getCustomByCustomkey(customLogin.getMasterCustom());
					if(masterChannelCustom.getCustomType()==1||masterChannelCustom.getCustomType()==5){
						//只有商户/集团商户有增加、修改、删除权限
						checkFlag = true;
					}
				}else{
					if(customLogin.getCustomType()==1||customLogin.getCustomType()==5){
						//只有商户/集团商户有增加、修改、删除权限
						checkFlag = true;
					}
				}
				if(checkFlag){
					//删除白名单用户
					int respFlag = whiteUserService.deleteWhiteUserById(id);
					if(respFlag>0){
						respstat = RespCode.success;
					}else{
						respstat = RespCode.DELETE_FAIL;
					}
				}else{
					respstat = RespCode.DO_NOT_HAVE_APPROVAL_RIGHT;
				}
			}
		}catch(Exception e){
			logger.error("白名单删除异常", e);
			respstat = RespCode.HAPPEND_EXCEPTION;
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return new ResponseEntity<Object>(result,HttpStatus.OK);
	}

	/**
	 * 审核/失效白名单用户
	 * @param id
	 * @param request
	 * @return
	 */
	@RequestMapping("/reviewWhiteUser")
	public ResponseEntity<?> reviewWhiteUser(Integer id,HttpServletRequest request,Integer status,String reason){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		//获取登陆信息
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		try{
			//审核必填参数校验
			Boolean checkFlag = checkReviewRequiredParams(id,status,reason);
			if(checkFlag){
				WhiteUser whiteUser = whiteUserService.getOne(id);
				if(status==1||status==2){
					//通过、驳回操作
					if(whiteUser.getStatus()==0){
						//只有待审核状态满足操作
						whiteUser.setStatus(status);
						whiteUser.setUpdateTime(DateUtils.getNowDate());
						whiteUser.setReviewUser(customLogin.getUsername());
						if(status==2){
							whiteUser.setReason(reason);
						}
						int respFlag = whiteUserService.updateStatusById(whiteUser);
						if(respFlag>0){
							respstat = RespCode.success;
						}else{
							respstat = RespCode.UPDATE_FAIL;
						}
					}else{
						//状态已落地，无法再次操作
						respstat = RespCode.CURRENT_STATUS_REFUSE;
					}
				}else if(status == 3){
					//失效操作需审核通过的白名单用户
					if(whiteUser.getStatus()==1){
						whiteUser.setStatus(status);
						whiteUser.setUpdateTime(DateUtils.getNowDate());
						whiteUser.setReviewUser(customLogin.getUsername());
						int respFlag = whiteUserService.updateStatusById(whiteUser);
						if(respFlag>0){
							respstat = RespCode.success;
						}else{
							respstat = RespCode.UPDATE_FAIL;
						}
					}else{
						//其他状态则不满足操作
						respstat = RespCode.NOT_SUPPORT_STATUS;
					}
				}else{
					//无法支持的操作
					respstat = RespCode.error101;
				}
			}else{
				//必填参数为空
				respstat = RespCode.REQUIRED_PARAMS_ISNULL;
			}
		}catch(Exception e){
			logger.error("白名单审核异常", e);
			respstat = RespCode.HAPPEND_EXCEPTION;
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return new ResponseEntity<Object>(result,HttpStatus.OK);
	}

	/**
	 * 校验审核必填参数
	 * @return
	 */
	public boolean checkReviewRequiredParams(Integer id,Integer status,String reason){
		//检验结果
		boolean checkFlag = true;
		if(status==null||id==null){
			checkFlag = false;
		}else{
			if(status==2){
				//驳回状态原因为必填
				if(StringUtil.isEmpty(reason)){
					checkFlag = false;
				}
			}
		}
		return checkFlag;		
	}

	/**
	 * 配合前端进行按钮的显示
	 * @param request
	 * @return
	 */
	@RequestMapping("/checkShowButton")
	public ResponseEntity<?> checkShowButton(HttpServletRequest request){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		Boolean showOperating = false;
		Boolean showReview = false;	
		//获取登陆信息
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		if(customLogin.getCustomType()==4&&customLogin.getMasterCustom()!=null){
			ChannelCustom masterChannelCustom = channelCustomService.getCustomByCustomkey(customLogin.getMasterCustom());
			if((masterChannelCustom.getCustomType()==1&&!CommonString.ROOT.equals(masterChannelCustom.getCustomkey()))||masterChannelCustom.getCustomType()==5){
				//只有商户/集团商户有增加、修改、删除权限
				showOperating = true;
			}else if(masterChannelCustom.getCustomType()==CustomType.COMPANY.getCode()){
				//下发公司有审核操作
				showReview = true;
			}
		}else{
			if((customLogin.getCustomType()==1&&!CommonString.ROOT.equals(customLogin.getCustomkey()))||customLogin.getCustomType()==5){
				//只有商户/集团商户有增加、修改、删除权限
				showOperating = true;
			}else if(customLogin.getCustomType()==CustomType.COMPANY.getCode()){
				//下发公司有审核操作
				showReview = true;
			}
		}
		result.put("showOperating", showOperating);
		result.put("showReview", showReview);
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return new ResponseEntity<Object>(result,HttpStatus.OK);
	}


	/**
	 * 校验是否有权限根据商户类型
	 * @param paramsMap
	 * @param customTypes
	 * @param customLogin
	 * @return
	 */
	public boolean getCustomKeysByType(Map<String, String> paramsMap,Integer []customTypes,ChannelCustom customLogin) {
		boolean checkFlag = false;
		if(customLogin.getCustomType()==4&&customLogin.getMasterCustom()!=null){
			customLogin = channelCustomService.getCustomByCustomkey(customLogin.getMasterCustom());
		}
		//校验是否有权限
		for (Integer customType : customTypes) {
			if(CommonString.ROOT.equals(customLogin.getCustomkey())&&customType==6){
				checkFlag = true;
				break;
			}else{
				if(customType == customLogin.getCustomType()){
					checkFlag = true;
					break;
				}	
			}
		}
		if(checkFlag){
			if(CommonString.ROOT.equals(customLogin.getCustomkey())){
				//超管
			}else if(customLogin.getCustomType() == CustomType.COMPANY.getCode()){
				//下发公司
				paramsMap.put("companyId", customLogin.getCustomkey());
			}else if(customLogin.getCustomType() == CustomType.GROUP.getCode()){
				//集团商户
				int nodeId = organizationTreeService.queryNodeIdByCustomKey(customLogin.getCustomkey());
				List<String> customKeyList = organizationTreeService.queryNodeCusotmKey(customLogin.getCustomType(), QueryType.QUERY_CURRENT_AND_CHILDREN, nodeId);
				String customKeys = String.join(",", customKeyList);
				paramsMap.put("customkey", customKeys);
			}else if(customLogin.getCustomType() == CustomType.CUSTOM.getCode()){
				//普通商户
				paramsMap.put("customkey", customLogin.getCustomkey());
			}
		}
		return checkFlag;
	}
}
