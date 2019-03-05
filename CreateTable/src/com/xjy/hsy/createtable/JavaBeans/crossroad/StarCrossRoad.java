package com.xjy.hsy.createtable.JavaBeans.crossroad;

import com.xjy.hsy.createtable.JavaBeans.Position;

import java.util.ArrayList;

//星型交叉路口
public class StarCrossRoad implements CrossRoad{
    private Position pos = new Position();
    private ArrayList<Branch> branchGroup = new ArrayList<>();
    private String ID = "";
    private int level = 0;

    public StarCrossRoad(Position pos, ArrayList<Branch> branchGroup) {
        this.pos = pos.clone();
        this.branchGroup = branchGroup;
    }

    public StarCrossRoad(Position pos, ArrayList<Branch> branchGroup, String ID, int level) {
        this.pos = pos;
        this.branchGroup = branchGroup;
        this.ID = ID;
        this.level = level;
    }

    @Override
    public Position getPos() {
        return pos;
    }

    @Override
    public void setPos(Position pos) {
        this.pos = pos;
    }

    @Override
    public ArrayList<Branch> getBranchGroup() {
        return branchGroup;
    }

    @Override
    public void setBranchGroup(ArrayList<Branch> branchGroup) {
        this.branchGroup = branchGroup;
    }

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public void setID(String ID) {
        this.ID = ID;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return "StarCrossRoad{" +
                "pos=" + pos +
                ", branchGroup=" + branchGroup +
                ", ID='" + ID + '\'' +
                ", level=" + level +
                '}'+"\n";
    }
}
