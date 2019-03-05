package com.xjy.hsy.navigation;

import com.alibaba.druid.sql.dialect.odps.ast.OdpsObjectImpl;
import com.xjy.hsy.crossroad.Branch;
import com.xjy.hsy.crossroad.CrossRoad;
import com.xjy.hsy.crossroad.Direction;
import com.xjy.hsy.crossroad.Position;
import com.xjy.hsy.drawmap.DrawMap;
import com.xjy.hsy.drawmap.MyGPSPosition;
import com.xjy.hsy.parsers.DistanceParser;
import com.xjy.hsy.parsers.TimeParser;
import com.xjy.hsy.parsers.databaseparser.DatabaseParser;
import com.xjy.hsy.road.Road;
import javafx.geometry.Pos;
import javafx.print.PrintSides;

import javax.crypto.spec.PSource;
import java.awt.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

public class DrawNavigation {
    public static ArrayList<CrossRoad> crossRoadsInRange = new ArrayList<>();
    public static ArrayList<Road> roadsInRange = new ArrayList<>(); //存放点的信息
    private static Road[][] roadID;                                 //存放初始化权值，也就是road的二维数组，这个邻接矩阵是根据crossroadinrange数组点的序号排序的，为了方便计算，做成了双向图，每次计算得最小路径需要清空两个值
    public static double[] dist;                                   //存放原点到各个点的最短时间
    private static Position[] pre;                                    //存放使得该点为最短路径的前一个点
    private static ArrayDeque<Position> minPath;                      //存放当前最短路径的Position集合
    private static double minTimes;                                    //存放最短路径集合每个阶段的对应时间
    private static Position myPosition;



    private static Road myPositionInRoads;                          //存放最近的路段
    private static Position centerPosition;
    private static Position myPositionInRoad;                        //存放距离该位置最短的地点
    private static Position MyPositionInCrossRoad;                  //存放距离该位置最短的交叉路口，也就是计算的起点
    private static Position targetPosition;
    private static Position closestTargetPosition;                   //存放距离目的地最近位置的点，及目的地附近点
    private static Road closestTargetRoad;                                 //存放和目的地最近的道路
    private static Position closestTargetCrossRoad;                   //存放和目的地最近的点
    private static boolean[] findMin;                       //标记该点是否已经作为最小路径点，序号参照crossroadInrange数组
    private static ArrayList<Road> RoadsInPath = new ArrayList<>();

    public static Position getClosestTargetPosition() {
        return closestTargetPosition;
    }

    public static Position getMyPositionInCrossroad(){
        return MyPositionInCrossRoad;
    }
    public static void setMyPosition(Position myPosition) {
        DrawNavigation.myPosition = myPosition;
    }

    public static Position getTargetPosition() {
        return targetPosition;
    }

    public static void setTargetPosition(Position targetPosition) {
        DrawNavigation.targetPosition = targetPosition;
    }

    private static ArrayList<Road> getRoadsToSearch() {
        double maxLength = getSearchRange();
        ArrayList<Road> roadInRange = new ArrayList<>();
        for (Road road : DrawMap.getMapLines()) {
            if (DistanceParser.getDistanceBetween(road.startPoint, myPosition) < maxLength && DistanceParser.getDistanceBetween(road.endPoint, myPosition) < maxLength) {
                roadInRange.add(road);
            }
        }
        return roadInRange;
    }

    private static ArrayList<CrossRoad> getCrossRoadToSearch() {
        double maxLength = getSearchRange();
        ArrayList<CrossRoad> crossoadInRange = new ArrayList<>();
        for (CrossRoad crossRoad : DrawMap.getMapPoint()) {
            if (DistanceParser.getDistanceBetween(crossRoad.getPos(), myPosition) < maxLength) {
                crossoadInRange.add(crossRoad);
            }
        }
        return crossoadInRange;
    }

    private static double getSearchRange() {
        return DistanceParser.getDistanceBetween(myPosition, targetPosition) + 5000;
    }

