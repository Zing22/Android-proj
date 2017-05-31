package com.dragracing.dragracing;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ReplayActivity extends AppCompatActivity {
    static public String TAG = "ReplayActivity";
    DRSGame drsGame;
    DRSGame.GameRecord record;

    Handler handler;

    //句柄
    //所有飞机贴图的id
    int[][] air_id = {
            {R.id.air_1_1_replay,R.id.air_1_2_replay,R.id.air_1_3_replay,R.id.air_1_4_replay},
            {R.id.air_2_1_replay,R.id.air_2_2_replay,R.id.air_2_3_replay,R.id.air_2_4_replay},
            {R.id.air_3_1_replay,R.id.air_3_2_replay,R.id.air_3_3_replay,R.id.air_3_4_replay},
            {R.id.air_4_1_replay,R.id.air_4_2_replay,R.id.air_4_3_replay,R.id.air_4_4_replay}
    };
    //所有骰子贴图的id
    int[] dice_id = {0, R.drawable.dice1,R.drawable.dice2,R.drawable.dice3,R.drawable.dice4,R.drawable.dice5,R.drawable.dice6};
    ImageView[][] image_airs;//飞机贴图对象
    FrameLayout layout_airs;//游戏棋盘的布局
    ImageButton imageButton_dice;//骰子按钮图片
    ImageView image_dice;//骰子图片

    //当前属性
    DRSGame.StepEvent cur_events;//事件
    int cur_events_ix;//事件ix

    int cur_record_ix;//record_ix
    boolean is_back;//是否要退出

    public class MyHandler extends Handler {
        public MyHandler(){}
        public MyHandler(Looper l){super(l);}
        @Override
        public void handleMessage(Message msg){
            String msgstr = msg.getData().getString("body");
            Log.i(TAG, "JH:get msg "+msgstr);

            if(is_back)return;

            if(msgstr.equals("do event")){
                doEvent();
            }
            else if(msgstr.equals("show dice")){
                showDice();
            }
            else if(msgstr.equals("show animation")){
                showAnimation();
            }
            else if(msgstr.equals("end animation")){
                if(drsGame.cur_dice!=6)
                    drsGame.nextStep();
                ++cur_record_ix;
                if(drsGame.whoWin() == -1)
                    handler.sendMessage(makeMsg("show dice"));
                else
                    setTitle("游戏结束");
            }

            super.handleMessage(msg);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replay);

        //handler
        handler = new MyHandler();

        //读文件
        Intent intent = this.getIntent();
        String filename = intent.getStringExtra("filename");
        record = new DRSGame.GameRecord();
        String[] names = new String[4];

        try {
            FileInputStream in = openFileInput(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String sline;
            for(int i=0;i<4;++i){
                names[i]=reader.readLine();
                Log.d(TAG,"JH:name->"+names[i]);
            }
            record.setPlayerNames(names);
            while((sline=reader.readLine())!=null) {
                String[] svalues = sline.split("\\s+");
                int[] ivalues = new int[3];
                for(int i=0;i<3;++i)
                    ivalues[i] = Integer.parseInt(svalues[i]);
                record.addRecord(ivalues[0],ivalues[1],ivalues[2]);
            }
        }catch (IOException e) {
            Log.e(TAG, "JH:file record read error!");
            AlertDialog.Builder builder = new AlertDialog.Builder(ReplayActivity.this);
            builder.setTitle("Replay");
            builder.setMessage("读取失败");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    ReplayActivity.this.finish();
                }
            });
            builder.create().show();
        }

        //句柄获取
        //飞机贴图
        image_airs = new ImageView[4][4];
        for (int i = 0; i < 4; ++i)
            for (int j = 0; j < 4; ++j)
                image_airs[i][j] = (ImageView) this.findViewById(air_id[i][j]);
        //棋盘布局
        layout_airs = (FrameLayout) this.findViewById(R.id.layout_airs_replay);
        //骰子按钮贴图
        imageButton_dice = (ImageButton) this.findViewById(R.id.imageButton_dice_replay);
        //骰子贴图
        image_dice = (ImageView) this.findViewById(R.id.image_dice_replay);

        //设置
        imageButton_dice.setVisibility(View.INVISIBLE);
        image_dice.setVisibility(View.INVISIBLE);

        //drsgame
        DRSGame.PlayerType[] playerType = new DRSGame.PlayerType[4];
        for(int i=0;i<4;++i) {
            if (names[i].equals(""))
                playerType[i] = DRSGame.PlayerType.EMPTY;
            else
                playerType[i] = DRSGame.PlayerType.INTERPEOPLE;
        }

        drsGame = new DRSGame();
        drsGame.doReady(playerType);
        String[] playerNames = new String[drsGame.num_players];
        int ix=0;
        for(int i=0;i<4;++i)
            if(!names[i].equals(""))
                playerNames[ix++]=names[i];
        drsGame.setPlayerNames(playerNames);
        drsGame.doPlay();

        is_back = false;
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
            cur_record_ix = 0;
            handler.sendMessage(makeMsg("show dice"));
        }
    }

    public void showDice(){
        updateTitle();
        image_dice.setImageResource(dice_id[record.dices.get(cur_record_ix)]);
        image_dice.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Thread.sleep(1000);
                }
                catch (InterruptedException e){
                    Log.e(TAG,"JH:thread interrupted!");
                }
                handler.sendMessage(makeMsg("show animation"));
            }
        }).start();
    }

    public void showAnimation(){
        cur_events = drsGame.doStep(record.iAirs.get(cur_record_ix),record.dices.get(cur_record_ix));
        cur_events_ix = 0;
        handler.sendMessage(makeMsg("do event"));
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
                    handler.sendMessage(makeMsg("end animation"));
                }else{
                    handler.sendMessage(makeMsg("do event"));
                }
            }
        }).start();
    }

    public void updateAirImage(int iplayer ,int iAir){
        Pair<Float,Float> pos = getAirXYPos(iplayer,iAir,drsGame.airPos[iplayer][iAir]);
        image_airs[drsGame.playerPos[iplayer]][iAir].setX(pos.first);
        image_airs[drsGame.playerPos[iplayer]][iAir].setY(pos.second);
        if(drsGame.airPos[iplayer][iAir] == drsGame.AIREND)
            image_airs[drsGame.playerPos[iplayer]][iAir].setVisibility(View.INVISIBLE);
    }

    public void updateTitle(){
        String name = drsGame.playerNames[drsGame.cur_player];
        setTitle(name + "的回合");
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
        is_back = true;
    }
}
