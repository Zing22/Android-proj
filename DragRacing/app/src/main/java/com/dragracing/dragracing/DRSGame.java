package com.dragracing.dragracing;

import android.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

//单机游戏类

public class DRSGame {
    public enum GameState{OFF,READY,PLAYING,END}//游戏状态
    public enum PlayerType{EMPTY,PEOPLE,AI,INTERPEOPLE}//玩家类型
    public enum TurnState{WAIT_DICE,WAIT_AIR,WAIT_INTER_DICE,WAIT_INTER_AIR,ANIMATING,OTHER}//当前回合状态
    public int[] READYDICE = {2,4,6};//能起机的骰子数

    //一些重要的相对坐标
    //public int OUTBEGPOS = 0;//外圈第一个点
    public int OUTENDPOS = 49;//外圈最后一个点
    public int INBEGPOS = 50;//内部直路第一个点
    public int INENDPOS = 54;//内部直路最后一个点
    public int FLYPOS = 17;//可以飞的那个点
    public int FLYTOPOS = 29;//飞到的那个点
    public int FLYCRUSHPOS = 52;//飞的时候可以撞的那个点

    //一些飞机的状态
    public int AIRREADY = -1;//起机点
    public int AIROFF = -2;//未起机
    public int AIREND = -3;//已完成
    public int AIRWIN = -4;//胜利点(就是中心那里)

    //游戏全局数据
    public GameState gameState;//游戏状态
    public int num_players;//游戏人数
    public int[] playerPos;//玩家所在的位置(左上,右上,右下,左下)
    public PlayerType[] playerType;//玩家类型
    public int[][] airPos;//飞机坐标(或状态)
    public Random randomDice;//随机产生器

    //游戏回合数据
    public TurnState turnState;//回合状态
    public int cur_turn;//当前的回合数
    public int cur_player;//当前玩家
    public int cur_air;//当前选择的飞机(记录玩家选择的飞机)
    public int cur_dice;//当前的骰子数(记录玩家选择飞机时的点数)

    //事件集合类(用于记录单次飞行操作的所有事件)
    public class StepEvent{
        public int num_event;//事件数
        public int[] poses;//发生的相对坐标
        public Boolean[] hits;//是否是攻击事件
        public int[] players;//被攻击的玩家
        public int[] airs;//被攻击的飞机

        //初始化数组
        public void init(int num){
            num_event = num;
            poses = new int[num_event];
            hits = new Boolean[num_event];
            players = new int[num_event];
            airs = new int[num_event];
        }
    }
    //事件合并函数
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

    //构造函数
    public DRSGame(){
        gameState = GameState.OFF;
        turnState = TurnState.OTHER;
    }

    //让游戏进入准备状态
    //param
    // players:四个位置的玩家类型
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

    //让游戏进入开始状态
    public void doPlay(){
        cur_turn = 1;
        cur_player = 0;
        cur_air = -1;
        cur_dice = -1;
        gameState = GameState.PLAYING;
    }

    //游戏轮到下一个玩家(外部调用,DRGame本身不决定是否要轮到下一个玩家)
    public void nextStep(){
        cur_player++;
        if(cur_player == num_players) {
            cur_player = 0;
            cur_turn++;
        }

        cur_air = -1;
        cur_dice = -1;
    }

