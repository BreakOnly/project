package com.jrmf.controller.constant;

public class QueryType {

    public static final String NULL = "NULL";
    /*查询所有节点*/
    public static final String QUERY_ALL = "O";
    /*查询当前节点下的所有子节点*/
    public static final String QUERY_CHILD_NODE = "C";
    /*查询当前节点下的一级子节点*/
    public static final String QUERY_CHILD_NODE_LEVEL_ONE = "C1";
    /*仅查询当前节点*/
    public static final String QUERY_CURRENT = "I";
    /*查询当前节点及其所有子节点*/
    public static final String QUERY_CURRENT_AND_CHILDREN = "G";
    /*查询当前节点及其一级子节点*/
    public static final String QUERY_CURRENT_AND_CHILDREN_LEVEL_ONE = "G1";
    /*查询代理商树，仅加载代理商数据*/
    public static final String QUERY_PROXY_TREE = "P";
    /*商户*/
    public static final int CUSTOM = 1;
    /*服务公司*/
    public static final int COMPANY = 2;
    /*集团型商户*/
    public static final int CUSTOM_GROUP = 5;
    /*代理商*/
    public static final int CUSTOM_PROXY = 3;
    /*关联性代理商*/
    public static final int CUSTOM_PROXY_CHILDEN = 6;

    /*平台机构*/
    public static final int CUSTOM_PLATFORM_CHILDEN = 7;
}
