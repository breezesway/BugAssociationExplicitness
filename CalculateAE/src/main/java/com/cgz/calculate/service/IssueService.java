package com.cgz.calculate.service;

import com.cgz.calculate.dao.IssueDao;
import com.cgz.calculate.model.Transition;

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



}
