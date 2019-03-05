import com.xjy.hsy.createtable.AlterTable;
import com.xjy.hsy.createtable.CreateTable;
import com.xjy.hsy.createtable.JavaBeans.LogicRoad;
import com.xjy.hsy.createtable.JavaBeans.Position;
import com.xjy.hsy.createtable.JavaBeans.crossroad.Branch;
import com.xjy.hsy.createtable.MyGPSPosition;
import com.xjy.hsy.createtable.Parsers.DistanceParser;
import com.xjy.hsy.createtable.Parsers.databaseparser.DatabaseParser;

import java.io.PrintStream;
import java.sql.SQLOutput;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Level;

import static com.xjy.hsy.createtable.AlterTable.addRandomCrossroads;
import static com.xjy.hsy.createtable.AlterTable.crossRoads;
import static com.xjy.hsy.createtable.AlterTable.roads;

public class Main {
    public static void main(String args[]){
         //System.out.println(DistanceParser.getDistanceBetween(new Position(0,0),new Position(-0.01,0)));
        initTable();

    }

    private static void initTable(){
        CreateTable.clear("Hotspot");
        CreateTable.clear("CROSSROAD");
        CreateTable.clear("ROAD_SECTION");
        CreateTable.clear("ROAD_POLINES");
        CreateTable.clear("Topography");
        CreateTable.clear("TopographyPolines");
        CreateTable.clear("crossroad_branch");
//        CreateTable.creatTable();
        CreateTable.insertCrossRoadTable(500);
        CreateTable.insetRoadTable();
        CreateTable.insertPolinesTable();
        CreateTable.insertTopographyTable(50);
        CreateTable.insertHotSpotTable(500);
        DatabaseParser.fillRoadAndCrossRoad(new Position());
        CreateTable.insertBranchsTable();
    }
}
