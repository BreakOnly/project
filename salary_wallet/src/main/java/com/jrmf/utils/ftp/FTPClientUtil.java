package com.jrmf.utils.ftp;

import com.jrmf.controller.constant.BestSignConfig;
import com.jrmf.utils.FtpTool;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;

/**
 * ftp客户端辅助bean
 *
 * @author jelly
 */
public class FTPClientUtil {

    private static Logger logger = LoggerFactory.getLogger(FTPClientUtil.class);


    private FTPClientPool ftpClientPool;

    @Autowired
    private BestSignConfig bestSignConfig;

    public void setFtpClientPool(FTPClientPool ftpClientPool) {
        this.ftpClientPool = ftpClientPool;
    }

    /**
     * 创建目录    单个不可递归
     *
     * @param pathname 目录名称
     * @return
     * @throws Exception
     */
    public boolean makeDirectory(String pathname) throws Exception {

        FTPClient client = null;
        try {
            client = ftpClientPool.borrowObject();
            return client.makeDirectory(pathname);
        } finally {
            ftpClientPool.returnObject(client);
        }
    }

    /**
     * 删除目录，单个不可递归
     *
     * @param pathname
     * @return
     * @throws IOException
     */
    public boolean removeDirectory(String pathname) throws Exception {
        FTPClient client = null;
        try {
            client = ftpClientPool.borrowObject();
            return client.removeDirectory(pathname);
        } finally {
            ftpClientPool.returnObject(client);
        }
    }

    /**
     * 删除文件 单个 ，不可递归
     *
     * @param pathname
     * @return
     * @throws Exception
     */
    public boolean deleteFile(String pathname) throws Exception {

        FTPClient client = null;
        try {
            client = ftpClientPool.borrowObject();
            return client.deleteFile(pathname);
        } finally {
            ftpClientPool.returnObject(client);
        }
    }

    /**
     * 上传文件
     *
     * @param remote
     * @param local
     * @return
     * @throws Exception
     */
    public boolean storeFile(String remote, InputStream local) throws Exception {
        FTPClient client = null;
        try {
            client = ftpClientPool.borrowObject();
            return client.storeFile(remote, local);
        } finally {
            ftpClientPool.returnObject(client);
        }
    }

    /**
     * @param ftpPath 文件路径
     * @return 文件是否存在
     */
    public boolean isExists(String ftpPath) {
        boolean b = checkFile(ftpPath);
//        if (!b) {
//            b = FtpTool.checkFile(bestSignConfig.getFtpURL(), 21, ftpPath, bestSignConfig.getUsername(), bestSignConfig.getPassword());
//        }
        return b;
    }

    private boolean checkFile(String ftpPath) {
        FTPClient client = null;
        try {
            try {
                client = ftpClientPool.borrowObject();
                FTPFile[] files = client.listFiles(ftpPath);
                if (files != null && files.length > 0) {
                    return true;
                } else {
                    logger.error("file is not exist");
                    return false;
                }
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
                return false;
            }
        } finally {
            ftpClientPool.returnObject(client);
        }
    }

    /**
     * Description: 向FTP服务器上传文件
     *  @param path     FTP服务器保存目录
     * @param filename 上传到FTP服务器上的文件名
     * @param input    输入流
     */
    public boolean uploadFile(String path, // FTP服务器保存目录
                              String filename, // 上传到FTP服务器上的文件名
                              InputStream input // 输入流
    ) {
        FTPClient ftp = new FTPClient();
        boolean storeFile = false;

        try {
            ftp = ftpClientPool.borrowObject();
            ftp.setControlEncoding("UTF-8");
//            File serverFile = new File(path);
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftp.enterLocalPassiveMode();
            //不存在   就创建
            if (!this.checkFile(path)) {
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

            storeFile = ftp.storeFile(new String((path + filename).getBytes("UTF-8"),"iso-8859-1"),input);
            input.close();
        }  catch (Exception e) {
            logger.error(e.getMessage(),e);
            ftpClientPool.returnObject(ftp);
        } finally {
            ftpClientPool.returnObject(ftp);
        }
        return storeFile;
    }

}