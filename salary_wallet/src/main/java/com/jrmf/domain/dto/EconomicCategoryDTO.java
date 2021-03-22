package com.jrmf.domain.dto;

import java.io.Serializable;
import lombok.Data;

@Data
public class EconomicCategoryDTO implements Serializable {

    private static final long serialVersionUID = 4066177195348426163L;
    private Integer id;

    private Integer level;

    private String levelCode;

    private Integer value;

    private String label;

}