package com.cgz.model;

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
}
