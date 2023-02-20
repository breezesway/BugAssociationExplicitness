package com.cgz.service;

import com.cgz.dao.CodeDependencyDao;
import com.cgz.model.BugPair;
import com.cgz.model.Commit;
import com.cgz.model.Reference;
import com.cgz.util.Const;
import com.cgz.util.KeyName;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CodeDependencyService {

    private CodeDependencyDao codeDependencyDao = new CodeDependencyDao();

    /**
     * 获取该BugPair对应文件的所有Reference
     *
     * @param bugPair
     * @param revision
     * @return
     */
    public List<Reference> getReferences(BugPair bugPair, String revision) {
        String bugAName = bugPair.getBugAName();
        String bugBName = bugPair.getBugBName();
        String keyPrefix = bugAName.substring(0, bugAName.indexOf('-'));
        String name = KeyName.getNameFromKey(keyPrefix).toLowerCase();
        String CodeRepositoryPath = Const.CodeRepositoryDir + "\\" + name;

        codeDependencyDao.CheckoutCommit(CodeRepositoryPath, revision);

        String udbPath = Const.dependencyOutputDir + "\\" + name + "\\udb\\" + bugAName + "_" + bugBName + ".udb";
        String outputPath = Const.dependencyOutputDir + "\\" + name + "\\output\\" + bugAName + "_" + bugBName + "_output.csv";
        String matrixPath = Const.dependencyOutputDir + "\\" + name + "\\matrix\\" + bugAName + "_" + bugBName + "_matrix.csv";

        codeDependencyDao.analyzeDependency(CodeRepositoryPath, udbPath, outputPath, matrixPath);

        List<Reference> referenceList = codeDependencyDao.findReferenceList(outputPath);
        Set<String> bugAFileSet = bugPair.getBugAFiles().stream().map(f -> getFileName(f.getFileName())).collect(Collectors.toSet());
        Set<String> bugBFileSet = bugPair.getBugBFiles().stream().map(f -> getFileName(f.getFileName())).collect(Collectors.toSet());
        return referenceList.parallelStream()
                .filter(r -> isRefInBugPair(r, bugAFileSet, bugBFileSet))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 判断该reference是否属于该bugPair
     *
     * @param reference
     * @param bugAFileSet
     * @param bugBFileSet
     * @return
     */
    private boolean isRefInBugPair(Reference reference, Set<String> bugAFileSet, Set<String> bugBFileSet) {
        String from = getFileName(reference.getFrom());
        String to = getFileName(reference.getTo());
        return bugAFileSet.contains(from) && bugBFileSet.contains(to)
                || bugAFileSet.contains(to) && bugBFileSet.contains(from);
    }

    /**
     * 将全路径文件名截取，返回文件名
     *
     * @param fileName
     * @return
     */
    private String getFileName(String fileName) {
        int i = fileName.lastIndexOf('\\');
        int j = fileName.lastIndexOf('.');
        return fileName.substring(i + 1, j);
    }
}
