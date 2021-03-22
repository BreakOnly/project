package com.jrmf.persistence;

import com.jrmf.domain.YuncrUserFailNode;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author: YJY
 * @date: 2020/12/14
 * @description:
 */
@Mapper
public interface YuncrUserFailNodeDao {

  int batchInsert(List<YuncrUserFailNode> list);

  List<YuncrUserFailNode> findByAuthenticationId(@Param("authenticationId") List authenticationId);
}
