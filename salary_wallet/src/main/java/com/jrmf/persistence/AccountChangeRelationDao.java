package com.jrmf.persistence;


import com.jrmf.domain.AccountChangeRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface AccountChangeRelationDao {

    int deleteByPrimaryKey(Integer id);

    int insert(AccountChangeRelation record);

    int insertSelective(AccountChangeRelation record);

    AccountChangeRelation selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(AccountChangeRelation record);

    int updateByPrimaryKey(AccountChangeRelation record);

    List<Map<String, Object>> changeAccountList(@Param("accountId") Integer accountId, @Param("changeAccountId") Integer changeAccountId);

    /**
     * 通过id查询账户切换列表信息
     * @param id
     * @return
     */
    AccountChangeRelation getAccountChangeRelationById(@Param("id") String id);

    /**
     * 通过id删除账号切换列表信息
     * @param id
     */
    void deleteAccountChangeRelationById(@Param("id") String id);

    /**
     * 新增账号切换列表信息
     * @param accountId
     * @param customKey
     * @param customName
     * @param changeAccountId
     * @param changeAccountName
     */
    void insertAccountChangeRelation(AccountChangeRelation accountChangeRelation);

    /**
     * 修改账号切换列表信息
     * @param accountChangeRelation
     */
    void updateAccountChangeRelation(AccountChangeRelation accountChangeRelation);

    /**
     * 通过id 查询accountId 和 changeAccountId相同的数据
     * @param id
     * @return
     */
    AccountChangeRelation getAccountChangeRelationByAccountId(@Param("id") int id);

    /**
     * 通过 accountId 和 changeAccountId 查询
     * @return
     */
    AccountChangeRelation getAccountByChangeIdAndChangeAccountId(@Param("accountId") int accountId, @Param("changeAccountId") int changeAccountId);
}