package com.xjy.hsy.createtable.JavaBeans.crossroad;

import com.xjy.hsy.createtable.JavaBeans.Position;

import java.util.ArrayList;

//该接口可以被扩展，以提供更多的交叉路口
public interface CrossRoad {

    //Position pos = new Position();
    //ArrayList<Branch> branchGroup = new ArrayList<>();
    //String ID = "";
    //private int level = 0;

    String getID();

    void setID(String ID);

    Position getPos();

    void setPos(Position pos);

    ArrayList<Branch> getBranchGroup();

    void setBranchGroup(ArrayList<Branch> branchGroup);

    int getLevel();

    void setLevel(int count);

}
