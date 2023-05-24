package com.cgz.calculate.dao;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.cgz.calculate.model.Issue;
import com.cgz.calculate.model.Transition;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class IssueDao {

    /**
     * 根据Key获取Issue
     */
    public Issue getIssue(String key){
        Issue issue = null;
        try {
            DruidPooledConnection conn = Database.getConnection();
            String sql = "select issuetype,status,created,resolutiondate from issue where issue.key = '"+key+"'";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            if(rs.next()){
                issue = new Issue();
                issue.setIssueType(rs.getString("issuetype"));
                issue.setStatus(rs.getString("status"));
                issue.setCreated(rs.getString("created"));
                issue.setResolutiondate(rs.getString("resolutiondate"));
            }
            pst.close();
            conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return issue;
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
}
