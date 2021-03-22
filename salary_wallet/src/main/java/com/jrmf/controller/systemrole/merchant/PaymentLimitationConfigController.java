package com.jrmf.controller.systemrole.merchant;

import com.jrmf.controller.BaseController;
import com.jrmf.domain.ChannelCustom;
import com.jrmf.domain.ChannelRelated;
import com.jrmf.domain.CustomLimitConf;
import com.jrmf.service.ChannelCustomService;
import com.jrmf.service.ChannelRelatedService;
import com.jrmf.service.CustomLimitConfService;
import com.jrmf.utils.RespCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 种路路
 * @create 2019-02-23 13:59
 * @desc 商户下发限额配置
 **/
@Controller
@RequestMapping("/wallet/payment/limitation")
public class PaymentLimitationConfigController extends BaseController{
	
	@Autowired
	private ChannelRelatedService relatedService;

    private static Logger logger = LoggerFactory.getLogger(PaymentLimitationConfigController.class);

    private final CustomLimitConfService customLimitConfService;
    private final ChannelCustomService channelCustomService;

    public PaymentLimitationConfigController(CustomLimitConfService customLimitConfService, ChannelCustomService channelCustomService) {
        this.customLimitConfService = customLimitConfService;
        this.channelCustomService = channelCustomService;
    }


    /**
     * 根据customkey查询
     * @return ChannelCustom
     */
    @RequestMapping(value = "/custom")
    public @ResponseBody
    Map<String, Object> custom(@RequestParam(value = "originalId") String customkey){
        ChannelCustom custom = channelCustomService.getCustomByCustomkey(customkey);
        Map<String, Object> resultMap = new HashMap<>(3);
        resultMap.put("custom",custom);
        return returnSuccess(resultMap);
    }
    /**
     *
     * @param pageSize 每页条数
     * @param pageNo 页码
     * @return list
     */
    @RequestMapping(value = "/configs")
    public @ResponseBody
    Map<String, Object> configs(@RequestParam(value = "pageSize",required = false,defaultValue = "10") int pageSize,
                                @RequestParam(value = "pageNo") String pageNo,
                                @RequestParam(value = "originalId") String customkey){
        Map<String, Object> hashMap = new HashMap<>(4);

        hashMap.put("customkey",customkey);
        int count = customLimitConfService.listLimitConfByParams(hashMap).size();

        hashMap.put("start",getFirst(pageNo,pageSize+""));
        hashMap.put("limit",pageSize);
        List<CustomLimitConf> customLimitConfs = customLimitConfService.listLimitConfByParams(hashMap);

        Map<String, Object> resultMap = new HashMap<>(3);
        resultMap.put("count",count);
        resultMap.put("customLimitConfs",customLimitConfs);
        return returnSuccess(resultMap);
    }

