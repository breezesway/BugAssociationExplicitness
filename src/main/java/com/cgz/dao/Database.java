package com.cgz.dao;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;

import java.sql.SQLException;

public class Database {

    private static String databaseName = null;
    private static String username = "root";
    private static String password = "123456";

    private static DruidDataSource dataSource;

    static {
        initDataSource("jira");
    }

    public static DruidPooledConnection getConnection() throws SQLException {
        //获取连接
        return dataSource.getConnection();
    }

    public static void initDataSource(String dbName){
        databaseName = dbName;
        //数据源配置
        dataSource = new DruidDataSource();
        dataSource.setUrl("jdbc:mysql://127.0.0.1/"+databaseName+"?serverTimezone=UTC&rewriteBatchedStatements=true");
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver"); //这个可以缺省的，会根据url自动识别
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        //下面都是可选的配置
        dataSource.setInitialSize(10);  //初始连接数，默认0
        dataSource.setMaxActive(20);  //最大连接数，默认8
        dataSource.setMinIdle(10);  //最小闲置数
        dataSource.setMaxWait(2000);  //获取连接的最大等待时间，单位毫秒
        dataSource.setPoolPreparedStatements(true); //缓存PreparedStatement，默认false
        dataSource.setMaxOpenPreparedStatements(20); //缓存PreparedStatement的最大数量，默认-1（不缓存）。大于0时会自动开启缓存PreparedStatement，所以可以省略上一句代码
    }
}
