package com.jrmf.common;

import java.io.Serializable;
import lombok.Data;

@Data
public class PageObject implements Serializable {

  private static final long serialVersionUID = -3806764885999501760L;

  private Integer pageNo;
  private Integer pageSize;
  private Integer offset;
  private Long totalElements;
  private Integer totalPages;

  public static class Builder {

    PageObject pageObject = new PageObject();

    public Builder pageNo(Integer pageNo) {
      if (pageNo != null) {
        pageObject.pageNo = pageNo;
      }
      return this;
    }

    public Builder pageSize(Integer pageSize) {
      if (pageSize != null) {
        pageObject.pageSize = pageSize;
      }
      return this;
    }

    public Builder offset(Integer offset) {
      if (offset != null) {
        pageObject.offset = offset;
      }
      return this;
    }

    public Builder totalElements(Long totalElements) {
      if (totalElements != null) {
        pageObject.totalElements = totalElements;
      }
      return this;
    }

    public Builder totalPages(Integer totalPages) {
      if (totalPages != null) {
        pageObject.totalPages = totalPages;
      }
      return this;
    }

    public PageObject build() {
      return pageObject;
    }
  }

}