    /**
     * 添加配置
     * @param customkey 商户id
     * @param companyId 服务公司
     * @param singleOrderLimit 单笔限额
     * @param singleDayLimit 单日限额
     * @param singleMonthLimit 单月限额
     * @param unAutoCompensatable 不自动补差价  Y  不自动补偿   N 自动补偿
     * @return success
     */
    @RequestMapping(value = "/config/add")
    public @ResponseBody
    Map<String, Object> addConfig(@RequestParam(value = "originalId") String customkey,
                                @RequestParam(value = "companyId") String companyId,
                                @RequestParam(value = "singleOrderLimit",required = false) String singleOrderLimit,
                                @RequestParam(value = "singleDayLimit",required = false) String singleDayLimit,
                                @RequestParam(value = "singleMonthLimit",required = false) String singleMonthLimit,
                                @RequestParam(value = "singleQuarterLimit",required = false) String singleQuarterLimit,
                                @RequestParam(value = "unAutoCompensatable",required = false,defaultValue = "N") String unAutoCompensatable,Integer realCompanyOperate){
        Map<String, Object> hashMap = new HashMap<>(3);

        hashMap.put("customkey",customkey);
        hashMap.put("companyId",companyId);
        ChannelRelated  channelRelated = relatedService.getRelatedByCompAndOrig(customkey, companyId);
        if(channelRelated!=null){
            channelRelated.setRealCompanyOperate(realCompanyOperate==null?0:realCompanyOperate);
            relatedService.updateChannelRelated(channelRelated);
        }
        List<CustomLimitConf> customLimitConfs = customLimitConfService.listLimitConfByParams(hashMap);
        if(!customLimitConfs.isEmpty()){
            logger.error("配置已存在");
            return returnFail(RespCode.PAYMENT_LIMITATION_EXIST,RespCode.codeMaps.get(RespCode.PAYMENT_LIMITATION_EXIST));
        }

        CustomLimitConf customLimitConf = new CustomLimitConf();
        customLimitConf.setUnAutoCompensatable(unAutoCompensatable);
        customLimitConf.setCompanyId(companyId);
        customLimitConf.setCustomkey(customkey);
        customLimitConf.setSingleOrderLimit(singleOrderLimit);
        customLimitConf.setSingleDayLimit(singleDayLimit);
        customLimitConf.setSingleMonthLimit(singleMonthLimit);
        customLimitConf.setSingleQuarterLimit(singleQuarterLimit);
        customLimitConfService.addConfig(customLimitConf);
        return returnSuccess();
    }
    /**
     * 删除配置
     * @param id 配置id
     * @return success
     */
    @RequestMapping(value = "/config/delete")
    public @ResponseBody
    Map<String, Object> deleteConfig(@RequestParam(value = "id") String id){
        customLimitConfService.deleteConfig(id);
        return returnSuccess();
    }

    /**
     * 修改配置
     * @param id 配置id
     * @param singleOrderLimit 单笔限额
     * @param singleDayLimit 单日限额
     * @param singleMonthLimit 单月限额
     * @param unAutoCompensatable 不自动补差价  Y  不自动补偿   N 自动补偿
     * @return success
     */
    @RequestMapping(value = "/config/update")
    public @ResponseBody
    Map<String, Object> updateConfig(@RequestParam(value = "id") String id,
                                     @RequestParam(value = "singleOrderLimit",required = false) String singleOrderLimit,
                                     @RequestParam(value = "singleDayLimit",required = false) String singleDayLimit,
                                     @RequestParam(value = "singleMonthLimit",required = false) String singleMonthLimit,
                                     @RequestParam(value = "singleQuarterLimit",required = false) String singleQuarterLimit,
                                     @RequestParam(value = "unAutoCompensatable",required = false,defaultValue = "N") String unAutoCompensatable,Integer realCompanyOperate){

        Map<String, Object> hashMap = new HashMap<>(3);

        hashMap.put("id",id);
        List<CustomLimitConf> customLimitConfs = customLimitConfService.listLimitConfByParams(hashMap);
        if(customLimitConfs.isEmpty()){
            logger.error("配置不存在");
            return returnFail(RespCode.PAYMENT_LIMITATION_NOT_FOUND,RespCode.codeMaps.get(RespCode.PAYMENT_LIMITATION_NOT_FOUND));
        }

        CustomLimitConf customLimitConf = customLimitConfs.get(0);
        ChannelRelated  channelRelated = relatedService.getRelatedByCompAndOrig(customLimitConf.getCustomkey(), customLimitConf.getCompanyId());
        if(channelRelated!=null){
            channelRelated.setRealCompanyOperate(realCompanyOperate==null?0:realCompanyOperate);
            relatedService.updateChannelRelated(channelRelated);
        }
        relatedService.updateChannelRelated(channelRelated);
        customLimitConf.setUnAutoCompensatable(unAutoCompensatable);
        customLimitConf.setSingleOrderLimit(singleOrderLimit);
        customLimitConf.setSingleDayLimit(singleDayLimit);
        customLimitConf.setSingleMonthLimit(singleMonthLimit);
        customLimitConf.setSingleQuarterLimit(singleQuarterLimit);
        customLimitConfService.updateConfig(customLimitConf);
        return returnSuccess();
    }

}
