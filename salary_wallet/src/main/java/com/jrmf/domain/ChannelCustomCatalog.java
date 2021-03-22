/**
 * 
 */
package com.jrmf.domain;

import java.io.Serializable;
import java.util.List;


/**
 * @author: liangyu
 * @date: 2016-3-18
 * @description:
 */
public class ChannelCustomCatalog implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -113733692728547012L;
	private int id;//当前目录id
	private String content;   //目录名称
	private String imgUrl;   //目录名称
	private String  link;  //   链接
	private String contentLevel;
	private int parentId;   //父权限
	private List<ChannelCustomCatalog> customCatalog;

	public String getImgUrl() {
		return imgUrl;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}

	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
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
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @return the customCatalog
	 */
	public List<ChannelCustomCatalog> getCustomCatalog() {
		return customCatalog;
	}
	/**
	 * @param customCatalog the customCatalog to set
	 */
	public void setCustomCatalog(List<ChannelCustomCatalog> customCatalog) {
		this.customCatalog = customCatalog;
	}

}
