package com.jrmf.utils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jrmf.controller.constant.BestSignConfig;
import java.util.Properties;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;

/**
 * @author 种路路
 * @version 创建时间：2017年9月8日 下午5:45:01
 * 类说明
 */
@Component
public class FtpTool {
    private static Logger logger = LoggerFactory.getLogger(FtpTool.class);

    @Autowired
    private BestSignConfig bestSignConfig;

    private static BestSignConfig defaultFtpConfit;

    @PostConstruct
    public void init() {
       defaultFtpConfit = bestSignConfig;
    }


    /**
     * Description: 向FTP服务器上传文件
     * @param url      FTP服务器hostname
     * @param port     FTP服务器端口
     * @param username FTP登录账号
     * @param password FTP登录密码
     * @param path     FTP服务器保存目录
     * @param filename 上传到FTP服务器上的文件名
     * @param input    输入流
     * @return 成功返回true，否则返回false *
     */
    public static String uploadFile(String url,
                                    int port,
                                    String path,
                                    String filename,
                                    InputStream input,
                                    String username, String password) {
        String success = "error";
        FTPClient ftp = new FTPClient();
        ftp.setControlEncoding("GB2312");
        try {
            int reply;
            // 连接FTP服务器
            ftp.connect(url, port);
            // 如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
            ftp.login(username, password);
            ftp.enterLocalPassiveMode();
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return success;
            }
            File serverFile = new File(path);
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
            //不存在   就创建
            if (!serverFile.exists()) {
                logger.info("创建文件夹"+path);
                StringTokenizer s = new StringTokenizer(path, "/");
                s.countTokens();
                String pathName = "";
                while (s.hasMoreElements()) {
                    pathName += "/" + s.nextElement();
                    try {
                        ftp.mkd(pathName);
                    } catch (Exception e) {
                        logger.error("ftp文件夹创建失败");
                    }
                }
            }
            boolean storeFile = ftp.storeFile(path + filename, input);

            if (storeFile) {
                success = "success";
            } else {
                logger.error("上传文件服务器返回====false");
                success = "error";
            }
            input.close();
            ftp.logout();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return success;
    }

    /**
     * 判断ftp服务器文件是否存在
     * @param path
     * @return
     * @throws IOException
     * @date 创建时间：2017年6月22日 上午11:52:52
     */
    public static boolean checkFile(String url,// FTP服务器hostname
                                    int port,// FTP服务器端口
                                    String path, // FTP服务器保存目录
                                    String username, String password) {
        FTPClient ftp = new FTPClient();
        ftp.setControlEncoding("GB2312");
        try {
            int reply;
            ftp.connect(url, port);
            ftp.login(username, password);
            ftp.enterLocalPassiveMode();
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return false;
            }
            boolean flag = false;
            FTPFile[] ftpFileArr = ftp.listFiles(path);
            if (ftpFileArr.length > 0) {
                flag = true;
            }
            ftp.logout();
            return flag;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return false;
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }


