package com.jrmf.controller.systemrole.merchant.linkconfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.LinkageBaseConfig;
import com.jrmf.domain.LinkageCustomConfig;
import com.jrmf.domain.LinkageTransferRecord;
import com.jrmf.domain.Page;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.LinkConfigService;
import com.jrmf.service.LinkageCustomConfigService;
import com.jrmf.service.LinkageTransferRecordService;
import com.jrmf.utils.DateUtils;
import com.jrmf.utils.RespCode;


@RestController
@RequestMapping("/merchantlinkConfig")
public class MerchantLinkConfigController {

	//商户信息service
	@Autowired
	private ChannelCustomService channelCustomService;
	@Autowired
	private LinkageCustomConfigService linkageCustomConfigService;
	@Autowired
	private LinkConfigService linkConfigService;
	@Autowired
	private LinkageTransferRecordService linkageTransferRecordService;

	/**
	 * 联动账户配置信息查询
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
		//1.操作权限校验
		if(checkFlag){
			List<Map<String, Object>> list = linkageCustomConfigService.getCustomLinkConfigList(page);
			int count = linkageCustomConfigService.getCustomLinkConfigListCount(page);
			result.put("total", count);
			result.put("relationList", list);
		}else{
			respstat = RespCode.DO_NOT_HAVE_APPROVAL_RIGHT;
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return new ResponseEntity<Object>(result, HttpStatus.OK);
	}

	/**
	 * 添加联动配置信息
	 * @param request
	 * @param linkageBaseConfig
	 * @return
	 */
	@RequestMapping("/addLinkConfig")
	public ResponseEntity<?> addLinkConfig(HttpServletRequest request,LinkageCustomConfig linkageCustomConfig){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		Page page = new Page(request);
		//校验是否有权限
		boolean checkFlag = true;
		//获取登陆信息
		ChannelCustom customLogin = (ChannelCustom) request.getSession().getAttribute("customLogin");
		Integer []allowCustomType = new Integer[]{6};
		checkFlag = channelCustomService.getCustomKeysByType(page.getParams(), allowCustomType, customLogin);
		//1.操作权限校验
		if(checkFlag){
			List<LinkageCustomConfig> linkageCustomConfigs = linkageCustomConfigService.getCustomConfigByLinkType(linkageCustomConfig);
			if(linkageCustomConfigs==null||linkageCustomConfigs.size()==0){
				//添加
				ChannelCustom channelCustom = channelCustomService.getCustomByCustomkey(linkageCustomConfig.getCustomKey());
				LinkageBaseConfig linkageBaseConfig = linkConfigService.getLinkConfigById(Integer.parseInt(linkageCustomConfig.getConfigId()));
				if(channelCustom.getCompanyName().equals(linkageBaseConfig.getCorporationAccountName().trim())){
					linkageCustomConfig.setCreateTime(DateUtils.getNowDate());
					linkageCustomConfig.setAddUser(customLogin.getUsername());
					int flag = linkageCustomConfigService.insert(linkageCustomConfig);
					if(flag>0){
						respstat = RespCode.success;
					}else{
						respstat = RespCode.HAPPEND_EXCEPTION;
					}
				}else{
					respstat = RespCode.ACCOUNT_NAME_DIFFERENT;
				}
			}else{
				respstat = RespCode.LINK_CONFIG_USERONE;
			}
		}else{
			respstat = RespCode.DO_NOT_HAVE_APPROVAL_RIGHT;
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return new ResponseEntity<Object>(result, HttpStatus.OK);
	}

	/**
	 * 删除联动基础配置信息
	 * @param id
	 * @return
	 */
	@RequestMapping("/deleteCustomLinkConfig")
	public ResponseEntity<?> deleteCustomLinkConfig(Integer id){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		LinkageCustomConfig config = linkageCustomConfigService.getCustomConfigById(id);
		List<LinkageTransferRecord> linkageTransferRecords = linkageTransferRecordService.checkIsExistRecord(config.getCustomKey());
		if(linkageTransferRecords.size()>0){
			result.put(RespCode.RESP_STAT, RespCode.EXISTS_ACCOUNT_RECORDS);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.EXISTS_ACCOUNT_RECORDS));
		}else{
			linkageCustomConfigService.deleteByPrimaryKey(id);
			result.put(RespCode.RESP_STAT, respstat);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		}
		return new ResponseEntity<Object>(result, HttpStatus.OK);
	}

}
