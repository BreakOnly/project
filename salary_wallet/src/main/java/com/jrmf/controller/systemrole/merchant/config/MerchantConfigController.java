package com.jrmf.controller.systemrole.merchant.config;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jrmf.controller.BaseController;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.ChannelHistory;
import com.jrmf.domain.ChannelPermission;
import com.jrmf.domain.CustomMenu;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.ChannelHistoryService;
import com.jrmf.service.CustomPermissionService;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/merchant/config")
public class MerchantConfigController extends BaseController {

	private static Logger logger = LoggerFactory.getLogger(MerchantConfigController.class);
	@Autowired
	private ChannelCustomService customService;
	@Autowired
	private ChannelHistoryService historyService;

	@Autowired
	private CustomPermissionService customPermissionService;

	/**
	 * 修改权限详情 说明:
	 * 
	 * @param model
	 * @param request
	 * @return:
	 */
	@RequestMapping(value = "/channelUser/savePermission")
	public @ResponseBody HashMap<String, Object> savePermission(Model model, HttpServletRequest request,
			ChannelPermission permission) {
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<String, Object>();
		try {
			if (permission.getId() != 0) {
				customPermissionService.updatePermission(permission);
			} else {
				customPermissionService.savePermission(permission);
			}
		} catch (Exception e) {
			logger.error("", e);
			respstat = RespCode.error000;
			result.put(RespCode.RESP_STAT, respstat);
			result.put(RespCode.RESP_MSG, "修改失败！");
			return result;
		}
		result.put("permission", permission);
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		return result;
	}

