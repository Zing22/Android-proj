package com.dragracing.dragracing;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
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

    TextView tv_chat;

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
            else if(msgstr.equals("game not ready")){
                alertGameStart();
            }
            else if(msgstr.equals("game started")){
                gameStart();
            }
            else if(msgstr.equals("chat msg")){
                updateMsg();
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
        tv_chat = (TextView)this.findViewById(R.id.textview_chat_room);

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
                else {
                    if(isReady())
                        Data.socketEmit("set ready", false);
                    else
                        Data.socketEmit("set ready", true);
                }
            }
        });
        //位置按钮
        for(int i=0;i<4;++i){
            final int ix = i;
            btn_pos[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DRSGame.PlayerType playerType = getPosPlayerType(ix);
                    if(playerType == DRSGame.PlayerType.EMPTY){
                        Data.socketEmit("swap chair", String.valueOf(ix));
                    }
                }
            });
            btn_pos[i].setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    DRSGame.PlayerType playerType = getPosPlayerType(ix);
                    if(playerType == DRSGame.PlayerType.AI && isHomer()){
                        String name = getPosPlayerID(ix);
                        if(name == "")
                            Log.e(TAG, "JH:btn pos long click error");
                        else
                            Data.socketEmit("remove AI", name);
                    }
                    return true;
                }
            });
        }
        //发送消息按钮
        btn_sendmsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slotSendMsg();
            }
        });
    }

    public void gameStart(){
        setSocketOff();
        Intent intent = new Intent();
        intent.setClass(RoomActivity.this, PlayActivity.class);
        startActivity(intent);
        RoomActivity.this.finish();
    }

    public void alertAddAI(){
        if(Data.addAIinfo.equals("room full"))
            Toast.makeText(RoomActivity.this, "没位置啦", Toast.LENGTH_SHORT).show();
        else if(Data.addAIinfo.equals("not host"))
            Toast.makeText(RoomActivity.this, "你不是房主啦", Toast.LENGTH_SHORT).show();
    }

    public void alertGameStart(){
        Toast.makeText(RoomActivity.this, "还有人未准备哦", Toast.LENGTH_SHORT).show();
    }

    public void slotSendMsg(){
        AlertDialog.Builder builder = new AlertDialog.Builder(RoomActivity.this);
        final EditText editText = new EditText(this);
        builder.setView(editText);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                Data.socketEmit("chat send", Data.username+": "+editText.getText());
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    public void updateMsg(){
        tv_chat.append(Data.chatMsg+"\n");
    }

    public void updatePlayersInfo(){
        for(int i=0;i<4;++i){
            if(getPosPlayerName(i).equals(""))
                btn_pos[i].setText("empty");
            else{
                if(getPosPlayerHost(i))
                    btn_pos[i].setText(getPosPlayerName(i)+"[房主]");
                else{
                    if(getPosPlayerReady(i))
                        btn_pos[i].setText(getPosPlayerName(i)+"[已准备]");
                    else
                        btn_pos[i].setText(getPosPlayerName(i));
                }
            }
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

    public DRSGame.PlayerType getPosPlayerType(int pos){
        try{
            JSONObject player = Data.playerInfo.getJSONObject(pos);
            if(player.has("empty"))
                return DRSGame.PlayerType.EMPTY;
            if(player.getString("ai").equals("true"))
                return DRSGame.PlayerType.AI;
            if(player.getString("user_id").equals(Data.mSocket.id()))
                return DRSGame.PlayerType.PEOPLE;
            return DRSGame.PlayerType.INTERPEOPLE;
        }
        catch (JSONException e){
            Log.e(TAG, "get pos player type error");
        }
        return DRSGame.PlayerType.EMPTY;
    }

    public String getPosPlayerID(int pos){
        try{
            JSONObject player = Data.playerInfo.getJSONObject(pos);
            if(player.has("empty"))
                return "";
            return player.getString("user_id");
        }
        catch (JSONException e){
            Log.e(TAG, "get pos player id error");
        }
        return "";
    }

    public String getPosPlayerName(int pos){
        try{
            JSONObject player = Data.playerInfo.getJSONObject(pos);
            if(player.has("empty"))
                return "";
            return player.getString("username");
        }
        catch (JSONException e){
            Log.e(TAG, "get pos player name error");
        }
        return "";
    }

    public boolean getPosPlayerReady(int pos){
        try{
            JSONObject player = Data.playerInfo.getJSONObject(pos);
            if(player.has("empty"))
                return false;
            return player.getString("ready").equals("true");
        }
        catch (JSONException e){
            Log.e(TAG, "get pos player ready error");
        }
        return false;
    }

    public boolean getPosPlayerHost(int pos){
        try{
            JSONObject player = Data.playerInfo.getJSONObject(pos);
            if(player.has("empty"))
                return false;
            return player.getString("host").equals("true");
        }
        catch (JSONException e){
            Log.e(TAG, "get pos player host error");
        }
        return false;
    }


    public boolean isHomer(){
        //return Data.room.room_id.equals(Data.mSocket.id());
        try{
            for(int i=0;i<4;++i){
                JSONObject player = Data.playerInfo.getJSONObject(i);
                if(player.has("empty"))
                    continue;
                if(player.getString("user_id").equals(Data.user_id))
                    return player.getString("host").equals("true");
            }
            Log.e(TAG, "not found myself in isHomer");
        }
        catch (JSONException e){
            Log.e(TAG, "isHomer error");
        }
        return false;
    }

    public boolean isReady(){
        try{
            for(int i=0;i<4;++i){
                JSONObject player = Data.playerInfo.getJSONObject(i);
                if(player.has("empty"))
                    continue;
                //if(player.getString("user_id").equals(Data.mSocket.id()))
                if(player.getString("user_id").equals(Data.user_id))
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

        mSocket.on("game not ready", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                handler.sendMessage(makeMsg("game not ready"));
            }
        });

        mSocket.on("game started", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                handler.sendMessage(makeMsg("game started"));
            }
        });

        mSocket.on("chat msg", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Data.chatMsg = (String)args[0];
                Log.d(TAG, "JH:"+Data.chatMsg);
                handler.sendMessage(makeMsg("chat msg"));
            }
        });
    }

    //关闭socket监听
    public void setSocketOff(){
        Socket mSocket = Data.mSocket;

        mSocket.off("players info");
        mSocket.off("add AI response");
        mSocket.off("game not ready");
        mSocket.off("game started");
        mSocket.off("chat msg");
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
