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
    public static int room_num = 0;//进入的房间号
    public static Socket mSocket;//socket

    //获取的数据
    public static String username;//用户名
    public static ArrayList<Room> rooms=new ArrayList<>();

    //房间class
    static public class Room{
        public String name;//房间名
        public int players;//玩家数
        public boolean isPlay;//是否已开始游戏

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
