package com.dragracing.dragracing;

import android.content.Intent;
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

public class SPlayActivity extends AppCompatActivity {
    DRSGame drsGame;
    int cur_dice;
    Button btn_dice;
    Button[] btn_airs;
    int[][] air_id = {
            {R.id.air_1_1_splay,R.id.air_1_2_splay,R.id.air_1_3_splay,R.id.air_1_4_splay},
            {R.id.air_2_1_splay,R.id.air_2_2_splay,R.id.air_2_3_splay,R.id.air_2_4_splay},
            {R.id.air_3_1_splay,R.id.air_3_2_splay,R.id.air_3_3_splay,R.id.air_3_4_splay},
            {R.id.air_4_1_splay,R.id.air_4_2_splay,R.id.air_4_3_splay,R.id.air_4_4_splay}
    };
    ImageView[][] image_airs;
    FrameLayout layout_airs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splay);

        TextView textView = (TextView)this.findViewById(R.id.textView5);
//        Animation animation = new TranslateAnimation(220, 700, 220, 700);
//        animation.setDuration(5000);
//        animation.setRepeatCount(222);
//        textView.setAnimation(animation);
//        animation.startNow();

        textView.setX(200);
        textView.setY(800);

//        List<View> views = new ArrayList<>();
//        List<Animation> anims = new ArrayList<>();
//
//        views.add(textView);
//        views.add(textView);
//        Animation animation = new TranslateAnimation(220, 700, 220, 700);
//        animation.setDuration(5000);
//        anims.add(animation);
//        animation = new TranslateAnimation(700, 1000, 700, 300);
//        animation.setDuration(5000);
//        anims.add(animation);
//
//        AnimManager animManager = new AnimManager(views, anims);
//        animManager.startAnimation();

        btn_dice = (Button)this.findViewById(R.id.button_dice_splay);

        int[] btn_airs_id = {R.id.button_air1_splay,R.id.button_air2_splay,R.id.button_air3_splay,R.id.button_air4_splay};
        btn_airs = new Button[4];
        for(int i=0;i<4;++i)
            btn_airs[i] = (Button)this.findViewById(btn_airs_id[i]);

        image_airs = new ImageView[4][4];
        for(int i=0;i<4;++i)
            for(int j=0;j<4;++j)
                image_airs[i][j] = (ImageView)this.findViewById(air_id[i][j]);

        layout_airs = (FrameLayout) this.findViewById(R.id.layout_airs_splay);


        btn_dice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int dice = drsGame.getDice();
                Log.i("SPlayActivity", String.format("JH:%d get dice %d", drsGame.cur_player, dice));
                ArrayList<Integer> airs = drsGame.getCandidateAir(dice);
                for(int i=0;i<airs.size();++i)
                    Log.i("SPlayActivity", String.format("JH:get candidateair %d", airs.get(i)));

                if(airs.size() == 0){
                    drsGame.nextStep();
                    play();
                }
                else {
                    cur_dice = dice;
                    drsGame.turnState = DRSGame.TurnState.WAIT_AIR;
                    setButtonEnable();
                }
            }
        });

        for(int i=0;i<4;++i){
            final int ix = i;
            btn_airs[i].setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    Log.i("SPlayActivity", String.format("JH:select %d", ix));
                    DRSGame.StepEvent events = drsGame.doStep(ix, cur_dice);
                    doEvent(events);
                    setAirImage(drsGame.cur_player, drsGame.cur_air);
                    for(int j=0;j<events.num_event;++j)
                        if(events.hits[j])
                            setAirImage(events.players[j], events.airs[j]);

                    drsGame.turnState = DRSGame.TurnState.OTHER;
                    if(cur_dice != 6) {
                        drsGame.nextStep();
                        play();
                    }
                    else{
                        drsGame.turnState = DRSGame.TurnState.WAIT_DICE;
                        setButtonEnable();
                    }
                }
            });
        }


        Intent intent = this.getIntent();
        DRSGame.PlayerType[] playerType = (DRSGame.PlayerType[])intent.getSerializableExtra("playerType");

        drsGame = new DRSGame();
        drsGame.doReady(playerType);
        drsGame.doPlay();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        if (hasFocus){
            setButtonEnable();
            for(int i=0;i<drsGame.num_players;++i)
                for(int j=0;j<4;++j) {
                    setAirImage(i, j);
                    image_airs[i][j].setVisibility(ImageView.VISIBLE);
                }

            play();
        }
    }

    public void play(){
        while(drsGame.getCurPlayerType() == DRSGame.PlayerType.AI){
            int dice = drsGame.getDice();
            Log.i("SPlayActivity", String.format("JH:%d get dice %d", drsGame.cur_player, dice));
            ArrayList<Integer> airs = drsGame.getCandidateAir(dice);
            for(int i=0;i<airs.size();++i)
                Log.i("SPlayActivity", String.format("JH:get candidateair %d", airs.get(i)));
            if(airs.size() == 0){
                drsGame.nextStep();
                continue;
            }

            DRSGame.StepEvent events = drsGame.doStep(airs.get(0), dice);
            doEvent(events);
            setAirImage(drsGame.cur_player, drsGame.cur_air);
            for(int i=0;i<events.num_event;++i)
                if(events.hits[i])
                    setAirImage(events.players[i], events.airs[i]);

            if(drsGame.gameState == DRSGame.GameState.END){
                Log.i("SPlayActivity", String.format("JH:game end and winner is %d", drsGame.whoWin()));
                break;
            }

            if(dice != 6)
                drsGame.nextStep();
        }

        if(drsGame.getCurPlayerType() == DRSGame.PlayerType.PEOPLE)
            drsGame.turnState = DRSGame.TurnState.WAIT_DICE;
        else if(drsGame.getCurPlayerType() == DRSGame.PlayerType.INTERPEOPLE)
            drsGame.turnState = DRSGame.TurnState.WAIT_INTER_DICE;
        setButtonEnable();
    }

    public void doEvent(DRSGame.StepEvent events){
        for(int i=0;i<events.num_event;++i){
            if(events.hits[i]){//hit event
                Log.i("SPlayActivity", String.format("JH:%d %d hits %d %d", drsGame.cur_player, drsGame.cur_air, events.players[i], events.airs[i]));
            }
            else{//go event
                Log.i("SPlayActivity", String.format("JH:%d %d to rpos-%d", drsGame.cur_player, drsGame.cur_air, events.poses[i]));
            }
        }
    }

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
        x = x / 100 * layout_airs.getWidth() - layout_airs.getWidth() / 2 + 22;
        y = y / 100 * layout_airs.getHeight() - 65;
        image_airs[drsGame.playerPos[iplayer]][iAir].setX((float)x);
        image_airs[drsGame.playerPos[iplayer]][iAir].setY((float)y);

        Log.i("SPlayActivity", String.format("JH:%d %d %f %f", layout_airs.getWidth(), layout_airs.getHeight(), x, y));
        Log.i("SPlayActivity", String.format("JH:%d %d %f %f", layout_airs.getWidth(), layout_airs.getHeight(), x, y));
        //image_airs[0][0].layout((int)x, (int)y, 0, 0);
    }
}
