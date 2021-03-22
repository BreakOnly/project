package com.jrmf.controller.constant;

import java.io.Serializable;

/**
 * @author 种路路
 * @create 2018-11-20 19:34
 * @desc 爱员工配置信息
 **/
public class BestSignConfig implements Serializable {

  private String ftpURL;
  private String ftpPort;
  private String ftpPath;
  private String seckey;
  private String pubkey;
  private String bestSignURL;
  private String serverNameUrl;
  private String username;
  private String password;
  private String clientServerNameUrl;
  private String clientUsername;
  private String clientPassword;


  public BestSignConfig(String ftpURL, String seckey, String pubkey, String bestSignURL,
      String serverNameUrl, String username, String password, String clientServerNameUrl,
      String clientUsername, String clientPassword) {
    this.ftpURL = ftpURL;
    this.seckey = seckey;
    this.pubkey = pubkey;
    this.bestSignURL = bestSignURL;
    this.serverNameUrl = serverNameUrl;
    this.username = username;
    this.password = password;
    this.clientServerNameUrl = clientServerNameUrl;
    this.clientUsername = clientUsername;
    this.clientPassword = clientPassword;
  }

  public BestSignConfig(String ftpURL, String ftpPort, String ftpPath, String username,
      String password) {
    this.ftpURL = ftpURL;
    this.ftpPort = ftpPort;
    this.ftpPath = ftpPath;
    this.username = username;
    this.password = password;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getSeckey() {
    return seckey;
  }

  public void setSeckey(String seckey) {
    this.seckey = seckey;
  }

  public String getPubkey() {
    return pubkey;
  }

  public void setPubkey(String pubkey) {
    this.pubkey = pubkey;
  }

  public String getBestSignURL() {
    return bestSignURL;
  }

  public void setBestSignURL(String bestSignURL) {
    this.bestSignURL = bestSignURL;
  }

  public String getFtpURL() {
    return ftpURL;
  }

  public void setFtpURL(String ftpURL) {
    this.ftpURL = ftpURL;
  }

  public String getServerNameUrl() {
    return serverNameUrl;
  }

  public void setServerNameUrl(String serverNameUrl) {
    this.serverNameUrl = serverNameUrl;
  }

  public String getClientUsername() {
    return clientUsername;
  }

  public void setClientUsername(String clientUsername) {
    this.clientUsername = clientUsername;
  }

  public String getClientPassword() {
    return clientPassword;
  }

  public void setClientPassword(String clientPassword) {
    this.clientPassword = clientPassword;
  }

  public String getClientServerNameUrl() {
    return clientServerNameUrl;
  }

  public void setClientServerNameUrl(String clientServerNameUrl) {
    this.clientServerNameUrl = clientServerNameUrl;
  }

  public String getFtpPort() {
    return ftpPort;
  }

  public void setFtpPort(String ftpPort) {
    this.ftpPort = ftpPort;
  }

  public String getFtpPath() {
    return ftpPath;
  }

  public void setFtpPath(String ftpPath) {
    this.ftpPath = ftpPath;
  }
}
