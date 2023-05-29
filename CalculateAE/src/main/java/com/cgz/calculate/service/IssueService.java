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
     * @param issueKey 该issue的Key
     * @param commits 该issue对应的commit
     * @param fileCommitsMap 该项目每个文件对应的commit
     * @param renamedFiles 所有发生过renamed的文件
     * @param keys 该项目的Key前缀
     * @return 是否引入了新的Bug
     */
    public boolean isLeadBug(String issueKey, List<Commit> commits, Map<String, List<Commit>> fileCommitsMap,
                             Map<String, List<String>> renamedFiles, List<String> keys) {
        for (Commit commit : commits) {
            for (Commit.FileChange fileChange : commit.getFilesChange()) {
                String fileName = fileChange.getFileName();
                List<Commit> fileCommits = fileCommitsMap.get(fileName);
                for (int i = 0; i < fileCommits.size(); i++) {
                    if (commit.getRevision().equals(fileCommits.get(i).getRevision())) {
                        List<Commit> nextCommits = new ArrayList<>();
                        if (i < fileCommits.size() - 1) { //是否还存在下一个commit
                            nextCommits.add(fileCommits.get(i + 1));
                        } else { //改文件名不存在下一个commit了，此时判断是否发生了Renamed
                            if (renamedFiles.containsKey(fileName)) {
                                for(String f : renamedFiles.get(fileName)){
                                    nextCommits.add(fileCommitsMap.get(f).get(0));
                                }
                            }
                        }
                        for(Commit nextCommit : nextCommits){
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
