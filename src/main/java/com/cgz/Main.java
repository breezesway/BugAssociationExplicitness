package com.cgz;


import com.cgz.controller.BugPairController;

/**
 * 运行该程序前请检查Const中的路径常量
 */
public class Main {
    public static void main(String[] args) {
        new BugPairController().getAllProjectBugPairs();
    }
}
