package com.jrmf.persistence;


import com.jrmf.domain.ApiRequestData;
import com.jrmf.domain.OpenUser;
import com.jrmf.domain.YuncrUserAuthentication;
import com.jrmf.domain.YuncrUserBank;
import com.jrmf.domain.YuncrUserFailNode;
import com.jrmf.domain.dto.YuncrUserAuthenticationRequestDTO;
import com.jrmf.domain.vo.YuncrUserBankVO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author: YJY
 * @date: 2020/9/10 10:14
 * @description:
 */
@Mapper
public interface YuncrUserAuthenticationDao {


  /**
  * @Description 个体户注册审核 查询
  **/
  List<YuncrUserAuthentication> findUserByCondition(YuncrUserAuthenticationRequestDTO yuncrUserAuthenticationRequestDTO);
  /**
   * @Description 根据商户查询证件号
   **/
  List<HashMap> findIdCardByCompanyName(String companyName);
  /**
   * @Description 根据证件号查询所属商户
   **/
  List<HashMap> findCustomByUserIdCard(@Param("idCard") List idCard);
  /**
   * @Description 根据审核表查询所属银行信息
   **/
  List<HashMap> findBankInfoByUserId(@Param("ids") List ids);
  /**
   * @Description 根据手机号查询微信信息
   **/
  OpenUser findWeChatInfo(@Param("id") String id);
  /**
   * @Description 根据id 查询个体认证信息
   **/
  YuncrUserAuthentication findUserInfoById(Integer id);

  /**
  * @Description 根据错误码查询返回信息
  **/
  HashMap  selectErrByCode(@Param("errCode")String errCode);

   int updateById(YuncrUserAuthentication update);
   /**
   * @Description 根据证件查询用户信息
   **/
   List<HashMap> findUsersByIdCard(String idCard);

   /**
   * @Description 更新用户认证信息
   **/
  int updateByExampleSelective(YuncrUserAuthentication yuncrUserAuthentication);


  List<YuncrUserAuthentication> selectByCondition(String idCard,String idCardFrontNumber,String phoneNumber,String applyNumber);


  int insertApiRequestData(ApiRequestData apiRequestData);

  /**
  * @Description 更新回调参数
  **/
  int updateCallBack(YuncrUserAuthentication yuncrUserAuthentication);

  void insertBank(YuncrUserBank yuncrUserBank);

  /**
   * 查询用户所绑定的银行卡信息
   */
  List<YuncrUserBankVO> listBankInfo(Integer id);

  List<Map<String, Object>> getSubBankByBankName(String bankName);

  void deleteUserBankCard(String id);

  String getBankByBankName(String bankName);

  void updateBank(YuncrUserBank yuncrUserBank);

  int selectBank(@Param("idCard")String idCard,@Param("bankCardNumber")String bankCardNumber);

  List<Map<String, Object>> getAllBank();

  Map<String, Object> getSubBankByBankId(String bankId);
 //查询回调数据
  List<YuncrUserAuthentication> findCallBackData(@Param("idCard")String idCard);

  List<YuncrUserFailNode> findErrNodeList(@Param("authenticationId")int authenticationId);
}
