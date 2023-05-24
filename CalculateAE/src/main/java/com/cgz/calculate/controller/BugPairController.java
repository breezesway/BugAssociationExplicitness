package com.cgz.calculate.controller;

import com.cgz.calculate.service.BugPairService;
import com.cgz.calculate.model.BugPair;
import com.cgz.calculate.util.Const;
import com.cgz.calculate.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BugPairController {

    BugPairService bugPairService = new BugPairService();

    /**
     * 获取所有项目的BugPair的Excel文件
     */
    public void getAllProjectBugPairs(){
        ArrayList<String> commitFilePaths = FileUtil.getSingleLayerFilePath(new File(Const.commitFileDir));
        System.out.println("获取到所有commit文件");
        for(String commitFilePath:commitFilePaths){
            String name = FileUtil.getNameFromCommitFilePath(commitFilePath);
            System.out.println("-------解析--- "+name+" ------");
            String bugPairMetricExcelPath = Const.excelFileDir+"\\"+ name +"BugPairMetrics.xls";
            if(!FileUtil.judgeFile(bugPairMetricExcelPath)){
                List<BugPair> bugPairList = bugPairService.getBugPairListByKeys(commitFilePath,name);
                bugPairService.saveProjectBugPairMapAsExcel(bugPairList,bugPairMetricExcelPath);
            }
        }

    }
}
