package com.jrmf.domain;

import java.io.Serializable;

/** 
* @author 种路路 
* @version 创建时间：2017年8月31日 下午8:28:05 
* 类说明 
* 参数：

列表顺序：我的红包，我的理财，我的保险，我的基金，账户信息，交易记录，安全设置，银行卡设置


item_icon
item_title
item_url
*/
public class Items implements Serializable{

	/** 
	* @Fields serialVersionUID : TODO() 
	*/ 
	
	private static final long serialVersionUID = -2462211033925507453L;
	private String item_icon;
	private String item_title;
	private String item_url;
	private String type;//0表示url类型；1 我的红包，2账户信息，3交易记录4安全设置5银行卡设置
	public String getItem_icon() {
		return item_icon;
	}
	public void setItem_icon(String item_icon) {
		this.item_icon = item_icon;
	}
	public String getItem_title() {
		return item_title;
	}
	public void setItem_title(String item_title) {
		this.item_title = item_title;
	}
	public String getItem_url() {
		return item_url;
	}
	public void setItem_url(String item_url) {
		this.item_url = item_url;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
}
 