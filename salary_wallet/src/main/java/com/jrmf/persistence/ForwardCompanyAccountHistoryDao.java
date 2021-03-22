package com.jrmf.persistence;

import com.jrmf.domain.ForwardCompanyAccountHistory;
import com.jrmf.domain.dto.ForwardCompanyAccountHistoryRequestDTO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author: YJY
 * @date: 2020/11/20 10:21
 * @description: 商户记账户历史记录Dao
 */
@Mapper
public interface ForwardCompanyAccountHistoryDao {


  /**
  * @Description 根据条件查询对应数据
  **/
  List<ForwardCompanyAccountHistory> findByCondition(
      ForwardCompanyAccountHistoryRequestDTO merchantTradeRecord);

  int insert(ForwardCompanyAccountHistory forwardCompanyAccountHistory);
}
