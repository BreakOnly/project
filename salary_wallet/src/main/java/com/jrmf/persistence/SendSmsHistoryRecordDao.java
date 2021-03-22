package com.jrmf.persistence;

import com.jrmf.domain.SendSmsHistoryRecord;
import java.util.HashMap;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author: YJY
 * @date: 2020/9/10 10:14
 * @description:
 */
@Mapper
public interface SendSmsHistoryRecordDao {

  /**
   * @Author YJY
   * @Description 批量插入
   * @Date  2020/10/28
   * @Param [list]
   * @return void
   **/
  int batchInsert(List<SendSmsHistoryRecord> list);

  /**
   * @Author YJY
   * @Description 根据条件查询
   * @Date  2020/10/28
   * @Param [customKey]
   * @return java.util.List<com.jrmf.domain.SendSmsHistoryRecord>
   **/
  List<SendSmsHistoryRecord> selectByCondition(@Param("customKey")String customKey);

  /**
   * @Author YJY
   * @Description 查询日发送量超限的手机号
   * @Date  2020/10/28
   * @Param [customKey]
   * @return java.util.List<com.jrmf.domain.SendSmsHistoryRecord>
   **/
  List<SendSmsHistoryRecord> findCountLimit(@Param("customKey")String customKey);

  /**
   * @Author YJY
   * @Description 批量更新
   * @Date  2020/10/28
   * @Param [customKey, list]
   * @return int
   **/
  int batchUpdate(@Param("customKey")String customKey,@Param("list") List list);

  /**
   * @Author YJY
   * @Description 根据名称和商户查询有无此人
   * @Date  2020/10/29
   * @Param [name, customKey]
   * @return int
   **/
  Integer checkByUserName(@Param("name")String name,@Param("customKey")String customKey);


}
