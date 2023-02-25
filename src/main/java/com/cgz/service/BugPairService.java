package com.cgz.service;

import com.cgz.dao.BugPairDao;
import com.cgz.dao.IssueDao;
import com.cgz.model.BugPair;
import com.cgz.model.Commit;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BugPairService {

    BugPairDao bugPairDao = new BugPairDao();
    IssueDao issueDao = new IssueDao();

    CommitService commitService = new CommitService();
    CodeDependencyService codeDependencyService = new CodeDependencyService();

    /**
     * 获取该key(或keys，即一个项目对应多个key)的所有BugPair
     * @param keys
     * @return
     */
    public List<BugPair> getBugPairListByKeys(String commitFilePath, List<String> keys) {
        List<BugPair> bugPairList = new ArrayList<>();
        for (String keyPrefix : keys) {
            List<BugPair> bugPairs = bugPairDao.findBugPairListByKeyPrefix(keyPrefix);
            bugPairList.addAll(bugPairs);
        }
        List<Commit> commitList = commitService.getCommitList(commitFilePath, keys);
        HashMap<String, ArrayList<Commit>> issueCommitsMap = commitService.parseIssueFromCommitList(commitList, keys);

        List<BugPair> filteredBugPairList = filterBugPairs(bugPairList, issueCommitsMap);
        filteredBugPairList.forEach(b -> setMetrics(b, commitList, issueCommitsMap));
        filteredBugPairList.forEach(this::calculateMetrics);
        return filteredBugPairList;
    }

    public void saveProjectBugPairMapAsExcel(List<BugPair> bugPairList, String excelFilePath) {
        bugPairDao.saveAllProjectBugPairMapAsExcel(bugPairList, excelFilePath);
    }

    /**
     * 过滤list，只留下两个都为Bug类型，且必须在数据库和commit文件中同时存在，（且两个Bug修改的文件数量均>=1），的BugPair
     *
     * @param bugPairList
     * @return
     */
    private List<BugPair> filterBugPairs(List<BugPair> bugPairList, HashMap<String, ArrayList<Commit>> issueCommitsMap) {
        return bugPairList.parallelStream().filter(bugPair -> {
            String bugAType = issueDao.findIssueTypeByKey(bugPair.getBugAName());
            String bugBType = issueDao.findIssueTypeByKey(bugPair.getBugBName());
            return ("Bug".equalsIgnoreCase(bugAType) || "Bug".equalsIgnoreCase(bugBType)) &&
                    issueCommitsMap.containsKey(bugPair.getBugAName()) && issueCommitsMap.containsKey((bugPair.getBugBName()));
        }).collect(Collectors.toList());
    }

    /**
     * 设置BugPair对应的Commit(s)、Files、FileName、SameCommit、interFileNum、unionFileNum、references
     * @param bugPair
     * @param commitList
     * @param issueCommitsMap
     */
    private void setMetrics(BugPair bugPair, List<Commit> commitList, HashMap<String, ArrayList<Commit>> issueCommitsMap) {
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
        setRef(bugPair,commitList);
    }

    /**
     * 设置该BugPair的文件交集和并集大小
     *
     * @param bugPair
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
     * @param bugPair
     * @param commitList
     */
    private void setRef(BugPair bugPair,List<Commit> commitList) {
        String revision = commitService.getRevisionByIssuePair(bugPair, commitList);
        bugPair.setReferences(codeDependencyService.getReferences(bugPair,revision));
    }

    /**
     * 计算该BugPair的HAE、CAE、AE
     * @param bugPair
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
                    (double) (bugAFileNum * bugBFileNum - (interFileNum == 0? 0:IntStream.rangeClosed(1,interFileNum).reduce(1, (a,b) -> a*b)));
        }
        bugPair.setCAE(cae);
    }

    private void calculateAE(BugPair bugPair) {
        if((bugPair.getBugAFileNum() + bugPair.getBugBFileNum()) == 0){
            bugPair.setAE(0);
            return;
        }
        double haeP = 2 * bugPair.getInterFileNum() / (double) (bugPair.getBugAFileNum() + bugPair.getBugBFileNum());
        bugPair.setAE(bugPair.getHAE() * haeP + bugPair.getCAE() * (1.0 - haeP));
    }
}
