package com.jrmf.taxsettlement.api;

import java.util.List;

public class PageData<T> {

	private int recordTotalCount;
	
	private List<T> pageRecords;

	public int getRecordTotalCount() {
		return recordTotalCount;
	}

	public void setRecordTotalCount(int recordTotalCount) {
		this.recordTotalCount = recordTotalCount;
	}

	public List<T> getPageRecords() {
		return pageRecords;
	}

	public void setPageRecords(List<T> pageRecords) {
		this.pageRecords = pageRecords;
	}
}
