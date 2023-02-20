package com.cgz.dao;

import com.alibaba.druid.pool.DruidPooledConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IssueDao {

    /**
     * 判断该issue的类型
     * @param key 该issue的key
     * @return bug的类型
     */
    public String findIssueTypeByKey(String key){
        String type = "";
        try {
            DruidPooledConnection conn = Database.getConnection();
            String sql = "select issuetype from issue where issue.key = '"+key+"'";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            if(rs.next()){
                type = rs.getString("issuetype");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return type;
    }

}
