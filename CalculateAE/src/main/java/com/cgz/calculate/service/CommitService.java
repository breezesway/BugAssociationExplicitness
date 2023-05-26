package com.cgz.calculate.service;

import com.cgz.calculate.model.BugPair;
import com.cgz.calculate.model.Commit;
import com.cgz.calculate.dao.CommitDao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommitService {

    CommitDao commitDao = new CommitDao();

    public List<Commit> getCommitList(String commitFilePath, List<String> keys) {
        List<Commit> commitList = null;
        try {
            commitList = commitDao.getCommitListFromText(commitFilePath, keys);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return commitList;
    }

    /**
     * 解析出所有的issue对应的commit(一个或多个)
     */
    public HashMap<String, ArrayList<Commit>> parseIssueFromCommitList(List<Commit> commitList, List<String> keys) {
        HashMap<String, ArrayList<Commit>> map = new HashMap<>();
        for (Commit commit : commitList) {
            ArrayList<String> issues = commitDao.getIssuesFromMessage(commit.getMessage(), keys);
            if (issues.size() != 0) {   //Message中是否有IssueKey
                ArrayList<Commit> commits = new ArrayList<>(1);
                commits.add(commit);
                for (String issue : issues) {//放入map
                    if (map.containsKey(issue)) {
                        map.get(issue).add(commit);
                    } else {
                        map.put(issue, commits);
                    }
                }
            }
        }
        return map;
    }

    /**
     * 给定一对issuepair，返回要导出的版本的commit的Revision
     * @param bugPair 给定的一对issuePair
     * @return 返回对应的commit的Revision
     */
    public String getRevisionByIssuePair(BugPair bugPair, List<Commit> commits) {
        ArrayList<Commit> tempCommitList = new ArrayList<>();
        tempCommitList.addAll(bugPair.getBugACommit());
        tempCommitList.addAll(bugPair.getBugBCommit());
        Commit lastCommit = tempCommitList.get(0);
        for (Commit commit : tempCommitList) {
            String dateA = lastCommit.getDate();
            String dateB = commit.getDate();
            String stringA = parseDateToString(dateA);
            String stringB = parseDateToString(dateB);
            lastCommit = stringA.compareTo(stringB) >= 0 ? lastCommit : commit;
        }
        for (Commit.FileChange fileChange : lastCommit.getFilesChange()) {
            if ("Added".equals(fileChange.getOperate())) {
                int lastCommitIndex = commits.indexOf(lastCommit);
                if (lastCommitIndex != 0) {
                    return commits.get(lastCommitIndex - 1).getRevision();
                }
            }
        }
        return lastCommit.getRevision();
    }

    /**
     * 将05/10/2011 00:57:13形式的日期转换为20111005005713的字符串形式
     */
    private String parseDateToString(String date) {
        return date.substring(6, 10) +
                date.substring(3, 5) +
                date.substring(0, 2) +
                date.substring(11, 13) +
                date.substring(14, 16) +
                date.substring(17, 19);
    }

    /**
     * 判断两个issue是否属于同一个commit
     * @param issueA issueA的Key
     * @param issueB issueB的Key
     * @param map    commitrecord的map
     * @return 在同一个commit中则返回true，否则返回false
     */
    public boolean isSameCommit(String issueA, String issueB, HashMap<String, ArrayList<Commit>> map) {
        if (map.containsKey(issueA) && map.containsKey(issueB)) {
            List<Commit> commits1 = map.get(issueA);
            List<Commit> commits2 = map.get(issueB);
            StringBuilder commitA = new StringBuilder();
            StringBuilder commitB = new StringBuilder();
            for (Commit commit : commits1) {
                commitA.append(commit.getRevision());
            }
            for (Commit commit : commits2) {
                commitB.append(commit.getRevision());
            }
            return commitA.toString().contentEquals(commitB);
        }
        return false;
    }

    /**
     * 解析出每个文件出现在哪些commit中，此时未处理Renamed情况
     */
    public Map<String, List<Commit>> parseFileInCommits(List<Commit> commits) {
        HashMap<String, List<Commit>> map = new HashMap<>(commits.size() * 2);
        for (Commit commit : commits) {
            for (Commit.FileChange fileChange : commit.getFilesChange()) {
                String fileName = fileChange.getFileName();
                if (map.containsKey(fileName)) {
                    map.get(fileName).add(commit);
                } else {
                    ArrayList<Commit> c = new ArrayList<>();
                    c.add(commit);
                    map.put(fileName, c);
                }
            }
        }
        return map;
    }

    /**
     * 解析出所有发生了Renamed的文件
     * @return 返回一个Map，key是文件的原名，value是文件更改后的名
     */
    public Map<String, String> parseRenamedFile(List<Commit> commits) {
        HashMap<String, String> map = new HashMap<>();
        for (Commit commit : commits) {
            for (Commit.FileChange fileChange : commit.getFilesChange()) {
                if ("Renamed".equals(fileChange.getOperate())) {
                    if (map.containsKey(fileChange.getOldFileName()) &&
                            !map.get(fileChange.getOldFileName()).equals(fileChange.getFileName())) {
                        System.out.println("已存在：" + fileChange.getOldFileName() + "--→ " + map.get(fileChange.getOldFileName()));
                        System.out.println("现在有：" + fileChange.getOldFileName() + "--→ " + fileChange.getFileName());
                        //throw new RuntimeException("该fileName"+fileChange+"已在map中");
                    } else {
                        map.put(fileChange.getOldFileName(), fileChange.getFileName());
                    }
                }
            }
        }
        return map;
    }
}