    public static boolean initData() {
        /*
            初始化不变计算量
         */
        crossRoadsInRange = getCrossRoadToSearch();
        roadsInRange = getRoadsToSearch();       //这个要变
        roadID = new Road[crossRoadsInRange.size()][crossRoadsInRange.size()];
        closestTargetRoad = getClosestRoad(targetPosition);
        closestTargetPosition = getClosestPosition(targetPosition);
        closestTargetCrossRoad = getTargetPositionInCrossRoad();

        //把road放入二维数组，方便查找
        /*
            存放初始化权值
         */
        for (Road road : roadsInRange) {
            roadID[getIndexById(road.startPoint.ID)][getIndexById(road.endPoint.ID)] = road;
            roadID[getIndexById(road.endPoint.ID)][getIndexById(road.startPoint.ID)] = road;
        }

        prepare();
        return true;
    }

    private static void prepare() {
        /*
            获得当前初始化的起点
         */
        myPosition = MyGPSPosition.getPosition();
        MyPositionInCrossRoad = getMyPositionInCrossRoad();
        myPositionInRoad = getClosestPosition(myPosition);
         /*
            初始化最短距离数组
         */
        dist = new double[crossRoadsInRange.size()];
        for (int i = 0; i < crossRoadsInRange.size(); i++) {
            dist[i] = Double.POSITIVE_INFINITY;
        }
        /*
            初始化最短路径数组
         */
        minPath = new ArrayDeque<>();
        /*
            初始化前置数组
         */
        pre = new Position[crossRoadsInRange.size()];
        /*
            初始化findMin
         */
        findMin = new boolean[crossRoadsInRange.size()];
        /*
            起点入数组
         */
        int startIndex = getIndexById(MyPositionInCrossRoad.ID);
        dist[startIndex] = 0;               //表示原点到该点的距离为0
        pre[startIndex] = myPositionInRoad; //输入一个特殊值
    }

    public static ArrayDeque<Position> getMinPosArray() {
        prepare();
        Position nowPosition = MyPositionInCrossRoad;                                      //保存当前最短路径的最终点
        int nowIndex = getIndexById(MyPositionInCrossRoad.ID);                             //保顿当前最短路径最终点的下标
        double minTime = Double.POSITIVE_INFINITY;                                         //标志最终点到周围点的最短时间
        int nextIndex = -1;                                                                    //保存下一个点的index
        Position nextPosition = null;                                                            //保存下一个点的坐标

        double rad;                                                                        //保存到下一个点所需要调整的弧度
        while (findMin[getIndexById(closestTargetCrossRoad.ID)] != true) {
            double time;
            time = Double.POSITIVE_INFINITY;
            minTime = Double.POSITIVE_INFINITY;
            for (int i = 0; i < roadID[nowIndex].length; i++) {
                if (roadID[nowIndex][i] != null && findMin[i] != true) {
                    /*
                        第一个的弧度和其他的略有不同
                     */
                    if (nowPosition == MyPositionInCrossRoad) {
                        rad = computeRad(DrawMap.parsePosition(MyPositionInCrossRoad), DrawMap.parsePosition(crossRoadsInRange.get(i).getPos())) - DrawMap.getRad();
                    } else {
                        rad = getRelativeRad(pre[nowIndex], nowPosition, nowPosition, getTheOtherPosition(roadID[nowIndex][i], nowPosition));
                    }
                    /*
                        根据不同的角度获得不同的延时来计算time值
                     */
                    if (rad < 0) {
                        time = getRealTime(nowIndex, nowPosition, Direction.RIGHT, roadID[nowIndex][i]);
                    } else if (rad > 0) {
                        time = getRealTime(nowIndex, nowPosition, Direction.LEFT, roadID[nowIndex][i]);
                    } else if (rad == 0) {
                        time = getRealTime(nowIndex, nowPosition, Direction.STRAIGHT, roadID[nowIndex][i]);
                    }

                    /*
                        重设最短距离
                     */
                    if (dist[i] > time + dist[nowIndex]) {
                        pre[i] = nowPosition;
                        dist[i] = time + dist[nowIndex];
                    }

                }
            }

            /*
                        比较获得短路径
            */
            minTime = Double.POSITIVE_INFINITY;
            for (int i = 0; i < dist.length; i++) {
                if (minTime > dist[i] && findMin[i] == false) {
                    minTime = dist[i];
                    nextPosition = crossRoadsInRange.get(i).getPos();
                    nextIndex = i;
                }
            }
            findMin[nowIndex] = true;                    //标志该点已经搜索过
            /*
                进入下一个循环
             */
            nowPosition = nextPosition;
            nowIndex = nextIndex;
        }

        return getMinPath(closestTargetCrossRoad);
    }

