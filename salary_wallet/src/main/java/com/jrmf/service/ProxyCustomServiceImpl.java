package com.jrmf.service;

import com.google.common.base.Joiner;
import com.jrmf.controller.BaseController;
import com.jrmf.controller.constant.AgainCalculateType;
import com.jrmf.controller.constant.ChannelTypeEnum;
import com.jrmf.controller.constant.CustomType;
import com.jrmf.domain.*;
import com.jrmf.persistence.CustomProxyDao;
import com.jrmf.persistence.ProxyCostMaintainDao;
import com.jrmf.persistence.ProxyCustomDao;
import com.jrmf.utils.ArithmeticUtil;
import com.jrmf.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 种路路
 * @create 2019-10-31 16:32
 * @desc
 **/
@Service("proxyCustomService")
public class ProxyCustomServiceImpl implements ProxyCustomService {

    private static Logger logger = LoggerFactory.getLogger(ProxyCustomServiceImpl.class);

    @Autowired
    private ProxyCustomDao proxyCustomDao;
    @Autowired
    private CustomProxyDao customProxyDao;
    @Autowired
    private ProxyCostMaintainDao proxyCostMaintainDao;
    @Autowired
    private ChannelCustomService channelCustomService;
    /**
     * 根据参数查询条数
     */
    @Override
    public int countByPage(Page page) {
        return proxyCustomDao.countByPage(page);
    }

    /**
     * 根据参数查询列表
     */
    @Override
    public List<CustomProxySubCommission> listByPage(Page page) {
        return proxyCustomDao.listByPage(page);
    }
    /**
     * 根据参数查询列表
     */
    @Override
    public List<CustomProxySubCommission> listByNoPage(Page page) {
        return proxyCustomDao.listByNoPage(page);
    }