    //游戏执行一次飞行操作
    //param
    // iAir:选择的飞机id
    // dice:骰子数(游戏本身不记录未确定执行的骰子数,因此需要参数传入)
    //return:这次飞行操作产生的事件
    public StepEvent doStep(int iAir, int dice){
        StepEvent res = new StepEvent();

        cur_air = iAir;
        cur_dice = dice;

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
                res1.init(2);
                res1.poses[0] = AIRWIN;
                res1.hits[0] = false;
                res1.poses[1] = AIREND;
                res1.hits[1] = false;
                res = merge(res, res1);

                airPos[cur_player][cur_air] = AIREND;

                if(whoWin() != -1)
                    gameState = GameState.END;
            }
        }

        for(int i=0;i<res.num_event;++i)
            if(res.hits[i])
                airPos[res.players[i]][res.airs[i]] = AIROFF;

        return res;
    }

    //获取当前玩家的类型
    public PlayerType getCurPlayerType(){
        return playerType[cur_player];
    }

    //获取可选择的飞机(不可选择的比如不能起机的,已完成飞行的)
    //param
    // dice:骰子点数(点数可以影响飞机是否能起机)
    //return 可选择的飞机列表
    public ArrayList<Integer> getCandidateAir(int dice){
        ArrayList<Integer> res = new ArrayList<Integer>();

        for(int i=0;i<4;++i){
            if(airPos[cur_player][i] == AIROFF){
                for(int j=0;j<READYDICE.length;++j)
                    if(READYDICE[j] == dice){
                        res.add(i);
                        break;
                    }
            }
            else if(airPos[cur_player][i] == AIREND)
                continue;
            else
                res.add(i);
        }

        return res;
    }

    //获取一个随机的骰子点数
    public int getDice(){
        return randomDice.nextInt(6)+1;
    }

    //相对坐标转绝对坐标
    //param
    // iplayer:玩家id(不同玩家的相对坐标对应了不同的绝对坐标)
    // rpos:相对坐标
    //return 绝对坐标
    public int rpos2apos(int iplayer, int rpos){
        if(rpos < 0)
            return rpos;
        else if(rpos <= OUTENDPOS)
            return (playerPos[iplayer]*13+rpos)%52;
        else
            return playerPos[iplayer]*5+52 + rpos-INBEGPOS;
    }

    //获取某架飞机的绝对坐标
    //param
    // iplayer:玩家id
    // iAir:飞机id
    //return 这架飞机的绝对坐标
    public int getapos(int iplayer, int iAir){
        return rpos2apos(iplayer, airPos[iplayer][iAir]);
    }

    //获取某个绝对坐标下的所有飞机
    //param
    // apos:绝对坐标
    //return 这个绝对坐标下的所有飞机(pair里分别是玩家id和飞机id)
    public ArrayList<Pair<Integer,Integer>> getAir(int apos){
        ArrayList<Pair<Integer,Integer>> res = new ArrayList<>();

        for(int i=0;i<num_players;++i)
            for(int j=0;j<4;++j)
                if(rpos2apos(i, airPos[i][j]) == apos)
                    res.add(Pair.create(i,j));
        return res;
    }

    //获取某个玩家可在某个绝对坐标下能撞的所有飞机
    //param
    // iplayer:玩家id
    // apos:绝对坐标
    //return 能撞的所有飞机(pair里分别是玩家id和飞机id)
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

    //返回一个正常行走的事件集合
    //param
    // rbeg:起点相对坐标
    // rend:终点相对坐标
    //return 事件集合
    public StepEvent makeGoEvent(int rbeg, int rend){
        StepEvent res = new StepEvent();
        res.init(rend-rbeg);
        for(int i=rbeg+1;i<=rend;++i){
            res.poses[i-rbeg-1] = i;
            res.hits[i-rbeg-1] = false;
        }
        return res;
    }

    //返回撞击的事件集合
    //param
    // airs:所有被撞击的飞机(pair里分别是玩家id和飞机id)
    //return 事件集合
    public StepEvent makeCrushEvent(ArrayList<Pair<Integer,Integer>> airs){
        StepEvent res = new StepEvent();
        res.init(airs.size());
        for(int i=0;i<airs.size();++i){
            res.poses[i] = airPos[airs.get(i).first][airs.get(i).second];
            res.hits[i] = true;
            res.players[i] = airs.get(i).first;
            res.airs[i] = airs.get(i).second;
        }
        return res;
    }

    //返回跳所产生的事件集合(包括跳和撞击)
    //param
    // iplayer:玩家id
    // rpos:跳之前的相对坐标
    //return 事件集合
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

    //返回飞所产生的事件集合(包括飞和撞击)
    //param
    // iplayer:玩家id
    // rpos:飞之前的相对坐标
    //return 事件集合
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

    //确定某个相对坐标是否能跳
    //param
    // rpos:相对坐标
    //return 是否能跳
    public boolean isJump(int rpos){
        if(rpos<0 || rpos>=OUTENDPOS)
            return false;
        return rpos%4 == 1;
    }

    //确定某个相对坐标是否能飞
    //param
    // rpos:相对坐标
    //return 是否能飞
    public boolean isFly(int rpos){
        return rpos == FLYPOS;
    }

    //返回谁赢(没有赢返回-1)
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

    //屏幕百分比坐标

    //基地的百分比坐标
    double[][][] ppos_off = {
            {{7.2,6.4},{17,6.4},{7.3,16},{17,16}},
            {{78.3,6.8},{88.1,6.8},{78.4,16.3},{88.1,16.3}},
            {{77.9,77.5},{87.7,77.5},{77.8,87.1},{87.7,87.1}},
            {{7.2,77.5},{17.2,77.5},{7.4,87.1},{17.2,87.1}}
    };
    //起机的百分比坐标
    double[][] ppos_ready = {
            {4.2,25.4},{68.5,3.5},{90.4,68.5},{26.5,90.5}
    };
    //所有外圈和内直路的绝对坐标对应的百分比坐标
    double[][] ppos_apos = {
            {8.8,30.7},{14.8,28},{20.3,28},{26,30},{30.5,26},
            {28.5,19.7},{28.5,14.4},{30.5,8.4},{36.5,6.3},{42.1,6.3},
            {47.6,6.3},{53,6.3},{58.2,6.3},{64.2,8.3},{66.5,14.6},
            {66.5,20},{64.5,26},{68.5,30.3},{74.8,28.3},{80.2,28.3},
            {86,30.5},{88,36.4},{88,41.7},{88,47},{88,52.5},
            {88,57.9},{86,63.9},{80,66},{74.7,66},{68.7,64},
            {64.2,68.2},{66.7,74.2},{66.7,79.7},{64.2,85.7},{58.3,88},
            {52.9,88},{47.5,88},{42.1,88},{36.7,88},{30.7,86},
            {28.7,79.8},{28.7,74.2},{30.7,68.2},{25.9,64.5},{20.2,66.2},
            {14.8,66.2},{8.8,64.2},{6.8,58.2},{6.8,52.6},{6.8,47.1},
            {6.8,41.7},{6.8,36.2},
            {14.9,47.1},{20.5,47.1},{25.9,47.1},{31.3,47.1},{36.7,47.1},
            {47.6,15.3},{47.6,20.6},{47.6,26.1},{47.6,31.4},{47.6,36.6},
            {79,47},{73.8,47},{68.6,47},{63.4,47},{58.2,47},
            {47.5,80},{47.5,74.6},{47.5,69.2},{47.5,63.8},{47.5,58.4},
    };
    //胜利点的百分比坐标
    double[][] ppos_win = {
            {42.1,47.1},{47.6,41.8},{53,47},{47.5,53}
    };
}
