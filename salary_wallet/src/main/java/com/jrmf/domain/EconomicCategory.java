package com.jrmf.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class EconomicCategory implements Serializable {

  private Integer id;

  private Integer level;

  private String levelCode;

  private Integer value;

  private String labelName;
}
