package com.cgz.controller;

import com.cgz.model.BugPair;
import com.cgz.service.BugPairService;
import com.cgz.util.Const;
import com.cgz.util.FileUtil;
import com.cgz.util.KeyName;

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
        for(String commitFilePath:commitFilePaths){
            String name = FileUtil.getNameFromCommitFilePath(commitFilePath);
            List<String> keys = KeyName.getKeyListFromName(name);
            String bugPairMetricExcelPath = Const.excelFileDir+"\\"+ name +"BugPairMetrics.xls";
            if(!FileUtil.judgeFile(bugPairMetricExcelPath)){
                List<BugPair> bugPairList = bugPairService.getBugPairListByKeys(commitFilePath,keys);
                bugPairService.saveProjectBugPairMapAsExcel(bugPairList,bugPairMetricExcelPath);
            }
        }

    }
}
