package com.dragracing.dragracing;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

//联机游戏进入房间后的界面

public class RoomActivity extends AppCompatActivity {
    static public String TAG = "RoomActivity";

    Handler handler;

    Button btn_gameready;
    Button btn_addAI;
    Button btn_sendmsg;
    Button[] btn_pos;
    int[] btn_posid = {R.id.button1_room,R.id.button2_room,R.id.button3_room,R.id.button4_room};

    public class MyHandler extends Handler {
        public MyHandler(){}
        public MyHandler(Looper l){super(l);}
        @Override
        public void handleMessage(Message msg){
            String msgstr = msg.getData().getString("body");
            Log.i(TAG, "JH:get msg "+msgstr);

            if(msgstr.equals("players info")){
                updatePlayersInfo();
                updateButtonAddAI();
                updateButtonGameready();
            }
            else if(msgstr.equals("add AI response")){
                alertAddAI();
            }

            super.handleMessage(msg);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        //handler
        handler = new MyHandler();

        //句柄
        btn_gameready = (Button)this.findViewById(R.id.button_gameready);
        btn_addAI = (Button)this.findViewById(R.id.button_addAI);
        btn_sendmsg = (Button)this.findViewById(R.id.button_sendmsg_room);
        btn_pos = new Button[4];
        for(int i=0;i<4;++i)
            btn_pos[i] = (Button)this.findViewById(btn_posid[i]);

        //socket
        setSocketOn();
        Data.socketEmit("in room","");

        //设置房间名字
        setTitle(Data.room.name);

        //按钮事件
        //AI按钮
        btn_addAI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Data.socketEmit("add AI", "");
            }
        });
        //准备按钮
        btn_gameready.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isHomer())
                    Data.socketEmit("wanna start game", "");
                else
                    Data.socketEmit("set ready", "");
            }
        });

        /*
        //开始游戏按钮
        Button btn_beggame = (Button)this.findViewById(R.id.button_gameready);
        btn_beggame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(RoomActivity.this, PlayActivity.class);
                startActivity(intent);
            }
        });

        //发送消息按钮
        Button btn_send = (Button)this.findViewById(R.id.button_send_room);
        final TextView tv_chat = (TextView)this.findViewById(R.id.textview_chat_room);
        final EditText et_chat = (EditText)this.findViewById(R.id.edittext_chat_room);

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_chat.setText(et_chat.getText());
                et_chat.setText("");
            }
        });*/
    }

    public void alertAddAI(){
        if(Data.addAIinfo.equals("room full"))
            Toast.makeText(RoomActivity.this, "没位置啦", Toast.LENGTH_SHORT).show();
        else if(Data.addAIinfo.equals("not host"))
            Toast.makeText(RoomActivity.this, "你不是房主啦", Toast.LENGTH_SHORT).show();
    }

    public void updatePlayersInfo(){
        try{
            for(int i=0;i<4;++i){
                JSONObject player = Data.playerInfo.getJSONObject(i);
                if(player.has("empty")){
                    btn_pos[i].setText("empty");
                }
                else{
                    btn_pos[i].setText(player.getString("username"));
                }
            }
        }
        catch (JSONException e){
            Log.e(TAG, "JH:update player info error");
        }
    }

    public void updateButtonAddAI(){
        if(isHomer())
            btn_addAI.setEnabled(true);
        else
            btn_addAI.setEnabled(false);
    }

    public void updateButtonGameready(){
        btn_gameready.setEnabled(true);
        if(isHomer())
            btn_gameready.setText("开始游戏");
        else{
            if(isReady())
                btn_gameready.setText("取消准备");
            else
                btn_gameready.setText("准备");
        }
    }

    public boolean isHomer(){
        return Data.room.room_id.equals(Data.mSocket.id());
//        try{
//            for(int i=0;i<4;++i){
//                JSONObject player = Data.playerInfo.getJSONObject(i);
//                if(player.has("empty"))
//                    continue;
//                if(player.getString("user_id").equals(Data.mSocket.id()))
//                    return player.getString("host").equals("true");
//            }
//            Log.e(TAG, "not found myself in isHomer");
//        }
//        catch (JSONException e){
//            Log.e(TAG, "isHomer error");
//        }
//        return false;
    }

    public boolean isReady(){
        try{
            for(int i=0;i<4;++i){
                JSONObject player = Data.playerInfo.getJSONObject(i);
                if(player.has("empty"))
                    continue;
                if(player.getString("user_id").equals(Data.mSocket.id()))
                    return player.getString("ready").equals("true");
            }
            Log.e(TAG, "not found myself in isReady");
        }
        catch (JSONException e){
            Log.e(TAG, "isReady error");
        }
        return false;
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Data.socketEmit("leave room","");
        setSocketOff();
    }

    //设置socket监听
    public void setSocketOn(){
        Socket mSocket = Data.mSocket;

        mSocket.on("players info", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Data.playerInfo = (JSONArray) args[0];
                Log.i(TAG, "JH:"+Data.playerInfo.toString());
                handler.sendMessage(makeMsg("players info"));
            }
        });

        mSocket.on("add AI response", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Data.addAIinfo = (String) args[0];
                handler.sendMessage(makeMsg("add AI response"));
            }
        });
    }

    //关闭socket监听
    public void setSocketOff(){
        Socket mSocket = Data.mSocket;

        mSocket.off("players info");
        mSocket.off("add AI response");
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
