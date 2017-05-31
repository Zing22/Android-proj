package com.dragracing.dragracing;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

//联机游戏界面

public class PlayActivity extends AppCompatActivity {
    static public String TAG = "PlayAvtivity";

    Handler handler;

    DRSGame drsGame;//游戏类
    Button[] btn_airs;//飞机按钮
    //所有飞机贴图的id
    int[][] air_id = {
            {R.id.air_1_1_play,R.id.air_1_2_play,R.id.air_1_3_play,R.id.air_1_4_play},
            {R.id.air_2_1_play,R.id.air_2_2_play,R.id.air_2_3_play,R.id.air_2_4_play},
            {R.id.air_3_1_play,R.id.air_3_2_play,R.id.air_3_3_play,R.id.air_3_4_play},
            {R.id.air_4_1_play,R.id.air_4_2_play,R.id.air_4_3_play,R.id.air_4_4_play}
    };
    //所有骰子贴图的id
    int[] dice_id = {0, R.drawable.dice1,R.drawable.dice2,R.drawable.dice3,R.drawable.dice4,R.drawable.dice5,R.drawable.dice6};
    ImageView[][] image_airs;//飞机贴图对象
    FrameLayout layout_airs;//游戏棋盘的布局
    ImageButton imageButton_dice;//骰子按钮图片
    ImageView image_dice;//骰子图片
    Button btn_sendmsg;//发送消息
    Button btn_tuoguan;//托管
    TextView tv_chat;//聊天框
    ScrollView sv_chat;//聊天框的滚动view

    //当前属性
    int cur_dice;//当前骰子点数
    int cur_air;//当前选择的飞机
    DRSGame.StepEvent cur_events;//事件
    int cur_events_ix;//事件ix

    boolean is_tuoguan;//是否托管

    //回合状态
    int turn_state;
    int TurnState_waitDice = 1;
    int TurnState_waitAir = 2;
    int TurnState_animation = 3;
    int TurnState_beforeGame = 4;