    public static ArrayDeque<Position> updateByTime() {
        /*
            更新当前位置
         */
        myPosition = MyGPSPosition.getPosition();
        MyPositionInCrossRoad = getMyPositionInCrossRoad();
        myPositionInRoad = getClosestPosition(myPosition);
        /*
            更新权值数组
         */
        for (int i = 0; i < roadsInRange.size(); i++) {
            Road road = roadsInRange.get(i);
            road.speed = DatabaseParser.updateSpeed(road.ID);
        }
        /*
            重新计算
         */
        return getMinPosArray();
    }

    private static ArrayDeque<Position> getMinPath(Position endPosition) {
        Position pos = endPosition;
        minPath.add(endPosition);
        while (pre[getIndexById(pos.ID)] != myPositionInRoad) {
            minPath.addFirst(pre[getIndexById(pos.ID)]);
            pos = pre[getIndexById(pos.ID)];
        }

        if(minPath.size() >1) {
            if (isContains((Position) minPath.toArray()[0], (Position) minPath.toArray()[1], myPositionInRoad)) {
                minPath.removeFirst();
            }
        }

        return minPath;
    }

    private static boolean isContains(Position startPosition,Position endPosition,Position myPosition){
        Road basicRoad = null;
        for(Road road : roadsInRange){
            if(road.startPoint.equals(startPosition) && road.endPoint.equals(endPosition)){
                basicRoad = road;
            }else if(road.endPoint.equals(startPosition) && road.startPoint.equals(endPosition)){
                basicRoad = road;
            }
        }
        if(basicRoad != null) {
            for (Position position : basicRoad.polylines){
                if(position.equals(myPosition)){
                    return true;
                }
            }
        }
        return false;
    }


    private static double getRealTime(int nowIndex, Position nowPosition, Direction direction, Road road) {
        double calibratedValue = 0;
        switch (direction) {
            case LEFT:
                calibratedValue = TurnOffset.Left;
                break;
            case RIGHT:
                calibratedValue = TurnOffset.right;
                break;
            case STRAIGHT:
                calibratedValue = TurnOffset.straight;
                break;
        }
        double time = calibratedValue + getTimePassTheRoad(road);
        double offset = dist[nowIndex];
        double waitTime = TimeParser.getTimeLeft(getBranchByID(nowPosition, road), direction, (int) offset);
        time += waitTime;
        return time;
    }

    private static Branch getBranchByID(Position nowPosition, Road road) {
        Position theNextPosition = getTheOtherPosition(road, nowPosition);
        String ID = "BC" + nowPosition.ID.substring(3) + "_" + theNextPosition.ID.substring(3);
        for (Branch branch : crossRoadsInRange.get(getIndexById(nowPosition.ID)).getBranchGroup()) {
            if (branch.getID().equals(ID)) {
                return branch;
            }
        }
        return null;
    }

    private static double getTimePassTheRoad(Road road) {
        double timwBasic = road.roadLength / (road.speed / 3.6D);
        return timwBasic;
    }

    private static Position getTheOtherPosition(Road road, Position position) { //计算路的另一个顶点
        if (road.startPoint.equals(position)) {
            return road.endPoint;
        } else {
            return road.startPoint;
        }
    }

    private static int getIndexById(String Id) {
        for (CrossRoad crossRoad : crossRoadsInRange) {
            if (crossRoad.getID().equals(Id)) {
                return crossRoadsInRange.indexOf(crossRoad);
            }
        }
        return -1;
    }

    private static Position getPrePosition(Position position) {
        return pre[getIndexById(position.ID)];
    }

    private static Position getClosestPosition(Position Pos) {
        double minDistance = Double.POSITIVE_INFINITY;
        Position closestPosition = null;
        myPositionInRoads = getClosestRoad(Pos);
        double nowDistance;
        for (Position position : myPositionInRoads.polylines) {
            nowDistance = DistanceParser.getDistanceBetween(Pos, position);
            if (nowDistance < minDistance) {
                minDistance = nowDistance;
                closestPosition = position;
            }
        }
        return closestPosition;
    }