	/**
	 * 获取商户项目树 说明:
	 * 
	 * @param request
	 * @return:
	 */
	@RequestMapping(value = "/channelUser/projectTree", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> projectTree(HttpServletRequest request) {
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		
		String customkey = (String) request.getSession().getAttribute("customkey");
		String enabled = request.getParameter("enabled");
		if (StringUtil.isEmpty(enabled)) {
			respstat = RespCode.error101;
			result.put(RespCode.RESP_STAT, respstat);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
			return result;
		}
		HashMap<String, Object> param = new HashMap<String, Object>();
		param.put("originalId", customkey);
		param.put("enabled", Integer.parseInt(enabled) == 0 ? null : Integer.parseInt(enabled));// 传零查所有（有效无效），不传零则按条件查询（有效或无效）
		List<CustomMenu> customMenuByOriginalId = customService.getCustomMenuByOriginalId(param);
		result.put("projectTree", customMenuByOriginalId);
		return result;
	}
	
	
	/**
	 * 获取商户项目树 说明:
	 * 
	 * @param request
	 * @return:
	 */
	@RequestMapping(value = "/channelUser/new/projectTree", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> projectTreeNew(HttpServletRequest request) {
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		
		String nodeCustomKey = null;
		String actAddMenu = request.getParameter("actAddMenu");
		if("1".equals(actAddMenu)){
			nodeCustomKey = (String) request.getSession().getAttribute("customkey");
		}else{
	        nodeCustomKey = request.getParameter("customkey");
		}
		
		String enabled = request.getParameter("enabled");
		if (StringUtil.isEmpty(enabled)) {
			respstat = RespCode.error101;
			result.put(RespCode.RESP_STAT, respstat);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
			return result;
		}
		HashMap<String, Object> param = new HashMap<String, Object>();
		param.put("originalId", nodeCustomKey);
		param.put("enabled", Integer.parseInt(enabled) == 0 ? null : Integer.parseInt(enabled));// 传零查所有（有效无效），不传零则按条件查询（有效或无效）
		List<CustomMenu> customMenuByOriginalId = customService.getCustomMenuByOriginalId(param);
		result.put("projectTree", customMenuByOriginalId);
		return result;
	}

	/**
	 * 添加和修改项目树子节点 说明:
	 * 
	 * @param request
	 * @param customMenu
	 * @return:
	 */
	@RequestMapping(value = "/channelUser/updateProjectTree", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> updateProjectTree(HttpServletRequest request, CustomMenu customMenu) {
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		String id = request.getParameter("id");
		if (StringUtil.isEmpty(id) || "0".equals(id)) {
			// id为空，做添加操作
			String originalId = (String) request.getSession().getAttribute("customkey");
			customMenu.setOriginalId(originalId);
			customMenu.setIsParentNode(-1);// 初始添加是：没有子节点，意味着不是父节点。
			customService.savePermission(customMenu);
			if (customMenu.getParentId() == 0) {
				customMenu.setLevelCode(customMenu.getId() + "-");
			} else {
				CustomMenu parent = customService.getCustomMenuById(customMenu.getParentId());
				// 当给一个节点添加子节点的时候更改isParentNode状态，置为有子节点的状态。
				if (parent.getIsParentNode() != 1) {
					parent.setIsParentNode(1);
					customService.updatePermission(parent);
				}
				customMenu.setLevelCode(parent.getLevelCode() + customMenu.getId() + "-");
			}
			customService.updatePermission(customMenu);
		} else {
			//按照id查询出本次被修改的节点，看是否进行有效性参数的修改
			CustomMenu customMenuById = customService.getCustomMenuById(customMenu.getId());
			customMenu.setIsParentNode(customMenuById.getIsParentNode());//isParentNode 字段在这里需要同步一下。不然会被覆盖成0
			//如果enabled不相等，则修改了enabled
			if(customMenuById.getEnabled() != customMenu.getEnabled()){
				HashMap<String, Object> param = new HashMap<String, Object>();
				param.put("levelCode", customMenuById.getLevelCode());
				List<CustomMenu> nodeTree = customService.getNodeTree(param);
				for (CustomMenu customMenu2 : nodeTree) {
					if(customMenu2.getId() == customMenu.getId()){
						continue;
					}
					if(customMenu.getEnabled() == -1){
						customMenu2.setEnabled(-1);
						customService.updatePermission(customMenu2);
					}else if(customMenu.getEnabled() == 1){
						customMenu2.setEnabled(1);
						customService.updatePermission(customMenu2);
					}
				}
			}
			customService.updatePermission(customMenu);
		}
		return result;
	}

	/**
	 * 删除用户树子节点 说明: 产生过下发数据的项目不让删除
	 * 
	 * @param request
	 * @return:
	 */
	@RequestMapping(value = "/channelUser/deleteProjectTree", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> deleteProjectTree(HttpServletRequest request,
			@RequestParam(required = true) String ids) {
		int respstat = RespCode.success;
		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		String[] nodeIds = ids.split(",");
		for (String id : nodeIds) {
			CustomMenu node = customService.getCustomMenuById(Integer.parseInt(id));
			if (node.getIsParentNode() == -1) {
				HashMap<String, Object> param1 = new HashMap<String, Object>();
				param1.put("menuId", id);
				List<ChannelHistory> channelHistoryByParam = historyService.getChannelHistoryByParam(param1);
				//如果待删除节点下有下发数据，则不能删除。
				if(channelHistoryByParam.size() != 0){
					respstat = RespCode.error107;
					result.put(RespCode.RESP_STAT, respstat);
					result.put(RespCode.RESP_MSG, "节点包含批次数据，无法删除。");
					return result;
				}
				customService.deleteNodeById(id);
				/**
				 * 查看已删除节点的父节点还有没有子节点。如果没有了，则把它的父节点置为 isParentNode = -1
				 */
				HashMap<String, Object> param2 = new HashMap<String, Object>();
				CustomMenu parant = customService.getCustomMenuById(node.getParentId());//获取父节点
				if(parant != null){
					param2.put("levelCode", parant.getLevelCode());
					List<CustomMenu> nodeTree = customService.getNodeTree(param2);
					if(nodeTree.size()==1){
						parant.setIsParentNode(-1);
						customService.updatePermission(parant);
					}
				}
			} else {
				respstat = RespCode.error107;
				result.put(RespCode.RESP_STAT, respstat);
				result.put(RespCode.RESP_MSG, "节点包含子节点，请先删除其下所有子节点再尝试删除此节点。");
				return result;
			}
		}
		return result;
	}

	/**
	 * 获取当前节点以及其子节点的信息 说明:
	 * 
	 * @param request
	 * @return:
	 */
	@RequestMapping(value = "/channelUser/getCustomMenuList", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> getCustomMenuList(HttpServletRequest request) {
		int respstat = RespCode.success;
		Map<String, Object> result = new HashMap<String, Object>();
		result.put(RespCode.RESP_STAT, respstat);
		result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
		String originalId = (String) request.getSession().getAttribute("customkey");
		String id = request.getParameter("id");
		String pageNo = request.getParameter("pageNo");
		String enabled = request.getParameter("enabled");// 项目状态
		String contentName = request.getParameter("contentName");// 项目名称
		String createTimeStart = request.getParameter("createTimeStart");// 创建时间起始
		String createTimeEnd = request.getParameter("createTimeEnd");// 创建时间结束
		if (StringUtil.isEmpty(id) || StringUtil.isEmpty(pageNo)) {
			respstat = RespCode.error101;
			result.put(RespCode.RESP_STAT, respstat);
			result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(respstat));
			return result;
		}
		List<CustomMenu> nodeTree = null;
		int total = 0;
		Map<String, Object> param = new HashMap<String, Object>();

		PageInfo page;

		if ("0".equals(id)) {
			param.put("originalId", originalId);
			param.put("enabled", StringUtil.isEmpty(enabled) ? null : Integer.parseInt(enabled));
			param.put("contentName", contentName);
			param.put("createTimeStart", createTimeStart);
			param.put("createTimeEnd", createTimeEnd);

			PageHelper.startPage(Integer.parseInt(pageNo), 10);
			nodeTree = customService.getCustomMenuByOriginalId(param);
			page = new PageInfo(nodeTree);
		} else {
			CustomMenu customMenuById = customService.getCustomMenuById(Integer.parseInt(id));
			if (customMenuById == null) {
				result.put("total", total);
				result.put("nodeTree", nodeTree);
				return result;
			}
			param.put("levelCode", customMenuById.getLevelCode());
			param.put("enabled", StringUtil.isEmpty(enabled) ? null : Integer.parseInt(enabled));
			param.put("contentName", contentName);
			param.put("createTimeStart", createTimeStart);
			param.put("createTimeEnd", createTimeEnd);

			PageHelper.startPage(Integer.parseInt(pageNo), 10);
			nodeTree = customService.getNodeTree(param);
			page = new PageInfo(nodeTree);
		}
		result.put("total", page.getTotal());
		result.put("nodeTree", page.getList());
		return result;
	}
}
