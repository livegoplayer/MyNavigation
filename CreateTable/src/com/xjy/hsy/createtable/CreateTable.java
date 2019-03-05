package com.xjy.hsy.createtable;

import com.alibaba.druid.pool.DruidDataSource;
import com.xjy.hsy.createtable.JavaBeans.LogicRoad;
import com.xjy.hsy.createtable.JavaBeans.Position;
import com.xjy.hsy.createtable.JavaBeans.Topography.Topography;
import com.xjy.hsy.createtable.JavaBeans.crossroad.Branch;
import com.xjy.hsy.createtable.JavaBeans.crossroad.CrossRoad;
import com.xjy.hsy.createtable.JavaBeans.hotspot.HotSpot;
import com.xjy.hsy.createtable.JavaBeans.road.Road;
import com.xjy.hsy.createtable.Parsers.DistanceParser;
import com.xjy.hsy.createtable.Parsers.databaseparser.DatabaseParser;
import javafx.geometry.Pos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class CreateTable {
    private static String CREATE_TABLE = "CREATE TABLE ";
    private static String INSERT = "INSERT INTO";

    private static DruidDataSource druidDataSource = new DruidDataSource(true);

    private static void init(){
        druidDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        druidDataSource.setUrl("jdbc:mysql://localhost:8868/NAVIGATION?useSSL=false&useUnicode=true&characterEncoding=UTF-8");
        druidDataSource.setUsername("root");
        druidDataSource.setPassword("root");
        druidDataSource.setInitialSize(8);
        druidDataSource.setMinIdle(2);
    }

    public static Connection getConnection(){     //获得连接
        init();
        try{
            return druidDataSource.getConnection();
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static boolean creatTable(){          //创建各种表格
        try{

            //创建表格
            //ROAD
            String body = "(" +
                    "道路名称 " + "varchar(10)" + "," +
                    "ID " + "varchar(20) "  + "PRIMARY KEY"  + "," +
                    "道路长度 " + "int" + "," +
                    "level " + "varchar(20)" + "," +
                    "start_x " + "double" + "," +
                    "start_y " + "double" + "," +
                    "end_x " + "double" + "," +
                    "end_y " + "double" + "," +
                    "道路段数 " + "int" +
                    ")";
            Statement statement = getConnection().createStatement();
            statement.execute(CREATE_TABLE + "ROAD" +body);

            //ROAD_SECTION
            body = "(" +
                    "道路ID " + "varchar(10)" + "," +
                    "路段ID " + "varchar(20)" + "PRIMARY KEY" + "," +
                    "路段长度 " + "int" + "," +
                    "level " + "varchar(20)" + "," +
                    "start_x " + "double" + "," +
                    "start_y " + "double" + "," +
                    "end_x " + "double" + "," +
                    "end_y " + "double" + "," +
                    "速度 " + "int"  +
                    ")";
            statement = getConnection().createStatement();
            statement.execute(CREATE_TABLE + "ROAD_SECTION" +body);

            //ROAD_POLINES
            body = "(" +
                    "路段ID " + "varchar(20) " + "," +
                    "x " + "double" + "," +
                    "y " + "double"  +
                    ")";
            statement = getConnection().createStatement();
            statement.execute(CREATE_TABLE + "ROAD_POLINES" +body);

            //CROSSROAD
            body = "(" +
                    "ID " + "varchar(20)" + "PRIMARY KEY" + "," +
                    "x " + "double" + "," +
                    "y " + "double" + "," +
                    "level " + "varchar(10)" +
                    ")";
            statement = getConnection().createStatement();
            statement.execute(CREATE_TABLE + "CROSSROAD" +body);

            //CROSSROAD_BRANCH
            body = "(" +
                    "CROSSROAD_ID " + "varchar(20)" + "," +
                    "BRANCH_ID " + "varchar(20)" + "PRIMARY KEY" + "," +
                    "LEFT_TIME " + "char(20)" + "," +
                    "RIGHT_TIME " + "char(20)" + "," +
                    "STRAIGHT_TIME " + "char(20)"	+ "," +
                    "LEFT_RED " + "int"+  "," +
                    "LEFT_YELLOW " + "int" +  "," +
                    "LEFT_GREEN " +"int" + "," +
                    "RIGHT_RED " + "int" + "," +
                    "RIGHT_YELLOW " +  "int" +  "," +
                    "RIGHT_GREEN " + "int" + "," +
                    "STRAIGHT_RED " + "int" + "," +
                    "STRAIGHT_YELLOW " + "int" +	"," +
                    "STRAIGHT_GREEN " + "int" +
                    ")";
            statement = getConnection().createStatement();
            statement.execute(CREATE_TABLE + "CROSSROAD_BRANCH" +body);

            //创建Topography
            body = "(" +
                    "地形ID " + "varchar(20) " + "PRIMARY KEY" +"," +
                    "x " + "double" + "," +
                    "y " + "double"  + "," +
                    "level " + "varchar(10)" +
                    ")";
            statement = getConnection().createStatement();
            statement.execute(CREATE_TABLE + "Topography" +body);

            //创建TopographyPolines
            body = "(" +
                    "地形ID " + "varchar(20) " + "," +
                    "x " + "double" + "," +
                    "y " + "double"  +
                    ")";
            statement = getConnection().createStatement();
            statement.execute(CREATE_TABLE + "TopographyPolines" +body);
            //creat more table

            /*
            *  HotSpot
            */
            body = "(" +
                    "热点ID " + "varchar(20) " + "PRIMARY KEY" +"," +
                    "x " + "double" + "," +
                    "y " + "double"  + "," +
                    "level " + "varchar(10)" +
                    ")";
            statement = getConnection().createStatement();
            statement.execute(CREATE_TABLE + "HotSpot" +body);


        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean insertCrossRoadTable(int count){                       //插入随机生成的信息到CROSSROAD
        try{
            //生成CROSSROAD数据
            AlterTable.addRandomCrossroads(count);
            ArrayList<Position> crossRoads = AlterTable.crossRoads;;
            for(int i = 0; i < crossRoads.size() ; i++ ){
                String ID  = "CD_" + i;
                crossRoads.get(i).ID = i + "";
                double xPos = crossRoads.get(i).xPos;
                double yPos = crossRoads.get(i).yPos;
                crossRoads.get(i).count = new Random().nextInt(3) + 3;
                String level = "" + crossRoads.get(i).count;            //表示Type 3-5
                insertCrossRoad(ID,xPos,yPos,level);

            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return true;
    }

    public static boolean insertCrossRoad(String ID,double xPos , double yPos,String level){         //插入一条信息到CROSSROAD

        String SQL = "INSERT INTO CROSSROAD(ID,x,y,level) VALUES(?,?,?,?)";
        try{
            Connection conn = getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(SQL);
            preparedStatement.setString(1,ID);
            preparedStatement.setDouble(2,xPos);
            preparedStatement.setDouble(3,yPos);
            preparedStatement.setString(4,level);
            preparedStatement.executeUpdate();
            conn.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return true;
    }

    public static void clear(String tableNanme){                 //清空任意一个数据库
        String SQL = "TRUNCATE " + tableNanme;
        try{
            Connection conn = getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(SQL);
            preparedStatement.execute();
            conn.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void insetRoadTable(){
        AlterTable.addRandomRoads();
        ArrayList<LogicRoad> roadSections = AlterTable.roads;
        for (LogicRoad logicRoad :roadSections) {
            if (Integer.parseInt(logicRoad.startPos.ID) != Integer.parseInt(logicRoad.endPos.ID)){
                String RID = "R_" + logicRoad.startPos.ID;
                String ID = "R" + logicRoad.startPos.ID + "_" + logicRoad.endPos.ID;       //ROADECTION的ID是两个点ID的拼接
                Position startPos = logicRoad.startPos;
                Position endPos = logicRoad.endPos;
                String level = logicRoad.level.toString();
                int length = (int)DistanceParser.getDistanceBetween(startPos,endPos);
                int speed =0;
                switch (logicRoad.level){
                    case EXPY_ROAD:
                        speed = new Random().nextInt(30) + 30;
                        break;
                    case NATIONAL_ROAD:
                        speed = new Random().nextInt(30) + 30;
                        break;
                    case MAIN_ROAD:
                        speed = new Random().nextInt(30) + 30;
                        break;
                    case OTHER_ROADS:
                        speed = new Random().nextInt(30) + 30;
                        break;
                }
                insertRoad(RID,ID,length ,level,startPos, endPos,speed);
             }
        }
    }

    public static boolean insertRoad(String RID ,String ID, int length,String level,Position startPos , Position endPos , int speed) { //添加一条信息到Road
        String SQL = "INSERT INTO ROAD_SECTION(道路ID,路段ID,路段长度,level,start_x,start_y,end_x,end_y,速度) VALUES(?,?,?,?,?,?,?,?,?)";
        try{
            Connection conn = getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(SQL);
            preparedStatement.setString(1,RID);
            preparedStatement.setString(2,ID);
            preparedStatement.setInt(3,length);
            //System.out.println(level);
            preparedStatement.setString(4,level);
            preparedStatement.setDouble(5,startPos.xPos);
            preparedStatement.setDouble(6,startPos.yPos);
            preparedStatement.setDouble(7,endPos.xPos);
            preparedStatement.setDouble(8,endPos.yPos);
            preparedStatement.setFloat(9,speed);
            preparedStatement.executeUpdate();
            conn.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return true;
    }

    public static boolean insertHotSpotTable(int count){
        AlterTable.addRandomHotSpots(count);
        for(HotSpot hotSpot : AlterTable.hotSpots){
            insertHotSpot(hotSpot);
        }
        return true;
    }

    private static boolean insertHotSpot(HotSpot hotSpot) {
        String SQL = "INSERT INTO HOTSPOT(热点ID,x,y,level) VALUES(?,?,?,?)";
        try{
            Connection conn = getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(SQL);
            preparedStatement.setString(1,hotSpot.ID);
            preparedStatement.setDouble(2,hotSpot.position.xPos);
            preparedStatement.setDouble(3,hotSpot.position.yPos);
            preparedStatement.setString(4,hotSpot.hotType.toString());
            preparedStatement.executeUpdate();
            conn.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return true;
    }

    public static boolean insertPolines(String ID,Position position){
        String SQL = "INSERT INTO ROAD_POLINES(路段ID,x,y) VALUES(?,?,?)";
        try{
            Connection conn = getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(SQL);
            preparedStatement.setString(1,ID);
            preparedStatement.setDouble(2,position.xPos);
            preparedStatement.setDouble(3,position.yPos);
            preparedStatement.executeUpdate();
            conn.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return true;

    }

    public static void insertPolinesTable(){
        for (LogicRoad road : AlterTable.roads) {
            ArrayList<Position> polines = AlterTable.getPolines(road);
            for (Position position : polines) {
                String ID = road.ID;
                insertPolines(ID,position);
            }
        }

    }

    public static void insertTopographyTable(int count){
        AlterTable.addRandomTopography(count);
        ArrayList<Topography> topographies = AlterTable.topographies;
        for(Topography topography: topographies){
            insertTopography(topography);
            String ID = topography.ID;
            insertTopographyPolinesTable(ID,topography.polines);
        }
    }

    public static boolean insertTopography(Topography topography){
        String SQL = "INSERT INTO TOPOGRAPHY (地形ID,x,y,level) VALUES(?,?,?,?)";
        try{
            Connection conn = getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(SQL);
            preparedStatement.setString(1,topography.ID);
            preparedStatement.setDouble(2,topography.centerPosition.xPos);
            preparedStatement.setDouble(3,topography.centerPosition.yPos);
            preparedStatement.setString(4,topography.type.toString());
            preparedStatement.executeUpdate();
            conn.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return true;
    }

    public static void insertTopographyPolinesTable(String ID,ArrayList<Position> polines){
        for (Position position : polines) {
            insertTopographyPolines(ID,position);
        }
    }

    public static boolean insertTopographyPolines(String ID,Position polines){
        String SQL = "INSERT INTO TOPOGRAPHYPOLINES (地形ID,x,y) VALUES(?,?,?)";
        try{

            Connection conn = getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(SQL);
            preparedStatement.setString(1,ID);
            preparedStatement.setDouble(2,polines.xPos);
            preparedStatement.setDouble(3,polines.yPos);
            preparedStatement.executeUpdate();
            conn.close();
        }catch(Exception e){
            e.printStackTrace();
        }

        return true;
    }

    public static boolean insertBranchsTable(){
        for(CrossRoad crossRoad: DatabaseParser.getCrossRoads()){
            for(Road road:DatabaseParser.getRoads()){
                if(DatabaseParser.isIncludeRoad(crossRoad.getID(),road.ID)){
                    String BranchId = "BC" + DatabaseParser.parseCrossRoadID(crossRoad.getID()) + "_" +DatabaseParser.getTheOthterPoint(crossRoad.getID(),road.ID);
                    Date randomDateLeft = DatabaseParser.getRandomDate();
                    Date randomDateRight = DatabaseParser.getRandomDate();
                    Date randomDateStraight = DatabaseParser.getRandomDate();
                    insertBranch(crossRoad.getID(),BranchId,randomDateLeft,randomDateRight,randomDateStraight,80,6,60,80,6,60,80,6,60);
                }
            }
        }
        return true;
    }

    private static boolean insertBranch(String id, String branchId, Date randomDateLeft, Date randomDateRight, Date randomDateStraight, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        String SQL = "INSERT INTO CROSSROAD_BRANCH(CROSSROAD_ID,BRANCH_ID,LEFT_TIME,RIGHT_TIME,STRAIGHT_TIME,LEFT_RED,LEFT_YELLOW,LEFT_GREEN,RIGHT_RED,RIGHT_YELLOW,RIGHT_GREEN,STRAIGHT_RED,STRAIGHT_YELLOW,STRAIGHT_GREEN) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try{
            Connection conn = getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(SQL);
            preparedStatement.setString(1,id);
            preparedStatement.setString(2,branchId);
            preparedStatement.setString(3,DatabaseParser.DateToString(randomDateLeft));
            preparedStatement.setString(4,DatabaseParser.DateToString(randomDateRight));
            preparedStatement.setString(5,DatabaseParser.DateToString(randomDateStraight));
            preparedStatement.setInt(6,i);
            preparedStatement.setInt(7,i1);
            preparedStatement.setInt(8,i2);
            preparedStatement.setInt(9,i3);
            preparedStatement.setInt(10,i4);
            preparedStatement.setInt(11,i5);
            preparedStatement.setInt(12,i6);
            preparedStatement.setInt(13,i7);
            preparedStatement.setInt(14,i8);
            preparedStatement.executeUpdate();
            conn.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return true;
    }

}
