/**
 * 
 */
package com.jrmf.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jrmf.domain.WechatConfig;

/**
 * @author: zh
 * @date: 2019-801-10
 * @description:
 */
@Mapper
public interface WechatConfigDao {
	public WechatConfig getWechatConfigByDomainName(@Param("domainName")String domainName);
	
	public WechatConfig getWechatConfigById(@Param("id")int id);
}
