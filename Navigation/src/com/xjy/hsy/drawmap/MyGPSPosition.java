package com.xjy.hsy.drawmap;

import com.xjy.hsy.crossroad.Position;
import com.xjy.hsy.navigation.DrawNavigation;
import com.xjy.hsy.parsers.DistanceParser;
import com.xjy.hsy.road.Road;

public abstract class MyGPSPosition {
    private static Position position =  new Position(0,0);
    private static Position nextPosition ;
    public static Position getPosition() {
        return position;
    }
    private static boolean lock;

    public static void movePositonTo(Position nextPosition1){
        if(DistanceParser.getDistanceBetween(position,nextPosition) <= 30)lock = false;
        else {
            lock= true;
        }
        if(lock == false){
            nextPosition = nextPosition1;
        }
        Road road = DrawNavigation.getClosestRoad(position);
        double speed = 480 / 3.6;
        double time = DistanceParser.getDistanceBetween(position,nextPosition)/speed;
        double offset = speed*0.3;
        double rate = 0.3/time;

        if(rate!=Double.NaN) {
            position = new Position(((nextPosition.xPos - position.xPos) * rate + position.xPos), (nextPosition.yPos - position.yPos) * rate + position.yPos);
        }
    }

    public static Road getClosestRoad(Position Pos) {
        double distance = 10000;
        double minDistance = Double.POSITIVE_INFINITY;
        Road closestRoad = null;
        double nowDistance ;
        for(Road road:DrawNavigation.roadsInRange){
            if(DistanceParser.getDistanceBetween(road.startPoint,Pos) < distance || DistanceParser.getDistanceBetween(road.endPoint,Pos) < distance){
                for(Position position:road.polylines){
                    nowDistance = DistanceParser.getDistanceBetween(Pos,position);
                    if(nowDistance < minDistance){
                        minDistance = nowDistance;
                        closestRoad = road;
                    }
                }
            }
        }
        return closestRoad;
    }

    public static boolean isLock() {
        return lock;
    }

    public static void setLock(boolean lock) {
        MyGPSPosition.lock = lock;
    }
}
