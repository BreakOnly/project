package com.jrmf.domain;

import com.jrmf.controller.constant.BestSignConfig;
import lombok.Data;

import java.io.Serializable;

/**
 * @description: <br/>
 * @author: <br/>
 * @create：2020年05⽉21⽇<br/>
 */
@Data
public class AutoImportReceipt implements Serializable {

    private String payType;

    private String companyId;

    private String batchId;

    private String pathPdfDir;

    private String fileName;

    private BestSignConfig bestSignConfig;
}
