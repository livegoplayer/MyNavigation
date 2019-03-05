package com.xjy.hsy.parsers;


import com.xjy.hsy.crossroad.Position;

public class DistanceParser {

    private static double EARTH_RADIUS = 6371.0;//km 地球半径 平均值，千米
    public static double getDistanceBetween(Position start , Position end){
        if(start == null || end == null ) return 0;
        return distance(start.xPos,start.yPos, end.xPos,end.yPos) * 1000;           //返回米为单位的
    }


    private static double HaverSin(double theta)
    {
        double v = Math.sin(theta / 2);
        return v * v;
    }


    /// <summary>
    /// 给定的经度1，纬度1；经度2，纬度2. 计算2个经纬度之间的距离。
    /// </summary>
    /// <param name="lat1">经度1</param>
    /// <param name="lon1">纬度1</param>
    /// <param name="lat2">经度2</param>
    /// <param name="lon2">纬度2</param>
    /// <returns>距离（公里、千米）</returns>
    private static double distance(double lat1,double lon1, double lat2,double lon2)
    {
        //用haversine公式计算球面两点间的距离。
        //经纬度转换成弧度
        lat1 = ConvertDegreesToRadians(lat1);
        lon1 = ConvertDegreesToRadians(lon1);
        lat2 = ConvertDegreesToRadians(lat2);
        lon2 = ConvertDegreesToRadians(lon2);

        //差值
        double vLon = Math.abs(lon1 - lon2);
        double vLat = Math.abs(lat1 - lat2);

        //h is the great circle distance in radians, great circle就是一个球体上的切面，它的圆心即是球心的一个周长最大的圆。
        double h = HaverSin(vLat) + Math.cos(lat1) * Math.cos(lat2) * HaverSin(vLon);

        double distance = 2 * EARTH_RADIUS * Math.asin(Math.sqrt(h));

        return distance;
    }

    /// <summary>
    /// 将角度换算为弧度。
    /// </summary>
    /// <param name="degrees">角度</param>
    /// <returns>弧度</returns>
    private static double ConvertDegreesToRadians(double degrees)
    {
        return degrees * Math.PI / 180;
    }

    private static double ConvertRadiansToDegrees(double radian)
    {
        return radian * 180.0 / Math.PI;
    }

}
