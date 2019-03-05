package com.xjy.hsy.createtable.Parsers.databaseparser;

import com.alibaba.druid.pool.DruidDataSource;
import com.xjy.hsy.createtable.JavaBeans.Position;
import com.xjy.hsy.createtable.JavaBeans.Topography.Topography;
import com.xjy.hsy.createtable.JavaBeans.Topography.Type;
import com.xjy.hsy.createtable.JavaBeans.crossroad.*;
import com.xjy.hsy.createtable.JavaBeans.hotspot.HotSpot;
import com.xjy.hsy.createtable.JavaBeans.hotspot.HotType;
import com.xjy.hsy.createtable.JavaBeans.road.Level;
import com.xjy.hsy.createtable.JavaBeans.road.Road;
import com.xjy.hsy.createtable.Parsers.DistanceParser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public abstract class DatabaseParser {
    private static ArrayList<CrossRoad> crossRoads = new ArrayList<>();         //存储对应的CrossRoad的信息
    private static ArrayList<Road> roads = new ArrayList<>();                   //存储对应的road的信息
    private static ArrayList<Topography> topographies = new ArrayList<>();
    private static ArrayList<HotSpot> hotSpots = new ArrayList<>();

    public static ArrayList<CrossRoad> crossRoadsParse(Position centerPosition){
        String SQL = "SELECT * FROM CROSSROAD";
        init();
        try{
            Connection conn = druidDataSource.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(SQL);
            ResultSet resultSet = preparedStatement.executeQuery();
            crossRoadsResaultSetParser(resultSet,centerPosition);
            conn.close();
            return crossRoads;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<Road> RoadParse(Position centerPosition){
        String SQL = "SELECT * FROM ROAD_SECTION";
        init();
        try{
            Connection conn = druidDataSource.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(SQL);
            ResultSet resultSet = preparedStatement.executeQuery();
            roadsResaultSetParser(resultSet,centerPosition);
            conn.close();
            return roads;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<Topography> TopographyParse(Position centerPosition){
        String SQL = "SELECT * FROM Topography";;
        init();
        try{
            Connection conn = druidDataSource.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(SQL);
            ResultSet resultSet = preparedStatement.executeQuery();
            topographies = topographyRsaultSetParser(resultSet,centerPosition);
            conn.close();
            return topographies;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static ArrayList<Topography> topographyRsaultSetParser(ResultSet resultSet,Position centerPosition){
        try{
            while(resultSet.next()){
                String ID = resultSet.getString(1);
                double xPos = resultSet.getDouble(2);
                double yPos = resultSet.getDouble(3);
                Position position = new Position(xPos,yPos);
                if(DistanceParser.getDistanceBetween(position,centerPosition) > 500000 ) continue;
                String level = resultSet.getString(4);
                Type type = Type.GRASS;
                switch (level){
                    case "RIVER":
                        type = Type.RIVER;
                        break;
                    case "GRASS":
                        type = Type.GRASS;
                        break;
                }

                Topography topography = new Topography(ID,position,type);
                topography.polines = topographyPolinesParse(ID);
                if(topography != null){
                    topographies.add(topography);
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        return topographies;
    }

    private static ArrayList<Position> topographyPolinesParse(String id) {
        String SQL = "SELECT * FROM TopographyPolines WHERE 地形ID = ?";
        try{
            ArrayList<Position> polines = new ArrayList<>();
            Connection conn = druidDataSource.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(SQL);
            preparedStatement.setString(1,id);
            ResultSet resultSet = preparedStatement.executeQuery();
            Position position = null;
            while(resultSet.next()){
                String ID = resultSet.getString(1);
                double xPos = resultSet.getDouble(2);
                double yPos = resultSet.getDouble(3);
                position = new Position(xPos, yPos);
                if(position != null)polines.add(position);
            }
            conn.close();
            return polines;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static DruidDataSource druidDataSource = new DruidDataSource(true);

    private static void init(){
        druidDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        druidDataSource.setUrl("jdbc:mysql://localhost:8868/NAVIGATION?useSSL=false&useUnicode=true&characterEncoding=UTF-8");
        druidDataSource.setUsername("root");
        druidDataSource.setPassword("root");
        druidDataSource.setInitialSize(8);
        druidDataSource.setMinIdle(2);
    }

    private static ArrayList<CrossRoad> crossRoadsResaultSetParser(ResultSet resultSet,Position centerPosition){
        try{
            while(resultSet.next()){
                String ID = resultSet.getString(1);
                double xPos = resultSet.getDouble(2);
                double yPos = resultSet.getDouble(3);
                Position position = new Position(xPos,yPos);
                if(DistanceParser.getDistanceBetween(position,centerPosition) > 500000 ) continue;
                int level = Integer.parseInt(resultSet.getString(4));
                ArrayList<Branch> branches = branchesResultSetParser(ID);
                CrossRoad crossRoad = null;
                switch (level){
                    case 3:
                        crossRoad = new TCrossRoad(position,branches,ID,level);
                        break;
                    case 4:
                        crossRoad = new XCrossRoad(position,branches,ID,level);
                        break;
                    case 5:
                        crossRoad = new StarCrossRoad(position,branches,ID,level);
                    default:
                        break;
                }
                if(crossRoad != null){
                    crossRoads.add(crossRoad);
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        return crossRoads;
    }

    private static ArrayList<Branch> branchesResultSetParser(String crossroadID){
        /*在这里添加代码*/

        return null;

    }

    private  static  ArrayList<Road> roadsResaultSetParser(ResultSet resultSet,Position centerPosition){     //根据centerPosition获取数据
        try{
            while(resultSet.next()){
                String name = resultSet.getString(1);
                String ID = resultSet.getString(2);
                ArrayList<Position> polines =  getRoadPolines(ID);
                int length = resultSet.getInt(3);
                String levelString = resultSet.getString(4);
                double startXPos = resultSet.getDouble(5);
                double startYPos = resultSet.getDouble(6);
                double endXPos = resultSet.getDouble(7);
                double endYPos = resultSet.getDouble(8);
                float degree = resultSet.getFloat(9);
                Position startPosition = new Position(startXPos,startYPos);
                    Position endPosition = new Position(endXPos,endYPos);
                    if(DistanceParser.getDistanceBetween(startPosition,centerPosition) > 500000 && DistanceParser.getDistanceBetween(endPosition,centerPosition) > 500000) continue;
                    ArrayList<Branch> branches = branchesResultSetParser(ID);
                    CrossRoad crossRoad = null;
                    Level level = null;
                    switch (levelString){
                    case "EXPY_ROAD":
                        level = Level.EXPY_ROAD;
                        break;
                    case "NATIONAL_ROAD":
                        level = Level.NATIONAL_ROAD;
                        break;
                    case "MAIN_ROAD":
                        level = Level.MAIN_ROAD;
                        break;
                    case "OTHER_ROADS":
                        level = Level.OTHER_ROADS;
                        break;
                    default :
                        break;
                }
                Road road  = new Road(polines,ID,name,length, startPosition,endPosition ,degree,level);
                if(road != null){
                    roads.add(road);
                }
            }
            return roads;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static ArrayList<Position> getRoadPolines(String id) {
        String SQL = "SELECT * FROM ROAD_POLINES WHERE 路段ID = ?";
        try{
            ArrayList<Position> polines = new ArrayList<>();
            Connection conn = druidDataSource.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(SQL);
            preparedStatement.setString(1,id);
            ResultSet resultSet = preparedStatement.executeQuery();
            Position position = null;
            while(resultSet.next()){
                String ID = resultSet.getString(1);
                double xPos = resultSet.getDouble(2);
                double yPos = resultSet.getDouble(3);
                position = new Position(xPos, yPos);
                if(position != null)polines.add(position);
            }
            conn.close();
            return polines;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }


    public static ArrayList<HotSpot> HotSpotParse(Position centerPosition) {
        String SQL = "SELECT * FROM hotspot";;
        init();
        try{
            Connection conn = druidDataSource.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(SQL);
            ResultSet resultSet = preparedStatement.executeQuery();
            hotSpots = HotSpotRsaultSetParser(resultSet,centerPosition);
            conn.close();
            return hotSpots;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static ArrayList<HotSpot> HotSpotRsaultSetParser(ResultSet resultSet,Position centerPosition) {
        try{
            while(resultSet.next()){
                String ID = resultSet.getString(1);
                double xPos = resultSet.getDouble(2);
                double yPos = resultSet.getDouble(3);
                String type = resultSet.getString(4);
                Position position = new Position(xPos,yPos);
                if(DistanceParser.getDistanceBetween(position,centerPosition) > 500000 ) continue;
                CrossRoad crossRoad = null;
                HotType hotType = null;
                switch (hotType){

                    case ENJOY:
                        hotType = HotType.ENJOY;
                        break;
                    case FOOD:
                        hotType = HotType.FOOD;
                        break;
                    case GOVERMENT:
                        hotType = HotType.GOVERMENT;
                        break;
                    case MEDICAL:
                        hotType = HotType.MEDICAL;
                        break;
                    case MOUNTAIN:
                        hotType = HotType.MOUNTAIN;
                        break;
                    case SHOPPING:
                        hotType = HotType.SHOPPING;
                        break;
                    case SLEEP:
                        hotType = HotType.SLEEP;
                        break;
                    case STATION:
                        hotType = HotType.STATION;
                        break;
                    case TRAVEL:
                        hotType = HotType.TRAVEL;
                        break;
                }

                HotSpot hotSpot = new HotSpot(position,ID,hotType);
                if(hotSpot != null){
                    hotSpots.add(hotSpot);
                }
            }
            return hotSpots;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static ArrayList<Branch> getBranchsOf(String ID){
        int num = parseCrossRoadID(ID);

        return null;
    }

    public static int parseCrossRoadID(String id) {
        int basicNum = Integer.parseInt(id.substring(3));
        return basicNum;
    }

    public static ArrayList<Integer> parseRoadID(String id){
        ArrayList<Integer> crossRoads = new ArrayList<>();
        String[] strs = id.substring(1).split("_");
        crossRoads.add(Integer.parseInt(strs[0]));
        crossRoads.add(Integer.parseInt(strs[1]));
        return crossRoads;
    }

    public static boolean isIncludeRoad(String crossRoadID,String roadID){
        for(Integer rd : parseRoadID(roadID)){
            if(parseCrossRoadID(crossRoadID) == rd){
                return true;
            }
        }
        return false;
    }

    public static int getTheOthterPoint(String crossRoadID,String roadID){
        if(parseRoadID(roadID).get(0) != parseCrossRoadID(crossRoadID))
        {
            return parseRoadID(roadID).get(0);
        }else{
            return parseRoadID(roadID).get(1);
        }
    }

    public static boolean fillRoadAndCrossRoad(Position centerPosition){
        roads = RoadParse(centerPosition);
        crossRoads = crossRoadsParse(centerPosition);
        return true;
    }

    public static ArrayList<CrossRoad> getCrossRoads() {
        return crossRoads;
    }

    public static void setCrossRoads(ArrayList<CrossRoad> crossRoads) {
        DatabaseParser.crossRoads = crossRoads;
    }

    public static ArrayList<Road> getRoads() {
        return roads;
    }

    public static void setRoads(ArrayList<Road> roads) {
        DatabaseParser.roads = roads;
    }

    public static Date StringtoDate(String time){
        DateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date dateTime = format.parse(time);
            return dateTime;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String DateToString(Date datetime){
        DateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(datetime);
    }

    public static Date getRandomDate(){
        String time = "2018-10-15 00:00:00";
        Date datetime = StringtoDate(time);
        long basicMillis = datetime.getTime();
        long period = new Date().getTime() - basicMillis;
        long realMillis = basicMillis + new Random().nextInt((int)period);
        return new Date(realMillis);
    }
}
