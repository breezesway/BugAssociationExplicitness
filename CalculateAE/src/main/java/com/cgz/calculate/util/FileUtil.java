package com.cgz.calculate.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class FileUtil {

    /**
     * 获取单层的文件夹下的所有文件绝对路径
     */
    public static ArrayList<String> getSingleLayerFilePath(File directory){
        ArrayList<String> list = new ArrayList<>();
        for (File file: Objects.requireNonNull(directory.listFiles())) {
            list.add(file.getAbsolutePath());
        }
        return list;
    }

    /**
     * 判断该文件夹是否存在，不存在就创建
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
     * 判断该文件是否存在
     */
    public static boolean judgeFile(String file){
        return new File(file).exists();
    }

    /**
     * 获取该commit文件对应的name
     */
    public static String getNameFromCommitFilePath(String commitFilePath){
        int i = commitFilePath.lastIndexOf("\\");
        String substring = commitFilePath.substring(i + 1);
        int j = substring.indexOf("_");
        return substring.substring(0, j);
    }
}
