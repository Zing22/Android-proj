package com.dragracing.dragracing;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.SyncStateContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Random;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

//联机游戏房间列表界面

public class RoomsActivity extends AppCompatActivity {
    static public String TAG = "RoomsActivity";

    LinearLayout layout_rooms;
    Button btn_flush;
    Button btn_newroom;
    Handler handler;

    public class MyHandler extends Handler{
        public MyHandler(){}
        public MyHandler(Looper l){super(l);}
        @Override
        public void handleMessage(Message msg){
            String msgstr = msg.getData().getString("body");
            Log.i("RoomsActivity", "JH:get msg "+msgstr);
            if(msgstr == "rooms list"){
                updateRooms();
            }
            else if(msgstr == "room enter"){
                if(Data.isEnter)
                    joinRoom();
            }
            super.handleMessage(msg);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);

        //handler
        handler = new MyHandler();

        //句柄
        layout_rooms = (LinearLayout)this.findViewById(R.id.linear_rooms);
        btn_newroom = (Button)this.findViewById(R.id.button_newroom);
        btn_flush = (Button)this.findViewById(R.id.button_flush);

        //socket设置
        if(Data.createSocket() == false){
            exitSocketError("网络连接失败");
            return;
        }
        setSocketOn();
        Data.socketEmit("random name", "玩家");
        Data.socketEmit("need rooms list", "");


        //按钮事件
        btn_flush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Data.socketEmit("rooms list","");
            }
        });
        btn_newroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Data.room = new Data.Room();
                Data.room.room_id = Data.mSocket.id();
                Data.room.name = Data.username+"的房间";
                try {
                    Data.socketEmit("create room", new JSONObject(String.format("{username:'%s'}", Data.username)));
                }
                catch (JSONException e){
                    Log.e(TAG, "JH:emit create room error");
                }
            }
        });
    }

    //更新房间列表
    public void updateRooms(){
        layout_rooms.removeAllViews();

        for(int i=0;i<Data.rooms.size();++i){
            final Data.Room room = Data.rooms.get(i);
            Button btn = new Button(this);
            String isPlay;
            if(room.isPlay) isPlay = "游戏中";
            else isPlay = "准备中";
            btn.setText(String.format("%s[%d/4][%s]",room.name,room.players,isPlay));
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(room.players == 4) return;
                    if(room.isPlay) return;

                    Data.room = room;
                    try{
                        Data.socketEmit("join room", new JSONObject(String.format("{room_id:'%s',username:'%s'}", room.room_id, Data.username)));
                    }
                    catch (JSONException e){
                        Log.e(TAG, "JH:emit create room error");
                    }
                }
            });

            layout_rooms.addView(btn);
        }
    }

    //进入房间
    public void joinRoom(){
        Intent intent = new Intent();
        intent.setClass(RoomsActivity.this, RoomActivity.class);
        startActivity(intent);
    }

    //网络连接失败退出
    public void exitSocketError(String error){
        AlertDialog.Builder builder = new AlertDialog.Builder(RoomsActivity.this);
        builder.setTitle("Error");
        builder.setMessage(error);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                RoomsActivity.this.finish();
            }
        });

        builder.create().show();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        setSocketOff();
        Data.closeSocket();
    }

    //设置socket监听
    public void setSocketOn(){
        Socket mSocket = Data.mSocket;

        mSocket.on("new username", new Emitter.Listener(){
            @Override
            public void call(Object... args){
                JSONObject data = (JSONObject) args[0];
                try{
                    Data.username = data.getString("new_name");
                    handler.sendMessage(makeMsg("new username"));
                }
                catch (JSONException e){
                    Log.e(TAG, "JH:on new username error");
                }
            }
        });

        mSocket.on("rooms list", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                Log.i(TAG, "JH:"+data.toString());
                try{
                    Data.rooms.clear();

                    Iterator iterator = data.keys();
                    while(iterator.hasNext()){
                        JSONObject roomObject = data.getJSONObject((String)iterator.next());
                        Data.Room room = new Data.Room();
                        room.name = roomObject.getString("owner") + "的房间";
                        JSONArray playersObject = roomObject.getJSONArray("players");
                        room.players = 0;
                        for(int i=0;i<4;++i)
                            if(playersObject.getJSONObject(i).has("user_id"))
                                ++room.players;
                        room.isPlay = roomObject.getBoolean("gaming");
                        room.room_id = roomObject.getString("id");

                        Data.rooms.add(room);
                    }
                    handler.sendMessage(makeMsg("rooms list"));
                }
                catch (JSONException e){
                    Log.e(TAG, "JH:on rooms list error");
                }
            }
        });

        mSocket.on("room enter", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Object data = args[0];
                Log.i(TAG, "JH:"+data.toString());
                if(data.toString() == "true")
                    Data.isEnter = true;
                else
                    Data.isEnter = false;

                handler.sendMessage(makeMsg("room enter"));
            }
        });
    }

    //关闭socket监听
    public void setSocketOff(){
        Socket mSocket = Data.mSocket;

        mSocket.off("new username");
        mSocket.off("rooms list");
        mSocket.off("room enter");
    }

    //创建msg
    public Message makeMsg(String body){
        Message msg = new Message();
        Bundle bdl = new Bundle();
        bdl.putString("body", body);
        msg.setData(bdl);
        return msg;
    }
}
