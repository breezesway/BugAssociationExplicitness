package com.cgz.calculate.service;

import com.cgz.calculate.dao.IssueDao;
import com.cgz.calculate.dao.BugPairDao;
import com.cgz.calculate.model.BugPair;
import com.cgz.calculate.model.Commit;
import com.cgz.calculate.util.KeyName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class BugPairService {

    BugPairDao bugPairDao = new BugPairDao();
    IssueDao issueDao = new IssueDao();
    CommitService commitService = new CommitService();
    CodeDependencyService codeDependencyService = new CodeDependencyService();
    IssueService issueService = new IssueService();

    /**
     * 获取该key(或keys，即一个项目对应多个key)的所有BugPair
     */
    public List<BugPair> getBugPairListByKeys(String commitFilePath, String name) {
        List<BugPair> bugPairList = new ArrayList<>();
        List<String> keys = KeyName.getKeyListFromName(name);
        for (String keyPrefix : keys) {
            List<BugPair> bugPairs = bugPairDao.findBugPairListByKeyPrefix(keyPrefix);
            bugPairList.addAll(bugPairs);
        }
        List<Commit> commitList = commitService.getCommitList(commitFilePath, keys);
        HashMap<String, ArrayList<Commit>> issueCommitsMap = commitService.parseIssueFromCommitList(commitList, keys);

        List<BugPair> filtered = filterBugPairs(bugPairList, issueCommitsMap);
        filtered.parallelStream().forEach(b -> setMetrics(name,b, commitList, issueCommitsMap));
        filtered.forEach(b -> setRef(name,b,commitList));
        filtered.forEach(this::calculateMetrics);
        return filtered;
    }

    public void saveProjectBugPairMapAsExcel(List<BugPair> bugPairList, String excelFilePath) {
        bugPairDao.saveAllProjectBugPairMapAsExcel(bugPairList, excelFilePath);
    }

    /**
     * 过滤list，只留下两个都为Bug类型，且必须在数据库和commit文件中同时存在，（且两个Bug修改的文件数量均>=1），的BugPair
     */
    private List<BugPair> filterBugPairs(List<BugPair> bugPairList, HashMap<String, ArrayList<Commit>> issueCommitsMap) {
        return bugPairList.parallelStream().filter(bugPair -> {
            String bugAType = issueDao.getIssueType(bugPair.getBugAName());
            String bugBType = issueDao.getIssueType(bugPair.getBugBName());
            return "Bug".equalsIgnoreCase(bugAType) &&
                    "Bug".equalsIgnoreCase(bugBType) &&
                    issueCommitsMap.containsKey(bugPair.getBugAName()) &&
                    issueCommitsMap.containsKey((bugPair.getBugBName()));
        }).collect(Collectors.toList());
    }

    /**
     * 设置BugPair对应的Commit(s)、Files、FileName、SameCommit、interFileNum、unionFileNum、references、reopen、opentime
     * @param name 项目名
     */
    private void setMetrics(String name,BugPair bugPair, List<Commit> commitList, HashMap<String, ArrayList<Commit>> issueCommitsMap) {
        ArrayList<Commit> bugACommits = issueCommitsMap.get(bugPair.getBugAName());
        ArrayList<Commit> bugBCommits = issueCommitsMap.get(bugPair.getBugBName());
        bugPair.setBugACommit(bugACommits);
        bugPair.setBugBCommit(bugBCommits);
        bugPair.setBugAFiles(bugACommits.stream().flatMap(b -> b.getFilesChange().stream()).distinct().collect(Collectors.toList()));
        bugPair.setBugBFiles(bugBCommits.stream().flatMap(b -> b.getFilesChange().stream()).distinct().collect(Collectors.toList()));
        bugPair.setSameCommit(commitService.isSameCommit(bugPair.getBugAName(), bugPair.getBugBName(), issueCommitsMap));
        bugPair.setBugAFileNum(bugPair.getBugAFiles().size());
        bugPair.setBugBFileNum(bugPair.getBugBFiles().size());
        setInterAndUnion(bugPair);
        //setRef(name,bugPair,commitList);
        reopen(bugPair);
        opentime(bugPair);
    }

    /**
     * 设置该BugPair的文件交集和并集大小
     */
    private void setInterAndUnion(BugPair bugPair) {
        HashSet<String> bugASet = bugPair.getBugAFiles().stream().map(Commit.FileChange::getFileName).collect(Collectors.toCollection(HashSet::new));
        HashSet<String> bugBSet = bugPair.getBugBFiles().stream().map(Commit.FileChange::getFileName).collect(Collectors.toCollection(HashSet::new));
        HashSet<String> inter = new HashSet<>(bugASet);
        inter.retainAll(bugBSet);
        bugPair.setInterFileNum(inter.size());
        HashSet<String> union = new HashSet<>(bugASet);
        union.addAll(bugBSet);
        bugPair.setUnionFileNum(union.size());
    }

    /**
     * 设置该BugPair的references
     * @param name 项目名
     */
    private void setRef(String name,BugPair bugPair,List<Commit> commitList) {
        String revision = commitService.getRevisionByIssuePair(bugPair, commitList);
        bugPair.setReferences(codeDependencyService.getReferences(name,bugPair,revision));
    }

    /**
     * 计算该BugPair的HAE、CAE、AE
     */
    private void calculateMetrics(BugPair bugPair) {
        calculateHAE(bugPair);
        calculateCAE(bugPair);
        calculateAE(bugPair);
    }

    private void calculateHAE(BugPair bugPair) {
        if(bugPair.getUnionFileNum() != 0){
            bugPair.setHAE(bugPair.getInterFileNum() / bugPair.getUnionFileNum());
        }
    }

    private void calculateCAE(BugPair bugPair) {
        int bugAFileNum = bugPair.getBugAFileNum();
        int bugBFileNum = bugPair.getBugBFileNum();
        int interFileNum = (int) bugPair.getInterFileNum();
        double cae;
        if(bugAFileNum == 0 || bugBFileNum == 0){
            cae = 0;
        }else if(bugAFileNum == 1 && bugBFileNum == 1 && interFileNum == 1){
            cae = 1;
        }else{
            cae = ((double) bugPair.getReferences().size())/
                    (bugAFileNum * bugBFileNum - (interFileNum*(interFileNum+1)/2.0));
        }
        bugPair.setCAE(cae);
    }

    private void calculateAE(BugPair bugPair) {
        if((bugPair.getBugAFileNum() + bugPair.getBugBFileNum()) == 0){
            bugPair.setAE(0);
            return;
        }
        double haeP = bugPair.getInterFileNum() / (double) (bugPair.getBugAFileNum() + bugPair.getBugBFileNum());
        bugPair.setAE(bugPair.getHAE() * haeP + bugPair.getCAE() * (1.0 - haeP));
    }

    private void reopen(BugPair bugPair) {
        bugPair.setBugAReopen(issueService.hasReopened(bugPair.getBugAName()));
        bugPair.setBugBReopen(issueService.hasReopened(bugPair.getBugBName()));
        bugPair.setReopen(bugPair.isBugAReopen() || bugPair.isBugBReopen());
    }

    private void opentime(BugPair bugPair) {
        bugPair.setBugAOpenDuration(issueService.getOpenDuration(bugPair.getBugAName()));
        bugPair.setBugBOpenDuration(issueService.getOpenDuration(bugPair.getBugBName()));
        bugPair.setOpenDuration((bugPair.getBugAOpenDuration() + bugPair.getBugBOpenDuration()) / 2);
    }
}
