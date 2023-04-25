package com.cgz.calculate.model;

import lombok.Data;

import java.util.List;

@Data
public class BugPair {
    private String bugAName;
    private List<Commit> bugACommit;
    private String bugBName;
    private List<Commit> bugBCommit;

    private String associationType;
    private boolean sameCommit;

    private List<Commit.FileChange> bugAFiles;
    private int bugAFileNum;
    private List<Commit.FileChange> bugBFiles;
    private int bugBFileNum;

    private double interFileNum;
    private double unionFileNum;

    private List<Reference> references;

    private double HAE;
    private double CAE;
    private double AE;

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
    private long bugAOpenDuration;
    private long bugBOpenDuration;
    private long openDuration;
}
