package com.xjy.hsy.createtable;

import com.xjy.hsy.createtable.JavaBeans.LogicRoad;
import com.xjy.hsy.createtable.JavaBeans.Position;

import com.xjy.hsy.createtable.JavaBeans.Topography.Topography;
import com.xjy.hsy.createtable.JavaBeans.Topography.Type;

import com.xjy.hsy.createtable.JavaBeans.hotspot.HotSpot;
import com.xjy.hsy.createtable.JavaBeans.hotspot.HotType;
import com.xjy.hsy.createtable.JavaBeans.road.Level;
import com.xjy.hsy.createtable.Parsers.DistanceParser;


import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class AlterTable {
    /*制造随机的路口*/
    public static ArrayList<Position> crossRoads = new ArrayList<>();           //随机点的集合
    public static int MAX_MAP_LENGTH = 500000;          //产生随机点的最大距离,500000 = 5
    public static int MAX_INTERVAL = 5000;            //随机点之间的最大距离         5000 = 0.05
    public static int MIN_INTERVAL = 500;             //随机点之间的最小距离         500 = 0.005

    public static void addRandomCrossroads(int count) {                             //添加随机点到数组Position数组
        initCrossRoads();
        boolean flag = true;
        while (count > 0) {                  //生成固定的数量的随机点
            flag = true;                    //true标识该点符合规范
            Position newPosition = getRandomPosition();

            for (Position oldPosition : crossRoads) {
                if (DistanceParser.getDistanceBetween(newPosition, oldPosition) < 500) {
                    flag = false;
                    break;
                }
            }
            if (flag == true) {
                crossRoads.add(newPosition);
                count--;
            }
        }
    }

    private static Position getRandomPosition() {
        return new Position(MyGPSPosition.getPosition().xPos + 2 * (Math.random() - 0.5) * MAX_MAP_LENGTH / 100000 / 1.2 / 2, MyGPSPosition.getPosition().yPos + 2 * (Math.random() - 0.5) * MAX_MAP_LENGTH / 100000 / 2);
    }

    private static void initCrossRoads() {      //初始化20*20个红绿灯   200公里 * 200公里内红绿灯的排布初始化
        if (crossRoads.size() > 0) crossRoads.clear();
        double[] xPosOffset = {-1, -0.9, -0.8, -0.7, -0.6, -0.5, -0.4, -0.3, -0.2, -0.1, 0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1};
        double[] yPosOffset = {-1, -0.9, -0.8, -0.7, -0.6, -0.5, -0.4, -0.3, -0.2, -0.1, 0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1};
        ;
        for (double x : xPosOffset) {                //初始化点
            for (double y : yPosOffset) {
                crossRoads.add(new Position(x, y));
            }
        }
    }

    /*制造随机的路线*/
    /*根据距离最短优先创建边*/
    public static ArrayList<LogicRoad> roads = new ArrayList<>();            //存储crossRoads数组对应边的index，每有一个边生成，必然有两个点的count都减去1

    //得到最小距离点的方法
    private static Position getClosestPosition(Position centerPosition, ArrayList<Position> crossRoadCollection) {   //在croosRoadCollection的范围内搜索最近点，每次搜索到的点会被剔除
        double distance = Double.POSITIVE_INFINITY;
        Position closestPosition = null;
        for (Position position : crossRoadCollection) {
            if (centerPosition.ID != position.ID && position.count > 0) {
                if (distance > DistanceParser.getDistanceBetween(position, centerPosition)) {
                    distance = DistanceParser.getDistanceBetween(position, centerPosition);
                    closestPosition = position;
                }
            }
        }
        return closestPosition;
    }

    //在ArrayList中添加Roads
    public static void addRandomRoads() {
        if (roads.size() > 0) roads.clear();
        ArrayList<Position> leftCrossRoads = new ArrayList<>();
        for (int i = crossRoads.size() - 1; i >= 0; i--) {
            Position position = crossRoads.get(i);
            leftCrossRoads = copyArrayList(crossRoads);                                  //重置leftCrossRoads
            while (position.count > 0) {
                Position targetPosition = getClosestPosition(position, leftCrossRoads);
                if (targetPosition == null) {
                    position.count = 0;
                    break;
                }

                LogicRoad road = new LogicRoad(targetPosition, position);
                road.ID = "R" + road.startPos.ID + "_" + road.endPos.ID;
                road.level = getLevel(road);

                roads.add(road);
                targetPosition.count = 0;                                       //把leftCrossRoads中的对应点置0，然后该点就不会再出现
                crossRoads.get(Integer.parseInt(targetPosition.ID)).count--;   //真的crossRoad数组的count --,
                position.count--;
            }
            leftCrossRoads.clear();
        }
    }

    private static Level getLevel(LogicRoad logicRoad) {
        Position startPos = logicRoad.startPos;
        Position endPos = logicRoad.endPos;
        if (isBigPoint(startPos) && isBigPoint(endPos)) {
            return Level.EXPY_ROAD;
        } else if (isBigPoint(startPos) || isBigPoint(endPos)) {
            return Level.NATIONAL_ROAD;
        } else {
            if (new Random(1).nextInt(2) == 1) {
                return Level.OTHER_ROADS;
            } else {
                return Level.MAIN_ROAD;
            }
        }
    }

    private static boolean isBigPoint(Position pos) {          //判断一个点是否为主要点
        double[] numSet = {-1, -0.9, -0.8, -0.7, -0.6, -0.5, -0.4, -0.3, -0.2, -0.1, 0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1};
        for (double x : numSet) {
            if (pos.xPos == x) {
                for (double y : numSet) {
                    if (pos.yPos == y) return true;
                }
            }
        }
        return false;
    }

    private static ArrayList<Position> copyArrayList(ArrayList<Position> oldList) {
        ArrayList<Position> newList = new ArrayList<>();
        for (Position oldPos : oldList) {
            newList.add(oldPos.clone());
        }
        return newList;
    }

    /*初始化每段路的中间ID*/
    /*用于绘制地图，这里初始化为直线*/

    public static ArrayList<Position> getPolines(LogicRoad logicRoad) {           //传入一条路，得到随机的路段举着点的数组
        ArrayList<Position> polines = new ArrayList<>();   //保存一条路段的临时数据
        int count = new Random().nextInt(7) + 3;             //随机产生一段路的曲折路段数
        int distance = (int) (DistanceParser.getDistanceBetween(logicRoad.startPos, logicRoad.endPos) * 1.2);    //路段长度
        int tmpCount = count;
        while (count > 0) {
            int section = tmpCount - count;                      //随机产生的路段点到起点的距离
            double xPos = logicRoad.startPos.xPos + section * (logicRoad.endPos.xPos - logicRoad.startPos.xPos) / tmpCount;   //计算该点的位置
            double yPos = logicRoad.startPos.yPos + section * (logicRoad.endPos.yPos - logicRoad.startPos.yPos) / tmpCount;
            Position position = new Position(xPos, yPos);
            polines.add(position);
            count--;
        }
        polines.add(logicRoad.endPos);
        return polines;
    }

    /*
        添加地形
     */
    public static ArrayList<Topography> topographies = new ArrayList<>();

    public static void addRandomTopography(int count) {
        int countGrass = 0;
        int countRiver = 0;
        while (count-- > 0) {
            ArrayList<Type> types = new ArrayList<>();
            types.add(Type.GRASS);
            types.add(Type.RIVER);
            Type randomType = types.get(new Random().nextInt(2));
            Position randomPosition = getRandomPosition();
            String ID = null;
            switch (randomType) {
                case GRASS:
                    ID = "G_" + countGrass++;
                    break;
                case RIVER:
                    ID = "R_" + countRiver++;
                    break;
            }
            Topography topography = new Topography(ID, randomPosition, randomType);
            topography.polines = getRetPolines(randomPosition);
            topographies.add(topography);
        }
    }

    private static ArrayList<Position> getRetPolines(Position centerPosition) {
        ArrayList<Position> polines = new ArrayList<>();
        double length = 0.1;
        Position left_top = new Position(centerPosition.xPos - length, centerPosition.yPos + length);
        Position left_bottom = new Position(centerPosition.xPos - length, centerPosition.yPos - length);
        Position right_top = new Position(centerPosition.xPos + length, centerPosition.yPos + length);
        Position right_bottom = new Position(centerPosition.xPos + length, centerPosition.yPos - length);
        polines.add(left_top);
        polines.add(left_bottom);
        polines.add(right_bottom);
        polines.add(right_top);
        return polines;
    }

    /*
        热点
     */
    public static ArrayList<HotSpot> hotSpots = new ArrayList<>();

    public static ArrayList<HotSpot> addRandomHotSpots(int count) {
        int[] counts = new int[9];
        ArrayList<HotType> types = new ArrayList<>();
        types.add(HotType.ENJOY);
        types.add(HotType.FOOD);
        types.add(HotType.GOVERMENT);
        types.add(HotType.MEDICAL);
        types.add(HotType.MOUNTAIN);
        types.add(HotType.SHOPPING);
        types.add(HotType.SLEEP);
        types.add(HotType.STATION);
        types.add(HotType.TRAVEL);
        while (count-- > 0) {
            HotType randomType = types.get(new Random().nextInt(9));
            Position randomPosition = getRandomPosition();
            String ID = null;
            switch (randomType) {
                case ENJOY:
                    ID = "ET_" + counts[0]++;
                    break;
                case FOOD:
                    ID = "FD_" + counts[1]++;
                    break;
                case GOVERMENT:
                    ID = "GT_" + counts[2]++;
                    break;
                case MEDICAL:
                    ID = "ML_" + counts[3]++;
                    break;
                case MOUNTAIN:
                    ID = "MN_" + counts[4]++;
                    break;
                case SHOPPING:
                    ID = "SG_" + counts[5]++;
                    break;
                case SLEEP:
                    ID = "SP_" + counts[6]++;
                    break;
                case STATION:
                    ID = "SN_" + counts[7]++;
                    break;
                case TRAVEL:
                    ID = "TL_" + counts[8]++;
                    break;
            }
            HotSpot hotSpot = new HotSpot(randomPosition, ID, randomType);
            hotSpots.add(hotSpot);
        }
        return hotSpots;
    }



}