    /**
     * 计算代理商分佣
     * @param againType 1.全部、2.服务公司、3.代理商、4.商户
     * @param merchantId customkey
     * @param month 月份
     */
    @Override
    public void calculate(int againType, String merchantId, String month, String businessPlatformId) {
        AgainCalculateType againCalculateType = AgainCalculateType.codeOf(againType);
        Map<Object, Object> map = new HashMap<>(4);
        map.put(againCalculateType.getCustomType(),merchantId);
        if (!StringUtil.isEmpty(businessPlatformId) && StringUtil.isEmpty(merchantId)){
            Map<String,Object> params = new HashMap<>();
            params.put("businessPlatformId",businessPlatformId);
            params.put("customType", CustomType.PROXY.getCode());
            List<ChannelCustom> channelCustomList = channelCustomService.getCustomByParam(params);
            if (channelCustomList != null && channelCustomList.size() > 0){
                List<String> proxyCustomKeys = new ArrayList<>();
                for (ChannelCustom channelCustom : channelCustomList) {
                    proxyCustomKeys.add(channelCustom.getCustomkey());
                }
                map.put("proxyCustomKeys", Joiner.on(",").join(proxyCustomKeys));
            }else{
                return;
            }
        }
        map.put("time",month);
        proxyCustomDao.deleteByParam(map);
        List<CustomProxySubCommission> list = proxyCustomDao.calculateByParam(map);
        List<CustomProxySubCommission> arrayList = new ArrayList<>();
        for (CustomProxySubCommission customProxySubCommission : list) {
            String key = customProxySubCommission.getCustomKey();
            ChannelCustom custom = channelCustomService.getCustomByCustomkey(key);
            if(custom != null){
                customProxySubCommission.setCustomName(custom.getCompanyName());
            }
            String proxyCustomKey = customProxySubCommission.getProxyCustomKey();
            ChannelCustom proxyCustom = channelCustomService.getCustomByCustomkey(proxyCustomKey);
            if(proxyCustom != null){
                customProxySubCommission.setProxyCustomName(proxyCustom.getCompanyName());
            }
            String directProxyCustomKey = customProxySubCommission.getDirectProxyCustomKey();
            ChannelCustom directProxyCustom = channelCustomService.getCustomByCustomkey(directProxyCustomKey);
            if(directProxyCustom != null){
                customProxySubCommission.setDirectProxyCustomName(directProxyCustom.getCompanyName());
            }

            String companyId = customProxySubCommission.getCompanyId();
            ChannelCustom company = channelCustomService.getCustomByCustomkey(companyId);
            if(company != null){
                customProxySubCommission.setCompanyName(company.getCompanyName());
            }

            String amount = customProxySubCommission.getAmount();
            amount = StringUtil.getFormatResult(amount,2);
            String customServiceFee = customProxySubCommission.getCustomServiceFee();
            customServiceFee = StringUtil.getFormatResult(customServiceFee, 2);
            customProxySubCommission.setCustomServiceFee(customServiceFee);
            customProxySubCommission.setAmount(amount);
            customProxySubCommission.setProxyType(1);
            String proxyFeeRate = customProxySubCommission.getProxyFeeRate();
            String proxyFee = ArithmeticUtil.mulStr(amount, proxyFeeRate);
            if(!StringUtil.isEmpty(customProxySubCommission.getModifyRate())){
                customProxySubCommission.setProxyFeeRate(customProxySubCommission.getModifyRate());
                customProxySubCommission.setModifyProxyFee(proxyFee);
            }
            String subFee = ArithmeticUtil.subStr2(customServiceFee,proxyFee);
            customProxySubCommission.setReturnCommissionAmount(StringUtil.getFormatResult(subFee,2));
            customProxySubCommission.setNextLevelProxyDiffEarnRate("0.00");
            if(customProxySubCommission.getCountType() != 1){
                customProxySubCommission.setCountType(2);
            }
            String parentId = customProxySubCommission.getParentId();
            if(StringUtil.isEmpty(parentId)){
                logger.info("parentId 不存在");
                continue;
            }
            if(!StringUtil.isNumeric(parentId)){
                logger.info("parentId 非数字");
                continue;
            }
            int id = Integer.parseInt(parentId);
            if(id == 0){
                logger.info("parentId =0, 不存在上级");
                continue;
            }
            OrganizationNode node = customProxyDao.getNodeById(id,null);
            if(node == null){
                continue;
            }
            String customKey = node.getCustomKey();
            Map<String, Object> paramMap = new HashMap<>(4);
            paramMap.put("customkey", customKey);
            paramMap.put("netfileId", customProxySubCommission.getCompanyNetFileRateConfId());
            List<ProxyCostMaintain> proxyCostMaintainList = proxyCostMaintainDao.getProxyCostMaintainList(paramMap);
            if(proxyCostMaintainList == null){
                logger.info("代理商成本查不到,跳过");
                continue;
            }
            if(proxyCostMaintainList.isEmpty()){
                logger.info("代理商成本查不到,跳过");
                continue;
            }
            ProxyCostMaintain proxyCostMaintain = proxyCostMaintainList.get(0);
            int countType = proxyCostMaintain.getCountType();
            if(countType == 1){
                logger.info("本级代理商直接成本统计,跳过");
                continue;
            }
            String parentProxyFeeRate = proxyCostMaintain.getProxyFeeRate();
            String nextLevelDiffRate = ArithmeticUtil.subStr(customProxySubCommission.getProxyFeeRate(),parentProxyFeeRate);

            CustomProxySubCommission proxyCustomProxySubCommission = new CustomProxySubCommission();
            proxyCustomProxySubCommission.setTime(customProxySubCommission.getTime());
            proxyCustomProxySubCommission.setProxyCustomKey(proxyCostMaintain.getCustomkey());
            proxyCustomProxySubCommission.setCustomKey(customProxySubCommission.getCustomKey());
            proxyCustomProxySubCommission.setCustomName(customProxySubCommission.getCustomName());
            proxyCustomProxySubCommission.setProxyType(2);
            proxyCustomProxySubCommission.setCompanyId(customProxySubCommission.getCompanyId());
            proxyCustomProxySubCommission.setCompanyName(customProxySubCommission.getCompanyName());
            proxyCustomProxySubCommission.setAmount(amount);
            proxyCustomProxySubCommission.setReturnCommissionAmount(ArithmeticUtil.mulStr(nextLevelDiffRate,amount,2));
            proxyCustomProxySubCommission.setCustomServiceFee(customProxySubCommission.getCustomServiceFee());
            proxyCustomProxySubCommission.setProxyFeeRate(parentProxyFeeRate);
            proxyCustomProxySubCommission.setCompanyNetFileRateConfId(customProxySubCommission.getCompanyNetFileRateConfId());
            proxyCustomProxySubCommission.setDirectProxyCustomKey(customProxySubCommission.getDirectProxyCustomKey());

            proxyCustomProxySubCommission.setCountType(2);
            proxyCustomProxySubCommission.setNextLevelProxyDiffEarnRate(nextLevelDiffRate);
            proxyCustomProxySubCommission.setModifyAddUser(customProxySubCommission.getModifyAddUser());
            proxyCustomProxySubCommission.setModifyRate(customProxySubCommission.getModifyRate());
            proxyCustomProxySubCommission.setModifyEffectStartTime(customProxySubCommission.getModifyEffectStartTime());
            proxyCustomProxySubCommission.setModifyEffectEndTime(customProxySubCommission.getModifyEffectEndTime());
            arrayList.add(proxyCustomProxySubCommission);
        }
        list.addAll(arrayList);
        try{
            proxyCustomDao.addCustomProxySubCommission(list);
        }catch (Exception e){
            logger.error("保存数据异常",e);
        }

    }

    /**
     * 根据条件删除数据
     * @param map 参数
     */
    @Override
    public void deleteByParam(Map<Object, Object> map) {
        proxyCustomDao.deleteByParam(map);
    }

}
