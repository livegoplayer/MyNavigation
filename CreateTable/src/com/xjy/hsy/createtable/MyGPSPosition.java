package com.xjy.hsy.createtable;

import com.xjy.hsy.createtable.JavaBeans.Position;

public abstract class MyGPSPosition {
    private static Position position =  new Position(0,0);

    public static Position getPosition() {
        return position;
    }

    public static Position movePosition(){
        return position;
    }


}