    public class MyHandler extends Handler {
        public MyHandler(){}
        public MyHandler(Looper l){super(l);}
        @Override
        public void handleMessage(Message msg){
            String msgstr = msg.getData().getString("body");
            Log.i(TAG, "JH:get msg "+msgstr);

            if(msgstr.equals("chat msg")){
                updateMsg();
            }
            else if(msgstr.equals("turn dice")){
                getTurnDice();
            }
            else if(msgstr.equals("dice result")){
                getDiceResult();
            }
            else if(msgstr.equals("chess move")){
                getChessMove();
            }
            else if(msgstr.equals("game over")){
                getGameOver();
            }
            else if(msgstr.equals("players info")){

            }
            else if(msgstr.equals("do event")){
                doEvent();
            }

            super.handleMessage(msg);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        //handler
        handler = new MyHandler();

        //句柄获取
        //飞机按钮
        int[] btn_airs_id = {R.id.button_air1_play, R.id.button_air2_play, R.id.button_air3_play, R.id.button_air4_play};
        btn_airs = new Button[4];
        for (int i = 0; i < 4; ++i)
            btn_airs[i] = (Button) this.findViewById(btn_airs_id[i]);
        //飞机贴图
        image_airs = new ImageView[4][4];
        for (int i = 0; i < 4; ++i)
            for (int j = 0; j < 4; ++j)
                image_airs[i][j] = (ImageView) this.findViewById(air_id[i][j]);
        //棋盘布局
        layout_airs = (FrameLayout) this.findViewById(R.id.layout_airs_play);
        //骰子按钮贴图
        imageButton_dice = (ImageButton) this.findViewById(R.id.imageButton_dice_play);
        //骰子贴图
        image_dice = (ImageView) this.findViewById(R.id.image_dice_play);
        //发送消息按钮
        btn_sendmsg = (Button)this.findViewById(R.id.button_sendmsg_play);
        //托管
        btn_tuoguan = (Button)this.findViewById(R.id.button_tuoguan_play);
        //聊天框
        tv_chat = (TextView)this.findViewById(R.id.textview_chat_play);
        sv_chat = (ScrollView)this.findViewById(R.id.scrollView_chat_play);

        //设置事件
        //发送消息按钮
        btn_sendmsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slotSendMsg();
            }
        });
        //骰子按钮贴图
        imageButton_dice.setEnabled(false);
        imageButton_dice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(drsGame.getCurPlayerType() == DRSGame.PlayerType.PEOPLE)
                    slotClickDice();
            }
        });
        //飞机按钮
        for(int i=0;i<4;++i){
            final int ix=i;
            btn_airs[i].setEnabled(false);
            btn_airs[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    slotClickAir(ix);
                }
            });
        }
        image_dice.setVisibility(View.INVISIBLE);
        //托管
        btn_tuoguan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slotTuoGuan();
            }
        });

        //socket
        setSocketOn();

        //获取玩家信息
        DRSGame.PlayerType[] playerType = getPlayerType();
        String[] playerNames = getPlayerName();
        //Log.d(TAG, "JH:"+String.valueOf(playerNames.length));

        //drsgame
        drsGame = new DRSGame();
        drsGame.doReady(playerType);
        //Log.d(TAG, "JH:"+String.valueOf(drsGame.num_players));
        drsGame.setPlayerNames(playerNames);
        drsGame.doPlay();
    }

    //窗口加载完后,加载飞机贴图,开始执行游戏
    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        if (hasFocus){
            for(int i=0;i<drsGame.num_players;++i) {
                for (int j = 0; j < 4; ++j) {
                    updateAirImage(i, j);
                    image_airs[i][j].setVisibility(ImageView.VISIBLE);
                }
            }
            Data.socketEmit("game loaded","");
            turn_state = TurnState_beforeGame;
        }
    }

    public void getTurnDice(){
        if(turn_state != TurnState_beforeGame)
            if(drsGame.cur_dice != 6)
                drsGame.nextStep();
        turn_state = TurnState_waitDice;
        updateTitle();
        updateImageDice();
        updateAirButton();

        Data.socketEmit("turn dice done", "");
        if(is_tuoguan && drsGame.getCurPlayerType() == DRSGame.PlayerType.PEOPLE)
            Data.socketEmit("wanna dice","");
    }

    public void getDiceResult(){
        cur_dice = Data.dice;
        if(drsGame.getCandidateAir(cur_dice).size() == 0){
            turn_state = TurnState_animation;
            Data.socketEmit("chess move done","");
        }else{
            turn_state = TurnState_waitAir;
            Data.socketEmit("dice result done", "");

            if(is_tuoguan && drsGame.getCurPlayerType() == DRSGame.PlayerType.PEOPLE && drsGame.getCandidateAir(cur_dice).size()>0){
                Data.socketEmit("move chessman",drsGame.getCandidateAir(cur_dice).get(0));
            }
        }
        updateImageDice();
        updateAirButton();
    }

    public void getChessMove(){
        cur_air = Data.iAir;
        turn_state = TurnState_animation;
        updateImageDice();
        updateAirButton();

        cur_events = drsGame.doStep(cur_air, cur_dice);
        cur_events_ix = 0;

        for(int i=0;i<cur_events.num_event;++i){
            if(cur_events.hits[i])
                Log.d(TAG,String.format("JH:hit event:%d %d %d",cur_events.poses[i],cur_events.players[i],cur_events.airs[i]));
            else
                Log.d(TAG,String.format("JH:step event:%d %d %d",cur_events.poses[i],cur_events.players[i],cur_events.airs[i]));
        }

        //Animation animation = makeAnimation(0);
        //startAnimation(animation);
//        for(int i=0;i<cur_events.num_event;++i){
//            int iplayer;
//            int iAir;
//            int rpos;
//
//            if (cur_events.hits[i]) {
//                iplayer = cur_events.players[i];
//                iAir = cur_events.airs[i];
//                rpos = -2;
//            } else {
//                iplayer = drsGame.cur_player;
//                iAir = drsGame.cur_air;
//                rpos = cur_events.poses[i];
//            }
//
//            Log.d(TAG,"JH:rpos="+String.valueOf(rpos));
//            Log.d(TAG,"JH:real-rpos="+String.valueOf(drsGame.airPos[iplayer][iAir]));
//            setAirImage(iplayer,iAir,rpos);
//            //updateAirImage(iplayer,iAir);
//        }
        //Data.socketEmit("chess move done","");
        doEvent();
    }

    public void getGameOver(){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);

        String filename = String.format("record-%d-%d-%d-%d-%d-%d.txt", year, month, day, hour, minute, second);

        try {
            //写record
            FileOutputStream out = openFileOutput(filename, Context.MODE_PRIVATE);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
            DRSGame.GameRecord gameRecord = drsGame.gameRecord;
            for (int i = 0; i < 4; ++i)
                writer.write(gameRecord.names[i] + "\n");
            for (int i = 0; i < gameRecord.iplayers.size(); ++i)
                writer.write(String.format("%d %d %d\n", gameRecord.iplayers.get(i), gameRecord.dices.get(i), gameRecord.iAirs.get(i)));
            writer.close();
            //写record目录
            out = openFileOutput("record.txt", Context.MODE_APPEND);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(filename+"\n");
            writer.close();
        } catch (IOException e) {
            Log.e(TAG, "JH:game over IO error!");
        }

        String winner = drsGame.playerNames[drsGame.whoWin()];
        AlertDialog.Builder builder = new AlertDialog.Builder(PlayActivity.this);
        builder.setTitle("游戏结束");
        builder.setMessage(winner + "赢了!");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                PlayActivity.this.finish();
            }
        });

        builder.create().show();
    }

    public void setAirImage(int iplayer,int iAir,int rpos){
        Pair<Float,Float> xyPos = getAirXYPos(iplayer, iAir, rpos);
        image_airs[drsGame.playerPos[iplayer]][iAir].setX(xyPos.first);
        image_airs[drsGame.playerPos[iplayer]][iAir].setY(xyPos.second);
        if(rpos == drsGame.AIREND)
            image_airs[drsGame.playerPos[iplayer]][iAir].setVisibility(View.INVISIBLE);
    }

    public void doEvent(){
        int iplayer;
        int iAir;
        int rpos;

        if (cur_events.hits[cur_events_ix]) {
            iplayer = cur_events.players[cur_events_ix];
            iAir = cur_events.airs[cur_events_ix];
            rpos = -2;
        } else {
            iplayer = drsGame.cur_player;
            iAir = drsGame.cur_air;
            rpos = cur_events.poses[cur_events_ix];
        }
        setAirImage(iplayer,iAir,rpos);

        ++cur_events_ix;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Thread.sleep(200);
                }
                catch (InterruptedException e){
                    Log.e(TAG,"JH:thread interrupted!");
                }
                if(cur_events_ix == cur_events.num_event){
                    Data.socketEmit("chess move done","");
                }else{
                    handler.sendMessage(makeMsg("do event"));
                }
            }
        }).start();
    }

    public Animation makeAnimation(int ix){
        int iplayer;
        int iAir;
        int rposLast;
        int rpos;
        Pair<Float, Float> begPos;
        Pair<Float, Float> endPos;
        Animation animation;

        if (cur_events.hits[ix]) {
            iplayer = cur_events.players[ix];
            iAir = cur_events.airs[ix];
            rposLast = cur_events.poses[ix];
            rpos = -2;

            begPos = getAirXYPos(iplayer, iAir, rposLast);
            endPos = getAirXYPos(iplayer, iAir, rpos);
        } else {
            iplayer = drsGame.cur_player;
            iAir = drsGame.cur_air;
            rpos = cur_events.poses[ix];
            if (ix == 0)
                rposLast = rpos - 1;
            else {
                int i;
                for (i = ix - 1; i > -1; --i)
                    if (cur_events.hits[i] == false)
                        break;
                //assert i!=-1
                rposLast = cur_events.poses[i];
            }
            begPos = getAirXYPos(iplayer, iAir, rposLast);
            endPos = getAirXYPos(iplayer, iAir, rpos);
        }
        animation = new TranslateAnimation(begPos.first, begPos.second, endPos.first, endPos.second);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ++cur_events_ix;
                if (cur_events_ix == cur_events.num_event) {
                    Data.socketEmit("chess move done","");
                } else {
                    Animation animation1 = makeAnimation(cur_events_ix);
                    startAnimation(animation1);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animation.setDuration(100);
        animation.setFillAfter(true);
        return animation;
    }

    public void startAnimation(Animation animation){
        int iplayer;
        int iAir;
        if (cur_events.hits[cur_events_ix]) {
            iplayer = cur_events.players[cur_events_ix];
            iAir = cur_events.airs[cur_events_ix];
        } else {
            iplayer = drsGame.cur_player;
            iAir = drsGame.cur_air;
        }
        image_airs[drsGame.playerPos[iplayer]][iAir].startAnimation(animation);
    }

    public void updateTuoguanButton(){
        if(is_tuoguan)
            btn_tuoguan.setText("取消托管");
        else
            btn_tuoguan.setText("托管");
    }

    public void updateAllAirImage(){
        for(int i=0;i<drsGame.num_players;++i)
            for(int j=0;j<4;++j)
                updateAirImage(i,j);
    }

    public void updateAirImage(int iplayer ,int iAir){
        Pair<Float,Float> pos = getAirXYPos(iplayer,iAir,drsGame.airPos[iplayer][iAir]);
        image_airs[drsGame.playerPos[iplayer]][iAir].setX(pos.first);
        image_airs[drsGame.playerPos[iplayer]][iAir].setY(pos.second);
        if(drsGame.airPos[iplayer][iAir] == drsGame.AIREND)
            image_airs[drsGame.playerPos[iplayer]][iAir].setVisibility(View.INVISIBLE);
    }

    public void updateImageDice(){
        if(turn_state == TurnState_waitDice){
            imageButton_dice.setVisibility(View.VISIBLE);
            if(drsGame.getCurPlayerType() == DRSGame.PlayerType.PEOPLE)
                imageButton_dice.setEnabled(true);
            else
                imageButton_dice.setEnabled(false);
            image_dice.setVisibility(View.INVISIBLE);
        }
        else if(turn_state == TurnState_waitAir){
            imageButton_dice.setVisibility(View.INVISIBLE);
            imageButton_dice.setEnabled(false);
            image_dice.setImageResource(dice_id[cur_dice]);
            image_dice.setVisibility(View.VISIBLE);
        }
        else if(turn_state == TurnState_animation){
            imageButton_dice.setVisibility(View.INVISIBLE);
            imageButton_dice.setEnabled(false);
            image_dice.setVisibility(View.INVISIBLE);
        }
    }

    public void updateTitle(){
        String name = drsGame.playerNames[drsGame.cur_player];
        setTitle(name + "的回合");
    }

    public void updateAirButton(){
        if(turn_state == TurnState_waitDice){
            for(int i=0;i<4;++i)
                btn_airs[i].setEnabled(false);
        }
        else if(turn_state == TurnState_waitAir){
            for(int i=0;i<4;++i)
                btn_airs[i].setEnabled(false);
            if(drsGame.getCurPlayerType() == DRSGame.PlayerType.PEOPLE){
                ArrayList<Integer> airs = drsGame.getCandidateAir(cur_dice);
                for(int i=0;i<airs.size();++i)
                    btn_airs[airs.get(i)].setEnabled(true);
            }
        }
        else if(turn_state == TurnState_animation){
            for(int i=0;i<4;++i)
                btn_airs[i].setEnabled(false);
        }
    }

    public void updateMsg(){
        tv_chat.append(Data.chatMsg+"\n");
        sv_chat.fullScroll(View.FOCUS_DOWN);
    }

    public Pair<Float,Float> getAirXYPos(int iplayer,int iAir,int rpos){
        double x;
        double y;
        if(rpos == drsGame.AIROFF){
            x = drsGame.ppos_off[drsGame.playerPos[iplayer]][iAir][0];
            y = drsGame.ppos_off[drsGame.playerPos[iplayer]][iAir][1];
        }
        else if(rpos == drsGame.AIRREADY){
            x = drsGame.ppos_ready[drsGame.playerPos[iplayer]][0];
            y = drsGame.ppos_ready[drsGame.playerPos[iplayer]][1];
        }
        else if(rpos == drsGame.AIRWIN){
            x = drsGame.ppos_win[drsGame.playerPos[iplayer]][0];
            y = drsGame.ppos_win[drsGame.playerPos[iplayer]][1];
        }
        else if(rpos == drsGame.AIREND){
            x = drsGame.ppos_off[drsGame.playerPos[iplayer]][iAir][0];
            y = drsGame.ppos_off[drsGame.playerPos[iplayer]][iAir][1];
        }
        else{
            x = drsGame.ppos_apos[drsGame.rpos2apos(iplayer, rpos)][0];
            y = drsGame.ppos_apos[drsGame.rpos2apos(iplayer, rpos)][1];
            //Log.d(TAG, "JH:apos:"+String.valueOf(drsGame.rpos2apos(iplayer, rpos)));
        }
        x = x / 100 * layout_airs.getWidth();
        y = y / 100 * layout_airs.getHeight();
        float scaleX = Float.valueOf(getResources().getString(R.string.image_air_scaleX));
        float scaleY = Float.valueOf(getResources().getString(R.string.image_air_scaleY));
        x -= image_airs[drsGame.playerPos[iplayer]][iAir].getWidth() * (1-scaleX) / 2;
        y -= image_airs[drsGame.playerPos[iplayer]][iAir].getHeight() * (1-scaleY) / 2;
        return Pair.create((float)x,(float)y);
    }

    public void slotSendMsg(){
        AlertDialog.Builder builder = new AlertDialog.Builder(PlayActivity.this);
        final EditText editText = new EditText(this);
        builder.setView(editText);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                Data.socketEmit("chat send", Data.username+": "+editText.getText());
                updateAllAirImage();
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

    public void slotClickDice(){
        Data.socketEmit("wanna dice", "");
    }

    public void slotClickAir(int iAir){
        Data.socketEmit("move chessman", iAir);
        /*JSONObject data = new JSONObject();
        try{
            data.put("num", iAir);
            Data.socketEmit("move chessman", data);
        }
        catch (JSONException e){
            Log.e(TAG,"JH:emit move chessman error");
        }*/
    }

    public void slotTuoGuan(){
        is_tuoguan = !is_tuoguan;
        updateTuoguanButton();
        if(is_tuoguan){
            if(turn_state == TurnState_waitDice && drsGame.getCurPlayerType() == DRSGame.PlayerType.PEOPLE)
                Data.socketEmit("wanna dice","");
            else if(turn_state == TurnState_waitAir && drsGame.getCurPlayerType() == DRSGame.PlayerType.PEOPLE && drsGame.getCandidateAir(cur_dice).size()>0)
                Data.socketEmit("move chessman",drsGame.getCandidateAir(cur_dice).get(0));
        }
    }

    public DRSGame.PlayerType[] getPlayerType(){
        DRSGame.PlayerType[] playerTypes = new DRSGame.PlayerType[4];
        try{
            for(int i=0;i<4;++i){
                JSONObject player = Data.playerInfo.getJSONObject(i);
                if(player.has("empty"))
                    playerTypes[i] = DRSGame.PlayerType.EMPTY;
                else if(player.getString("ai").equals("true"))
                    playerTypes[i] = DRSGame.PlayerType.AI;
                else if(player.getString("user_id").equals(Data.mSocket.id()))
                    playerTypes[i] = DRSGame.PlayerType.PEOPLE;
                else
                    playerTypes[i] = DRSGame.PlayerType.INTERPEOPLE;
            }
        }
        catch (JSONException e){
            Log.e(TAG, "get player type error");
        }
        return playerTypes;
    }

    public String[] getPlayerName(){
        String[] playerNames = null;
        try{
            int num_people = 0;
            for(int i=0;i<4;++i){
                JSONObject player = Data.playerInfo.getJSONObject(i);
                if(!player.has("empty"))
                    ++num_people;
            }
            playerNames = new String[num_people];
            int ix=0;

            for(int i=0;i<4;++i){
                JSONObject player = Data.playerInfo.getJSONObject(i);
                if(!player.has("empty"))
                    playerNames[ix++] = player.getString("username");
            }
        }
        catch (JSONException e){
            Log.e(TAG, "get player name error");
        }
        return playerNames;
    }

    //设置socket监听
    public void setSocketOn(){
        Socket mSocket = Data.mSocket;

        mSocket.on("chat msg", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Data.chatMsg = (String)args[0];
                Log.d(TAG, "JH:"+Data.chatMsg);
                handler.sendMessage(makeMsg("chat msg"));
            }
        });
        mSocket.on("turn dice", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                handler.sendMessage(makeMsg("turn dice"));
            }
        });
        mSocket.on("dice result", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject)args[0];
                try{
                    Data.dice = data.getInt("dice");
                    handler.sendMessage(makeMsg("dice result"));
                }
                catch (JSONException e){
                    Log.e(TAG, "JH:on dice result error");
                }
            }
        });
        mSocket.on("chess move", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject)args[0];
                try{
                    Data.iAir = data.getInt("chess_num");
                    handler.sendMessage(makeMsg("chess move"));
                }
                catch (JSONException e){
                    Log.e(TAG, "JH:on chess move error");
                }
            }
        });
        mSocket.on("game over", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                handler.sendMessage(makeMsg("game over"));
            }
        });
        mSocket.on("players info", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Data.playerInfo = (JSONArray)args[0];
                handler.sendMessage(makeMsg("players info"));
            }
        });
    }

    //关闭socket监听
    public void setSocketOff(){
        Socket mSocket = Data.mSocket;

        mSocket.off("chat msg");
        mSocket.off("turn dice");
        mSocket.off("dice result");
        mSocket.off("chess move");
        mSocket.off("game over");
        mSocket.off("players info");
    }

    //创建msg
    public Message makeMsg(String body){
        Message msg = new Message();
        Bundle bdl = new Bundle();
        bdl.putString("body", body);
        msg.setData(bdl);
        return msg;
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Data.socketEmit("leave room","");
        setSocketOff();
    }
}
