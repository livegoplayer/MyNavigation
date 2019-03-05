package com.xjy.hsy.createtable.JavaBeans.hotspot;

import com.xjy.hsy.createtable.JavaBeans.Position;

import java.awt.*;

public class HotSpot {
    public Position position;
    public String ID;
    public HotType hotType;

    public HotSpot(Position position, String ID, HotType hotType) {
        this.position = position;
        this.ID = ID;
        this.hotType = hotType;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public HotType getHotType() {
        return hotType;
    }

    public void setHotType(HotType hotType) {
        this.hotType = hotType;
    }

    public String getPic(){
        switch (hotType){
            case FOOD:
                return HotPics.FOOD;
            case ENJOY:
                return HotPics.ENJOY;
            case SHOPPING:
                return HotPics.SHOPPING;
            case SLEEP:
                return HotPics.SLEEP;
            case TRAVEL:
                return HotPics.TRAVEL;
            case MEDICAL:
                return HotPics.MEDICAL;
            case STATION:
                return HotPics.STATION;
            case MOUNTAIN:
                return HotPics.MOUNYAIN;
            case GOVERMENT:
                return HotPics.GOVERMENT;
        }
        return HotPics.SHOPPING;

    }
}
