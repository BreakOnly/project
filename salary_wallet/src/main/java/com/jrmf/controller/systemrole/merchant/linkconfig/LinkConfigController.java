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
@RequestMapping("/linkConfig")
public class LinkConfigController {

	//商户信息service
	@Autowired
	private ChannelCustomService channelCustomService;
	//联动基础配置service
	@Autowired
	private LinkConfigService linkConfigService;
	//商户联动关联service
	@Autowired
	private LinkageCustomConfigService linkageCustomConfigService;
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
			List<Map<String, Object>> list = linkConfigService.getLinkConfigList(page);
			int count = linkConfigService.getLinkConfigListCount(page);
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
	public ResponseEntity<?> addLinkConfig(HttpServletRequest request,LinkageBaseConfig linkageBaseConfig){
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
			//添加
			if(linkageBaseConfig.getId()==null){
				linkageBaseConfig.setStatus(0);
				linkageBaseConfig.setCreateTime(DateUtils.getNowDate());
				linkageBaseConfig.setAddUser(customLogin.getUsername());
				linkConfigService.insert(linkageBaseConfig);
			}else{
				//修改
				if(linkageBaseConfig.getStatus()==1){
					//失效操作
					List<LinkageTransferRecord> linkageTransferRecords = linkageTransferRecordService.checkIsExistRecordByConfigId(linkageBaseConfig.getId());
					if(linkageTransferRecords.size()>0){
						respstat = RespCode.EXISTS_ACCOUNT_RECORDS;
					}else{
						List<LinkageCustomConfig> customConfigs = linkageCustomConfigService.getLinkConfigByConfigId(String.valueOf(linkageBaseConfig.getId()));
						if(customConfigs.size()>0){
							respstat = RespCode.LINK_CONFIG_USE;
						}else{
							linkageBaseConfig.setUpdateTime(DateUtils.getNowDate());
							linkConfigService.update(linkageBaseConfig);
						}
					}
				}else{
					linkageBaseConfig.setUpdateTime(DateUtils.getNowDate());
					linkConfigService.update(linkageBaseConfig);
				}
			}
		}else{
			respstat = RespCode.DO_NOT_HAVE_APPROVAL_RIGHT;
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return new ResponseEntity<Object>(result, HttpStatus.OK);
	}

	/**
	 * 获取付款通道信息
	 * @return
	 */
	@RequestMapping("/getPathInfo")
	public ResponseEntity<?> getPathInfo(){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		List<Map<String, String>> pathInfoList = linkConfigService.getPathInfo();
		result.put("relationList", pathInfoList);
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return new ResponseEntity<Object>(result, HttpStatus.OK);
	}

	/**
	 * 删除联动基础配置信息
	 * @param id
	 * @return
	 */
	@RequestMapping("/deleteLinkConfig")
	public ResponseEntity<?> deleteLinkConfig(Integer id){
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<>();
		List<LinkageCustomConfig> customConfigs = linkageCustomConfigService.getLinkConfigByConfigId(String.valueOf(id));
		if(customConfigs.size()>0){
			//有商户引用则无法删除
			respstat = RespCode.LINK_CONFIG_USE;
		}else{
			linkConfigService.deleteByPrimaryKey(id);
		}
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return new ResponseEntity<Object>(result, HttpStatus.OK);
	}
}
