package com.cgz.calculate.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Transition {

    private String author;

    private String created;

    private String issueKey;

    private String fromString;

    private String toString;
}
