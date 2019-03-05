package com.xjy.hsy.crossroad;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Position {
    //默认初始化为0.000000
    public String ID;
    public double xPos;
    public double yPos;
    public int count;        //用来辅助计算剩下需要连接的边的数量，用于路径的自动生成，他的初始值的String形式就是level变量，会在生成边的时候逐步减小，如果count = 0，说明该点的边已经生成结束

    //格式标准
    private static DecimalFormat format = new DecimalFormat("0.000000", new DecimalFormatSymbols(Locale.US));

    public double getxPos() {
        return xPos;
    }

    public void setxPos(double xPos) {
        this.xPos = xPos;
    }

    public Position(double xPos, double yPos) {   //标准构造方法
        if ((-180.0D <= yPos) && (yPos < 180.0D))
            this.yPos = parse(yPos);
        else
        {
            throw new IllegalArgumentException("the yPos range [-180, 180].");
            // this.yPos = parse(((yPos - 180.0D) % 360.0D + 360.0D) %
            // 360.0D - 180.0D);
        }

        if ((xPos < -90.0D) || (xPos > 90.0D))
        {
            throw new IllegalArgumentException("the xPos range [-90, 90].");
        }
        this.xPos = xPos;
    }

    public Position(String ID,double xPos, double yPos,int count){
        this(xPos,yPos);
        this.count = count;
        this.ID = ID;
    }

    public Position() { }       //用来替代Null值

    private static double parse(double d)   //规范化地址
    {
        return Double.parseDouble(format.format(d));
    }

    @Override
    public boolean equals(Object obj)          //比较大小
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Position other = (Position) obj;
        if (Double.doubleToLongBits(xPos) != Double.doubleToLongBits(other.xPos))
            return false;
        if (Double.doubleToLongBits(yPos) != Double.doubleToLongBits(other.yPos))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return ID + ":(" + xPos +
                ", " + yPos +
                ')' + "[" + count + "]";
    }

    public Position clone(){
        return new Position(this.ID,this.xPos,this.yPos,this.count);
    }
}
