package com.cgz.calculate.service;

import com.cgz.calculate.dao.CommitDao;
import com.cgz.calculate.dao.IssueDao;
import com.cgz.calculate.model.Commit;
import com.cgz.calculate.model.Issue;
import com.cgz.calculate.model.Transition;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IssueService {

    IssueDao issueDao = new IssueDao();

    CommitDao commitDao = new CommitDao();

    DateTimeFormatter formatterMySQL = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    //DateTimeFormatter formatterCommit = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");

    /**
     * 检查该issue是否reopened过
     */
    public boolean hasReopened(String key) {
        List<Transition> transitions = issueDao.getTransitions(key);
        for (Transition transition : transitions) {
            if (transition.getFromString().equalsIgnoreCase("REOPENED") ||
                    transition.getToString().equalsIgnoreCase("REOPENED")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查修复该issue时是否引入了新的bug
     */
    public boolean isLeadBug(String issueKey, List<Commit> commits, Map<String, List<Commit>> fileCommitsMap,
                             Map<String, List<String>> renamedFiles, List<String> keys) {
        for (Commit commit : commits) {
            for (Commit.FileChange fileChange : commit.getFilesChange()) {
                String fileName = fileChange.getFileName();
                List<Commit> fileCommits = fileCommitsMap.get(fileName);
                for (int i = 0; i < fileCommits.size(); i++) {
                    if (commit.getRevision().equals(fileCommits.get(i).getRevision())) {
                        Commit nextCommit = null;
                        if (i < fileCommits.size() - 1) { //判断下一个commit是否修复了bug
                            nextCommit = fileCommits.get(i + 1);
                        } else { //判断是否发生了Renamed，如果发生则检查此commit是否修复了bug
                            if (renamedFiles.containsKey(fileName)) {
                                nextCommit = fileCommitsMap.get(renamedFiles.get(fileName)).get(0);
                            }
                        }
                        if (nextCommit != null) {
                            ArrayList<String> issues = commitDao.getIssuesFromMessage(nextCommit.getMessage(), keys);
                            for (String issue : issues) {
                                //判断该issue是否是一个新的bug
                                Issue issueDB = issueDao.getIssue(issue);
                                if (issueDB != null &&
                                        !issueKey.equals(issue) &&
                                        "Bug".equalsIgnoreCase(issueDB.getIssueType())) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * 获取两个issue的OpenDuration，规则是：最迟解决时间-最早报告时间
     */
    public long getOpenDuration(String keyA, String keyB) {
        Issue issueA = issueDao.getIssue(keyA);
        Issue issueB = issueDao.getIssue(keyB);
        LocalDateTime createdA = LocalDateTime.parse(issueA.getCreated(), formatterMySQL);
        LocalDateTime createdB = LocalDateTime.parse(issueB.getCreated(), formatterMySQL);
        LocalDateTime resolutiondateA = LocalDateTime.parse(issueA.getResolutiondate(), formatterMySQL);
        LocalDateTime resolutiondateB = LocalDateTime.parse(issueB.getResolutiondate(), formatterMySQL);
        LocalDateTime created = createdA.isBefore(createdB) ? createdA : createdB;
        LocalDateTime end = resolutiondateA.isAfter(resolutiondateB) ? resolutiondateA : resolutiondateB;
        return Duration.between(created, end).toMinutes();
    }
}
