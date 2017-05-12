package com.dragracing.dragracing;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class SPlayActivity extends AppCompatActivity {
    DRSGame drsGame;
    int cur_dice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splay);

        Button btn_dice = (Button)this.findViewById(R.id.button_dice_splay);
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
                else
                    cur_dice = dice;
            }
        });

        Button btn_air = (Button)this.findViewById(R.id.button_air_splay);
        btn_air.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int dice = cur_dice;
                ArrayList<Integer> airs = drsGame.getCandidateAir(dice);
                Random r = new Random();
                int iAir = airs.get(r.nextInt(airs.size()));
                Log.i("SPlayActivity", String.format("JH:select %d", iAir));
                DRSGame.StepEvent events = drsGame.doStep(iAir, dice);
                doEvent(events);
                if(dice != 6) {
                    drsGame.nextStep();
                    play();
                }
            }
        });

        Intent intent = this.getIntent();
        DRSGame.PlayerType[] playerType = (DRSGame.PlayerType[])intent.getSerializableExtra("playerType");

        drsGame = new DRSGame();
        drsGame.doReady(playerType);
        drsGame.doPlay();

        play();
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

            if(drsGame.gameState == DRSGame.GameState.END){
                Log.i("SPlayActivity", String.format("JH:game end and winner is %d", drsGame.whoWin()));
                break;
            }

            if(dice != 6)
                drsGame.nextStep();
        }
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
}
