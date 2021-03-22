package com.jrmf.domain.dto;

import java.io.Serializable;

import com.alibaba.excel.annotation.format.NumberFormat;
import com.jrmf.taxsettlement.api.gateway.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

/**
 * @author: YJY
 * @date: 2021/1/6 13:54
 * @description:
 */
@Data
public class ApplyBatchInvoiceDTO implements Serializable {
    //商户名称
    private String companyName;
    //个体户名称
    private String individualName;
    //实发服务公司ID
    @NotNull
    private Integer companyId;
    //开票状态  1:已开票 2:开票中 3:开票失败
    private Integer invoiceStatus;
    // 开始日期
    private String startDate;
    // 结束日期
    private String endDate;
    //申请批次备注
    private String applyBatchRemark;
    // 证件号
    private String  idCard;
    //执行步骤 1:推送合同 2:推送结算 3:上传结算单 4:上传发票 5:全部完成
    private Integer step;
    //步骤状态
    private String stepStatus;
    //开始月份
    private String startMoney;
    //结束月份
    private String endMoney;
    //发包商公司名称
    private String customFirmName;
    //项目名称
    private String channelTaskName;

    private Integer pageNo;
    private Integer pageSize;

    public Integer getPageNo() {
        return pageNo == null ? 1 : pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize == null ? 10 : pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
