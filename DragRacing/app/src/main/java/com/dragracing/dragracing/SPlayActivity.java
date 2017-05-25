package com.dragracing.dragracing;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cunoraz.gifview.library.GifView;
import com.jero.multiviewanimation_library.AnimManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

//单机游戏界面

public class SPlayActivity extends AppCompatActivity {
    DRSGame drsGame;//游戏类
    Button btn_dice;//骰子按钮
    Button[] btn_airs;//飞机按钮
    //所有飞机贴图的id
    int[][] air_id = {
            {R.id.air_1_1_splay,R.id.air_1_2_splay,R.id.air_1_3_splay,R.id.air_1_4_splay},
            {R.id.air_2_1_splay,R.id.air_2_2_splay,R.id.air_2_3_splay,R.id.air_2_4_splay},
            {R.id.air_3_1_splay,R.id.air_3_2_splay,R.id.air_3_3_splay,R.id.air_3_4_splay},
            {R.id.air_4_1_splay,R.id.air_4_2_splay,R.id.air_4_3_splay,R.id.air_4_4_splay}
    };
    //所有骰子贴图的id
    int[] dice_id = {0, R.drawable.dice1,R.drawable.dice2,R.drawable.dice3,R.drawable.dice4,R.drawable.dice5,R.drawable.dice6};
    ImageView[][] image_airs;//飞机贴图对象
    FrameLayout layout_airs;//游戏棋盘的布局
    ImageButton imageButton_dice;//骰子按钮图片
    ImageView image_dice;//骰子图片

    int cur_dice;//当前骰子点数
    int cur_air;//当前选择的飞机

