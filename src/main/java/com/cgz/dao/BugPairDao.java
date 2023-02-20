package com.cgz.dao;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.cgz.model.BugPair;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BugPairDao {
    /**
     * 根据前缀返回该前缀的所有IssueLink
     * @param keyPrefix 前缀
     * @return 所有IssueLink的list
     */
    public List<BugPair> findBugPairListByKeyPrefix(String keyPrefix){
        List<BugPair> bugPairs = new ArrayList<>();
        try {
            DruidPooledConnection conn = Database.getConnection();
            String sql = "select id,linktype,inwardissuekey,outwardissuekey from issuelink where inwardissuekey like '"+keyPrefix+"%'";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            while ((rs.next())){
                BugPair bugPair = new BugPair();
                bugPair.setBugAName(rs.getString("inwardissuekey"));
                bugPair.setBugBName(rs.getString("outwardissuekey"));
                bugPair.setAssociationType(rs.getString("linktype"));
                bugPairs.add(bugPair);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return bugPairs;
    }

    /**
     * 将所有项目的BugPair存为一个excel文件，每个sheet对应一个key(或多个key)
     * @param allProjectBugPairMap
     * @param excelFilePath
     */
    public void saveAllProjectBugPairMapAsExcel(LinkedHashMap<String, List<BugPair>> allProjectBugPairMap, String excelFilePath) {
        try {
            WritableWorkbook workbook = Workbook.createWorkbook(new File(excelFilePath));
            AtomicInteger i = new AtomicInteger(0);
            allProjectBugPairMap.forEach((k,v)->{
                WritableSheet sheet = workbook.createSheet(k, i.getAndIncrement());
                setHeaders(sheet);
                int row = 1;
                for(BugPair bugPair:v){
                    setData(sheet,bugPair,row++);
                }
            });
            workbook.write();
            workbook.close();
        } catch (IOException | WriteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置该sheet的表头
     * @param sheet
     */
    private void setHeaders(WritableSheet sheet){
        String[] headers = new String[]{"bugAName","bugBName","sameCommit","bugAFileNum","bugBFileNum"
                ,"interFileNum","unionFileNum","RefNum","HAE","CAE","AE"};
        for(int j = 0;j<headers.length;j++){
            try {
                sheet.addCell(new Label(j,0,headers[j]));
            } catch (WriteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 设置一行数据
     * @param sheet
     */
    private void setData(WritableSheet sheet,BugPair bugPair,int row){
        try {
            sheet.addCell(new Label(0,row,bugPair.getBugAName()));
            sheet.addCell(new Label(1,row,bugPair.getBugBName()));
            sheet.addCell(new Label(2,row,bugPair.isSameCommit()?"true":"false"));
            sheet.addCell(new Number(3,row,bugPair.getBugAFileNum()));
            sheet.addCell(new Number(4,row,bugPair.getBugBFileNum()));
            sheet.addCell(new Number(5,row,bugPair.getInterFileNum()));
            sheet.addCell(new Number(6,row,bugPair.getUnionFileNum()));
            sheet.addCell(new Number(7,row,bugPair.getReferences().size()));
            sheet.addCell(new Number(8,row,bugPair.getHAE()));
            sheet.addCell(new Number(9,row,bugPair.getCAE()));
            sheet.addCell(new Number(10,row,bugPair.getAE()));
        } catch (WriteException e) {
            e.printStackTrace();
        }
    }
}