package com.jrmf.domain;

import java.io.Serializable;
import lombok.Data;

/**
 * @author: YJY
 * @date: 2021/1/8 16:58
 * @description:
 */
@Data
public class ApplyBatchInvoiceAmount implements Serializable {

  private int id;

  private String  idCard;

  private String tradeMonth;

  private String tradeMoney;
}
