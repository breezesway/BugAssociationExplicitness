package com.cgz.calculate.service;

import com.cgz.calculate.dao.IssueDao;
import com.cgz.calculate.model.Transition;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class IssueService {

    IssueDao issueDao = new IssueDao();

    /**
     * 检查该issue是否reopened过
     */
    public boolean hasReopened(String key){
        List<Transition> transitions = issueDao.getTransitions(key);
        for (Transition transition : transitions) {
            if(transition.getFromString().equalsIgnoreCase("REOPENED") ||
            transition.getToString().equalsIgnoreCase("REOPENED")){
                return true;
            }
        }
        return false;
    }

    public long getOpenTime(String key){
        String createTime = issueDao.getCreateTime(key);
        List<Transition> transitions = issueDao.getTransitions(key);
        String endTime = null;
        for(int i = transitions.size()-1;i>=0;i--){
            Transition transition = transitions.get(i);
            if(transition.getToString().equalsIgnoreCase("RESOLVED") ||
            transition.getToString().equalsIgnoreCase("CLOSED")){
                endTime = transition.getCreated();
                break;
            }
        }
        if(endTime == null){
            throw new RuntimeException("endTime为空");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        LocalDateTime created = LocalDateTime.parse(createTime, formatter);
        LocalDateTime end = LocalDateTime.parse(endTime, formatter);
        Duration duration = Duration.between(created, end);
        return duration.toMinutes();
    }

}
