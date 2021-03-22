package com.jrmf.utils.eazyexcel.read.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.fastjson.JSON;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener 不能被spring管理，要每次读取excel都要new,然后里面用到spring可以构造方法传进去
 */
public class InputBatchListener<T> extends AnalysisEventListener<T> {

  private static final Logger logger = LoggerFactory.getLogger(InputBatchListener.class);

  List<T> list = new ArrayList<>();

  @Override
  public void invoke(T data, AnalysisContext analysisContext) {
//    logger.info("解析到一条数据:{}", JSON.toJSONString(data));
    list.add(data);
  }

  @Override
  public void doAfterAllAnalysed(AnalysisContext analysisContext) {
    logger.info("所有数据解析完成！");
  }

  public List<T> getList() {
    return list;
  }
}
