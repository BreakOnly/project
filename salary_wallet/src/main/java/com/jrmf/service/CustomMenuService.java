
package com.jrmf.service;


public interface CustomMenuService {

  String getProjectIdByMenu(int menuId, String customKey);

  String getProjectIdByCustomKey(String customKey);

}
