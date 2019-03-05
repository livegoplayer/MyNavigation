package out.production.Navigation.GPS;


import com.xjy.hsy.crossroad.Position;
import netscape.javascript.JSObject;
import org.json.JSONObject;

import java.awt.desktop.OpenURIEvent;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 根据IP地址获取详细的地域信息
 * @File AddressUtils.java
 * @Package org.gditc.weicommunity.util
 */
public class AddressUtils {
    /**
     *
     * @param content
     * 请求的参数
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String getAddresses(String content, String encodingString)
            throws UnsupportedEncodingException {
        String urlStr = "http://api.map.baidu.com/location/ip";
        String returnStr = getResult(urlStr, content, encodingString);
        if (returnStr != null) {
            // 处理返回的省市区信息
            returnStr = returnStr;
            String[] temp = returnStr.split(",");
            if(temp.length<3){
                return "0";//无效IP，局域网测试
            }
            return returnStr;
        }
        return null;
    }
    /**
     * @param urlStr
     *            请求的地址
     * @param content
     *            请求的参数 格式为：name=xxx&pwd=xxx
     * @param encoding
     *            服务器端请求编码。如GBK,UTF-8等
     * @return
     */
    private static String getResult(String urlStr, String content, String encoding) {
        URL url = null;
        HttpURLConnection connection = null;
        try {
            url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();// 新建连接实例
            connection.setConnectTimeout(2000);// 设置连接超时时间，单位毫秒
            connection.setReadTimeout(2000);// 设置读取数据超时时间，单位毫秒
            connection.setDoOutput(true);// 是否打开输出流 true|false
            connection.setDoInput(true);// 是否打开输入流true|false
            connection.setRequestMethod("POST");// 提交方法POST|GET
            connection.setUseCaches(false);// 是否缓存true|false
            connection.connect();// 打开连接端口
            DataOutputStream out = new DataOutputStream(connection
                    .getOutputStream());// 打开输出流往对端服务器写数据
            out.writeBytes(content);// 写数据,也就是提交你的表单 name=xxx&pwd=xxx
            out.flush();// 刷新
            out.close();// 关闭输出流
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), encoding));// 往对端写完数据对端服务器返回数据
            // ,以BufferedReader流来读取
            StringBuffer buffer = new StringBuffer();
            String line = "";
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            reader.close();
            return buffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();// 关闭连接
            }
        }
        return null;
    }
    // 测试
    public static void main(String[] args) {
        AddressUtils addressUtils = new AddressUtils();
        // 测试ip 219.136.134.157 中国=华南=广东省=广州市=越秀区=电信
        String ip = "219.136.134.157";
        String address = "";
        try {
            address = addressUtils.getAddresses("ip="+ ip + "&ak=" +  "I6PDj7mploQ6s5XxzTe4z2tTUdcGjLf6", "utf-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Position position = getPosition(address);
        System.out.println(position);
        // 输出结果为：广东省,广州市,越秀区
    }

    //解析json数据
    private static Position getPosition(String jsonData){
        try{
            JSONObject jsObject = new JSONObject(jsonData);
            JSONObject content = jsObject.getJSONObject("content");
            JSONObject point = content.getJSONObject("point");
            Double x = point.getDouble("x") /1000000;
            Double y = point.getDouble("y") /1000000;
            return new Position(x,y);
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

}


