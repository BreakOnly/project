package com.jrmf.utils;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/**
 * 用途：
 * 作者：郭桐宁
 * 时间：2018/12/18 9:45
 * Version:1.0
 */
@Component
public class PermissionImgUploadUtil {
    protected static final Logger logger = LoggerFactory.getLogger(PermissionImgUploadUtil.class);
    public static final String STOREPATH = "/permissionimg";
    private static PermissionImgUploadUtil permissionImgUploadUtil;
    private FTPClient ftpClient;

    /**
     * 服务器地址
     */
    private String serverUrl;
    /**
     * 服务器端口
     */
    private String port;
    /**
     * 用户登录密码
     */
    private String password;
    /**
     * 用户登录名
     */
    private String username;
    /**
     * 文件上传输入流
     */
    private InputStream is;

    public PermissionImgUploadUtil() {
        if (null == ftpClient) {
            ftpClient = new FTPClient();
        }
    }

    public synchronized static PermissionImgUploadUtil getInstance() {
        if (null == permissionImgUploadUtil) {
            permissionImgUploadUtil = new PermissionImgUploadUtil();
        }
        return permissionImgUploadUtil;
    }

    /**
     * Author Nicholas-Ning
     * Description //TODO 连接到ftp服务器
     * Date 10:12 2018/12/18
     * Param [remotePath]
     * return boolean
     **/
    public boolean connectToFTP(String remotePath) {
        // 定义返回值
        boolean result = false;
        try {
            // 连接至服务器，端口默认为21时，可直接通过URL连接
            ftpClient.connect(serverUrl);
            // 登录服务器
            ftpClient.login(username, password);
            // 判断返回码是否合法
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                // 不合法时断开连接
                ftpClient.disconnect();
                // 结束程序
                return result;
            }
            //设置文件传输模式
            //被动模式
//			ftpClient.enterLocalPassiveMode();
            //创建目录
            ftpClient.makeDirectory(remotePath);
            // 设置文件操作目录
            result = ftpClient.changeWorkingDirectory(remotePath);
            // 设置文件类型，二进制
            result = ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            // 设置缓冲区大小
            ftpClient.setBufferSize(3072);
            // 设置字符编码
            ftpClient.setControlEncoding("UTF-8");

        } catch (IOException e) {
            logger.error("连接FTP服务器异常",e);
            throw new RuntimeException("连接FTP服务器异常" , e);
        }
        return result;
    }
    /**
     * Author Nicholas-Ning
     * Description //TODO 上传文件至FTP服务器
     * Date 11:05 2018/12/18
     * Param [storePath, fileName, is]
     * return boolean
     **/
    public boolean storeFile(String fileName, InputStream is) {
        boolean result = false;
        try {
            // 连接至服务器
            result = connectToFTP(STOREPATH);
            // 判断服务器是否连接成功
            if (result) {
                // 上传文件
                result = ftpClient.storeFile(fileName, is);
            }
            // 关闭输入流
            is.close();
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
        } finally {
            // 判断输入流是否存在
            if (null != is) {
                try {
                    // 关闭输入流
                    is.close();
                } catch (IOException e) {
                    logger.error("上传文件至FTP异常"+e.getMessage());
                    throw new RuntimeException("上传文件至FTP异常" , e);
                }
            }
            // 登出服务器并断开连接
            permissionImgUploadUtil.logout();
        }
        return result;
    }
    /**
     * Author Nicholas-Ning
     * Description //TODO 登出服务器并断开连接
     * Date 11:04 2018/12/18
     * Param []
     * return boolean
     **/
    public boolean logout() {
        boolean result = false;
        if (null != is) {
            try {
                // 关闭输入流
                is.close();
            } catch (IOException e) {
                logger.error("登录FTP服务器异常"+e.getMessage());
                throw new RuntimeException("登录FTP服务器异常" , e);
            }
        }
        if (null != permissionImgUploadUtil) {
            try {
                // 登出服务器
                result = ftpClient.logout();
            } catch (IOException e) {
                logger.error("登录FTP服务器异常"+e.getMessage());
                throw new RuntimeException("登录FTP服务器异常" , e);
            } finally {
                // 判断连接是否存在
                if (ftpClient.isConnected()) {
                    try {
                        // 断开连接
                        ftpClient.disconnect();
                    } catch (IOException e) {
                        logger.error("关闭FTP服务器异常"+e.getMessage());
                        throw new RuntimeException("关闭FTP服务器异常" , e);
                    }
                }
            }
        }
        return result;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
