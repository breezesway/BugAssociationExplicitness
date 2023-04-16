package com.cgz.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * 该类用于维护项目名到key的映射，以及Key到项目名的映射
 */
public class KeyName {

    private static HashMap<String, List<String>> nameToKey;
    private static HashMap<String,String> keyToName;

    static {
        initNameToKey();
        initKeyToName();
    }

    /**
     * 根据给定的name返回一组key
     * @param name
     * @return
     */
    public static List<String> getKeyListFromName(String name){
        List<String> keyList = nameToKey.get(name);
        if(keyList == null){
            System.out.println(name+"找不到对应key");
        }
        return keyList;
    }

    /**
     * 根据给定的key返回其项目名
     * @param key
     * @return
     */
    public static String getNameFromKey(String key){
        return keyToName.get(key);
    }

    private static void initNameToKey(){
        nameToKey = new HashMap<>();
        nameToKey.put("hadoop", Arrays.asList("HADOOP","MAPREDUCE","YARN","HDFS"));
        nameToKey.put("hbase", Arrays.asList("HADOOP","HBASE"));
        nameToKey.put("hive", Arrays.asList("HADOOP","HIVE"));
        nameToKey.put("kafka", Arrays.asList("KAFKA"));
        nameToKey.put("impala", Arrays.asList("IMPALA"));
        nameToKey.put("ambari", Arrays.asList("AMBARI"));
        nameToKey.put("daffodil", Arrays.asList("DAFFODIL"));
        nameToKey.put("jackrabbit-oak", Arrays.asList("OAK"));
        nameToKey.put("jackrabbitoak", Arrays.asList("OAK"));
        nameToKey.put("subversion", Arrays.asList("SVN"));
        nameToKey.put("wicket", Arrays.asList("WICKET"));
        nameToKey.put("arrow", Arrays.asList("ARROW"));
        nameToKey.put("cordova", Arrays.asList("CB"));
        nameToKey.put("activemq", Arrays.asList("AMQ"));
        nameToKey.put("guacamole", Arrays.asList("GUACAMOLE"));
        nameToKey.put("trafficserver", Arrays.asList("TS"));
        nameToKey.put("cloudstack", Arrays.asList("CLOUDSTACK"));
        nameToKey.put("mesos", Arrays.asList("MESOS"));
        nameToKey.put("drill", Arrays.asList("DRILL"));
        nameToKey.put("lucene", Arrays.asList("LUCENE","SOLR"));
        nameToKey.put("solr", Arrays.asList("LUCENE","SOLR"));
        nameToKey.put("hudi", Arrays.asList("HUDI"));
        nameToKey.put("ignite", Arrays.asList("IGNITE"));
        nameToKey.put("nifi", Arrays.asList("NIFI"));
        nameToKey.put("ofbiz", Arrays.asList("OFBIZ"));
        nameToKey.put("spark", Arrays.asList("SPARK"));
        nameToKey.put("axis2", Arrays.asList("AXIS2"));
        nameToKey.put("groovy", Arrays.asList("GROOVY"));
        nameToKey.put("thrift", Arrays.asList("THRIFT"));
        nameToKey.put("geode", Arrays.asList("GEODE"));
        nameToKey.put("netbeans", Arrays.asList("NETBEANS"));
        nameToKey.put("ozone", Arrays.asList("HDDS"));
        nameToKey.put("camel", Arrays.asList("CAMEL"));
        nameToKey.put("qpid", Arrays.asList("QPID"));
        nameToKey.put("flink", Arrays.asList("FLINK"));
        nameToKey.put("accumulo", Arrays.asList("ACCUMULO"));
        nameToKey.put("calcite", Arrays.asList("CALCITE"));
        nameToKey.put("maven", Arrays.asList("MNG"));
        nameToKey.put("pdfbox", Arrays.asList("PDFBOX"));
    }

    private static void initKeyToName(){
        keyToName = new HashMap<>();
        keyToName.put("HADOOP","Hadoop");
        keyToName.put("MAPREDUCE","Hadoop");
        keyToName.put("YARN","Hadoop");
        keyToName.put("HDFS","Hadoop");
        keyToName.put("HBASE","HBase");
        keyToName.put("HIVE","Hive");
        keyToName.put("KAFKA","Kafka");
        keyToName.put("IMPALA","Impala");
        keyToName.put("AMBARI","Ambari");
        keyToName.put("DAFFODIL","Daffodil");
        keyToName.put("OAK","Jackrabbit-Oak");
        keyToName.put("SVN","Subversion");
        keyToName.put("WICKET","Wicket");
        keyToName.put("ARROW","Arrow");
        keyToName.put("CB","Cordova");
        keyToName.put("AMQ","ActiveMQ");
        keyToName.put("GUACAMOLE","Guacamole");
        keyToName.put("TS","Traffic Server");
        keyToName.put("CLOUDSTACK","CloudStack");
        keyToName.put("MESOS","Mesos");
        keyToName.put("DRILL","Drill");
        keyToName.put("LUCENE","Lucene");
        keyToName.put("SOLR","Solr");
        keyToName.put("HUDI","Hudi");
        keyToName.put("IGNITE","Ignite");
        keyToName.put("NIFI","NiFi");
        keyToName.put("OFBIZ","OFBiz");
        keyToName.put("SPARK","Spark");
        keyToName.put("AXIS2","Axis2");
        keyToName.put("GROOVY","Groovy");
        keyToName.put("THRIFT","Thrift");
        keyToName.put("GEODE","Geode");
        keyToName.put("NETBEANS","Netbeans");
        keyToName.put("HDDS","Ozone");
        keyToName.put("CAMEL","Camel");
        keyToName.put("FLINK","Flink");
        keyToName.put("QPID","Qpid");
        keyToName.put("ACCUMULO","Accumulo");
        keyToName.put("CALCITE","Calcite");
        keyToName.put("MNG","Maven");
        keyToName.put("PDFBOX","PDFBox");
    }
}
