package com.jrmf.utils.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class DbUtil {

	private static final Logger logger = LoggerFactory.getLogger(DbUtil.class);
	
	// 获取 springframework JDBC模板
	private JdbcTemplate jdbcTemplate = SpringContextHelper.getBean(JdbcTemplate.class);
	
	private Connection conn = null;
    
    @SuppressWarnings("unused")
	private static Properties p =null;
    
    //单利模式 --懒汉式(双重锁定)保证线程的安全性
    public static DbUtil db = null;
    
    public static DbUtil getDbUtil(){
    	
    	return new DbUtil();
    }
    
    public DbUtil(){
    	Connection connection = null;
        try {  
        	
//        	String driverName = ConfigProperties.getString("jdbc.master.driver");
//        	String paymentURL = ConfigProperties.getString("jdbc.master.url");
//        	String paymentUserName = ConfigProperties.getString("jdbc.master.username");
//        	String paymentPassWord = ConfigProperties.getString("jdbc.master.password");
//        	
//            Class.forName(driverName);//指定连接类型  
//            connection = DriverManager.getConnection(paymentURL, paymentUserName, paymentPassWord);//获取连接
            
            connection =   jdbcTemplate.getDataSource().getConnection();// 从框架里获取
        	setConn(connection);
        } catch (Exception e) {
          logger.error("---获取数据库连接错误："+e);
          close(null,null,connection);
        }
    }
    

//    public static DbUtil getInstance(){
//        if(db == null){
//            synchronized(DbUtil.class){
//                if(db == null){
//                    db = new DbUtil();
//                }            
//            }        
//        }
//        return db;
//    }
    
//    //建立数据库的连接
//    public Connection getConn(){
//    	
////		DataSource dataSource = jdbcTemplate.getDataSource();
//// 		try {
////			conn = dataSource.getConnection();
////		} catch (SQLException e) {
////          e.printStackTrace();
////          logger.error("---获取数据库连接错误："+e);
////          return null;
////		}
//    	
//
//    	
// 		return conn;
//    }
    
    public int  statementExecuteUpdate(String sql) throws SQLException{
    	
       int count;
       
		try {
			
			Statement statement = (Statement) conn.createStatement();
			logger.info("---执行SQL："+sql);
	        count = statement.executeUpdate(sql); 
	        statement.close();
	        logger.info("---执行SQL影响行数："+count);
		} catch (SQLException e) {
			logger.error("---执行SQL错误,SQL为 ："+sql);
			throw e;
		}  
 
        return count;
    }
    
    public Connection getConn() {
		return conn;
	}
	public void setConn(Connection conn) {
		this.conn = conn;
	}
	public int  preparedStatementExecuteUpdate(String sql,Object...params) throws SQLException{
    	
    	logger.info("---SQL ："+sql);
    	logger.info("---params ："+params);
    	
    	int count;
    	try {
    		
        	PreparedStatement pst = (PreparedStatement) conn.prepareStatement(sql);
            
            //处理将数据插入占位符
            int paramsIndex = 1;
            for(Object p : params){
                pst.setObject(paramsIndex++, p);
            }
            count = pst.executeUpdate(); 
            pst.close();
            logger.info("---执行SQL影响行数："+count);
		} catch (SQLException e) {
			
			logger.error("---执行SQL错误,SQL为 ："+sql);
			throw e;
		}
        return count;
    }
    
    
    //关闭资源
    public static void close(ResultSet rs,PreparedStatement pst,Connection conn){
        if(rs!=null){
            try {
                rs.close();
            } catch (SQLException e) {
                logger.error(e.getMessage(),e);
            }
            rs = null;
        }
        if(pst!=null){
            try {
                pst.close();
            } catch (SQLException e) {
                logger.error(e.getMessage(),e);
            }
            pst = null;
        }
        if(conn!=null){
            try {
                conn.close();
            } catch (SQLException e) {
                logger.error(e.getMessage(),e);
            }
            conn = null;
        }
    }
    

    
    //查询返回List容器
    public List<Map<String,Object>> query(String sql,Object...params){
    	logger.info("运行的 query Sql："+sql);
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            //获得连接
            conn = getConn();
            //获得preparedSttement对象进行预编译（？占位符）
            pst = conn.prepareStatement(sql);
            int paramsIndex = 1;
            for(Object p : params){
                pst.setObject(paramsIndex++, p);
            }
            //执行sql语句获得结果集的对象
            rs = pst.executeQuery();
            //获得结果集中列的信息
            ResultSetMetaData rst = rs.getMetaData();
            //获得结果集的列的数量
            int column = rst.getColumnCount();
            //创建List容器
            List<Map<String,Object>> rstList = new ArrayList<Map<String,Object>>();
            //处理结果
            int rowCount = 0; 
            while(rs.next()){
                //创建Map容器存取每一列对应的值
                Map<String,Object> m = new HashMap<String,Object>();
                StringBuffer sbRs = new StringBuffer();
                for(int i=1;i<=column;i++){
                    m.put(rst.getColumnName(i), rs.getObject(i));
                    sbRs.append(rst.getColumnName(i)+"="+rs.getString(rst.getColumnName(i)) +",");
                }
                logger.info("行号："+rowCount+",行数据："+sbRs);
                //将Map容器放入List容器中
                rstList.add(m);
            }
            return rstList;
        } catch (SQLException e) {
            logger.error(e.getMessage(),e);
            return null;
        }finally{
            //关闭资源
            close(rs, pst, conn);
        }
    }
    
  //查询返回List容器
    public List<Map<String,Object>> query(Connection conn,String sql,Object...params) throws SQLException{
    	logger.info("运行的 query Sql："+sql);
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            //获得preparedSttement对象进行预编译（？占位符）
            pst = conn.prepareStatement(sql);
            int paramsIndex = 1;
            for(Object p : params){
                pst.setObject(paramsIndex++, p);
            }
            //执行sql语句获得结果集的对象
            rs = pst.executeQuery();
            //获得结果集中列的信息
            ResultSetMetaData rst = rs.getMetaData();
            //获得结果集的列的数量
            int column = rst.getColumnCount();
            //创建List容器
            List<Map<String,Object>> rstList = new ArrayList<Map<String,Object>>();
            //处理结果
            int rowCount = 0; 
            while(rs.next()){
            	rowCount++; //记录查询返回的行数
                //创建Map容器存取每一列对应的值
                Map<String,Object> m = new HashMap<String,Object>();
                StringBuffer sbRs = new StringBuffer();
                for(int i=1;i<=column;i++){
                    m.put(rst.getColumnName(i), rs.getObject(i));
                    sbRs.append(rst.getColumnName(i)+"="+rs.getString(rst.getColumnName(i)) +",");
                   
                }
                logger.info("行号："+rowCount+",行数据："+sbRs);
                //将Map容器放入List容器中
                rstList.add(m);
            }
            logger.info("运行的 query Sql rowCount ："+rowCount);
            return rstList;
        } catch (SQLException e) {
            logger.error(e.getMessage(),e);
            throw e;
        }
    }
     
     public List<Map<String,Object>> query(String sql,List<Object> params){
            PreparedStatement pst = null;
            ResultSet rs = null;
            try {
                //获得连接
                conn = getConn();
                //获得preparedSttement对象进行预编译（？占位符）
                pst = conn.prepareStatement(sql);
                int paramsIndex = 1;
                for(Object p : params){
                    pst.setObject(paramsIndex++, p);
                }
                //执行sql语句获得结果集的对象
                rs = pst.executeQuery();
                //获得结果集中列的信息
                ResultSetMetaData rst = rs.getMetaData();
                //获得结果集的列的数量
                int column = rst.getColumnCount();
                //创建List容器
                List<Map<String,Object>> rstList = new ArrayList<Map<String,Object>>();
                //处理结果
                while(rs.next()){
                    //创建Map容器存取每一列对应的值
                    Map<String,Object> m = new HashMap<String,Object>();
                    for(int i=1;i<=column;i++){
                        m.put(rst.getColumnName(i), rs.getObject(i));
                    }
                    //将Map容器放入List容器中
                    rstList.add(m);
                }
                return rstList;
            } catch (SQLException e) {
                logger.error(e.getMessage(),e);
                return null;
            }finally{
                //关闭资源
                close(rs, pst, conn);
            }
        }
    
    //分页查询总共有多少条记录totleSize
    public long queryLong(String sql,Object...params){
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            //获得连接
            conn = getConn();
            //获得preparedSttement对象进行预编译（？占位符）
            pst = conn.prepareStatement(sql);
            int paramsIndex = 1;
            for(Object p : params){
                pst.setObject(paramsIndex++, p);
            }
            //执行sql语句获得结果集的对象
            rs = pst.executeQuery();
            while(rs.next()){
                return Long.valueOf(rs.getLong(1));
            }
            return 0;
        } catch (SQLException e) {
            logger.error(e.getMessage(),e);
            return 0;
        }
    }
    //插入
    @SuppressWarnings("unused")
	public boolean insert(String sql,Object...params){
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            //获得连接
            conn = getConn();
            //获得PrepareStatement对象进行预编译
            pst = conn.prepareStatement(sql);
            //处理将数据插入占位符
            int paramsIndex = 1;
            for(Object p : params){
                pst.setObject(paramsIndex++, p);
            }
            //执行sql语句
            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.error(e.getMessage(),e);
            return false;
        }finally{
            //关闭资源
            close(null, pst, conn);
        }
    }
    
    //修改
    @SuppressWarnings("unused")
	public boolean update(String sql,Object...params){
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            //获得连接
            conn = getConn();
            //获得PrepareStatement对象进行预编译
            pst = conn.prepareStatement(sql);
            //处理将数据插入占位符
            int paramsIndex = 1;
            for(Object p : params){
                pst.setObject(paramsIndex++, p);
            }
            //执行sql语句
            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.error(e.getMessage(),e);
            return false;
        }finally{
            //关闭资源
            close(null, pst, conn);
        }
    }
    
    //删除
    @SuppressWarnings("unused")
	public boolean delete(String sql,Object...params){
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            //获得连接
            conn = getConn();
            //获得PrepareStatement对象进行预编译
            pst = conn.prepareStatement(sql);
            //处理将数据插入占位符
            int paramsIndex = 1;
            for(Object p : params){
                pst.setObject(paramsIndex++, p);
            }
            //执行sql语句
            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.error(e.getMessage(),e);
            return false;
        }finally{
            //关闭资源
            close(null, pst, conn);
        }
    }

}
