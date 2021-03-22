package com.jrmf.splitorder.service;

import com.jrmf.domain.SplitOrderConf;
import com.jrmf.splitorder.domain.CustomSplitOrder;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;
import java.util.Map;

public interface SplitOrderService {
    Map<String, Object> splitOrder(Workbook workBook, CustomSplitOrder customSplitOrder, Integer templateNo);
}
