package com.jrmf.service;

import com.jrmf.persistence.CustomMenuDao;
import com.jrmf.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomMenuServiceImpl implements CustomMenuService {

  @Autowired
  private CustomMenuDao customMenuDao;

  @Override
  public String getProjectIdByMenu(int menuId, String customKey) {
    String projectId = customMenuDao.getProjectIdByMenuId(menuId);
    if (StringUtil.isEmpty(projectId)) {
      projectId = customMenuDao.getProjectIdByCustomKey(customKey);
    }
    return projectId;
  }

  @Override
  public String getProjectIdByCustomKey(String customKey) {
    return customMenuDao.getProjectIdByCustomKey(customKey);
  }
}
