package com.jrmf.common;

import java.util.ArrayList;
import java.util.List;

public class DataValue {
	private String[] 		title;				//标题名称。
	private List<String[]> 	value;				//值域集合.
	
	private List<String[]>  illegal  = new ArrayList<String[]>(0);				//不合法的数据.
	private String[] 		oldTitle = new String[0];							//中文名称.
	
	public String[] getTitle() {
		return title;
	}
	public void setTitle(String[] title) {
		this.title = title;
	}
	public List<String[]> getValue() {
		return value;
	}
	public void setValue(List<String[]> value) {
		this.value = value;
	}
	public List<String[]> getIllegal() {
		return illegal;
	}
	
	public String[] getOldTitle() {
		return oldTitle;
	}
	public void setOldTitle(String[] oldTitle) {
		this.oldTitle = oldTitle;
	}
	
}
