package com.cgz.util;

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
     */
    public static String getNameFromKey(String key){
        return keyToName.get(key);
    }

    private static void initNameToKey(){
        nameToKey = new HashMap<>();
        nameToKey.put("hadoop", List.of("HADOOP","MAPREDUCE","YARN","HDFS"));
        nameToKey.put("hbase", List.of("HADOOP","HBASE"));
        nameToKey.put("hive", List.of("HADOOP","HIVE"));
        nameToKey.put("kafka", List.of("KAFKA"));
        nameToKey.put("impala", List.of("IMPALA"));
        nameToKey.put("ambari", List.of("AMBARI"));
        nameToKey.put("daffodil", List.of("DAFFODIL"));
        nameToKey.put("jackrabbit-oak", List.of("OAK"));
        nameToKey.put("jackrabbitoak", List.of("OAK"));
        nameToKey.put("subversion", List.of("SVN"));
        nameToKey.put("wicket", List.of("WICKET"));
        nameToKey.put("arrow", List.of("ARROW"));
        nameToKey.put("cordova", List.of("CB"));
        nameToKey.put("activemq", List.of("AMQ"));
        nameToKey.put("guacamole", List.of("GUACAMOLE"));
        nameToKey.put("trafficserver", List.of("TS"));
        nameToKey.put("cloudstack", List.of("CLOUDSTACK"));
        nameToKey.put("mesos", List.of("MESOS"));
        nameToKey.put("drill", List.of("DRILL"));
        nameToKey.put("lucene", List.of("LUCENE","SOLR"));
        nameToKey.put("solr", List.of("LUCENE","SOLR"));
        nameToKey.put("hudi", List.of("HUDI"));
        nameToKey.put("ignite", List.of("IGNITE"));
        nameToKey.put("nifi", List.of("NIFI"));
        nameToKey.put("ofbiz", List.of("OFBIZ"));
        nameToKey.put("spark", List.of("SPARK"));
        nameToKey.put("axis2", List.of("AXIS2"));
        nameToKey.put("groovy", List.of("GROOVY"));
        nameToKey.put("thrift", List.of("THRIFT"));
        nameToKey.put("geode", List.of("GEODE"));
        nameToKey.put("netbeans", List.of("NETBEANS"));
        nameToKey.put("ozone", List.of("HDDS"));
        nameToKey.put("camel", List.of("CAMEL"));
        nameToKey.put("qpid", List.of("QPID"));
        nameToKey.put("flink", List.of("FLINK"));
        nameToKey.put("accumulo", List.of("ACCUMULO"));
        nameToKey.put("calcite", List.of("CALCITE"));
        nameToKey.put("maven", List.of("MNG"));
        nameToKey.put("pdfbox", List.of("PDFBOX"));
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
