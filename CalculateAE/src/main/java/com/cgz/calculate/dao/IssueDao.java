package com.cgz.calculate.dao;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.cgz.calculate.model.Transition;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class IssueDao {

    /**
     * 判断该issue的类型
     * @param key 该issue的key
     * @return bug的类型
     */
    public String getIssueType(String key){
        String type = "";
        try {
            DruidPooledConnection conn = Database.getConnection();
            String sql = "select issuetype from issue where issue.key = '"+key+"'";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            if(rs.next()){
                type = rs.getString("issuetype");
            }
            pst.close();
            conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return type;
    }

    /**
     * 获取该issue的所有transition
     */
    public List<Transition> getTransitions(String key){
        ArrayList<Transition> list = new ArrayList<>();
        try {
            DruidPooledConnection conn = Database.getConnection();
            String sql = "select * from transition where issuekey = '"+key+"'";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            while (rs.next()){
                Transition transition = Transition.builder()
                        .author(rs.getString("authordisplayname"))
                        .created(rs.getString("created"))
                        .issueKey(rs.getString("issuekey"))
                        .fromString(rs.getString("fromstring"))
                        .toString(rs.getString("tostring"))
                        .build();
                list.add(transition);
            }
            pst.close();
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    /**
     * 获取该issue的创建时间
     */
    public String getCreateTime(String key) {
        String createTime = "";
        try {
            DruidPooledConnection conn = Database.getConnection();
            String sql = "select created from issue where issue.key = '" + key + "'";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            if(rs.next()){
                createTime = rs.getString("created");
            }
            pst.close();
            conn.close();
        }catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return createTime;
    }
}
