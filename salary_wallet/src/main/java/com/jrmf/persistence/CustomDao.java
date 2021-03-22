package com.jrmf.persistence;

import com.jrmf.splitorder.domain.Custom;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CustomDao {
    List<Custom> listCustomInfo();
}
