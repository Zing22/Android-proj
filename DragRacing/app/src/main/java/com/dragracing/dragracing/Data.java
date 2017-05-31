package com.dragracing.dragracing;

import android.os.SystemClock;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

//全局数据结构体

public class Data {
    public static Socket mSocket;//socket

    //数据
    public static String username;//用户名
    public static String user_id;//用户id
    public static ArrayList<Room> rooms=new ArrayList<>();//房间列表
    public static Boolean isEnter;//是否进入房间
    public static Room room;//进入的房间
    public static JSONArray playerInfo;//玩家信息
    public static String addAIinfo;//添加AI时的异常消息
    public static String chatMsg;//消息
    public static int dice;//骰子
    public static int iAir;//选择的飞机

    //房间class
    static public class Room{
        public String name;//房间名
        public int players;//玩家数
        public boolean isPlay;//是否已开始游戏
        public String room_id;//房间id

        public Room(){}
    }

    //创建socket
    public static boolean createSocket(){
        try{
            mSocket = IO.socket("http://dragracing.tech");
            //mSocket = IO.socket("http://localhost:3000");
        }
        catch (URISyntaxException e){
            Log.e("Data", "JH:create socket error");
            return false;
        }

        mSocket.connect();

        Log.i("Data", "JH:connect success!");

        return true;
    }

    //关闭socket
    public static void closeSocket(){
        mSocket.disconnect();
    }

    public static void socketEmit(String title, Object body){
        Log.i("Data", "JH:emit " + title);
        mSocket.emit(title, body);
    }
}
