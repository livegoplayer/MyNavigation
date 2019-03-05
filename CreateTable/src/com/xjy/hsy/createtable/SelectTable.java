package com.xjy.hsy.createtable;

import com.alibaba.druid.pool.DruidDataSource;
import com.xjy.hsy.createtable.JavaBeans.crossroad.CrossRoad;
import com.xjy.hsy.createtable.JavaBeans.road.Road;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class SelectTable {
    public static ArrayList<CrossRoad> crossRoads = new ArrayList<>();                  //保存路口的列表
    public static ArrayList<Road> roads = new ArrayList<>();                            //保存所有道路的列表
    private static String SELECT = "SELECT ? FROM ? WHERE ?";

    private static DruidDataSource druidDataSource = new DruidDataSource(true);

    private static void selectCrossroadTable(){
        String SQL = "SELECT * FROM CROSSROAD";
        try{
            Connection conn = CreateTable.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(SQL);
            ResultSet rs = preparedStatement.executeQuery();

        }catch(Exception e){
            e.printStackTrace();
        }

    }
}
