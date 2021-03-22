package com.jrmf.domain;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Scope;
@Scope("prototype")
public class Page {
	
    private int pageNo = 1;//页码，默认是第一页
    private int pageSize = 10;//每页显示的记录数，默认是15
    private int totalRecord=0;//总记录数
    private int totalPage;//总页数
    private int offset;

    private Map<String, String> params=new HashMap<String,String>();//其他的参数我们把它分装成一个Map对象
    
    public Page(HttpServletRequest request){        	
    	Enumeration enu=request.getParameterNames();  
		while(enu.hasMoreElements()){  
			String paraName=(String)enu.nextElement();
			String notInclude="_,pageNo,pageSize";//参数，排除分页等参数
			if(!notInclude.contains(paraName)){
				params.put(paraName, request.getParameter(paraName).trim());
			}
			if(request.getParameter("pageNo")!=null){
	    		this.setPageNo(Integer.parseInt(request.getParameter("pageNo")));
	    		this.setPageSize(Integer.parseInt(request.getParameter("pageSize")==null?"10":request.getParameter("pageSize")));
			}			
		}
		this.setOffset((this.getPageNo()-1)*this.getPageSize());
    }

    public Page(String pageNo, String pageSize) {
        this.setPageNo(Integer.parseInt(pageNo));
        this.setPageSize(Integer.parseInt(pageSize));
        this.setOffset((this.getPageNo() - 1) * this.getPageSize());
    }
    
	public int getPageNo() {
		return pageNo;
	}
	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public int getTotalRecord() {
		return totalRecord;
	}
    public void setTotalRecord(int totalRecord) {
        this.totalRecord = totalRecord;
     }
	public int getTotalPage() {
		return totalPage;
	}
	public void setTotalPage(int totalPage) {
        //在设置总页数的时候计算出对应的总页数，在下面的三目运算中加法拥有更高的优先级，所以最后可以不加括号。
        this.totalPage = totalRecord%pageSize==0 ? totalRecord/pageSize : totalRecord/pageSize + 1;
	}
	public Map<String, String> getParams() {
		return params;
	}
	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

    
}
