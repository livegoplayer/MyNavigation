package com.xjy.hsy.parsers;


import com.xjy.hsy.crossroad.Branch;
import com.xjy.hsy.crossroad.Direction;

import java.util.Date;

public class TimeParser {
    public static int getTimeLeft(Branch branch, Direction direction, int offset){  //获得offset时间后需要等待的时间的时间,如果为正数，则无需等待
        long second = 0;

        switch(direction){
            case LEFT:
                if(branch.getIson_left() == 0){
                    second = Integer.MAX_VALUE;
                }else {
                    second = (new Date().getTime() / 1000 + offset - branch.getLEFT().getTime() / 1000) % branch.getPeriod(Direction.LEFT) - branch.getLEFT_TIME_GROUP().get(0);
                }
                break;
            case RIGHT:
                if(branch.getIson_right() == 0){
                    second = Integer.MAX_VALUE;
                }else {
                    second = (new Date().getTime() / 1000 + offset - branch.getRIGHT().getTime() / 1000) % branch.getPeriod(Direction.RIGHT) - branch.getRIGHT_TIME_GROUP().get(0);
                }
                break;
            case STRAIGHT:
                if(branch.getIson_right() == 0){
                    second = Integer.MAX_VALUE;
                }else {
                    second = (new Date().getTime() / 1000 + offset - branch.getSTRAIGHT().getTime() / 1000) % branch.getPeriod(Direction.STRAIGHT) - branch.getSTRAIGHT_TIME_GROUP().get(0);
                }
                break;
        }
        if(second < 0){
            second = -second;
        } else{
            second = 0;
        }
        return (int)second;
    }
}

