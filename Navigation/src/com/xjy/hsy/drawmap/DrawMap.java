package com.xjy.hsy.drawmap;

import com.xjy.hsy.Topography.Topography;
import com.xjy.hsy.crossroad.CrossRoad;
import com.xjy.hsy.crossroad.Position;
import com.xjy.hsy.hotspot.HotSpot;
import com.xjy.hsy.navigation.DrawNavigation;
import com.xjy.hsy.navigation.NavigationColor;
import com.xjy.hsy.navigation.NavigationIcon;
import com.xjy.hsy.parsers.DistanceParser;
import com.xjy.hsy.parsers.databaseparser.DatabaseParser;
import com.xjy.hsy.road.Road;
import com.xjy.hsy.road.RoadColor;

import javax.imageio.ImageIO;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;

public class DrawMap {
    private static ArrayList<CrossRoad> MapPoint;
    private static ArrayList<Road> MapLines;
    private static ArrayList<Topography> MapArea;
    private static ArrayList<HotSpot> MapHotSpots;
    private static Position centerPosition = new Position();
    private static int staff = 200;
    private static JFrame jFrame;
    private static double rad = 0;
    private static ArrayDeque<Position> NavigationLine =new ArrayDeque<>();
    private static RoadCanvas roadCanvas;
    public static Timer reflash;
    public static Timer reflashRoads;
    private static Position searchPsoition;
    private static JLabel Inf = new JLabel();
    /*
        绘制变量
     */
    private static boolean drawNavigation;
    /*
        提示信息变量
     */
    private static JPanel jset;
    private static JPanel jshow;
    private static boolean inited;
    private static Box InformationBox;
    private static Box LoacationBox;
    private static JLabel Information = new JLabel();   //用来提示用户下一段路怎么走
    private static JLabel RoadPathNames = new JLabel();  //途经路段ID
    private static ArrayList<String> PATHIDS = new ArrayList<>();  //途径roadID
    private static ArrayList<Road> PATHROADSPRE = new ArrayList<>();  //前置ROAD
    private static ArrayList<Road> PATHROADS = new ArrayList<>();  //途径RoaD
    private static boolean change = false;          //导航路径是否修改
    private static String changeStr = null;         //提示信息
    /*
        控制路段速度
     */
    public static boolean drawAll(Position position){

        /*
            用来改变位置的timer
       */
        Timer timerForUpdatePosition = new Timer(300,e-> {
            if (drawNavigation  && NavigationLine.size() >= 1){
                Position next = (Position) NavigationLine.toArray()[0];
                //if(next.equals(MyGPSPosition.getPosition())) next = (Position) NavigationLine.toArray()[1];
                MyGPSPosition.movePositonTo(next);
                centerPosition = MyGPSPosition.getPosition();
            }
        });
        setCenterPosition(position);
        /*读取表格中的数据,写在initData中*/

        /*新建一个绘图框架*/
        jFrame = frameInit(new JFrame("Navigation"));
        roadCanvas = new RoadCanvas();
        JPanel jsetContainer = new JPanel();


        jset = new JPanel();
        jset.setLayout(new BoxLayout(jset,BoxLayout.Y_AXIS));
        jsetContainer.add(jset);
        jset.add(new JPanel());           //添加这个是为了填充下面的空白
        /*
            比例尺
         */
        Box rateBox = Box.createHorizontalBox();
        JLabel rateLable = new JLabel("比例尺：");
        JSpinner jSpinner1 = new JSpinner(new SpinnerNumberModel(staff,200,10000,100));
        rateBox.add(rateLable);
        rateBox.add(jSpinner1);
        jset.add(rateBox);
        /*
            旋转角度调整
         */
        Box radBox = Box.createHorizontalBox();
        JLabel radLabel = new JLabel("旋转角度：");
        JSpinner jSpinner2 = new JSpinner(new SpinnerNumberModel((int)(rad / Math.PI * 180), 0 ,360, 5));     //旋转角度，每5度一个刻度
        radBox.add(radLabel);
        radBox.add(jSpinner2);
        jset.add(radBox);
        JPanel jPanelBlank = new JPanel();
        jPanelBlank.setSize(new Dimension(100,800));
        /*
            输入查询的地点
         */
        Box searchBox = Box.createHorizontalBox();
        JLabel searchLabel = new JLabel("查询");
        JTextField search = new JTextField(5);
        JButton searchConfirm = new JButton("确认");
        searchBox.add(searchLabel);
        searchBox.add(search);
        searchBox.add(searchConfirm);
        jset.add(searchBox);
        searchConfirm.addActionListener(e->{
            Position searchResault = searchHotSpot(search.getText());
            if(searchResault != null){
                setSearchPosition(searchResault);
            }
        });
        /*
            输入导航的终点
         */
        Box targetBox = Box.createHorizontalBox();
        JLabel targetLabel = new JLabel("目的地");
        JTextField target = new JTextField(5);
        JButton targetConfirm = new JButton("确认");
        JButton targetCannel = new JButton("取消");
        targetBox.add(targetLabel);
        targetBox.add(target);
        targetBox.add(targetConfirm);
        targetBox.add(targetCannel);
        jset.add(targetBox);
        targetConfirm.addActionListener(e->{
            Position searchResault = searchHotSpot(target.getText());
            drawNavigation = false;
            MyGPSPosition.setLock(false);
            InformationBox.removeAll();
            LoacationBox.remove(1);
            LoacationBox.add(new Label());
            jset.repaint();
            jshow.repaint();
            Inf.setText("");
            NavigationLine = new ArrayDeque<>();
            if(searchResault != null && DistanceParser.getDistanceBetween(MyGPSPosition.getPosition(),searchResault) > 30){
                setTargetPosition(searchResault);
                inited = false;
                jset.repaint();
                jshow.repaint();
            }
            timerForUpdatePosition.start();
        });

        targetCannel.addActionListener(e -> {
            drawNavigation = false;
            MyGPSPosition.setLock(false);
            InformationBox.removeAll();
            LoacationBox.remove(1);
            LoacationBox.add(new Label());
            jset.repaint();
            jshow.repaint();
            NavigationLine = new ArrayDeque<>();
        });
        /*
            结束导航
         */

        jshow = new JPanel();
        LoacationBox = Box.createVerticalBox();
        jshow.add(LoacationBox);

        JLabel jLabelPosition = new JLabel("绘图中心点：" + getCenterPosition().xPos + "," + getCenterPosition().yPos + "\n" +
                "你的位置： " + MyGPSPosition.getPosition().xPos + "," + MyGPSPosition.getPosition().yPos);
        LoacationBox.add(jLabelPosition);
        LoacationBox.add(new JLabel());
        JSplitPane jSplitPaneV = new JSplitPane(JSplitPane.VERTICAL_SPLIT,roadCanvas,jshow);
        jSplitPaneV.setDividerLocation(600);
        JSplitPane jSplitPaneH = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,jSplitPaneV,jsetContainer);
        jSplitPaneH.setDividerLocation(964);
        jFrame.add(jSplitPaneH);
        /*
            添加导航终点
         */

