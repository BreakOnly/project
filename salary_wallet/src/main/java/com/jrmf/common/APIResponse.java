package com.jrmf.common;

import lombok.Data;

@Data
public class APIResponse<T> {

  private Integer state;
  private String respmsg;
  private T data;
  private Long total;

  public static APIResponse successResponse() {
    return successResponse(null);
  }

  public static APIResponse successResponse(Object data) {
    return generateAPIResponse(ResponseCodeMapping.SUCCESS, data);
  }
  public static APIResponse successResponse(Object data,Long total) {
    return generateAPIResponse(ResponseCodeMapping.SUCCESS, data,total);
  }

  public static APIResponse successResponse(Object data, PageObject page) {
    return generateAPIResponse(ResponseCodeMapping.SUCCESS, data, page);
  }

  public static APIResponse errorResponse(ResponseCodeMapping responseCodeMapping) {
    return generateAPIResponse(responseCodeMapping, null);
  }

  public static APIResponse errorResponse(Integer code, String msg) {
    APIResponse apiResponse = new APIResponse();
    apiResponse.setState(code);
    apiResponse.setRespmsg(msg);
    return apiResponse;
  }

  public static APIResponse errorResponse(ResponseCodeMapping responseCodeMapping, Object data) {
    return generateAPIResponse(responseCodeMapping, data);
  }


  private static APIResponse generateAPIResponse(ResponseCodeMapping responseCodeMapping,
      Object data) {
    APIResponse apiResponse = new APIResponse();
    apiResponse.setState(responseCodeMapping.getCode());
    apiResponse.setRespmsg(responseCodeMapping.getMessage());
    apiResponse.setData(data);
    return apiResponse;
  }
  private static APIResponse generateAPIResponse(ResponseCodeMapping responseCodeMapping,
      Object data,Long total) {
    APIResponse apiResponse = new APIResponse();
    apiResponse.setState(responseCodeMapping.getCode());
    apiResponse.setRespmsg(responseCodeMapping.getMessage());
    apiResponse.setData(data);
    apiResponse.setTotal(total);
    return apiResponse;
  }
  private static APIResponse generateAPIResponse(ResponseCodeMapping responseCodeMapping,
      Object data, PageObject page) {
    APIResponse apiResponse = new APIResponse();
    apiResponse.setState(responseCodeMapping.getCode());
    apiResponse.setRespmsg(responseCodeMapping.getMessage());
    apiResponse.setData(data);
    return apiResponse;
  }

  public static APIResponse response(Integer code, String msg,
      Object data, PageObject page) {
    APIResponse apiResponse = new APIResponse();
    apiResponse.setState(code);
    apiResponse.setRespmsg(msg);
    apiResponse.setData(data);
    return apiResponse;
  }


}
