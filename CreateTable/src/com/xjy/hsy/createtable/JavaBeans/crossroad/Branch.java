package com.xjy.hsy.createtable.JavaBeans.crossroad;

import java.util.ArrayList;
import java.util.Date;

public class Branch {
    private Date LEFT;   //第一轮周期开始的时间
    private Date RIGHT;
    private Date STRAIGHT;

    private ArrayList<Integer> LEFT_TIME_GROUP;     //保存三个数字，依次为绿，黄，红灯的持续时间
    private ArrayList<Integer> RIGHT_TIME_GROUP;
    private ArrayList<Integer> STRAIGHT_TIME_GROUP;

    public Branch(Date LEFT, Date RIGHT, Date STRAIGHT, ArrayList<Integer> LEFT_TIME_GROUP, ArrayList<Integer> RIGHT_TIME_GROUP, ArrayList<Integer> STRAIGHT_TIME_GROUP) {
        this.LEFT = (Date) LEFT.clone();
        this.RIGHT = (Date) RIGHT.clone();
        this.STRAIGHT = (Date) STRAIGHT.clone();
        this.LEFT_TIME_GROUP = copyOf(LEFT_TIME_GROUP);
        this.RIGHT_TIME_GROUP = copyOf(RIGHT_TIME_GROUP);
        this.STRAIGHT_TIME_GROUP = copyOf(STRAIGHT_TIME_GROUP);
    }

    //getter和setter方法

    public Date getLEFT() {
        return LEFT;
    }

    public void setLEFT(Date LEFT) {
        this.LEFT = LEFT;
    }

    public Date getRIGHT() {
        return RIGHT;
    }

    public void setRIGHT(Date RIGHT) {
        this.RIGHT = RIGHT;
    }

    public Date getSTRAIGHT() {
        return STRAIGHT;
    }

    public void setSTRAIGHT(Date STRAIGHT) {
        this.STRAIGHT = STRAIGHT;
    }

    public ArrayList<Integer> getLEFT_TIME_GROUP() {
        return LEFT_TIME_GROUP;
    }

    public void setLEFT_TIME_GROUP(ArrayList<Integer> LEFT_TIME_GROUP) {
        this.LEFT_TIME_GROUP = LEFT_TIME_GROUP;
    }

    public ArrayList<Integer> getRIGHT_TIME_GROUP() {
        return RIGHT_TIME_GROUP;
    }

    public void setRIGHT_TIME_GROUP(ArrayList<Integer> RIGHT_TIME_GROUP) {
        this.RIGHT_TIME_GROUP = RIGHT_TIME_GROUP;
    }

    public ArrayList<Integer> getSTRAIGHT_TIME_GROUP() {
        return STRAIGHT_TIME_GROUP;
    }

    public void setSTRAIGHT_TIME_GROUP(ArrayList<Integer> STRAIGHT_TIME_GROUP) {
        this.STRAIGHT_TIME_GROUP = STRAIGHT_TIME_GROUP;
    }

    //新增的方法
    //得到指定方向灯亮的周期
    public int getPeriod(Direction direction){
        switch(direction) {
            case LEFT:
                return LEFT_TIME_GROUP.get(0) + LEFT_TIME_GROUP.get(1) + LEFT_TIME_GROUP.get(2);
            case RIGHT:
                return RIGHT_TIME_GROUP.get(0) + RIGHT_TIME_GROUP.get(1) + RIGHT_TIME_GROUP.get(2);
            case STRAIGHT:
                return  STRAIGHT_TIME_GROUP.get(0) + STRAIGHT_TIME_GROUP.get(1) + RIGHT_TIME_GROUP.get(2);
        }
        return 0;
    };

    //得到一个周期红灯和黄灯的总时间
    public int getRedTime(Direction direction){
        switch(direction) {
            case LEFT:
                return LEFT_TIME_GROUP.get(0) + LEFT_TIME_GROUP.get(2);
            case RIGHT:
                return RIGHT_TIME_GROUP.get(0)  + RIGHT_TIME_GROUP.get(2);
            case STRAIGHT:
                return  STRAIGHT_TIME_GROUP.get(0) + RIGHT_TIME_GROUP.get(2);
        }
        return 0;
    }

    private ArrayList<Integer> copyOf(ArrayList<Integer> list){
        ArrayList<Integer> copyList = new ArrayList<>();
        for(int x : list){
            copyList.add(x);
        }
        return copyList;
    }
}
