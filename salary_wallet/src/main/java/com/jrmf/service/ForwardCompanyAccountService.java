package com.jrmf.service;

import com.github.pagehelper.PageInfo;
import com.jrmf.common.APIResponse;
import com.jrmf.domain.ForwardCompanyAccount;
import com.jrmf.domain.dto.ForwardCompanyAccountRequestDTO;
import com.jrmf.domain.dto.ForwardCompanyAccountUpdateRequestDTO;
import com.jrmf.domain.vo.CompanyAccountVo;
import java.util.List;

/**
 * @author: YJY
 * @date: 2020/11/20 11:00
 * @description: 商户记账户余额
 */
public interface ForwardCompanyAccountService {

  /**
   * @Description 根据条件查询 记账户余额
   **/
  PageInfo<ForwardCompanyAccount> findByCondition(ForwardCompanyAccountRequestDTO requestDTO);
  /**
   * @Description 新增商户记账户
   **/
  APIResponse insert(ForwardCompanyAccount forwardCompanyAccount);


  /**
   * @Description 查询  Balance 单表
   **/
  List<ForwardCompanyAccount> findBalanceByCondition(ForwardCompanyAccount qbCompanyBalance);


  /**
   * @Description 更新余额和状态
   **/
  APIResponse updateById(ForwardCompanyAccountUpdateRequestDTO requestDTO);

  /**
   * @Description 以转包服务公司的角度查询数据列表
   **/
  PageInfo<ForwardCompanyAccount> findCompanyList(ForwardCompanyAccountRequestDTO requestDTO);


  /**
   * @Author YJY
   * @Description 对外修改记账户余额接口
   * @Date  2020/11/25
   * @Param [accountVo]
   * @return boolean
   **/
  APIResponse updateCompanyAccount(CompanyAccountVo accountVo);

}
