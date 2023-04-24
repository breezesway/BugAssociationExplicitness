package com.cgz.calculate.dao;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.cgz.calculate.model.BugPair;
import jxl.Workbook;
import jxl.write.Number;
import jxl.write.*;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
            pst.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bugPairs;
    }

    /**
     * 将该项目的BugPair存为一个excel文件
     */
    public void saveAllProjectBugPairMapAsExcel(List<BugPair> bugPairList, String excelFilePath) {
        try {
            WritableWorkbook workbook = Workbook.createWorkbook(new File(excelFilePath));
            WritableSheet sheet = workbook.createSheet("metric", 0);
            setHeaders(sheet);
            int row = 1;
            for (BugPair bugPair : bugPairList) {
                setData(sheet, bugPair, row++);
            }
            workbook.write();
            workbook.close();
        } catch (IOException | WriteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置该sheet的表头
     */
    private void setHeaders(WritableSheet sheet){
        String[] headers = new String[]{"bugAName","bugBName","associationType","sameCommit","bugAFileNum","bugBFileNum"
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
     */
    private void setData(WritableSheet sheet,BugPair bugPair,int row){
        try {
            sheet.addCell(new Label(0,row,bugPair.getBugAName()));
            sheet.addCell(new Label(1,row,bugPair.getBugBName()));
            sheet.addCell(new Label(2,row,bugPair.getAssociationType()));
            sheet.addCell(new Label(3,row,bugPair.isSameCommit()?"true":"false"));
            sheet.addCell(new Number(4,row,bugPair.getBugAFileNum()));
            sheet.addCell(new Number(5,row,bugPair.getBugBFileNum()));
            sheet.addCell(new Number(6,row,bugPair.getInterFileNum()));
            sheet.addCell(new Number(7,row,bugPair.getUnionFileNum()));
            sheet.addCell(new Number(8,row,bugPair.getReferences().size()));
            sheet.addCell(new Number(9,row,bugPair.getHAE()));
            sheet.addCell(new Number(10,row,bugPair.getCAE()));
            sheet.addCell(new Number(11,row,bugPair.getAE()));
        } catch (WriteException e) {
            e.printStackTrace();
        }
    }
}
