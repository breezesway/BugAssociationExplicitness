package com.cgz.dao;

import com.cgz.model.Reference;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.*;
import java.util.ArrayList;
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
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "gbk"));
            while ((reader.readLine()) != null) {
            }
            process.waitFor();
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
            runtime.exec("und create -db " + udbProjectPath + " -languages Java").waitFor();
            runtime.exec("und -db " + udbProjectPath + " add -exclude \"*.git\" " + sourceCodePath).waitFor();
            Process process = runtime.exec("und analyze " + udbProjectPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "gbk"));
            while ((reader.readLine()) != null) {
            }
            process.waitFor();
            runtime.exec("und export -dependencies file csv " + outputPath + " " + udbProjectPath).waitFor();
            runtime.exec("und export -dependencies file matrix " + matrixPath + " " + udbProjectPath).waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取output文件的所有reference
     *
     * @param outputPath
     * @return
     */
    public List<Reference> findReferenceList(String outputPath) {
        List<Reference> refList = new ArrayList<>();
        try {
            CSVReader csvReader = new CSVReader(new FileReader(outputPath));
            String[] line;
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