    public static Road getClosestRoad(Position Pos) {
        double distance = 10000;
        double minDistance = Double.POSITIVE_INFINITY;
        Road closestRoad = null;
        double nowDistance;
        for (Road road : roadsInRange) {
            if (DistanceParser.getDistanceBetween(road.startPoint, Pos) < distance || DistanceParser.getDistanceBetween(road.endPoint, Pos) < distance) {
                for (Position position : road.polylines) {
                    nowDistance = DistanceParser.getDistanceBetween(Pos, position);
                    if (nowDistance < minDistance) {
                        minDistance = nowDistance;
                        closestRoad = road;
                    }
                }
            }
        }

        return closestRoad;
    }

    private static Position getMyPositionInCrossRoad() {
        myPositionInRoads = getClosestRoad(myPosition);
        double distanceToStart = DistanceParser.getDistanceBetween(getClosestPosition(myPosition), myPositionInRoads.startPoint);
        double distanceToEnd = DistanceParser.getDistanceBetween(getClosestPosition(myPosition), myPositionInRoads.endPoint);
        return distanceToStart > distanceToEnd ? myPositionInRoads.endPoint : myPositionInRoads.startPoint;
    }

    private static Position getTargetPositionInCrossRoad() {
        Road road = getClosestRoad(targetPosition);
        double distanceToStart = DistanceParser.getDistanceBetween(getClosestPosition(targetPosition), road.startPoint);
        double distanceToEnd = DistanceParser.getDistanceBetween(getClosestPosition(targetPosition), road.endPoint);
        return distanceToStart > distanceToEnd ? road.endPoint : road.startPoint;
    }

    private static double getRelativeRad(Position basicStartPosition, Position basicEndposition, Position nextStartPosition, Position nextEndPosition) {          //返回两条路之间的相对弧度
        Point basicStartPoint = DrawMap.parsePosition(basicStartPosition);
        Point basicEndPoint = DrawMap.parsePosition(basicEndposition);
        Point nextStartPoint = DrawMap.parsePosition(nextStartPosition);
        Point nextEndPoint = DrawMap.parsePosition(nextEndPosition);
        double basicRad = computeRad(basicStartPoint, basicEndPoint);
        double nextRad = computeRad(nextStartPoint, nextEndPoint);
        if (nextRad - basicRad < 0.01 && nextRad - basicRad < -0.01) return 0;
        return nextRad - basicRad;
    }

    public static double computeRad(Point startPoint, Point endPoint)               //计算得出相对于图的象限偏移
    {
        //斜边长度
        double length = PointLength(endPoint, startPoint);
        //对边比斜边 sin
        double hudu = Math.asin(Math.abs(endPoint.y - startPoint.y) / length);
        double ag = hudu * 180 / Math.PI;
        //第一象限90-
        if ((startPoint.x - endPoint.x) <= 0 && (startPoint.y - endPoint.y) >= 0) ag = ag - 90;
            //第二象限90+
        else if ((startPoint.x - endPoint.x) >= 0 && (startPoint.y - endPoint.y) >= 0)
            ag = 90 - ag;
            //第三象限270-
        else if ((startPoint.x - endPoint.x) >= 0 && (startPoint.y - endPoint.y) <= 0)
            ag = ag - 180;
            //第四象限270+
        else if ((startPoint.x - endPoint.x) <= 0 && (startPoint.y - endPoint.y) <= 0)
            ag = -ag;
        return ag / 180 * Math.PI;
    }

    private static double PointLength(Point pa, Point pb) {
        return Math.sqrt(Math.pow((pa.x - pb.x), 2) + Math.pow((pa.y - pb.y), 2));
    }

    public static ArrayList<Road> getRoadsInPath(){
        RoadsInPath = new ArrayList<>();
        for(int i= 0 ;i < minPath.size() - 1;i++){
            Position p1 = (Position) minPath.toArray()[i];
            Position p2 = (Position) minPath.toArray()[i+1];
            for(Road road : roadsInRange){
                if(road.startPoint.equals(p1) && road.endPoint.equals(p2) || road.startPoint.equals(p2) && road.endPoint.equals(p1)){
                    RoadsInPath.add(road);
                }
            }
        }
        return RoadsInPath;
    }

    public static Road getMyPositionInRoads() {
        return myPositionInRoads;
    }

    //根据传入的交叉入口坐标获取所在road
    public static Road getRoad(Position startPoint , Position endPoint){
        for(Road road:roadsInRange){
            if(road.startPoint.equals(startPoint) && road.endPoint.equals(endPoint) || road.startPoint.equals(endPoint) && road.endPoint.equals(startPoint)){
                return road;
            }
        }
        return null;
    }

}
