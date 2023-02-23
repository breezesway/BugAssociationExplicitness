package com.cgz.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {

    /**
     * 获取单层的文件夹下的所有文件绝对路径
     * @param directory
     * @return
     */
    public static ArrayList<String> getSingleLayerFilePath(File directory){
        ArrayList<String> list = new ArrayList<>();
        for (File file:directory.listFiles()) {
            list.add(file.getAbsolutePath());
        }
        return list;
    }

    /**
     * 判断该文件夹是否存在，不存在就创建
     * @param dir
     */
    public static void judgeAndCreateDir(String dir){
        File folder = new File(dir);
        if (!folder.exists() && !folder.isDirectory()) {
            if(!folder.mkdirs()){
                System.out.println(dir+" 该文件夹不存在且创建失败...");
            }
        }
    }

    /**
     * 获取该commit文件对应的所有Key
     * @param commitFilePath
     * @return
     */
    public static List<String> getKeyFromCommitFilePath(String commitFilePath){
        int i = commitFilePath.lastIndexOf("\\");
        String substring = commitFilePath.substring(i + 1);
        int j = substring.indexOf("_");
        String name = substring.substring(0, j);
        List<String> keyList = KeyName.getKeyListFromName(name);
        if(keyList == null){
            System.out.println(name+"找不到对应key");
        }
        return keyList;
    }
}
