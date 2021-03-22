package com.jrmf.persistence;

import com.jrmf.domain.ForwardCompanyAccount;
import com.jrmf.domain.dto.ForwardCompanyAccountRequestDTO;
import com.jrmf.domain.dto.ForwardCompanyAccountUpdateRequestDTO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author: YJY
 * @date: 2020/11/20 14:34
 * @description: 商户记账户
 */
@Mapper
public interface ForwardCompanyAccountDao {


  /**
  * @Description 根据条件查询 记账户余额
  **/
  List<ForwardCompanyAccount> findByCondition(ForwardCompanyAccountRequestDTO requestDTO);

  /**
  * @Description 新增商户记账户
  **/
  int  insert(ForwardCompanyAccount forwardCompanyAccount);

  /**
  * @Description 查询  Balance 单表
  **/
  List<ForwardCompanyAccount> findBalanceByCondition(ForwardCompanyAccount forwardCompanyAccount);

  /**
  * @Description 更新余额和状态
  **/
  int updateById(ForwardCompanyAccountUpdateRequestDTO qbCompanyBalance);

  /**
  * @Description 以服务公司的角度查询数据列表
  **/
  List<ForwardCompanyAccount> findCompanyList(ForwardCompanyAccountRequestDTO requestDTO);

  int updateAccountBalance(ForwardCompanyAccount forwardCompanyAccount);

  ForwardCompanyAccount findByUpdate(int id);

}
