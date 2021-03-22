/**
 * 
 */
package com.jrmf.domain;

import java.io.Serializable;

/**
 * @author: 张泽辉
 * @date: 2018-10-16
 * @description:
 */
public class CustomMenu implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4913875291946575903L;
	private int id;
	private String contentName; //项目名称
	private String contentLevel;
	private int parentId;   
	private String createTime;
	private String originalId;
	private int enabled;// 1启用 -1禁用
	private String remark;
	private int showLevel;
	private int isShow;
	private String levelCode;//项目层级编码
	private String updateTime;//最后更新时间
	private int isParentNode;
	private Integer menuType;
	private String projectId;
	
	public String getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}
	public int getShowLevel() {
		return showLevel;
	}
	public void setShowLevel(int showLevel) {
		this.showLevel = showLevel;
	}
	public int getIsShow() {
		return isShow;
	}
	public void setIsShow(int isShow) {
		this.isShow = isShow;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getContentName() {
		return contentName;
	}
	public void setContentName(String contentName) {
		this.contentName = contentName;
	}
	public String getContentLevel() {
		return contentLevel;
	}
	public void setContentLevel(String contentLevel) {
		this.contentLevel = contentLevel;
	}
	public int getParentId() {
		return parentId;
	}
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public String getOriginalId() {
		return originalId;
	}
	public void setOriginalId(String originalId) {
		this.originalId = originalId;
	}
	public int getEnabled() {
		return enabled;
	}
	public void setEnabled(int enabled) {
		this.enabled = enabled;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getLevelCode() {
		return levelCode;
	}
	public void setLevelCode(String levelCode) {
		this.levelCode = levelCode;
	}
	public int getIsParentNode() {
		return isParentNode;
	}
	public void setIsParentNode(int isParentNode) {
		this.isParentNode = isParentNode;
	}

	public Integer getMenuType() {
		return menuType;
	}

	public void setMenuType(Integer menuType) {
		this.menuType = menuType;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}
}
