package com.jrmf.persistence;

import com.jrmf.domain.TaxCode;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaxCodeDao {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tax_code
     *
     * @mbg.generated Mon Nov 16 18:05:53 CST 2020
     */
    int insert(TaxCode record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tax_code
     *
     * @mbg.generated Mon Nov 16 18:05:53 CST 2020
     */
    int insertSelective(TaxCode record);

    List<TaxCode> listTaxCode(String level, String nextLevel, String levelCode);
}