    ClickDiceTask clickDiceTask;
    ClickAirTask clickAirTask;
    PlayTask playTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splay);

        //句柄获取
        //骰子按钮
        btn_dice = (Button)this.findViewById(R.id.button_dice_splay);
        //飞机按钮
        int[] btn_airs_id = {R.id.button_air1_splay,R.id.button_air2_splay,R.id.button_air3_splay,R.id.button_air4_splay};
        btn_airs = new Button[4];
        for(int i=0;i<4;++i)
            btn_airs[i] = (Button)this.findViewById(btn_airs_id[i]);
        //飞机贴图
        image_airs = new ImageView[4][4];
        for(int i=0;i<4;++i)
            for(int j=0;j<4;++j)
                image_airs[i][j] = (ImageView)this.findViewById(air_id[i][j]);
        //棋盘布局
        layout_airs = (FrameLayout) this.findViewById(R.id.layout_airs_splay);
        //骰子按钮贴图
        imageButton_dice = (ImageButton)this.findViewById(R.id.imageButton_dice);
        //骰子贴图
        image_dice = (ImageView)this.findViewById(R.id.image_dice);

        //事件设置
        //骰子按钮
        btn_dice.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                btn_dice.setEnabled(false);
                clickDiceTask = new ClickDiceTask();
                clickDiceTask.execute(SPlayActivity.this);
            }
        });
        //飞机按钮
        for(int i=0;i<4;++i){
            final int ix=i;
            btn_airs[i].setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    for(int i=0;i<4;++i)
                        btn_airs[i].setEnabled(false);
                    cur_air = ix;
                    clickAirTask = new ClickAirTask();
                    clickAirTask.execute(SPlayActivity.this);
                }
            });
        }
        //骰子图片按钮
        imageButton_dice.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                clickDiceTask = new ClickDiceTask();
                clickDiceTask.execute(SPlayActivity.this);
            }
        });


        //Bundle bundle = this.getIntent().getExtras();
        //ApplicationInfo appInfo = getApplicationInfo();
        //int bmpid = getResources().getIdentifier("paperplane", "drawable", appInfo.packageName);
        //Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.paperplane);
        //Matrix matrix = new Matrix();
        //int bmpWidth = bmp.getWidth();
        //int bmpHeight = bmp.getHeight();

        //float scaleX = Float.valueOf(getResources().getString(R.string.image_air_scaleX));
        //float scaleY = Float.valueOf(getResources().getString(R.string.image_air_scaleY));
        //float scaleX = R.string.image_air_scaleX;
        //float scaleY = R.string.image_air_scaleY;
        //Log.i("SPlayActivity", "JH:scale "+scaleX+" "+scaleY);
        //Log.i("SPlayActivity", "JH:bmp "+bmpWidth+" "+bmpHeight);
        //matrix.postScale(scaleX, scaleY);
        //Bitmap sbmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

        //layout_airs.removeView(image_airs[0][0]);
        //image_airs[0][0] = new ImageView(this);
        //image_airs[0][0].setId(air_id[0][0]);
        //image_airs[0][0].setImageBitmap(sbmp);
        //layout_airs.addView(image_airs[0][0]);

        //setContentView(R.layout.activity_splay);
        /*
        GifView gifView = new GifView(this);
        gifView.setVisibility(View.VISIBLE);
        gifView.setGifResource(R.drawable.waiting);
        layout_airs.addView(gifView);
        gifView.play();*/


        //获取玩家信息
        Intent intent = this.getIntent();
        DRSGame.PlayerType[] playerType = (DRSGame.PlayerType[])intent.getSerializableExtra("playerType");
        String[] names = new String[playerType.length];
        for(int i=0,cntAI=1;i<playerType.length;++i){
            if(playerType[i] == DRSGame.PlayerType.PEOPLE){
                names[i] = "You";
            }
            else if(playerType[i] == DRSGame.PlayerType.AI){
                names[i] = "AI" + cntAI;
                ++cntAI;
            }
        }

        //开始游戏
        drsGame = new DRSGame();
        drsGame.doReady(playerType);
        drsGame.setPlayerNames(names);
        drsGame.doPlay();
    }

    //窗口加载完后,加载飞机贴图,开始执行游戏
    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        if (hasFocus){
            for(int i=0;i<drsGame.num_players;++i) {
                for (int j = 0; j < 4; ++j) {
                    setAirImage(i, j);
                    image_airs[i][j].setVisibility(ImageView.VISIBLE);
                }
            }
            setButtonEnable();
            updateTitle();

            if(drsGame.getCurPlayerType() == DRSGame.PlayerType.AI){
                updateDice(true, false, false);
                playTask = new PlayTask();
                playTask.execute(SPlayActivity.this);
            }
            else if(drsGame.getCurPlayerType() == DRSGame.PlayerType.INTERPEOPLE){
                updateDice(true, false, false);
            }
            else if(drsGame.getCurPlayerType() == DRSGame.PlayerType.PEOPLE) {
                updateDice(true, false, true);
            }
        }
    }

    //设置骰子按钮和飞机按钮是否可点击
    public void setButtonEnable(){
        if(drsGame.turnState == DRSGame.TurnState.WAIT_DICE){
            btn_dice.setEnabled(true);
            for(int i=0;i<4;++i)
                btn_airs[i].setEnabled(false);
        }
        else if(drsGame.turnState == DRSGame.TurnState.WAIT_AIR){
            btn_dice.setEnabled(false);
            for(int i=0;i<4;++i)
                btn_airs[i].setEnabled(false);
            ArrayList<Integer> candidateAirs = drsGame.getCandidateAir(cur_dice);
            for(int i=0;i<candidateAirs.size();++i)
                btn_airs[candidateAirs.get(i)].setEnabled(true);
        }
        else{
            btn_dice.setEnabled(false);
            for(int i=0;i<4;++i)
                btn_airs[i].setEnabled(false);
        }
    }

    //设置游戏结束
    public void setGameEnd(){
        //need a game record save

        String winner = drsGame.playerNames[drsGame.whoWin()];
        AlertDialog.Builder builder = new AlertDialog.Builder(SPlayActivity.this);
        builder.setTitle("游戏结束");
        builder.setMessage(winner+"赢了!");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                SPlayActivity.this.finish();
            }
        });

        builder.create().show();
    }

    //刷新某架飞机的位置
    //param
    // iplayer:玩家id
    // iAir:飞机id
    public void setAirImage(int iplayer, int iAir){
        Log.d("SPlayActivity", String.format("JH:setAirImage %d %d", iplayer, iAir));
        double x;
        double y;
        if(drsGame.airPos[iplayer][iAir] == drsGame.AIROFF){
            x = drsGame.ppos_off[drsGame.playerPos[iplayer]][iAir][0];
            y = drsGame.ppos_off[drsGame.playerPos[iplayer]][iAir][1];
        }
        else if(drsGame.airPos[iplayer][iAir] == drsGame.AIRREADY){
            x = drsGame.ppos_ready[drsGame.playerPos[iplayer]][0];
            y = drsGame.ppos_ready[drsGame.playerPos[iplayer]][1];
        }
        else if(drsGame.airPos[iplayer][iAir] == drsGame.AIRWIN){
            x = drsGame.ppos_win[drsGame.playerPos[iplayer]][0];
            y = drsGame.ppos_win[drsGame.playerPos[iplayer]][1];
        }
        else if(drsGame.airPos[iplayer][iAir] == drsGame.AIREND){
            x = drsGame.ppos_off[drsGame.playerPos[iplayer]][iAir][0];
            y = drsGame.ppos_off[drsGame.playerPos[iplayer]][iAir][1];
        }
        else{
            x = drsGame.ppos_apos[drsGame.getapos(iplayer, iAir)][0];
            y = drsGame.ppos_apos[drsGame.getapos(iplayer, iAir)][1];
        }
        x = x / 100 * layout_airs.getWidth();
        y = y / 100 * layout_airs.getHeight();
        float scaleX = Float.valueOf(getResources().getString(R.string.image_air_scaleX));
        float scaleY = Float.valueOf(getResources().getString(R.string.image_air_scaleY));
        x -= image_airs[drsGame.playerPos[iplayer]][iAir].getWidth() * (1-scaleX) / 2;
        y -= image_airs[drsGame.playerPos[iplayer]][iAir].getHeight() * (1-scaleY) / 2;
        image_airs[drsGame.playerPos[iplayer]][iAir].setX((float)x);
        image_airs[drsGame.playerPos[iplayer]][iAir].setY((float)y);

        //Log.i("SPlayActivity", String.format("JH:%d %d %f %f", layout_airs.getWidth(), layout_airs.getHeight(), x, y));
        //Log.i("SPlayActivity", String.format("JH:%d %d %f %f", layout_airs.getWidth(), layout_airs.getHeight(), x, y));
        //image_airs[0][0].layout((int)x, (int)y, 0, 0);
    }

    //更新标题
    public void updateTitle(){
        String name = drsGame.playerNames[drsGame.cur_player];
        setTitle(name + "的回合");
    }

    //更新骰子按钮和图片
    //param
    // fa:是否显示骰子gif
    // fb:是否显示骰子结果
    // fc:骰子gif是否可点
    public void updateDice(Boolean fa, Boolean fb, Boolean fc){
        if(fa){
            imageButton_dice.setVisibility(View.VISIBLE);
            imageButton_dice.setEnabled(fc);
        }
        else{
            imageButton_dice.setVisibility(View.INVISIBLE);
        }

        if(fb){
            image_dice.setImageResource(dice_id[cur_dice]);
            image_dice.setVisibility(View.VISIBLE);
        }
        else{
            image_dice.setVisibility(View.INVISIBLE);
        }
    }

    //执行一次AI的飞行,返回事件集
    public DRSGame.StepEvent st_play(){
        //assert now turn to ai
        DRSGame.StepEvent events;
        int dice = drsGame.getDice();
        ArrayList<Integer> airs = drsGame.getCandidateAir(dice);

        if(airs.size() == 0){
            events=new DRSGame.StepEvent();
            events.init(0);
        }
        else {
            cur_dice = dice;
            cur_air = airs.get(0);
            events = drsGame.doStep(cur_air, cur_dice);
        }

        return events;
    }

    //AI任务类,处理AI回合
    class PlayTask extends AsyncTask<SPlayActivity, DRSGame.StepEvent, Boolean>{
        int ANITYPE_DICE=-1;
        int ANITYPE_AIR=-2;

        SPlayActivity sPlayActivity;
        int aniType;
        int s1;//信号量

        @Override
        protected void onPreExecute(){

        }
        @Override
        protected Boolean doInBackground(SPlayActivity... params){
            sPlayActivity = params[0];

            while(sPlayActivity.drsGame.getCurPlayerType() == DRSGame.PlayerType.AI){
                SystemClock.sleep(1000);
                DRSGame.StepEvent events = sPlayActivity.st_play();
                aniType = ANITYPE_DICE;
                doEvent(events);
                SystemClock.sleep(1000);
                aniType = ANITYPE_AIR;
                doEvent(events);

                if(sPlayActivity.drsGame.gameState == DRSGame.GameState.END)
                    return false;
                if(sPlayActivity.drsGame.cur_dice != 6)
                    drsGame.nextStep();
            }

            return true;
        }
        @Override
        protected void onProgressUpdate(DRSGame.StepEvent... values){
            DRSGame.StepEvent events = values[0];
            if(aniType == ANITYPE_DICE) {
                sPlayActivity.updateTitle();
                sPlayActivity.btn_dice.setText("Dice" + String.valueOf(sPlayActivity.cur_dice));
                sPlayActivity.updateDice(false, true, false);
            }
            else if(aniType == ANITYPE_AIR){
                sPlayActivity.updateDice(false, false, false);
                if (events.num_event > 0) {
                    sPlayActivity.setAirImage(sPlayActivity.drsGame.cur_player, sPlayActivity.drsGame.cur_air);
                    for (int i = 0; i < events.num_event; ++i)
                        if (events.hits[i])
                            sPlayActivity.setAirImage(events.players[i], events.airs[i]);
                }
            }
            s1 = 0;
        }
        @Override
        protected void onPostExecute(Boolean result){
            sPlayActivity.btn_dice.setText("Dice");
            if(result) {
                sPlayActivity.setButtonEnable();
                sPlayActivity.updateTitle();
                if(sPlayActivity.drsGame.getCurPlayerType() == DRSGame.PlayerType.PEOPLE)
                    sPlayActivity.updateDice(true, false, true);
                else
                    sPlayActivity.updateDice(true, false, false);
            }
            else
                sPlayActivity.setGameEnd();
        }
        public void doEvent(DRSGame.StepEvent events){
            s1 = 1;
            publishProgress(events);
            while(s1 != 0)
                SystemClock.sleep(50);
        }
    }

    //骰子点击任务类,处理骰子点击后的操作
    class ClickDiceTask extends AsyncTask<SPlayActivity, Void, ArrayList<Integer>>{
        SPlayActivity sPlayActivity;
        int s1;//信号量

        @Override
        protected void onPreExecute(){

        }
        @Override
        protected ArrayList<Integer> doInBackground(SPlayActivity... params){
            SystemClock.sleep(500);
            sPlayActivity = params[0];

            sPlayActivity.cur_dice = sPlayActivity.drsGame.getDice();

            publishProgress();
            SystemClock.sleep(1500);

            ArrayList<Integer> airs = drsGame.getCandidateAir(sPlayActivity.cur_dice);
            if(airs.size() == 0){
                sPlayActivity.drsGame.nextStep();
            }
            else{
                sPlayActivity.drsGame.turnState = DRSGame.TurnState.WAIT_AIR;
            }

            return airs;
        }
        @Override
        protected void onProgressUpdate(Void... values){
            sPlayActivity.updateDice(false, true, false);
        }
        @Override
        protected void onPostExecute(ArrayList<Integer> result){
            sPlayActivity.btn_dice.setText("Dice"+String.valueOf(sPlayActivity.cur_dice));
            sPlayActivity.setButtonEnable();
            sPlayActivity.updateTitle();
            if(result.size() == 0){
                if(sPlayActivity.drsGame.getCurPlayerType() == DRSGame.PlayerType.AI) {
                    sPlayActivity.updateDice(true, false, false);
                    sPlayActivity.playTask = new PlayTask();
                    sPlayActivity.playTask.execute(sPlayActivity);
                }
                else if(sPlayActivity.drsGame.getCurPlayerType() == DRSGame.PlayerType.PEOPLE){
                    sPlayActivity.updateDice(true, false, true);
                }
                else if(sPlayActivity.drsGame.getCurPlayerType() == DRSGame.PlayerType.INTERPEOPLE) {
                    sPlayActivity.updateDice(true, false, false);
                }
            }
            else{
                sPlayActivity.updateDice(false, true, false);
            }
        }
    }

    //飞机点击任务类,用于处理点击飞机按钮后的操作
    class ClickAirTask extends AsyncTask<SPlayActivity, DRSGame.StepEvent, Boolean>{
        SPlayActivity sPlayActivity;
        int s1;//信号量

        @Override
        protected void onPreExecute(){

        }
        @Override
        protected Boolean doInBackground(SPlayActivity... params){
            SystemClock.sleep(500);
            sPlayActivity = params[0];

            DRSGame.StepEvent events = sPlayActivity.drsGame.doStep(sPlayActivity.cur_air, sPlayActivity.cur_dice);
            s1 = 1;
            publishProgress(events);
            while(s1 != 0)
                SystemClock.sleep(50);

            Log.d("SPlayActivity","JH:click air bg");

            if(sPlayActivity.drsGame.gameState == DRSGame.GameState.END)
                return false;
            if(sPlayActivity.drsGame.cur_dice != 6)
                drsGame.nextStep();
            else
                sPlayActivity.drsGame.turnState = DRSGame.TurnState.WAIT_DICE;

            return true;
        }
        @Override
        protected void onProgressUpdate(DRSGame.StepEvent... values){
            sPlayActivity.updateDice(false, false, false);
            DRSGame.StepEvent events = values[0];
            sPlayActivity.setAirImage(sPlayActivity.drsGame.cur_player, sPlayActivity.drsGame.cur_air);
            for(int i=0;i<events.num_event;++i)
                if(events.hits[i])
                    sPlayActivity.setAirImage(events.players[i], events.airs[i]);
            s1 = 0;
        }
        @Override
        protected void onPostExecute(Boolean result){
            sPlayActivity.btn_dice.setText("Dice");
            if(result) {
                sPlayActivity.setButtonEnable();
                sPlayActivity.updateTitle();
                if(sPlayActivity.drsGame.getCurPlayerType() == DRSGame.PlayerType.AI) {
                    sPlayActivity.updateDice(true, false, false);
                    sPlayActivity.playTask = new PlayTask();
                    sPlayActivity.playTask.execute(sPlayActivity);
                }
                else if(sPlayActivity.drsGame.getCurPlayerType() == DRSGame.PlayerType.PEOPLE){
                    sPlayActivity.updateDice(true, false, true);
                }
                else if(sPlayActivity.drsGame.getCurPlayerType() == DRSGame.PlayerType.INTERPEOPLE) {
                    sPlayActivity.updateDice(true, false, false);
                }
            }
            else
                sPlayActivity.setGameEnd();
        }
    }
}