        /*
            添加更新用的Timer
         */
        Timer timer = new Timer(300,e->{
            jSpinner1.setValue(staff);
            jSpinner2.setValue((int)(rad / Math.PI * 180));
            jLabelPosition.setText("绘图中心点：" + getCenterPosition().xPos + "," + getCenterPosition().yPos + " " +
                    "你的位置： " + MyGPSPosition.getPosition().xPos + "," + MyGPSPosition.getPosition().yPos);
        });
        timer.start();

        reflash = new Timer(299,e-> {
            roadCanvas.repaint();
            InformationBox.repaint();
        });

        reflashRoads = new Timer(1000, e->{
            refleshPathRoads();
        });

        /*
        添加监听器
         */
        jSpinner1.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                setStaff((int)jSpinner1.getValue());
                roadCanvas.repaint();
            }
        });

        jSpinner2.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int degree = (int)jSpinner2.getValue();
                setRad(degree/ 180D * Math.PI);
                roadCanvas.repaint();
            }
        });
        /*
            提示信息
         */
        InformationBox = Box.createVerticalBox();
        InformationBox.add(Information);
        InformationBox.add(RoadPathNames);
        jset.add(InformationBox);
        return true;
    }

    private static void setSearchPosition(Position searchResault) {
        searchPsoition = searchResault;
        drawNavigation = false;
    }

    private static Position searchHotSpot(String text) {
        for(HotSpot hotSpot: MapHotSpots){
            if(hotSpot.name.equals(text.trim())){
                return hotSpot.position;
            }
        }
        for(CrossRoad crossRoad:MapPoint){
            if(crossRoad.getName().equals(text.trim())){
                return crossRoad.getPos();
            }
        }
        return null;
    }

    public static void initNavigationList(Position targetPosition){
        DrawNavigation.setMyPosition(MyGPSPosition.getPosition());
        DrawNavigation.setTargetPosition(targetPosition);
        DrawNavigation.initData();
        NavigationLine = DrawNavigation.getMinPosArray();
    }

    public static ArrayDeque updateNavigationList(){
        return DrawNavigation.getMinPosArray();
    }

    public static boolean initData(Position position){
        centerPosition = position;
        MapLines = DatabaseParser.RoadParse(centerPosition);
        MapPoint = DatabaseParser.crossRoadsParse(centerPosition);
        MapArea = DatabaseParser.TopographyParse(centerPosition);
        MapHotSpots = DatabaseParser.HotSpotParse(centerPosition);
        return true;
    }

    public static Position getCenterPosition() {
        return centerPosition;
    }

    public static void setCenterPosition(Position centerPosition) {
        DrawMap.centerPosition = centerPosition;
    }

    private static ArrayList<Road> getRoadsToDraw(Position centerPosition){
        double maxLength = getDrawRange();
        ArrayList<Road> roadInRange = new ArrayList<>();
        for (Road road:MapLines ) {
            if(DistanceParser.getDistanceBetween(road.startPoint,centerPosition) < maxLength || DistanceParser.getDistanceBetween(road.endPoint,centerPosition) < maxLength){
                roadInRange.add(road);
            }
        }
        return roadInRange;
    }

    private static ArrayList<CrossRoad> getCrossRoadToDraw(Position centerPosition){
        double maxLength = getDrawRange();
        ArrayList<CrossRoad> crossoadInRange = new ArrayList<>();
        for (CrossRoad crossRoad :MapPoint) {
            if(DistanceParser.getDistanceBetween(crossRoad.getPos() ,centerPosition) < maxLength ){
                crossoadInRange.add(crossRoad);
            }
        }
        return crossoadInRange;
    }

    private static double getDrawRange(){                                                   //获得该窗口的对角线长度（cm）/2
        double max = Math.sqrt(Math.pow(jFrame.getHeight(),2) + Math.pow(jFrame.getWidth(),2)) * 2;           //得到最大像素
        return max / getRate() * staff /2;                                                                   //得到绘制范围
    }

    public static double getRate(){
        int dpi = Toolkit.getDefaultToolkit().getScreenResolution();                                      //得到dpi，也就是一英寸多少像素
        double rate = dpi /2.54;                                                                          //得到一厘米多少像素
        return rate;
    }

    public static JFrame frameInit(JFrame j){
        j.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        /*设置初始化尺寸*/
        j.setPreferredSize(new Dimension(1024,685));
        j.setVisible(true);
        j.pack();
        return j;
    }

    public static Point getCenterInCanvas(){
        Dimension border = jFrame.getSize();
        Point centerPoint = new Point((int)(border.getWidth()/2),(int)(border.getHeight()/2));         //获取屏幕的中心点
        return centerPoint;
    }

    private static ArrayList<HotSpot> getHotSpotToDraw(Position centerPosition) {
        double maxLength = getDrawRange();
        ArrayList<HotSpot> hotSpotInRange = new ArrayList<>();
        for (HotSpot hotSpot :MapHotSpots) {
            if(DistanceParser.getDistanceBetween(hotSpot.getPosition() ,centerPosition) < maxLength ){
                hotSpotInRange.add(hotSpot);
            }
        }
        return hotSpotInRange;
    }

    private static boolean drawTopographiesPolines(Topography topography, Graphics2D g) {
        g.setColor(topography.getColor());
        int[] xPos = new int[topography.polines.size()];
        int[] yPos = new int[topography.polines.size()];
        for(int i = 0; i < topography.polines.size(); i++){
            Point point = parsePosition(topography.polines.get(i));
            xPos[i] = point.x;
            yPos[i] = point.y;
        }
        g.fillPolygon(xPos,yPos,xPos.length);
        return true;
    }

    private static ArrayList<Topography> getTopographiesToDraw(Position centerPosition){
        double maxLength = getDrawRange();
        ArrayList<Topography> topographiesInRange = new ArrayList<>();
        for (Topography topography :MapArea) {
            if(DistanceParser.getDistanceBetween(topography.getCenterPosition() ,centerPosition) < maxLength ){
                topographiesInRange.add(topography);
            }
        }
        return topographiesInRange;
    }

    public static Point parsePosition(Position position){
        Position tmpPosition = new Position(position.xPos,centerPosition.yPos);                  //这个点用来辅助计算绘制位置的x和y
        double xDistance = DistanceParser.getDistanceBetween(tmpPosition,centerPosition)/staff * getRate();
        double yDistance = DistanceParser.getDistanceBetween(tmpPosition,position)/staff * getRate();
        if(position.xPos < centerPosition.xPos) xDistance = -xDistance;
        if(position.yPos < centerPosition.yPos) yDistance = -yDistance;
        double x = getCenterInCanvas().x + xDistance;
        double y = getCenterInCanvas().y - yDistance;
        Point point = new Point((int)x,(int)y);
        return getRotationPoint(point);
    }

    private static boolean drawPolines(Road road,Graphics2D g){
        //Point startPoint = parsePosition(road.startPoint);
        //Point endPoint = parsePosition(road.endPoint);
        g.setColor(road.getRoadColor());
        BasicStroke basicStroke = new BasicStroke((float) (road.getRoadWidth()/staff * getRate()));
        g.setStroke(basicStroke);
        int[] xPos = new int[road.polylines.size()];
        int[] yPos = new int[road.polylines.size()];
        for(int i = 0; i < road.polylines.size(); i++){
            Point point = parsePosition(road.polylines.get(i));
            xPos[i] = point.x;
            yPos[i] = point.y;
        }
        g.drawPolyline(xPos,yPos,xPos.length);
        return true;
    }

    private static Point getClosestPoint(Position start,ArrayList<Position> positions){
        double closestLength = Double.POSITIVE_INFINITY;
        Position closestPosition = null;
        for(Position position : positions){
            if(DistanceParser.getDistanceBetween(position,start) < closestLength && position.count!=1){
                closestPosition = position;
                closestLength = DistanceParser.getDistanceBetween(position,start);
            }
        }
        if (closestPosition != null){
            closestPosition.count=1;
            return parsePosition(closestPosition);
        }else return null;
    }

    public static Point getRotationPoint(Point point){                 //围绕原点旋转一个Point
        point.x -= getCenterInCanvas().x;
        point.y -= getCenterInCanvas().y;
        double x = point.x * Math.cos(rad) - point.y * Math.sin(rad);
        double y = point.x * Math.sin(rad) + point.y * Math.cos(rad);
        return new Point((int)x + getCenterInCanvas().x,(int)y + getCenterInCanvas().y);
    }

    public static int getStaff() {
        return staff;
    }

    public static void setStaff(int staff) {
        DrawMap.staff = staff;
    }

    public static double getRad() {
        return rad;
    }

    public static void setRad(double rad) {
        DrawMap.rad = rad%(2*Math.PI);
    }

    private static ArrayList<Position> getCopyOf(ArrayList<Position> positions){
        ArrayList<Position> positionsCopy = new ArrayList<>();
        for (Position position: positions) {
            positionsCopy.add(position.clone());
        }
        return positionsCopy;
    }

    public static Position getRoadCenter(Road road){
        Position startPosition = road.polylines.get( road.polylines.size()/2 );
        Position endPosition = road.polylines.get( road.polylines.size()/2 + 1 );
        return new Position((startPosition.xPos + endPosition.xPos)/2 , (startPosition.yPos + endPosition.yPos )/2 );
    }

    public static ArrayList<CrossRoad> getMapPoint() {
        return MapPoint;
    }

    public static ArrayList<Road> getMapLines() {
        return MapLines;
    }

    public static ArrayList<HotSpot> getMapHotSpots() {
        return MapHotSpots;
    }

    public static void setTargetPosition(Position position){
        DrawNavigation.setTargetPosition(position);
        DrawNavigation.initData();
        drawNavigation = true;
    }
    public static class RoadCanvas extends JPanel
    {
        public RoadCanvas(){
            setPreferredSize(new Dimension(1024,685));
        };
        public void paint(Graphics g){

            if(drawNavigation == true) {
                centerPosition = MyGPSPosition.getPosition();
            }
            Graphics2D g_2D = (Graphics2D)g;
            g_2D.setBackground(RoadColor.BACKGROUND);
            g_2D.clearRect(0,0,2000,2000);
            /*绘制地形*/
            ArrayList<Topography> topographiesInRange = getTopographiesToDraw(centerPosition);

            for(Topography topography:topographiesInRange){
                drawTopographiesPolines(topography,g_2D);
                Point point = parsePosition(topography.centerPosition);
                g_2D.drawString(topography.ID,point.x,point.y);
            }

            /*绘制道路*/
            ArrayList<Road> roadInRange = getRoadsToDraw(centerPosition);

            for(Road road : roadInRange){
                drawPolines(road,g_2D);
                Point point = parsePosition(getRoadCenter(road));
                g_2D.drawString(road.roadName,point.x,point.y);
            }
            /*
                绘制红绿灯
             */
            ArrayList<CrossRoad> crossRoadInRange = getCrossRoadToDraw(centerPosition);
            try{
                BufferedImage streetLights = ImageIO.read(new File("G:\\毕业设计\\Navigation\\src\\com\\xjy\\hsy\\drawmap\\StreetLights.png"));
                for(CrossRoad crossRoad : crossRoadInRange){
                    Point point = parsePosition(crossRoad.getPos());
                    g_2D.drawImage(streetLights,point.x,point.y,15,30,null);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            /*
                绘制热点
             */
            ArrayList<HotSpot> hotSpotsInRange = getHotSpotToDraw(centerPosition);
            try{
                for(HotSpot hotSpot : hotSpotsInRange){
                    BufferedImage hotspotImage = ImageIO.read(new File(hotSpot.getPic()));
                    Point point = parsePosition(hotSpot.position);
                    g_2D.drawImage(hotspotImage,point.x,point.y,30,30,null);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            /*
                绘制导航和定位
             */
            String changeID;
            if(drawNavigation == true) {
                if(NavigationLine.size() < 1 || NavigationLine ==null){
                    NavigationLine = DrawNavigation.updateByTime();
                }else if(DistanceParser.getDistanceBetween(getCenterPosition(),NavigationLine.getFirst()) % 500 == 0 || DistanceParser.getDistanceBetween(getCenterPosition(),NavigationLine.getFirst()) <= 50) {
                    NavigationLine = DrawNavigation.updateByTime();
                }else if((changeID = searchSpeed())!= null){                //判断该路径中是否有拥堵的路段
                    changeStr = changeID;
                    Inf.setText("检测到" + changeStr + "路段较为拥挤,已为您选择最佳路径" );
                    NavigationLine = DrawNavigation.updateByTime();
                }
                try{
                    BufferedImage myLocationICO = ImageIO.read(new File(NavigationIcon.MYNAVIGATIONPOSITION));
                    BufferedImage targetLocation = ImageIO.read(new File(NavigationIcon.TARGETPOSITION));
                    Point myLocationPosition = parsePosition(MyGPSPosition.getPosition());
                    Point targetPosition = parsePosition(DrawNavigation.getTargetPosition());
                    drawNavigationLines(g_2D);
                    g_2D.drawImage(myLocationICO,myLocationPosition.x - 15,myLocationPosition.y - 15 ,30,30,null);
                    g_2D.drawImage(targetLocation,targetPosition.x - 15 ,targetPosition.y - 15 ,30,30,null);
                    rad = getnextRad();
                    if(DistanceParser.getDistanceBetween(centerPosition,DrawNavigation.getClosestTargetPosition()) <= 10){
                        drawNavigation = false;
                        Information.setText("到达目的地附近");
                    }else{
                        double distance = DistanceParser.getDistanceBetween(centerPosition,NavigationLine.getFirst());
                        if(NavigationLine.size() > 1) {
                            double nextrad = DrawNavigation.computeRad(parsePosition((Position) NavigationLine.toArray()[0]), parsePosition((Position) NavigationLine.toArray()[1]));
                            String direction = null;
                            if (nextrad > 0) {
                                direction = "左拐";
                            } else if (nextrad < 0) {
                                direction = "右拐";
                            } else {
                                direction = "直行";
                            }
                            Information.setText(distance + "米后" + direction);
                        }else{
                            Information.setText(distance + "米后到达目的地");
                        }
                    }
                    if(drawNavigation) {
                        change = comparePATH();
                        if(!inited) {
                            initInformationBox();
                        }
                        else if(change) {
                            initInformationBox();
                        }
                    }else{
                        InformationBox.removeAll();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }else{
                try{
                    BufferedImage myLocationICO = ImageIO.read(new File(NavigationIcon.MYLOCATION));
                    BufferedImage searchLocation = ImageIO.read(new File(NavigationIcon.LOCATION));
                    Point myLocationPosition = parsePosition(MyGPSPosition.getPosition());
                    g_2D.drawImage(myLocationICO,myLocationPosition.x - 15,myLocationPosition.y - 15 ,30,30,null);
                    if(searchPsoition != null){
                        Point position = parsePosition(searchPsoition);
                        setCenterPosition(searchPsoition);
                        g_2D.drawImage(searchLocation,position.x - 15 ,position.y - 15 ,30,30,null);
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }

        private String searchSpeed() {
            for(Road road:PATHROADS){
                if(road.speed < 20){
                    return road.ID;
                }
            }
            return null;
        }

        private boolean comparePATH() {
            if(PATHROADS.size() != PATHROADSPRE.size())return false;
            for(int i = 0;i < PATHROADS.size();i++ ){
                if(!PATHROADSPRE.get(i).ID.equals(PATHROADS.get(i).ID)){
                    return false;
                }
            }
            return true;
        }

        private static String getPathsString(){
            PATHIDS = new ArrayList<>();
            for(Road road : PATHROADS){
                PATHIDS.add(road.ID);
            }

            StringBuffer str =new StringBuffer();

            for(String s:PATHIDS) {
                str.append(s);
                str.append("->");
            }
            str.append("目的地");
            return str.toString();
        }

        private static void createSpinners(int num) {
            InformationBox.removeAll();
            LoacationBox.remove(1);
            LoacationBox.add(RoadPathNames);
            InformationBox.add(Information);

            if(num >  7){
                num = 7;
            }
            for(int i = 0;i < num ; i++){
                Box box =  Box.createHorizontalBox();
//                JLabel jLabel = new JLabel(PATHROADS.get(i).ID);
                String ID  = PATHROADS.get(i).ID;
                int speed  = DatabaseParser.getSpeed(ID);
                JSpinner jSpinner = new JSpinner(new SpinnerNumberModel(speed, 10 ,60, 5));
//                box.add(jLabel);
                box.add(jSpinner);
                InformationBox.add(box);
                jSpinner.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        DatabaseParser.insertSpeed(ID,(int)jSpinner.getValue());
                        copyOfPATHROADS();
                        setPathRoads();
                    }
                });
            }
            InformationBox.add(Inf);
        }

        private static void copyOfPATHROADS(){
            PATHROADSPRE = new ArrayList<>();
            for(Road road:PATHROADS){
                PATHROADSPRE.add(road);
            }
        }

        private static void initInformationBox(){
            setPathRoads();
            RoadPathNames.setText(getPathsString());
            int num = PATHROADS.size();
            createSpinners(num);
        }

        private double getnextRad() {
            return DrawNavigation.computeRad(parsePosition(centerPosition),parsePosition((Position)NavigationLine.toArray()[0])) + rad;
        }

        private void drawNavigationLines(Graphics2D g_2D) {
            //绘制起点路线
            BasicStroke basicStroke = new BasicStroke((float) (60/staff * getRate()));
            g_2D.setColor(NavigationColor.navigationColor);
            g_2D.setStroke(basicStroke);

            ArrayList<Integer> xPos = new ArrayList<>();
            ArrayList<Integer> yPos = new ArrayList<>();

            Position MyPositionInCrossroad = DrawNavigation.getMyPositionInCrossroad();
            DrawMap.setPathRoads();
            //保存相对偏移方向
            int offsetx;
            int offsety;

            if(DistanceParser.getDistanceBetween(MyPositionInCrossroad,NavigationLine.getFirst()) > DistanceParser.getDistanceBetween(centerPosition,NavigationLine.getFirst())) {
             //添加第一个点
                //获取当前路段
                Road myRoad = getRoad(MyPositionInCrossroad,NavigationLine.getFirst());
                xPos.add(parsePosition(centerPosition).x);
                yPos.add(parsePosition(centerPosition).y);
                //添加其他点
                //初始化偏移值
                Position nextPosition = NavigationLine.getFirst();
                offsetx = (nextPosition.xPos - MyGPSPosition.getPosition().xPos >= 0 ? 1 : -1);
                offsety = (nextPosition.yPos - MyGPSPosition.getPosition().yPos >= 0 ? 1 : -1);
                //根据偏移值绘制

                //判定遍历方向
                //如果方向反了，需要往startposition走
//                System.out.print(myRoad.startPoint);
//                System.out.print(myRoad.endPoint);
//                System.out.println(nextPosition);
                if(myRoad.startPoint.equals(nextPosition)){
                    for(int i = myRoad.polylines.size() - 1 ; i>=0 ;i--){
//                        System.out.print(offsetx * (nextPosition.xPos - myRoad.polylines.get(i).xPos) >= 0);
//                        System.out.print(offsety+" ");
//                        System.out.print(nextPosition.yPos + " ");
//                        System.out.print(myRoad.polylines.get(i).yPos + " ");
//                        System.out.println(nextPosition.yPos - myRoad.polylines.get(i).yPos);
                        if(offsetx * (myRoad.polylines.get(i).xPos - MyGPSPosition.getPosition().xPos) >= 0 && offsety * (myRoad.polylines.get(i).yPos - MyGPSPosition.getPosition().yPos) >= 0){
                            xPos.add(parsePosition(myRoad.polylines.get(i)).x);
                            yPos.add(parsePosition(myRoad.polylines.get(i)).y);
                        }
                    }
                }else if(myRoad.endPoint.equals(nextPosition)){  //如果方向正确
                    for(int i = 0 ; i< myRoad.polylines.size() ;i++){
                        if(offsetx * (myRoad.polylines.get(i).xPos - MyGPSPosition.getPosition().xPos) >= 0 && offsety * (myRoad.polylines.get(i).yPos - MyGPSPosition.getPosition().yPos) >= 0){
                            xPos.add(parsePosition(myRoad.polylines.get(i)).x);
                            yPos.add(parsePosition(myRoad.polylines.get(i)).y);
                        }
                    }
                }
            }else{
                //获取当前路段
                Road myRoad = DrawNavigation.getMyPositionInRoads();
                //初始化下一步方向
                Position nextPosition = MyPositionInCrossroad;
                offsetx =(nextPosition.xPos - MyGPSPosition.getPosition().xPos >= 0 ? 1 : -1);
                offsety =(nextPosition.yPos - MyGPSPosition.getPosition().yPos >= 0 ? 1 : -1);
                //根据方向绘制路线
                if(myRoad.startPoint.equals(nextPosition)){
                    for(int i = myRoad.polylines.size() - 1 ; i>=0 ;i--){
                        if(offsetx * (myRoad.polylines.get(i).xPos - MyGPSPosition.getPosition().xPos) >= 0 && offsety * (myRoad.polylines.get(i).yPos - MyGPSPosition.getPosition().yPos) >= 0){
                            xPos.add(parsePosition(myRoad.polylines.get(i)).x);
                            yPos.add(parsePosition(myRoad.polylines.get(i)).y);
                        }
                    }
                }else if(myRoad.endPoint.equals(nextPosition)){  //如果方向正确
                    for(int i = 0 ; i< myRoad.polylines.size() ;i++){
                        if(offsetx * (myRoad.polylines.get(i).xPos - MyGPSPosition.getPosition().xPos) >= 0 && offsety * (myRoad.polylines.get(i).yPos - MyGPSPosition.getPosition().yPos) >= 0){
                            xPos.add(parsePosition(myRoad.polylines.get(i)).x);
                            yPos.add(parsePosition(myRoad.polylines.get(i)).y);
                        }
                    }
                }
                //绘制MyPositionInCrossroad 到NavigationLine.getfirst的道路
                //获取路段信息
                Road road = DrawNavigation.getRoad(nextPosition,NavigationLine.getFirst());
                if(road!= null) drawPolines(road,g_2D);
            }

            int[] xposs = new int[xPos.size()];
            int[] yposs = new int[xPos.size()];
            for(int i= 0; i<xPos.size();i++){
                xposs[i] = xPos.get(i);
                yposs[i] = yPos.get(i);
            }

//            System.out.print("[");
//            for(int i : xposs){
//                System.out.print(i+",");
//            }
//            System.out.print("] ");
//
//            System.out.print("[");
//            for(int i : yposs){
//                System.out.print(i+",");
//            }
//            System.out.println("]");

            g_2D.drawPolyline(xposs,yposs,xposs.length);
            //绘制其他路线
            for(Road road:PATHROADS){
                drawNavigationPolines(road,g_2D);
            }
        }
    }

    private static void setPathRoads(){
        PATHROADS = DrawNavigation.getRoadsInPath();
    }

    private static void drawNavigationPolines(Road road,Graphics2D g){
        BasicStroke basicStroke = new BasicStroke((float) (60/staff * getRate()));
        g.setStroke(basicStroke);
        g.setColor(NavigationColor.navigationColor);
        int[] xPos = new int[road.polylines.size()];
        int[] yPos = new int[road.polylines.size()];
        for(int i = 0; i < road.polylines.size(); i++){
            Point point = parsePosition(road.polylines.get(i));
            xPos[i] = point.x;
            yPos[i] = point.y;
        }
        g.drawPolyline(xPos,yPos,xPos.length);
    }

    private static void refleshPathRoads() {
        for(int i=0;i<PATHROADS.size();i++){
            Road road = PATHROADS.get(i);
            road.speed = DatabaseParser.getSpeed(road.ID);
        }
    }

    //根据传入的交叉入口坐标获取所在road
    private static Road getRoad(Position startPoint , Position endPoint){
        for(Road road : getMapLines()){
            if((road.startPoint.equals(startPoint) && road.endPoint.equals(endPoint)) || (road.startPoint.equals(endPoint) && road.endPoint.equals(startPoint))){
                return road;
            }
        }
        return null;
    }

}
