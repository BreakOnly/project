package com.jrmf.common;

import lombok.Data;

@Data
public class ServiceResponse<T> {

  private String code;
  private String msg;
  private T data;
  private PageObject page;



  public static ServiceResponse errorResponse(String code, String msg) {
     ServiceResponse apiResponse = new ServiceResponse();
    apiResponse.setCode(code);
    apiResponse.setMsg(msg);
    return apiResponse;
  }



  public static ServiceResponse response(String code, String msg,
      Object data, PageObject page) {
     ServiceResponse apiResponse = new ServiceResponse();
    apiResponse.setCode(code);
    apiResponse.setMsg(msg);
    apiResponse.setData(data);
    apiResponse.setPage(page);
    return apiResponse;
  }


}
