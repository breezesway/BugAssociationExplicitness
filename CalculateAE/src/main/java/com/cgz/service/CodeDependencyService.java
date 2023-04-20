package com.cgz.service;

import com.cgz.dao.CodeDependencyDao;
import com.cgz.model.BugPair;
import com.cgz.model.Reference;
import com.cgz.util.Const;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class CodeDependencyService {

    private final CodeDependencyDao codeDependencyDao = new CodeDependencyDao();

    /**
     * 获取该BugPair对应文件的所有Reference
     * @param name 项目名
     */
    public List<Reference> getReferences(String name,BugPair bugPair, String revision) {
        String bugAName = bugPair.getBugAName();
        String bugBName = bugPair.getBugBName();
        String CodeRepositoryPath = Const.CodeRepositoryDir + "\\" + name;

        String udbPath = Const.dependencyOutputDir + "\\" + name + "\\udb\\" + bugAName + "_" + bugBName + ".udb";
        String outputPath = Const.dependencyOutputDir + "\\" + name + "\\output\\" + bugAName + "_" + bugBName + "_output.csv";
        String matrixPath = Const.dependencyOutputDir + "\\" + name + "\\matrix\\" + bugAName + "_" + bugBName + "_matrix.csv";

        if (!new File(outputPath).exists()) {
            codeDependencyDao.CheckoutCommit(CodeRepositoryPath, revision);
            codeDependencyDao.analyzeDependency(CodeRepositoryPath, udbPath, outputPath, matrixPath);
        }

        List<Reference> referenceList = codeDependencyDao.findReferenceList(outputPath);
        List<String> bugAFileList = bugPair.getBugAFiles().stream().map(f -> getCommitFileName(f.getFileName())).collect(Collectors.toList());
        List<String> bugBFileList = bugPair.getBugBFiles().stream().map(f -> getCommitFileName(f.getFileName())).collect(Collectors.toList());
        return referenceList.parallelStream()
                .filter(r -> isRefInBugPair(r, bugAFileList, bugBFileList))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 判断该reference是否属于该bugPair
     */
    private boolean isRefInBugPair(Reference reference, List<String> bugAFileList, List<String> bugBFileList) {
        String from = getCodeFileName(reference.getFrom());
        String to = getCodeFileName(reference.getTo());
        /*return bugAFileSet.contains(from) && bugBFileSet.contains(to)
                || bugAFileSet.contains(to) && bugBFileSet.contains(from);*/
        for(String bugAFile:bugAFileList){
            if(from.contains(bugAFile)){
                for(String bugBFile:bugBFileList){
                    if(to.contains(bugBFile)) return true;
                }
            }
        }
        for(String bugAFile:bugAFileList){
            if(to.contains(bugAFile)){
                for(String bugBFile:bugBFileList){
                    if(from.contains(bugBFile)) return true;
                }
            }
        }
        return false;
    }

    /**
     * 截取commit文件的全路径，返回其文件名
     */
    private String getCommitFileName(String fileName) {
        int i = fileName.lastIndexOf('/');
        int j = fileName.lastIndexOf('.');
        if(i>j){
            System.out.println("文件名存在问题："+fileName);
        }
        return fileName.substring(i + 1, j);
    }

    /**
     * 截取代码文件的全路径，返回其文件名
     * (此处有个问题：output中的文件路径划分是\，\在读取的时候会被转义)
     * (此处的处理不完善，还有待改进)
     */
    private String getCodeFileName(String fileName) {
        int i = Const.CodeRepositoryDir.length();
        /*char[] array = fileName.toCharArray();
        for (; i < array.length; i++) {
            if (array[i] >= 'A' && array[i] <= 'Z') {
                break;
            }
        }*/
        int j = fileName.lastIndexOf('.');
        if(i>j){
            System.out.println("文件名存在问题："+fileName);
        }
        return fileName.substring(i, j);
    }
}
