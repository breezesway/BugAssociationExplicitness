package com.cgz.controller;

import com.cgz.model.BugPair;
import com.cgz.service.BugPairService;
import com.cgz.util.Const;
import com.cgz.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class BugPairController {

    BugPairService bugPairService = new BugPairService();

    /**
     * 生成一个所有项目的BugPair（包含所有属性）的excel文件
     */
    public void getAllProjectBugPairs(){
        ArrayList<String> commitFilePaths = FileUtil.getSingleLayerFilePath(new File(Const.commitFileDir));
        LinkedHashMap<String, List<BugPair>> allProjectBugPairMap = new LinkedHashMap<>();
        for(String commitFilePath:commitFilePaths){
            List<String> keys = FileUtil.getKeyFromCommitFilePath(commitFilePath);
            List<BugPair> bugPairList = bugPairService.getBugPairListByKeys(commitFilePath,keys);
            if(allProjectBugPairMap.containsKey(keys.toString())){
                allProjectBugPairMap.get(keys.toString()).addAll(bugPairList);
            }else{
                allProjectBugPairMap.put(keys.toString(),bugPairList);
            }
        }
        bugPairService.saveAllProjectBugPairMapAsExcel(allProjectBugPairMap,Const.excelFilePath);
    }
}
