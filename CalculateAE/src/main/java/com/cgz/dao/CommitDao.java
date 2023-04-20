package com.cgz.dao;

import com.cgz.model.Commit;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommitDao {

    /**
     * 读取commit文件，根据时间顺序返回一个commit的List
     *
     * @param commitFilePath commit文件路径
     * @return 一个commit的List
     */
    public List<Commit> getCommitListFromText(String commitFilePath, List<String> keys) throws IOException {
        List<Commit> list = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(commitFilePath));
        String line = reader.readLine();
        String revision;
        String author;
        String date;
        String message;
        while (line != null) {
            if (line.startsWith("Revision")) {
                int pre = line.indexOf(":");
                revision = line.substring(pre + 1).trim();
                line = reader.readLine();
                pre = line.indexOf(":");
                author = line.substring(pre + 1).trim();
                line = reader.readLine();
                pre = line.indexOf(":");
                date = line.substring(pre + 1).trim();
                reader.readLine();
                line = reader.readLine();
                message = line;
                Commit commit = new Commit(revision, author, date, message);
                list.add(commit);
                ArrayList<String> issues = getIssuesFromMessage(message, keys);
                if (issues.size() != 0) {   //Message中是否有IssueKey
                    while ((line = reader.readLine()) != null) {   //本次Commit是否有变更的文件
                        if (line.startsWith("Revision")) {
                            break;
                        } else if (line.startsWith("Modified:")
                                || line.startsWith("Added:")
                                || line.startsWith("Deleted:")
                                || line.startsWith("Renamed:")) {
                            Commit.FileChange fileChange;
                            if ((fileChange = getJavaFile(line)) != null) {
                                commit.getFilesChange().add(fileChange);
                            }
                        }
                    }
                }
            } else {
                line = reader.readLine();
            }
        }
        return list;
    }

    /**
     * 从一个commit的message中提取出所有的Key
     * @param keys    前缀，一个或多个
     * @return 一个list
     */
    public ArrayList<String> getIssuesFromMessage(String message, List<String> keys) {
        ArrayList<String> list = new ArrayList<>(1);
        for (String keyPrefix : keys) {
            Pattern pattern = Pattern.compile(keyPrefix + "-\\d+");
            Matcher matcher = pattern.matcher(message);
            while (matcher.find()) {
                list.add(matcher.group());
            }
        }
        return list;
    }

    /**
     * 从本行中提取出java文件的全名和其操作
     *
     * @param line 一行字符串
     * @return FileChange对象，如果不是java则文件返回Null
     */
    private Commit.FileChange getJavaFile(String line) {
        String operate;
        String fileName;
        int pre = line.indexOf(":");
        operate = line.substring(0, pre).trim();
        int suf = line.lastIndexOf("|");
        fileName = line.substring(pre + 1, suf).trim();
        if ("Renamed".equals(operate)) {
            int kuo = fileName.indexOf("[");
            int huikuo = fileName.indexOf("]");
            String oldFileName = fileName.substring(kuo + 6, huikuo);
            fileName = fileName.substring(0, kuo - 1);
            int i = fileName.lastIndexOf(".");
            if (i == -1) return null;
            if (".java".equals(fileName.substring(i))) {
                return new Commit.FileChange(operate, fileName, oldFileName);
            } else return null;
        } else {
            int i = fileName.lastIndexOf(".");
            if (i == -1) return null;
            if (".java".equals(fileName.substring(i))) return new Commit.FileChange(operate, fileName);
            else return null;
        }
    }
}
