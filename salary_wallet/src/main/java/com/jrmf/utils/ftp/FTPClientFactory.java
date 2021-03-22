package com.jrmf.utils.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
 
/**
 * ftpclient 工厂
 * @author jelly
 *
 */
public class FTPClientFactory extends BasePooledObjectFactory<FTPClient> {
	
    private static Logger logger = LoggerFactory.getLogger(FTPClientFactory.class);
    
    private FtpPoolConfig ftpPoolConfig;
    
     
   
	public FtpPoolConfig getFtpPoolConfig() {
		return ftpPoolConfig;
	}
 
	public void setFtpPoolConfig(FtpPoolConfig ftpPoolConfig) {
		this.ftpPoolConfig = ftpPoolConfig;
	}
 
	/**
     * 新建对象
     */
    @Override
    public FTPClient create() throws Exception {
        FTPClient ftpClient = new FTPClient();
        try {
        	
        	logger.info("连接ftp服务器:" +ftpPoolConfig.getHost()+":"+ftpPoolConfig.getPort());
        	ftpClient.connect(ftpPoolConfig.getHost(), ftpPoolConfig.getPort());
            
            int reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                logger.error("FTPServer 拒绝连接");
                return null;
            }
            boolean result = ftpClient.login(ftpPoolConfig.getUsername(),ftpPoolConfig.getPassword());
            if (!result) {
                logger.error("ftpClient登录失败!");
                throw new Exception("ftpClient登录失败! userName:"+ ftpPoolConfig.getUsername() + ", password:"
                        + ftpPoolConfig.getPassword());
            }
				logger.info("进入ftp被动模式");
				ftpClient.enterLocalPassiveMode();
        } catch (IOException e) {
            logger.error("FTP连接失败：", e);
        }
        return ftpClient;
    }

    @Override
    public PooledObject<FTPClient> wrap(FTPClient ftpClient) {
        return new DefaultPooledObject<FTPClient>(ftpClient);
    }

    /**
     * 销毁对象
     */
    @Override
    public void destroyObject(PooledObject<FTPClient> p) throws Exception {
        FTPClient ftpClient = p.getObject();
        try {
            if (ftpClient != null && ftpClient.isConnected()) {
                ftpClient.logout();
            }
        } catch (IOException io) {
            logger.error(io.getMessage(), io);
        } finally {
            // 注意,一定要在finally代码中断开连接，否则会导致占用ftp连接情况
            try {
                ftpClient.disconnect();
            } catch (IOException io) {
                logger.error(io.getMessage(), io);
            }
        }
    }
 
    /**
     * 验证对象
     */
    @Override
    public boolean validateObject(PooledObject<FTPClient> p) {
        FTPClient ftpClient = p.getObject();
        boolean connect = false;
        try {
            connect = ftpClient.sendNoOp();
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
        }
        return connect;
    }
    
 
    /**
     *  No-op.
     *
     *  @param p ignored
     */
    @Override
    public void activateObject(PooledObject<FTPClient> p) throws Exception {
        // The default implementation is a no-op.
    }
 
    /**
     *  No-op.
     *
     * @param p ignored
     */
    @Override
    public void passivateObject(PooledObject<FTPClient> p)
        throws Exception {
        // The default implementation is a no-op.
    }
}