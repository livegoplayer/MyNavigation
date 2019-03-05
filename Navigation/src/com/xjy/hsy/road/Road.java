package com.xjy.hsy.road;

import com.xjy.hsy.crossroad.Position;
import com.xjy.hsy.crossroad.CrossRoad;

import java.awt.*;
import java.util.ArrayList;

//道路
/*  道路类型:
 *
 *  高速
 *  城市快速路、国道
 *  主要道路
 *  一般道路
*/

public class Road {
    public ArrayList<Position> polylines ;  //道路的轨迹数组
    public String roadName ;                //道路的名称
    public String ID;                       //路段的ID
    public double roadLength ;              //路段的长度

    //路段的其他信息
    public Position startPoint;    //起点
    public Position endPoint;       //终点
    public double speed;            //行车速度（指的是到该路上行驶的速度，并非当前统计的速度）
    public Level RoadLevel;         //道路的等级

    public Road(ArrayList<Position> polylines, String ID,String roadName, double roadLength, Position startPoint, Position enPoint, int speed, Level roadLevel) {
        this.polylines = polylines;
        this.roadName = roadName;
        this.ID = ID;
        this.roadLength = roadLength;
        this.startPoint = startPoint;
        this.endPoint = enPoint;
        this.speed = speed;
        RoadLevel = roadLevel;
    }

    @Override
    public String toString() {
        return "Road{" +
                //"polylines=" + polylines + polylines.size() + "\n" +
                //", roadName='" + roadName + '\'' +
                //", ID='" + ID + '\'' +
                //", roadLength=" + roadLength +
                ", startPoint=" + startPoint +
                ", enPoint=" + endPoint +
                ", speed=" + speed +
                ", RoadLevel=" + RoadLevel +
                '}'+ "\n";
    }

    public Color getRoadColor(){
        switch (RoadLevel){
            case EXPY_ROAD:
                return RoadColor.EXPY_ROAD_COLOR;
            case NATIONAL_ROAD:
                return RoadColor.NATIONAL_ROAD_COLOR;
            case MAIN_ROAD:
                return RoadColor.MAIN_ROAD_COLOR;
            case OTHER_ROADS:
                return RoadColor.OTHER_ROAD_COLOR;
        }
        return null;
    }
    public double getRoadWidth(){
        switch (RoadLevel){
            case EXPY_ROAD:
                return 60;
            case NATIONAL_ROAD:
                return 40;
            case MAIN_ROAD:
                return 20;
            case OTHER_ROADS:
                return 10;
        }
        return 5;
    }


}
