package com.cgz.calculate.service;

import com.cgz.calculate.dao.IssueDao;
import com.cgz.calculate.dao.BugPairDao;
import com.cgz.calculate.model.BugPair;
import com.cgz.calculate.model.Commit;
import com.cgz.calculate.util.KeyName;

import java.util.*;
import java.util.stream.Collectors;

public class BugPairService {

    BugPairDao bugPairDao = new BugPairDao();
    IssueDao issueDao = new IssueDao();
    CommitService commitService = new CommitService();
    CodeDependencyService codeDependencyService = new CodeDependencyService();
    IssueService issueService = new IssueService();

    /**
     * 获取该key(或keys，即一个项目对应多个key)的所有BugPair
     * @param commitFilePath commit文件的路径
     * @param name 项目名
     * @return 该项目所有的符合要求的BugPair
     */
    public List<BugPair> getBugPairListByKeys(String commitFilePath, String name) {
        List<BugPair> bugPairList = new ArrayList<>();

        //根据项目名获取该项目的Key（一个项目可能对应对各Key）
        List<String> keys = KeyName.getKeyListFromName(name);

        //查找MySQL中的issue
        for (String keyPrefix : keys) {
            List<BugPair> bugPairs = bugPairDao.findBugPairListByKeyPrefix(keyPrefix);
            bugPairList.addAll(bugPairs);
        }
        //获取所有commit记录
        List<Commit> commitList = commitService.getCommitList(commitFilePath, keys);
        //从commit记录中解析出Issue，得到一个Map，每个Issue对应的commit(s)
        HashMap<String, ArrayList<Commit>> issueCommitsMap = commitService.parseIssueFromCommitList(commitList, keys);
        //从commit记录中解析出每个文件出现在哪些Commit(s)
        Map<String, List<Commit>> fileCommitsMap = commitService.parseFileInCommits(commitList);
        //从commit记录中解析出发生了Renamed的文件
        Map<String, String> renamedFiles = commitService.parseRenamedFile(commitList);

        //过滤bugPair，只留下两个都为Bug类型，且必须在数据库和commit文件中同时存在的BugPair
        List<BugPair> filtered = bugPairList.parallelStream().filter(bugPair -> {
            String bugAType = issueDao.getIssueType(bugPair.getBugAName());
            String bugBType = issueDao.getIssueType(bugPair.getBugBName());
            return "Bug".equalsIgnoreCase(bugAType) &&
                    "Bug".equalsIgnoreCase(bugBType) &&
                    issueCommitsMap.containsKey(bugPair.getBugAName()) &&
                    issueCommitsMap.containsKey((bugPair.getBugBName()));
        }).collect(Collectors.toList());

        //设置每个bugPair的部分指标
        filtered.parallelStream().forEach(bugPair -> {
            ArrayList<Commit> bugACommits = issueCommitsMap.get(bugPair.getBugAName());
            ArrayList<Commit> bugBCommits = issueCommitsMap.get(bugPair.getBugBName());
            //设置BugPair对应的Commit(s)
            bugPair.setBugACommit(bugACommits);
            bugPair.setBugBCommit(bugBCommits);
            //设置BugPair对应的File(s)
            bugPair.setBugAFiles(bugACommits.stream().flatMap(b -> b.getFilesChange().stream()).distinct().collect(Collectors.toList()));
            bugPair.setBugBFiles(bugBCommits.stream().flatMap(b -> b.getFilesChange().stream()).distinct().collect(Collectors.toList()));
            //设置该bugPair是否属于同一个commit
            bugPair.setSameCommit(commitService.isSameCommit(bugPair.getBugAName(), bugPair.getBugBName(), issueCommitsMap));
            //设置该bugPair涉及的文件数量
            bugPair.setBugAFileNum(bugPair.getBugAFiles().size());
            bugPair.setBugBFileNum(bugPair.getBugBFiles().size());
            //设置该BugPair的文件交集和并集大小
            setInterAndUnion(bugPair);
            //设置该bugPair是否Reopen过
            bugPair.setBugAReopen(issueService.hasReopened(bugPair.getBugAName()));
            bugPair.setBugBReopen(issueService.hasReopened(bugPair.getBugBName()));
            bugPair.setReopen(bugPair.isBugAReopen() || bugPair.isBugBReopen());
            //设置修复该bugPair时是否引入了新的bug
            bugPair.setBugALeadBug(issueService.isLeadBug(bugPair.getBugAName(), bugPair.getBugACommit(), fileCommitsMap, renamedFiles, keys));
            bugPair.setBugBLeadBug(issueService.isLeadBug(bugPair.getBugBName(), bugPair.getBugBCommit(), fileCommitsMap, renamedFiles, keys));
            bugPair.setLeadBug(bugPair.isBugALeadBug() || bugPair.isBugBLeadBug());
            //设置该bugPair打开的时间
            bugPair.setBugAOpenDuration(issueService.getOpenDuration(bugPair.getBugAName(), bugPair.getBugACommit()));
            bugPair.setBugBOpenDuration(issueService.getOpenDuration(bugPair.getBugBName(), bugPair.getBugBCommit()));
            bugPair.setOpenDuration((bugPair.getBugAOpenDuration() + bugPair.getBugBOpenDuration()) / 2);
        });

        //再次过滤bugPair，只留下两个Bug都至少修改了一个文件的BugPair
        filtered = filtered.parallelStream()
                .filter(bugPair -> bugPair.getBugAFileNum()>0 && bugPair.getBugBFileNum()>0)
                .collect(Collectors.toList());

        //设置bugPair的references
        filtered.forEach(bugPair -> {
            String revision = commitService.getRevisionByIssuePair(bugPair, commitList);
            bugPair.setReferences(codeDependencyService.getReferences(name,bugPair,revision));
        });

        //计算该BugPair的HAE、CAE、AE
        filtered.forEach(bugPair -> {
            calculateHAE(bugPair);
            calculateCAE(bugPair);
            calculateAE(bugPair);
        });

        return filtered;
    }

    public void saveProjectBugPairMapAsExcel(List<BugPair> bugPairList, String excelFilePath) {
        bugPairDao.saveAllProjectBugPairMapAsExcel(bugPairList, excelFilePath);
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
}
