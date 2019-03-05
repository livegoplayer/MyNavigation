package com.xjy.hsy.Topography;
import com.xjy.hsy.crossroad.Position;

import java.awt.*;
import java.util.ArrayList;

public class Topography {
    public String ID ;
    public ArrayList<Position> polines = new ArrayList<>();
    public Position centerPosition;
    public Type type;

    public Topography(String ID, Position centerPosition, Type type) {
        this.ID = ID;
        this.centerPosition = centerPosition;
        this.type = type;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public ArrayList<Position> getPolines() {

        return polines;
    }

    public void setPolines(ArrayList<Position> polines) {
        this.polines = polines;
    }

    public Position getCenterPosition() {
        return centerPosition;
    }

    public void setCenterPosition(Position centerPosition) {
        this.centerPosition = centerPosition;
    }

    public Color getColor(){
        switch (type){
            case GRASS:
                return TypeColor.GRASS_COLOR;
            case RIVER:
                return  TypeColor.RIVER_COLOR;
        }
        return TypeColor.GRASS_COLOR;
    }
}
