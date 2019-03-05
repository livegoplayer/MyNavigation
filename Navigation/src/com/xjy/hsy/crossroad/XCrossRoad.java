package com.xjy.hsy.crossroad;

import java.util.ArrayList;

public class XCrossRoad implements CrossRoad{
    private Position pos = new Position();
    private ArrayList<Branch> branchGroup = new ArrayList<>();
    private String ID = "";
    private String name;

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public void setLevel(int level) {
        this.level = level;
    }

    public int level= 0;

    public XCrossRoad(Position pos, ArrayList<Branch> branchGroup) {
        this.pos = pos;
        this.branchGroup = branchGroup;
    }

    public XCrossRoad(Position pos, ArrayList<Branch> branchGroup, String ID, int level,String name) {
        this.pos = pos;
        this.branchGroup = branchGroup;
        this.ID = ID;
        this.level = level;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
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

    @Override
    public String toString() {
        return "XCrossRoad{" +
                "pos=" + pos +
                ", branchGroup=" + branchGroup +
                ", ID='" + ID + '\'' +
                ", level=" + level +
                '}'+"\n";
    }


}
