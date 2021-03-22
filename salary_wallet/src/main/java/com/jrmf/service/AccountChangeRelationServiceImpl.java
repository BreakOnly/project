package com.jrmf.service;


import com.jrmf.domain.AccountChangeRelation;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.persistence.AccountChangeRelationDao;
import com.jrmf.persistence.ChannelCustomDao;
import com.jrmf.utils.RespCode;
import com.jrmf.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class AccountChangeRelationServiceImpl implements AccountChangeRelationService {

    @Autowired
    private AccountChangeRelationDao accountChangeRelationDao;

    @Autowired
    private ChannelCustomDao customDao;

    private static Logger logger = LoggerFactory.getLogger(AccountChangeRelationServiceImpl.class);


    @Override
    public int deleteByPrimaryKey(Integer id) {
        return accountChangeRelationDao.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(AccountChangeRelation record) {
        return accountChangeRelationDao.insert(record);
    }

    @Override
    public int insertSelective(AccountChangeRelation record) {
        return accountChangeRelationDao.insertSelective(record);
    }

    @Override
    public AccountChangeRelation selectByPrimaryKey(Integer id) {
        return accountChangeRelationDao.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(AccountChangeRelation record) {
        return accountChangeRelationDao.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(AccountChangeRelation record) {
        return accountChangeRelationDao.updateByPrimaryKey(record);
    }

    @Override
    public List<Map<String, Object>> changeAccountList(Integer accountId, Integer changeAccountId) {
        return accountChangeRelationDao.changeAccountList(accountId, changeAccountId);
    }

    /**
     * 通过id查询账户切换列表信息
     *
     * @param id
     * @return
     */
    @Override
    public AccountChangeRelation getAccountChangeRelationById(String id) {
        return accountChangeRelationDao.getAccountChangeRelationById(id);
    }

    /**
     * 通过id删除账号切换列表信息
     *
     * @param id
     */
    @Override
    public void deleteAccountChangeRelationById(String id) {
        accountChangeRelationDao.deleteAccountChangeRelationById(id);
    }

    /**
     * 切换账号配置管理-> 新增/修改
     *
     * @param customId
     * @param configCustomId
     * @return
     */
    @Override
    @Transactional
    public Map<String, Object> configAccount(ChannelCustom customLogin, String customId, String[] configCustomId, String id) {
        Map<String, Object> result = new HashMap<>(4);
        result.put(RespCode.RESP_STAT, RespCode.success);
        result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.success));

        ChannelCustom channelCustom;
        List<ChannelCustom> custom = new ArrayList<>();
        ChannelCustom custom2 = null;
        logger.info("切换账号配置开始------customId：{},configCustomId：{},id：{}", customId, configCustomId, id);

        try {
            if (!StringUtil.isEmpty(customId)) {
                channelCustom = customDao.getCustomById(Integer.parseInt(customId));
            } else {
                result.put(RespCode.RESP_STAT, RespCode.error101);
                result.put(RespCode.RESP_MSG, "商户不存在，请刷新页面重试");
                return result;
            }

            if (configCustomId != null && configCustomId.length > 0) {
                for (String key : configCustomId) {
                    custom2 = customDao.getCustomById(Integer.parseInt(key));
                    custom.add(custom2);
                }
            } else {
                result.put(RespCode.RESP_STAT, RespCode.error101);
                result.put(RespCode.RESP_MSG, "配置商户不存在，请刷新页面重试");
                return result;
            }

            if (channelCustom != null && custom.size() > 0) {
                AccountChangeRelation acr = accountChangeRelationDao.getAccountChangeRelationByAccountId(channelCustom.getId());
                if (acr == null) {
                    AccountChangeRelation changeRelation = new AccountChangeRelation();
                    changeRelation.setAccountId(channelCustom.getId());
                    changeRelation.setCustomKey(channelCustom.getCustomkey());
                    changeRelation.setCustomName(channelCustom.getCompanyName());
                    changeRelation.setChangeAccountId(channelCustom.getId());
                    changeRelation.setChangeAccountName(channelCustom.getUsername());
                    changeRelation.setAddUser(customLogin.getUsername());
                    accountChangeRelationDao.insertAccountChangeRelation(changeRelation);
                }
            } else {
                result.put(RespCode.RESP_STAT, RespCode.error101);
                result.put(RespCode.RESP_MSG, RespCode.codeMaps.get(RespCode.error101));
                return result;
            }



            if (!StringUtil.isEmpty(id)) {
                AccountChangeRelation accountChangeRelationById = accountChangeRelationDao.getAccountChangeRelationById(id);
                if (custom2.getId() != accountChangeRelationById.getChangeAccountId()) {
                    AccountChangeRelation cr = accountChangeRelationDao.getAccountByChangeIdAndChangeAccountId(channelCustom.getId(), custom2.getId());
                    if (cr != null) {
                        result.put(RespCode.RESP_STAT, RespCode.error101);
                        result.put(RespCode.RESP_MSG, "请勿重复配置同一账号");
                        return result;
                    }
                }
                AccountChangeRelation accountChangeRelation = new AccountChangeRelation();
                accountChangeRelation.setId(Integer.parseInt(id));
                accountChangeRelation.setAccountId(channelCustom.getId());
                accountChangeRelation.setCustomKey(custom2.getCustomkey());
                accountChangeRelation.setCustomName(custom2.getCompanyName());
                accountChangeRelation.setChangeAccountId(custom2.getId());
                accountChangeRelation.setChangeAccountName(custom2.getUsername());
                accountChangeRelation.setAddUser(customLogin.getUsername());
                accountChangeRelationDao.updateAccountChangeRelation(accountChangeRelation);
            } else {
                custom.forEach(c -> {
                    AccountChangeRelation cr2 = accountChangeRelationDao.getAccountByChangeIdAndChangeAccountId(channelCustom.getId(), c.getId());
                    Optional.ofNullable(cr2)
                            .orElseGet(() -> {
                                AccountChangeRelation changeRelation = new AccountChangeRelation();
                                changeRelation.setAccountId(channelCustom.getId());
                                changeRelation.setCustomKey(c.getCustomkey());
                                changeRelation.setCustomName(c.getCompanyName());
                                changeRelation.setChangeAccountId(c.getId());
                                changeRelation.setChangeAccountName(c.getUsername());
                                changeRelation.setAddUser(customLogin.getUsername());
                                accountChangeRelationDao.insertAccountChangeRelation(changeRelation);
                                return changeRelation;
                            });
                });
            }
        } catch (Exception e) {
            logger.error("配置切换账号异常：", e);
            result.put(RespCode.RESP_STAT, RespCode.error101);
            result.put(RespCode.RESP_MSG, "配置切换账号失败，请联系管理员！");
        }
        return result;
    }
}