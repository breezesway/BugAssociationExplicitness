package com.cgz.calculate;

import com.cgz.calculate.controller.BugPairController;

/**
 * 运行该程序前请检查Const中的路径常量
 */
public class CalculateAEMain {
    public static void main(String[] args) {
        new BugPairController().getAllProjectBugPairs();
    }
}
