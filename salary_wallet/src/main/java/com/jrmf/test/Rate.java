package com.jrmf.test;

import com.jrmf.domain.ChannelRelated;
import com.jrmf.domain.CompanyRateConf;
import com.jrmf.domain.CustomCompanyRateConf;
import com.jrmf.domain.Parameter;
import com.jrmf.persistence.CompanyRateConfDao;
import com.jrmf.service.ChannelRelatedService;
import com.jrmf.service.CompanyRateConfService;
import com.jrmf.service.CustomCompanyRateConfService;
import com.jrmf.service.ParameterService;
import com.jrmf.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author 种路路
 * @create 2019-02-26 11:00
 * @desc
 **/
@Controller
public class Rate {
    private  final  ChannelRelatedService channelRelatedService;
    private  final CustomCompanyRateConfService customCompanyRateConfService;
    private  final CompanyRateConfService companyRateConfService;
    private  final CompanyRateConfDao companyRateConfDao;
    private  final ParameterService parameterService;

    @Autowired
    public Rate(ChannelRelatedService channelRelatedService, CustomCompanyRateConfService customCompanyRateConfService, CompanyRateConfService companyRateConfService, CompanyRateConfDao companyRateConfDao, ParameterService parameterService) {
        this.channelRelatedService = channelRelatedService;
        this.customCompanyRateConfService = customCompanyRateConfService;
        this.companyRateConfService = companyRateConfService;
        this.companyRateConfDao = companyRateConfDao;
        this.parameterService = parameterService;
    }

    @RequestMapping("/insert")
    @ResponseBody
    public String insert(){
        List<ChannelRelated> relatedByParam = channelRelatedService.getRelatedByParam(new HashMap<>(1));
        for (ChannelRelated channelRelated : relatedByParam) {

            List<CompanyRateConf> companyRateConfs = getRateConf(channelRelated);
            for (CompanyRateConf companyRateConf : companyRateConfs) {
                CustomCompanyRateConf conf = customCompanyRateConfService.getConfByCustomKeyAndCompanyRateConfId(channelRelated.getOriginalId(), companyRateConf.getId());
                if(conf == null){
                    CustomCompanyRateConf customCompanyRateConf = new CustomCompanyRateConf();
                    customCompanyRateConf.setCustomkey(channelRelated.getOriginalId());
                    Integer gearPosition = companyRateConf.getGearPosition();
                    String profilt = channelRelated.getServiceRates();
                    if (gearPosition == 2) {
                        profilt = channelRelated.getUpperServiceRates();
                    }
                    customCompanyRateConf.setCustomRate(profilt);
                    customCompanyRateConf.setMfIncomeRate(0 + "");
                    customCompanyRateConf.setChargeRule("1");
                    customCompanyRateConf.setConfDesc("");
                    customCompanyRateConf.setRateConfId(companyRateConf.getId());
                    customCompanyRateConf.setFeeRuleType(Integer.parseInt(channelRelated.getFeeRuleType()));

                    customCompanyRateConfService.addCustomCompanyRateConf(customCompanyRateConf);
                }
            }
        }
        return "success";
    }

    private List<CompanyRateConf> getRateConf(ChannelRelated channelRelated) {

        HashMap<String, Object> map = new HashMap<>(3);
        int length = "aiyuangong".equals(channelRelated.getMerchantId())?1:2;
        List<CompanyRateConf> list = new ArrayList<>();
        for(int i=1;i<=length;i++){
            map.put("companyId",channelRelated.getCompanyId());
            map.put("gearPosition",i);
            List<CompanyRateConf> confs = companyRateConfService.getCompanyRateConfByParam(map);
            if(confs.isEmpty()){
                CompanyRateConf companyRateConf = new CompanyRateConf();
                companyRateConf.setMerchantId(channelRelated.getMerchantId());
                companyRateConf.setCompanyId(Integer.parseInt(channelRelated.getCompanyId()));
                companyRateConf.setBusinessType(1);
                companyRateConf.setGearPosition(i);
                String start = "0.00";
                String end = "29999.99";
                if("aiyuangong".equals(channelRelated.getMerchantId())){
                    end = "28000.00";
                }
                if(i == 2){
                    start = "29999.99";
                    end = "299999.99";
                }
                companyRateConf.setAmountStart(start);
                companyRateConf.setAmountEnd(end);
                companyRateConf.setOperator("<=");
                companyRateConf.setGearPositionShorthand("大于"+start+"小于等于"+end);
                companyRateConf.setGearPositionDesc("大于"+start+"小于等于"+end);
                companyRateConf.setCostRate("0");
                companyRateConf.setMfkjCostRate("0");
                companyRateConfDao.insertCompanyRateConf(companyRateConf);
                list.add(companyRateConf);
            }else{
                list.addAll(confs);
            }

        }
        return list;
    }

}
