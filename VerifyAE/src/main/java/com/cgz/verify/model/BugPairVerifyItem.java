package com.cgz.verify.model;

import com.cgz.calculate.model.BugPair;
import lombok.Data;

@Data
public class BugPairVerifyItem extends BugPair {

    /**
     * 该bug是否reopen过
     */
    private boolean bugAReopen;
    private boolean bugBReopen;
    private boolean reopen;

    /**
     * 修复该bug的提交中是否引入了其他bug
     */
    private boolean bugALeadBug;
    private boolean bugBLeadBug;
    private boolean leadBug;

    /**
     * 该bug的open时长, 单位min
     */
    private Integer bugAOpenDuration;
    private Integer bugBOpenDuration;
    private Integer openDuration;

}
