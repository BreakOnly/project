/**
 * 
 */
package com.jrmf.persistence;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jrmf.domain.ChannelCustomCatalog;
import com.jrmf.domain.CustomMenu;
import org.springframework.stereotype.Repository;

/**
 * @author: liangyu
 * @date: 2016-3-18
 * @description:
 */
@Mapper
public interface CustomMenuDao {

	List<ChannelCustomCatalog> getCustomContentByCustomId(@Param("originalId")String originalId,@Param("level") String level);

	List<CustomMenu> getAllPermission(@Param("originalId")String originalId);

	List<CustomMenu> getCustomMenuByOriginalId(Map<String, Object> param);
	
	List<CustomMenu> getCustomMenuByName(@Param("originalId")String originalId,@Param("contentName")String contentName);
	
	CustomMenu getCustomMenuById(int id);
	
	public int savePermission(CustomMenu munu);

	int updatePermission(CustomMenu munu);

	List<CustomMenu> getNodeTree(Map<String, Object> param);
	
	List<CustomMenu> getCustomMenuList(Map<String, Object> param);

	int deleteNodeById(String id);

	String getProjectIdByMenuId(int menuId);

	String getProjectIdByCustomKey(String customKey);
}
