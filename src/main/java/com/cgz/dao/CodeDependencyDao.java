package com.cgz.dao;

import com.cgz.model.Reference;
import com.cgz.util.FileUtil;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.text.StringEscapeUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CodeDependencyDao {

    /**
     * 将该项目的代码库切换到指定版本
     *
     * @param sourceCodePath
     * @param revision
     */
    public void CheckoutCommit(String sourceCodePath, String revision) {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process process = runtime.exec("git checkout -f " + revision, null, new File(sourceCodePath));
            handleInput(process,"git checkout -f "+revision);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 使用Understand分析代码依赖
     *
     * @param sourceCodePath
     * @param udbProjectPath
     * @param outputPath
     * @param matrixPath
     */
    public void analyzeDependency(String sourceCodePath, String udbProjectPath, String outputPath, String matrixPath) {
        Runtime runtime = Runtime.getRuntime();
        try {
            FileUtil.judgeAndCreateDir(udbProjectPath.substring(0,udbProjectPath.lastIndexOf('\\')));
            FileUtil.judgeAndCreateDir(outputPath.substring(0,outputPath.lastIndexOf('\\')));
            FileUtil.judgeAndCreateDir(matrixPath.substring(0,matrixPath.lastIndexOf('\\')));
            Process process1 = runtime.exec("und create -db " + udbProjectPath + " -languages Java");
            handleInput(process1,"und create -db " + udbProjectPath);
            Process process2 = runtime.exec("und -db " + udbProjectPath + " add -exclude \"*.git\" " + sourceCodePath);
            handleInput(process2,"und -db " + udbProjectPath);
            Process process3 = runtime.exec("und analyze " + udbProjectPath);
            handleInput(process3,"und analyze "+udbProjectPath);
            runtime.exec("und export -dependencies file csv " + outputPath + " " + udbProjectPath).waitFor();
            runtime.exec("und export -dependencies file matrix " + matrixPath + " " + udbProjectPath).waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理process的错误输入流
     * @param process
     * @param command
     * @throws IOException
     * @throws InterruptedException
     */
    private void handleInput(Process process,String command) throws IOException, InterruptedException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "gbk"));
        while ((reader.readLine()) != null) {
        }
        BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), "gbk"));
        String err;
        while ((err = errReader.readLine()) != null) {
            System.out.println(err);
        }
        int state = process.waitFor();
        if(state!=0){
            System.out.println("\033[31m"+"执行 "+command+" 出错了...\n");
        }
        reader.close();
        errReader.close();
    }

    /**
     * 读取output文件的所有reference
     * @param outputPath
     * @return
     */
    public List<Reference> findReferenceList(String outputPath) {
        List<Reference> refList = new ArrayList<>();
        try {

            CSVReader csvReader = new CSVReader(new FileReader(outputPath));
            String[] line = csvReader.readNext();
            while ((line = csvReader.readNext()) != null) {
                Reference reference = new Reference(line[0], line[1], Integer.parseInt(line[2]), Integer.parseInt(line[3]), Integer.parseInt(line[4]));
                refList.add(reference);
            }
        } catch (CsvValidationException | IOException e) {
            e.printStackTrace();
        }
        return refList;
    }
}
