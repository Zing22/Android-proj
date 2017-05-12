package com.dragracing.dragracing;

import android.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class DRSGame {
    public enum GameState{OFF,READY,PLAYING,END}
    public enum PlayerType{EMPTY,PEOPLE,AI,INTERPEOPLE}
    public int[] READYDICE = {2,4,6};

    //public int OUTBEGPOS = 0;
    public int OUTENDPOS = 49;
    public int INBEGPOS = 50;
    public int INENDPOS = 54;
    public int FLYPOS = 17;
    public int FLYTOPOS = 29;
    public int FLYCRUSHPOS = 52;

    public int AIRREADY = -1;
    public int AIROFF = -2;
    public int AIREND = -3;

    public GameState gameState;
    public int num_players;
    public int[] playerPos;
    public PlayerType[] playerType;
    public int[][] airPos;
    public Random randomDice;

    public int cur_turn;
    public int cur_player;
    public int cur_air;

    public class StepEvent{
        public int num_event;
        public int[] poses;
        public Boolean[] hits;
        public int[] players;
        public int[] airs;

        public void init(int num){
            num_event = num;
            poses = new int[num_event];
            hits = new Boolean[num_event];
            players = new int[num_event];
            airs = new int[num_event];
        }

        public void merge(StepEvent e){
            //StepEvent e3 = new StepEvent();
            //e3.init(e1.num_event + e2.num_event);

        }
    }
    public StepEvent merge(StepEvent e1, StepEvent e2){
        StepEvent e3 = new StepEvent();
        //e3.num_event = e1.num_event + e2.num_event;

        e3.init(e1.num_event + e2.num_event);
        for(int i=0;i<e1.num_event;++i){
            e3.poses[i] = e1.poses[i];
            e3.hits[i] = e1.hits[i];
            e3.players[i] = e1.players[i];
            e3.airs[i] = e1.airs[i];
        }
        for(int i=0;i<e2.num_event;++i){
            e3.poses[e1.num_event+i] = e2.poses[i];
            e3.hits[e1.num_event+i] = e2.hits[i];
            e3.players[e1.num_event+i] = e2.players[i];
            e3.airs[e1.num_event+i] = e2.airs[i];
        }

        return e3;
    }

    public DRSGame(){
        gameState=GameState.OFF;
    }

    public void doReady(PlayerType[] players){
        num_players = 0;
        for(int i=0;i<4;++i)
            if(players[i] != PlayerType.EMPTY)
                num_players++;

        playerPos = new int[num_players];
        playerType = new PlayerType[num_players];
        int ix=0;
        for(int i=0;i<4;++i){
            if(players[i] != PlayerType.EMPTY){
                playerPos[ix] = i;
                playerType[ix] = players[i];
                ix++;
            }
        }

        airPos = new int[num_players][4];
        for(int i=0;i<num_players;++i)
            for(int j=0;j<4;++j)
                airPos[i][j]=AIROFF;

        randomDice = new Random(233);

        gameState = GameState.READY;
    }

    public void doPlay(){
        cur_turn = 1;
        cur_player = 0;
        cur_air = -1;
        gameState = GameState.PLAYING;
    }

    public void nextStep(){
        cur_player++;
        if(cur_player == num_players) {
            cur_player = 0;
            cur_turn++;
        }

        cur_air = -1;
    }

    public StepEvent doStep(int iAir, int dice){
        StepEvent res = new StepEvent();

        cur_air = iAir;

        //assert not air end
        if(airPos[cur_player][cur_air] == AIROFF){//起机
            //assert dice ok
            res.init(1);
            res.poses[0] = AIRREADY;
            res.hits[0] = false;

            airPos[cur_player][cur_air] = AIRREADY;
        }
        else{
            int pos = airPos[cur_player][cur_air] + dice;
            if(pos<=OUTENDPOS){//外圈
                res = makeGoEvent(airPos[cur_player][cur_air], pos);

                ArrayList<Pair<Integer,Integer>> airs = getCrushAir(cur_player, rpos2apos(cur_player, pos));
                if(airs.size() != 0){
                    StepEvent res1 = makeCrushEvent(airs);
                    res = merge(res, res1);
                }

                if(isJump(pos)){//跳
                    StepEvent res1 = makeJumpEvent(cur_player, pos);
                    res = merge(res, res1);
                    pos += 4;
                    if(isFly(pos)){
                        StepEvent res2 = makeFlyEvent(cur_player, pos);
                        res = merge(res, res2);
                        pos = FLYTOPOS;
                    }
                }
                else if(isFly(pos)){//飞
                    StepEvent res1 = makeFlyEvent(cur_player, pos);
                    res = merge(res, res1);
                    pos = FLYTOPOS;
                    if(isJump(pos)){
                        StepEvent res2 = makeJumpEvent(cur_player, pos);
                        res = merge(res, res2);
                        pos += 4;
                    }
                }

                airPos[cur_player][cur_air] = pos;
            }
            else if(pos<=INENDPOS){//内环
                res = makeGoEvent(airPos[cur_player][cur_air], pos);

                airPos[cur_player][cur_air] = pos;
            }
            else{//终点
                res = makeGoEvent(airPos[cur_player][cur_air], INENDPOS);
                StepEvent res1 = new StepEvent();
                res1.init(1);
                res1.poses[0] = AIREND;
                res1.hits[0] = false;
                res = merge(res, res1);

                airPos[cur_player][cur_air] = AIREND;

                if(whoWin() != -1)
                    gameState = GameState.END;
            }
        }

        return res;
    }

    public ArrayList<Integer> getCandidateAir(int dice){
        ArrayList<Integer> res = new ArrayList<Integer>();

        for(int i=0;i<4;++i){
            if(airPos[cur_player][i] == AIROFF){
                if(Arrays.asList(READYDICE).contains(dice))
                    res.add(i);
            }
            else if(airPos[cur_player][i] == AIREND)
                continue;
            else
                res.add(i);
        }

        return res;
    }

    public int getDice(){
        return randomDice.nextInt(6)+1;
    }

    public int rpos2apos(int iplayer, int rpos){
        if(rpos < 0)
            return rpos;
        else if(rpos <= OUTENDPOS)
            return (playerPos[iplayer]*13+rpos)%52;
        else
            return playerPos[iplayer]*5+52 + rpos-INBEGPOS;
    }

    public ArrayList<Pair<Integer,Integer>> getAir(int apos){
        ArrayList<Pair<Integer,Integer>> res = new ArrayList<>();

        for(int i=0;i<num_players;++i)
            for(int j=0;j<4;++j)
                if(rpos2apos(i, airPos[i][j]) == apos)
                    res.add(Pair.create(i,j));
        return res;
    }

    public ArrayList<Pair<Integer,Integer>> getCrushAir(int iplayer,int apos){
        ArrayList<Pair<Integer,Integer>> airs = getAir(apos);
        if(airs.isEmpty())
            return airs;
        else{
            ArrayList<Pair<Integer,Integer>> res = new ArrayList<>();
            for(int i=0;i<airs.size();++i)
                if(airs.get(i).first != iplayer)
                    res.add(airs.get(i));
            return res;
        }
    }

    public StepEvent makeGoEvent(int rbeg, int rend){
        StepEvent res = new StepEvent();
        res.init(rend-rbeg);
        for(int i=rbeg+1;i<rend;++i){
            res.poses[i] = i;
            res.hits[i] = false;
        }
        return res;
    }

    public StepEvent makeCrushEvent(ArrayList<Pair<Integer,Integer>> airs){
        StepEvent res = new StepEvent();
        res.init(airs.size());
        for(int i=0;i<airs.size();++i){
            res.hits[i] = true;
            res.players[i] = airs.get(i).first;
            res.airs[i] = airs.get(i).second;
        }
        return res;
    }

    public StepEvent makeJumpEvent(int iplayer, int rpos){
        StepEvent res = new StepEvent();
        res.init(1);
        res.poses[0] = rpos + 4;
        res.hits[0] = false;

        ArrayList<Pair<Integer,Integer>> airs = getCrushAir(iplayer, rpos2apos(iplayer, rpos+4));
        if(airs.size() != 0){
            StepEvent res1 = makeCrushEvent(airs);
            res = merge(res, res1);
        }

        return res;
    }

    public StepEvent makeFlyEvent(int iplayer, int rpos){
        StepEvent res = new StepEvent();
        res.init(1);
        res.poses[0] = FLYTOPOS;
        res.hits[0] = false;

        int apos = (playerPos[iplayer]+2)%4 * 5 + 54;

        ArrayList<Pair<Integer,Integer>> airs = getCrushAir(iplayer, apos);
        if(airs.size() != 0){
            StepEvent res1 = makeCrushEvent(airs);
            res = merge(res, res1);
        }

        return res;
    }

    public boolean isJump(int rpos){
        if(rpos<0 || rpos>=OUTENDPOS)
            return false;
        return rpos%4 == 1;
    }

    public boolean isFly(int rpos){
        return rpos == FLYPOS;
    }

    public int whoWin(){
        for(int i=0;i<num_players;++i){
            int j;
            for(j=0;j<4;++j)
                if(airPos[i][j] != AIREND)
                    break;
            if(j==4)
                return i;
        }
        return -1;
    }
}
