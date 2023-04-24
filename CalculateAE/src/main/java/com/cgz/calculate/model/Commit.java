package com.cgz.calculate.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.Objects;

@Data
public class Commit {
    private String Revision;
    private String Author;
    private String Date;
    private String Message;
    private ArrayList<FileChange> filesChange;

    public Commit(String revision, String author, String date, String message) {
        Revision = revision;
        Author = author;
        Date = date;
        Message = message;
        filesChange = new ArrayList<>(5);
    }

    @Data
    public static class FileChange {
        private String operate;
        private String fileName;
        private String oldFileName;

        public FileChange(String operate, String fileName) {
            this.operate = operate;
            this.fileName = fileName;
        }

        public FileChange(String operate, String fileName, String oldFileName) {
            this.operate = operate;
            this.fileName = fileName;
            this.oldFileName = oldFileName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FileChange that = (FileChange) o;
            return Objects.equals(fileName, that.fileName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fileName);
        }
    }
}
