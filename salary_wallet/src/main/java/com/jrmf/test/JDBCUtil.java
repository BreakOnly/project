package com.jrmf.test;

import java.io.InputStream;
import java.sql.*;

/**
 * 用途：
 * 作者：郭桐宁
 * 时间：2019/1/31 17:12
 * Version:1.0
 */
public class JDBCUtil {

    public static String DRIVERNAME = "com.mysql.cj.jdbc.Driver";
    public static String URL = "jdbc:mysql://10.0.0.6:3306/mf_salary_wallet";
    public static String USER = "mf_web";
    public static String PASSWORD = "mfkj_2016@1121!";

    public static Connection conn = null;

    public static Connection getConnection() throws Exception {
        if (conn != null) {
            return conn;
        }

        Class.forName(DRIVERNAME);
        conn = DriverManager.getConnection(URL, USER, PASSWORD);

        return conn;
    }

    public static void closeResource(Connection conn, PreparedStatement st) throws SQLException {
        st.close();
        conn.close();
    }

    public static void closeResource(Connection conn, ResultSet rs, PreparedStatement st) throws SQLException {
        st.close();
        rs.close();
        conn.close();
    }
}
