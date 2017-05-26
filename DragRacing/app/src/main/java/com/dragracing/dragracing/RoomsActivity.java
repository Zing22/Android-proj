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
    LinearLayout layout_rooms;
    Handler handler;

    public class MyHandler extends Handler{
        public MyHandler(){}
        public MyHandler(Looper l){super(l);}
        @Override
        public void handleMessage(Message msg){
            Log.i("RoomsActivity", "JH:get msg");
            String msgstr = msg.getData().getString("body");
            if(msgstr == "rooms list"){
                updateRooms();
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);

        //handler
        handler = new MyHandler();

        //房间布局句柄
        layout_rooms = (LinearLayout)this.findViewById(R.id.linear_rooms);

        //socket设置
        if(Data.createSocket() == false){
            exitSocketError("网络连接失败");
            return;
        }
        setSocketOn();
        Data.socketEmit("random name", "玩家");
        Data.socketEmit("need rooms list", "");

        /*
        for(int i=0;i<10;++i){
            LayoutInflater inflater = getLayoutInflater();
            Button btn = (Button)inflater.inflate(R.layout.button_room, null);
            Random r = new Random();
            final int sa=r.nextInt(100);
            btn.setText("房间" + String.valueOf(sa));
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setClass(RoomsActivity.this, RoomActivity.class);
                    Data.setRoom_num(sa);
                    startActivity(intent);
                }
            });
            ll.addView(btn);
        }*/

//        Button btn1 = (Button)this.findViewById(R.id.button);
//        btn1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent();
//                intent.setClass(RoomsActivity.this, RoomActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        Button btn_newroom = (Button)this.findViewById(R.id.button_newroom);
//        final LinearLayout ll = (LinearLayout)this.findViewById(R.id.linear_rooms);
//        btn_newroom.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Button btn_new = new Button(RoomsActivity.this);
//                btn_new.setText("new btn");
//                ll.addView(btn_new);
//            }
//        });
    }

    public void updateRooms(){
        layout_rooms.removeAllViews();

        for(int i=0;i<Data.rooms.size();++i){
            Data.Room room = Data.rooms.get(i);
            Button btn = new Button(this);
            String isPlay;
            if(room.isPlay) isPlay = "游戏中";
            else isPlay = "准备中";
            btn.setText(String.format("%s[%d/4][%s]",room.name,room.players,isPlay));
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Data.socketEmit("join room", ???);
                }
            });

            layout_rooms.addView(btn);
        }
    }

    public void joinRoom(){

    }

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
                }
                catch (JSONException e){
                    Log.e("Data", "JH:on new username error");
                }

                handler.sendMessage(makeMsg("new username"));
            }
        });

        mSocket.on("rooms list", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
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

                        Data.rooms.add(room);
                    }
                }
                catch (JSONException e){
                    Log.e("Data", "JH:on rooms list error");
                }

                handler.sendMessage(makeMsg("rooms list"));
            }
        });
    }

    //关闭socket监听
    public void setSocketOff(){
        Socket mSocket = Data.mSocket;

        mSocket.off("new username");
        mSocket.off("rooms list");
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
