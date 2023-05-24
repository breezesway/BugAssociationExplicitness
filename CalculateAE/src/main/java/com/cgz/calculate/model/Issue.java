package com.cgz.calculate.model;

import lombok.Data;

@Data
public class Issue {
    private String key;
    private String issueType;
    private String status;
    private String created;
    private String resolutiondate;
}