    /**
     * 从FTP服务器下载文件
     *
     * @param ftpHost     FTP IP地址
     * @param ftpUserName FTP 用户名
     * @param ftpPassword FTP用户名密码
     * @param ftpPort     FTP端口
     * @param ftpPath     FTP服务器中文件所在路径 格式： ftptest/aa
     * @param fileName    文件名称
     */
    public static byte[] downloadFtpFile(String ftpHost, String ftpUserName, String ftpPassword, int ftpPort, String ftpPath, String fileName) {


        FTPClient ftpClient = null;
        try {
            ftpClient = new FTPClient();
            ftpClient.setControlEncoding("GBK");
            int reply;
            ftpClient.connect(ftpHost, ftpPort);
            ftpClient.login(ftpUserName, ftpPassword);
            ftpClient.enterLocalPassiveMode();//设置被动连接
            reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                return null;
            }
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.setRemoteVerificationEnabled(false);
            ftpClient.changeWorkingDirectory(ftpPath);
            ByteArrayOutputStream ops = new ByteArrayOutputStream();

            ftpClient.retrieveFile(fileName, ops);
            byte[] bytes = ops.toByteArray();
            ops.close();
            ftpClient.logout();
            return bytes;

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (ftpClient != null) {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

        return null;
    }

    /**
     * 从FTP服务器下载文件
     *
     * @param ftpHost     FTP IP地址
     * @param ftpUserName FTP 用户名
     * @param ftpPassword FTP用户名密码
     * @param ftpPort     FTP端口
     * @param ftpPath     FTP服务器中文件所在路径 格式： ftptest/aa
     * @param fileName    文件名称
     */
    public static byte[] downloadFtpsFile(String ftpHost, String ftpUserName, String ftpPassword, int ftpPort, String ftpPath, String fileName) {

        ChannelSftp sftp;
        Channel channel = null;
        Session sshSession = null;
        try {
            JSch jsch = new JSch();
            sshSession = jsch.getSession(ftpUserName, ftpHost, ftpPort);
            sshSession.setPassword(ftpPassword);

            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(sshConfig);
            sshSession.connect();
            logger.info("Session connected!");
            channel = sshSession.openChannel("sftp");
            channel.connect();
            logger.info("Channel  connected!");

            sftp = (ChannelSftp) channel;

            sftp.cd(ftpPath);

            ByteArrayOutputStream ops = new ByteArrayOutputStream();
            sftp.get(fileName, ops);
            byte[] bytes = ops.toByteArray();
            ops.close();

            closeChannel(channel);
            closeSession(sshSession);

            return bytes;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (channel != null) {
                closeChannel(channel);
            }
            if (sshSession != null) {
                closeSession(sshSession);
            }
        }

        return null;
    }

    /**
     * Description: 向FTP服务器上传文件
     *
     *
     * @param path     FTP服务器保存目录
     * @param filename 上传到FTP服务器上的文件名
     * @param input    输入流
     * @return 成功返回true，否则返回false *
     */
    public static boolean uploadFile(String path,
                                    String filename,
                                    InputStream input) {
        FTPClient ftp = new FTPClient();
        ftp.setControlEncoding("UTF-8");
        try {
            int reply;
            // 连接FTP服务器
            ftp.connect(defaultFtpConfit.getFtpURL(), 21);
            // 如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
            ftp.login(defaultFtpConfit.getUsername(), defaultFtpConfit.getPassword());
            ftp.enterLocalPassiveMode();
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return true;
            }
            File serverFile = new File(path);
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
            //不存在   就创建
            if (!serverFile.exists()) {
                logger.info("创建文件夹"+path);
                StringTokenizer s = new StringTokenizer(path, "/");
                s.countTokens();
                String pathName = "";
                while (s.hasMoreElements()) {
                    pathName += "/" + s.nextElement();
                    try {
                        ftp.mkd(pathName);
                    } catch (Exception e) {
                        logger.error("ftp文件夹创建失败");
                    }
                }
            }
            boolean storeFile = ftp.storeFile(path + filename, input);

            input.close();
            ftp.logout();

            if (storeFile) {
                return true;
            } else {
                logger.error("上传文件服务器返回====false");
                return false;
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return false;
    }


    /**
     * 判断ftp服务器文件是否存在
     * @param path FTP服务器保存目录
     * @return
     * @throws IOException
     * @date 创建时间：2017年6月22日 上午11:52:52
     */
    public static boolean checkFile(String path) {
        FTPClient ftp = new FTPClient();
        ftp.setControlEncoding("UTF-8");
        try {
            int reply;
            ftp.connect(defaultFtpConfit.getFtpURL(), 21);
            ftp.login(defaultFtpConfit.getUsername(), defaultFtpConfit.getPassword());
            ftp.enterLocalPassiveMode();
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return false;
            }
            boolean flag = false;
            FTPFile[] ftpFileArr = ftp.listFiles(path);
            if (ftpFileArr.length > 0) {
                flag = true;
            }
            ftp.logout();
            return flag;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return false;
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }


    /**
     * 从FTP服务器下载文件
     *
     * @param ftpPath     FTP服务器中文件所在路径 格式： ftptest/aa
     * @param fileName    文件名称
     */
    public static byte[] downloadFtpFile(String ftpPath, String fileName) {


        FTPClient ftpClient = null;
        try {
            ftpClient = new FTPClient();
            ftpClient.setControlEncoding("UTF-8");
            int reply;
            ftpClient.connect(defaultFtpConfit.getFtpURL(), 21);
            ftpClient.login(defaultFtpConfit.getUsername(), defaultFtpConfit.getPassword());
            ftpClient.enterLocalPassiveMode();//设置被动连接
            reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                return null;
            }
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.setRemoteVerificationEnabled(false);
            ftpClient.changeWorkingDirectory(ftpPath);
            ByteArrayOutputStream ops = new ByteArrayOutputStream();

            ftpClient.retrieveFile(fileName, ops);
            byte[] bytes = ops.toByteArray();
            ops.close();
            ftpClient.logout();
            return bytes;

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            try {
                if (ftpClient != null) {
                    ftpClient.logout();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } finally {
            // 注意,一定要在finally代码中断开连接，否则会导致占用ftp连接情况
            try {
                ftpClient.disconnect();
            } catch (IOException io) {
                logger.error(io.getMessage(), io);
            }
        }

        return null;
    }

    private static void closeChannel(Channel channel) {
        if (channel != null) {
            if (channel.isConnected()) {
                channel.disconnect();
            }
        }
    }

    private static void closeSession(Session session) {
        if (session != null) {
            if (session.isConnected()) {
                session.disconnect();
            }
        }
    }


    public static byte[] renameFile(String fromFileUrl, String toFileUrl) {

        FTPClient ftpClient = null;
        try {
            ftpClient = new FTPClient();
            ftpClient.setControlEncoding("UTF-8");
            int reply;
            ftpClient.connect(defaultFtpConfit.getFtpURL(), 21);
            ftpClient.login(defaultFtpConfit.getUsername(), defaultFtpConfit.getPassword());
            ftpClient.enterLocalPassiveMode();//设置被动连接
            reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                return null;
            }
            ftpClient.rename(fromFileUrl, toFileUrl);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            try {
                if (ftpClient != null) {
                    ftpClient.logout();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } finally {
            // 注意,一定要在finally代码中断开连接，否则会导致占用ftp连接情况
            try {
                ftpClient.disconnect();
            } catch (IOException io) {
                logger.error(io.getMessage(), io);
            }
        }

        return null;
    }

}