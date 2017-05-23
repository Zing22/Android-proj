package com.dragracing.dragracing;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    ImageView[][] image_airs;//飞机贴图对象
    FrameLayout layout_airs;//游戏棋盘的布局

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

        //获取玩家信息
        Intent intent = this.getIntent();
        DRSGame.PlayerType[] playerType = (DRSGame.PlayerType[])intent.getSerializableExtra("playerType");

        //开始游戏
        drsGame = new DRSGame();
        drsGame.doReady(playerType);
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

            if(drsGame.getCurPlayerType() == DRSGame.PlayerType.AI){
                playTask = new PlayTask();
                playTask.execute(SPlayActivity.this);
            }
            //play();
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

    }

    //刷新某架飞机的位置
    //param
    // iplayer:玩家id
    // iAir:飞机id
    public void setAirImage(int iplayer, int iAir){
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
        image_airs[drsGame.playerPos[iplayer]][iAir].setX((float)x);
        image_airs[drsGame.playerPos[iplayer]][iAir].setY((float)y);

        Log.i("SPlayActivity", String.format("JH:%d %d %f %f", layout_airs.getWidth(), layout_airs.getHeight(), x, y));
        Log.i("SPlayActivity", String.format("JH:%d %d %f %f", layout_airs.getWidth(), layout_airs.getHeight(), x, y));
        //image_airs[0][0].layout((int)x, (int)y, 0, 0);
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
            events = drsGame.doStep(airs.get(0), dice);
        }

        return events;
    }

    //AI任务类,处理AI回合
    class PlayTask extends AsyncTask<SPlayActivity, DRSGame.StepEvent, Boolean>{
        SPlayActivity sPlayActivity;

        @Override
        protected void onPreExecute(){

        }
        @Override
        protected Boolean doInBackground(SPlayActivity... params){
            sPlayActivity = params[0];

            while(sPlayActivity.drsGame.getCurPlayerType() == DRSGame.PlayerType.AI){
                DRSGame.StepEvent events = sPlayActivity.st_play();
                publishProgress(events);

                if(sPlayActivity.drsGame.gameState == DRSGame.GameState.END)
                    return false;
                if(sPlayActivity.drsGame.cur_dice != 6)
                    drsGame.nextStep();
            }

            if(sPlayActivity.drsGame.getCurPlayerType() == DRSGame.PlayerType.PEOPLE)
                sPlayActivity.drsGame.turnState = DRSGame.TurnState.WAIT_DICE;
            else if(sPlayActivity.drsGame.getCurPlayerType() == DRSGame.PlayerType.INTERPEOPLE)
                sPlayActivity.drsGame.turnState = DRSGame.TurnState.WAIT_INTER_DICE;

            return true;
        }
        @Override
        protected void onProgressUpdate(DRSGame.StepEvent... values){
            DRSGame.StepEvent events = values[0];
            sPlayActivity.setAirImage(sPlayActivity.drsGame.cur_player, sPlayActivity.drsGame.cur_air);
            for(int i=0;i<events.num_event;++i)
                if(events.hits[i])
                    sPlayActivity.setAirImage(events.players[i], events.airs[i]);
        }
        @Override
        protected void onPostExecute(Boolean result){
            if(result)
                sPlayActivity.setButtonEnable();
            else
                sPlayActivity.setGameEnd();
        }
    }

    //骰子点击任务类,处理骰子点击后的操作
    class ClickDiceTask extends AsyncTask<SPlayActivity, DRSGame.StepEvent, ArrayList<Integer>>{
        SPlayActivity sPlayActivity;

        @Override
        protected void onPreExecute(){

        }
        @Override
        protected ArrayList<Integer> doInBackground(SPlayActivity... params){
            sPlayActivity = params[0];

            sPlayActivity.cur_dice = sPlayActivity.drsGame.getDice();

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
        protected void onProgressUpdate(DRSGame.StepEvent... values){

        }
        @Override
        protected void onPostExecute(ArrayList<Integer> result){
            sPlayActivity.setButtonEnable();
            if(result.size() == 0){
                if(sPlayActivity.drsGame.getCurPlayerType() == DRSGame.PlayerType.AI) {
                    sPlayActivity.playTask = new PlayTask();
                    sPlayActivity.playTask.execute(sPlayActivity);
                }
            }
        }
    }

    //飞机点击任务类,用于处理点击飞机按钮后的操作
    class ClickAirTask extends AsyncTask<SPlayActivity, DRSGame.StepEvent, Boolean>{
        SPlayActivity sPlayActivity;

        @Override
        protected void onPreExecute(){

        }
        @Override
        protected Boolean doInBackground(SPlayActivity... params){
            sPlayActivity = params[0];

            DRSGame.StepEvent events = sPlayActivity.drsGame.doStep(sPlayActivity.cur_air, sPlayActivity.cur_dice);
            publishProgress(events);

            if(sPlayActivity.drsGame.gameState == DRSGame.GameState.END)
                return false;
            if(sPlayActivity.drsGame.cur_dice != 6)
                drsGame.nextStep();

            if(sPlayActivity.drsGame.getCurPlayerType() == DRSGame.PlayerType.PEOPLE)
                sPlayActivity.drsGame.turnState = DRSGame.TurnState.WAIT_DICE;
            else if(sPlayActivity.drsGame.getCurPlayerType() == DRSGame.PlayerType.INTERPEOPLE)
                sPlayActivity.drsGame.turnState = DRSGame.TurnState.WAIT_INTER_DICE;

            return true;
        }
        @Override
        protected void onProgressUpdate(DRSGame.StepEvent... values){
            DRSGame.StepEvent events = values[0];
            sPlayActivity.setAirImage(sPlayActivity.drsGame.cur_player, sPlayActivity.drsGame.cur_air);
            for(int i=0;i<events.num_event;++i)
                if(events.hits[i])
                    sPlayActivity.setAirImage(events.players[i], events.airs[i]);
        }
        @Override
        protected void onPostExecute(Boolean result){
            if(result) {
                sPlayActivity.setButtonEnable();
                if(sPlayActivity.drsGame.getCurPlayerType() == DRSGame.PlayerType.AI) {
                    sPlayActivity.playTask = new PlayTask();
                    sPlayActivity.playTask.execute(sPlayActivity);
                }
            }
            else
                sPlayActivity.setGameEnd();
        }
    }
}
