
import com.xjy.hsy.crossroad.Position;
import com.xjy.hsy.drawmap.DrawMap;


public class Main {
    public static void main(String[] args){
        DrawMap.initData(new Position(0,0));               //以0，0点为核心读取500公里的数据
        DrawMap.drawAll(new Position(0,0));             //以0，0点为中心设置初始绘图点，中心绘图点可以改变
        DrawMap.reflash.start();
        DrawMap.reflashRoads.start();
    }
}
