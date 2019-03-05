package com.xjy.hsy.createtable.JavaBeans;

import com.xjy.hsy.createtable.JavaBeans.road.Level;

public class LogicRoad {
    //存储保存在ArrayList中的边的Position序号的连接
    public String ID;
    public Position startPos;
    public Position endPos;
    public Level level;

    public LogicRoad(Position startPos, Position endPos) {
        this.startPos = startPos.clone();
        this.endPos = endPos.clone();

    }

    @Override
    public String toString() {
        return "LogicRoad{" +
                "startPos=" + startPos +
                ", endPos=" + endPos +
                '}' + "\n";
    }
}
