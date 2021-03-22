package com.jrmf.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class ProvinceCityArea implements Serializable {

  private Integer provinceId;

  private String provinceName;

  private Integer cityId;

  private String cityName;

  private Integer areaId;

  private String areaName;
}
