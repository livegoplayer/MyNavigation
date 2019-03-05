package com.xjy.hsy.parsers.databaseparser;

import com.alibaba.druid.pool.DruidDataSource;
import com.xjy.hsy.Topography.Topography;
import com.xjy.hsy.Topography.Type;
import com.xjy.hsy.crossroad.*;
import com.xjy.hsy.hotspot.HotSpot;
import com.xjy.hsy.hotspot.HotType;
import com.xjy.hsy.parsers.DistanceParser;
import com.xjy.hsy.road.Level;
import com.xjy.hsy.road.Road;
import javafx.geometry.Pos;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public abstract class DatabaseParser {
    private static ArrayList<CrossRoad> crossRoads = new ArrayList<>();         //存储对应的CrossRoad的信息
    private static ArrayList<Road> roads = new ArrayList<>();                   //存储对应的road的信息
    private static ArrayList<Topography> topographies = new ArrayList<>();
    private static ArrayList<HotSpot> hotSpots = new ArrayList<>();

    public static ArrayList<CrossRoad> crossRoadsParse(Position centerPosition) {
        String SQL = "SELECT * FROM CROSSROAD";
        init();
        try {
            Connection conn = druidDataSource.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(SQL);
            ResultSet resultSet = preparedStatement.executeQuery();
            crossRoadsResaultSetParser(resultSet, centerPosition);
            conn.close();
            return crossRoads;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<Road> RoadParse(Position centerPosition) {
        String SQL = "SELECT * FROM ROAD_SECTION";
        init();
        try {
            Connection conn = druidDataSource.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(SQL);
            ResultSet resultSet = preparedStatement.executeQuery();
            roadsResaultSetParser(resultSet, centerPosition);
            conn.close();
            return roads;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<Topography> TopographyParse(Position centerPosition) {
        String SQL = "SELECT * FROM Topography";
        ;
        init();
        try {
            Connection conn = druidDataSource.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(SQL);
            ResultSet resultSet = preparedStatement.executeQuery();
            topographies = topographyRsaultSetParser(resultSet, centerPosition);
            conn.close();
            return topographies;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ArrayList<Topography> topographyRsaultSetParser(ResultSet resultSet, Position centerPosition) {
        try {
            while (resultSet.next()) {
                String ID = resultSet.getString(1);
                double xPos = resultSet.getDouble(2);
                double yPos = resultSet.getDouble(3);
                Position position = new Position(xPos, yPos);
                if (DistanceParser.getDistanceBetween(position, centerPosition) > 500000) continue;
                String level = resultSet.getString(4);
                Type type = Type.GRASS;
                switch (level) {
                    case "RIVER":
                        type = Type.RIVER;
                        break;
                    case "GRASS":
                        type = Type.GRASS;
                        break;
                }

                Topography topography = new Topography(ID, position, type);
                topography.polines = topographyPolinesParse(ID);
                if (topography != null) {
                    topographies.add(topography);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return topographies;
    }

    private static ArrayList<Position> topographyPolinesParse(String id) {
        String SQL = "SELECT * FROM TopographyPolines WHERE 地形ID = ?";
        try {
            ArrayList<Position> polines = new ArrayList<>();
            Connection conn = druidDataSource.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(SQL);
            preparedStatement.setString(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            Position position = null;
            while (resultSet.next()) {
                String ID = resultSet.getString(1);
                double xPos = resultSet.getDouble(2);
                double yPos = resultSet.getDouble(3);
                position = new Position(xPos, yPos);
                if (position != null) polines.add(position);
            }
            conn.close();
            return polines;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static DruidDataSource druidDataSource = new DruidDataSource(true);

    private static void init() {
        druidDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        druidDataSource.setUrl("jdbc:mysql://localhost:8868/NAVIGATION?useSSL=false&useUnicode=true&characterEncoding=UTF-8");
        druidDataSource.setUsername("root");
        druidDataSource.setPassword("root");
        druidDataSource.setInitialSize(8);
        druidDataSource.setMinIdle(2);
    }

    private static ArrayList<CrossRoad> crossRoadsResaultSetParser(ResultSet resultSet, Position centerPosition) {
        try {
            while (resultSet.next()) {
                String ID = resultSet.getString(1);
                double xPos = resultSet.getDouble(2);
                double yPos = resultSet.getDouble(3);
                Position position = new Position(xPos, yPos);
                if (DistanceParser.getDistanceBetween(position, centerPosition) > 500000) continue;
                int level = Integer.parseInt(resultSet.getString(4));
                String name = resultSet.getString(5);
                ArrayList<Branch> branches = branchesParser(ID);
                CrossRoad crossRoad = null;
                switch (level) {
                    case 3:
                        crossRoad = new TCrossRoad(position, branches, ID, level, name);
                        break;
                    case 4:
                        crossRoad = new XCrossRoad(position, branches, ID, level, name);
                        break;
                    case 5:
                        crossRoad = new StarCrossRoad(position, branches, ID, level, name);
                    default:
                        break;
                }

                if (crossRoad != null) {
                    crossRoad.getPos().ID = crossRoad.getID();
                    crossRoads.add(crossRoad);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return crossRoads;
    }

    private static ArrayList<Branch> branchesParser(String crossroadID) {
        /*在这里添加代码*/
        String SQL = "SELECT * FROM CROSSROAD_BRANCH WHERE CROSSROAD_ID = ?";
        try {
            ArrayList<Branch> branches;
            Connection conn = druidDataSource.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(SQL);
            preparedStatement.setString(1, crossroadID);
            ResultSet resultSet = preparedStatement.executeQuery();
            branches = branchesResaultSetParser(resultSet);
            conn.close();
            return branches;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    private static ArrayList<Branch> branchesResaultSetParser(ResultSet resultSet) {
        ArrayList<Branch> branches = new ArrayList<>();
        try {
            while (resultSet.next()) {
                String ID = resultSet.getString(2);
                Date LeftTime = StringtoDate(resultSet.getString(3));
                Date RightTime = StringtoDate(resultSet.getString(4));
                Date StraightTIme = StringtoDate(resultSet.getString(5));

                int LeftRed = resultSet.getInt(6);
                int LeftYellow = resultSet.getInt(7);
                int LeftGreen = resultSet.getInt(8);
                ArrayList<Integer> Left_Group = new ArrayList<>();
                Left_Group.add(LeftRed);
                Left_Group.add(LeftGreen);
                Left_Group.add(LeftYellow);

                int RightRed = resultSet.getInt(9);
                int RightYellow = resultSet.getInt(10);
                int RightGreen = resultSet.getInt(11);
                ArrayList<Integer> Right_Group = new ArrayList<>();
                Right_Group.add(RightRed);
                Right_Group.add(RightGreen);
                Right_Group.add(RightYellow);

                int StraightRed = resultSet.getInt(9);
                int StraightYellow = resultSet.getInt(10);
                int StraightGreen = resultSet.getInt(11);
                ArrayList<Integer> Straight_Group = new ArrayList<>();
                Straight_Group.add(StraightRed);
                Straight_Group.add(StraightGreen);
                Straight_Group.add(StraightYellow);

                int ison_left = resultSet.getInt(12);
                int ison_right = resultSet.getInt(13);
                int ison_straight = resultSet.getInt(14);

                Branch branch = new Branch(ID, LeftTime, RightTime, StraightTIme, Left_Group, Right_Group, Straight_Group, ison_left, ison_right, ison_straight);
                branches.add(branch);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return branches;

    }

    private static ArrayList<Road> roadsResaultSetParser(ResultSet resultSet, Position centerPosition) {     //根据centerPosition获取数据
        try {
            while (resultSet.next()) {
                String name = resultSet.getString(1);
                String ID = resultSet.getString(2);
                ArrayList<Position> polines = getRoadPolines(ID);
                int length = resultSet.getInt(3);
                String levelString = resultSet.getString(4);
                double startXPos = resultSet.getDouble(5);
                double startYPos = resultSet.getDouble(6);
                double endXPos = resultSet.getDouble(7);
                double endYPos = resultSet.getDouble(8);
                int speed = resultSet.getInt(10);
                Position startPosition = new Position(startXPos, startYPos);
                Position endPosition = new Position(endXPos, endYPos);
                if (DistanceParser.getDistanceBetween(startPosition, centerPosition) > 500000 && DistanceParser.getDistanceBetween(endPosition, centerPosition) > 500000)
                    continue;
                Level level = null;
                switch (levelString) {
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
                    default:
                        break;
                }
                Road road = new Road(polines, ID, name, length, startPosition, endPosition, speed, level);
                startPosition.ID = parseRoadID(ID).get(0);
                endPosition.ID = parseRoadID(ID).get(1);
                if (road != null) {
                    roads.add(road);
                }
            }
            return roads;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ArrayList<Position> getRoadPolines(String id) {
        String SQL = "SELECT * FROM ROAD_POLINES WHERE 路段ID = ?";
        try {
            ArrayList<Position> polines = new ArrayList<>();
            Connection conn = druidDataSource.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(SQL);
            preparedStatement.setString(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            Position position = null;
            while (resultSet.next()) {
                String ID = resultSet.getString(1);
                double xPos = resultSet.getDouble(2);
                double yPos = resultSet.getDouble(3);
                position = new Position(xPos, yPos);
                if (position != null) polines.add(position);
            }
            conn.close();
            return polines;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static ArrayList<HotSpot> HotSpotParse(Position centerPosition) {
        String SQL = "SELECT * FROM hotspot";
        ;
        init();
        try {
            Connection conn = druidDataSource.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(SQL);
            ResultSet resultSet = preparedStatement.executeQuery();
            hotSpots = HotSpotRsaultSetParser(resultSet, centerPosition);
            conn.close();
            return hotSpots;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ArrayList<HotSpot> HotSpotRsaultSetParser(ResultSet resultSet, Position centerPosition) {
        try {
            while (resultSet.next()) {
                String ID = resultSet.getString(1);
                String name = resultSet.getString(2);
                double xPos = resultSet.getDouble(3);
                double yPos = resultSet.getDouble(4);
                String type = resultSet.getString(5);
                Position position = new Position(xPos, yPos);
                if (DistanceParser.getDistanceBetween(position, centerPosition) > 500000) continue;
                CrossRoad crossRoad = null;
                HotType hotType = null;
                switch (type) {
                    case "ENJOY":
                        hotType = HotType.ENJOY;
                        break;
                    case "FOOD":
                        hotType = HotType.FOOD;
                        break;
                    case "GOVERMENT":
                        hotType = HotType.GOVERMENT;
                        break;
                    case "MEDICAL":
                        hotType = HotType.MEDICAL;
                        break;
                    case "MOUNTAIN":
                        hotType = HotType.MOUNTAIN;
                        break;
                    case "SHOPPING":
                        hotType = HotType.SHOPPING;
                        break;
                    case "SLEEP":
                        hotType = HotType.SLEEP;
                        break;
                    case "STATION":
                        hotType = HotType.STATION;
                        break;
                    case "TRAVEL":
                        hotType = HotType.TRAVEL;
                        break;
                }

                HotSpot hotSpot = new HotSpot(position, ID, hotType, name);
                if (hotSpot != null) {
                    hotSpots.add(hotSpot);
                }
            }
            return hotSpots;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Date StringtoDate(String time) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date dateTime = format.parse(time);
            return dateTime;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static ArrayList<String> parseRoadID(String id) {
        ArrayList<String> crossRoads = new ArrayList<>();
        String[] strs = id.substring(1).split("_");
        crossRoads.add("CD_" + Integer.parseInt(strs[0]));
        crossRoads.add("CD_" + Integer.parseInt(strs[1]));
        return crossRoads;
    }

    public static int updateSpeed(String id) {
        String SQL = "SELECT 速度 FROM ROAD_SECTION WHERE 路段ID= ?";
        int speed = 0;
        try {
            Connection conn = druidDataSource.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(SQL);
            preparedStatement.setString(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                speed = resultSet.getInt(1);
            }
            conn.close();
            return speed;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return speed;
    }

    public static void insertSpeed(String ID, int speed) {
        String SQL = "UPDATE ROAD_SECTION SET 速度 = ? WHERE 路段ID = ?";
        try {
            Connection conn = druidDataSource.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(SQL);
            preparedStatement.setInt(1, speed);
            preparedStatement.setString(2, ID);
            preparedStatement.executeUpdate();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getSpeed(String ID) {
        String SQL = "SELECT 速度 FROM ROAD_SECTION WHERE 路段ID = ?";
        int speed = 0;
        try {
            Connection conn = druidDataSource.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(SQL);
            preparedStatement.setString(1, ID);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                speed = resultSet.getInt(1);
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return speed;
    }
}
