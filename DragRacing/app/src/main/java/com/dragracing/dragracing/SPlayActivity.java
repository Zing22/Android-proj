package com.dragracing.dragracing;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cunoraz.gifview.library.GifView;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.jero.multiviewanimation_library.AnimManager;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

//单机游戏界面

public class SPlayActivity extends AppCompatActivity {
    DRSGame drsGame;//游戏类
    Button btn_dice;//骰子按钮
    Button[] btn_airs;//飞机按钮
    //所有飞机贴图的id
    int[][] air_id = {
            {R.id.air_1_1_splay, R.id.air_1_2_splay, R.id.air_1_3_splay, R.id.air_1_4_splay},
            {R.id.air_2_1_splay, R.id.air_2_2_splay, R.id.air_2_3_splay, R.id.air_2_4_splay},
            {R.id.air_3_1_splay, R.id.air_3_2_splay, R.id.air_3_3_splay, R.id.air_3_4_splay},
            {R.id.air_4_1_splay, R.id.air_4_2_splay, R.id.air_4_3_splay, R.id.air_4_4_splay}
    };
    //所有骰子贴图的id
    int[] dice_id = {0, R.drawable.dice1, R.drawable.dice2, R.drawable.dice3, R.drawable.dice4, R.drawable.dice5, R.drawable.dice6};
    ImageView[][] image_airs;//飞机贴图对象
    FrameLayout layout_airs;//游戏棋盘的布局
    ImageButton imageButton_dice;//骰子按钮图片
    ImageView image_dice;//骰子图片

    int cur_dice;//当前骰子点数
    int cur_air;//当前选择的飞机

    DRSGame.StepEvent cur_events;
    int cur_event_ix;
    int s1;

    ClickDiceTask clickDiceTask;
    ClickAirTask clickAirTask;
    PlayTask playTask;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splay);

        //句柄获取
        //骰子按钮
        btn_dice = (Button) this.findViewById(R.id.button_dice_splay);
        //飞机按钮
        int[] btn_airs_id = {R.id.button_air1_splay, R.id.button_air2_splay, R.id.button_air3_splay, R.id.button_air4_splay};
        btn_airs = new Button[4];
        for (int i = 0; i < 4; ++i)
            btn_airs[i] = (Button) this.findViewById(btn_airs_id[i]);
        //飞机贴图
        image_airs = new ImageView[4][4];
        for (int i = 0; i < 4; ++i)
            for (int j = 0; j < 4; ++j)
                image_airs[i][j] = (ImageView) this.findViewById(air_id[i][j]);
        //棋盘布局
        layout_airs = (FrameLayout) this.findViewById(R.id.layout_airs_splay);
        //骰子按钮贴图
        imageButton_dice = (ImageButton) this.findViewById(R.id.imageButton_dice);
        //骰子贴图
        image_dice = (ImageView) this.findViewById(R.id.image_dice);

        //事件设置
        //骰子按钮
        btn_dice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_dice.setEnabled(false);
                clickDiceTask = new ClickDiceTask();
                clickDiceTask.execute(SPlayActivity.this);
            }
        });
        //飞机按钮
        for (int i = 0; i < 4; ++i) {
            final int ix = i;
            btn_airs[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (int i = 0; i < 4; ++i)
                        btn_airs[i].setEnabled(false);
                    cur_air = ix;
                    clickAirTask = new ClickAirTask();
                    clickAirTask.execute(SPlayActivity.this);
                }
            });
        }
        //骰子图片按钮
        imageButton_dice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cur_dice = -1;
                clickDiceTask = new ClickDiceTask();
                clickDiceTask.execute(SPlayActivity.this);
            }
        });
        imageButton_dice.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SPlayActivity.this);
                final EditText editText = new EditText(SPlayActivity.this);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                builder.setView(editText);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        cur_dice = Integer.parseInt(editText.getText().toString());
                        clickDiceTask = new ClickDiceTask();
                        clickDiceTask.execute(SPlayActivity.this);
                    }
                });

                builder.create().show();
                return true;
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
        DRSGame.PlayerType[] playerType = (DRSGame.PlayerType[]) intent.getSerializableExtra("playerType");
        String[] playerNames = new String[playerType.length];
        for (int i = 0, cntAI = 1; i < playerType.length; ++i) {
            if (playerType[i] == DRSGame.PlayerType.PEOPLE) {
                playerNames[i] = "You";
            } else if (playerType[i] == DRSGame.PlayerType.AI) {
                playerNames[i] = "AI" + cntAI;
                ++cntAI;
            }
        }

        //开始游戏
        drsGame = new DRSGame();
        drsGame.doReady(playerType);
        drsGame.setPlayerNames(playerNames);
        drsGame.doPlay();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    //窗口加载完后,加载飞机贴图,开始执行游戏
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            for (int i = 0; i < drsGame.num_players; ++i) {
                for (int j = 0; j < 4; ++j) {
                    //final int ii=i;
                    //final int jj=j;

                    setAirImage(i, j);
                    image_airs[i][j].setVisibility(ImageView.VISIBLE);
//                    image_airs[i][j].setOnClickListener(new View.OnClickListener(){
//                        @Override
//                        public void onClick(View view) {
//                            slotClickAir(ii, jj);
//                        }
//                    });
                }
            }
            setButtonEnable();
            updateTitle();

            if (drsGame.getCurPlayerType() == DRSGame.PlayerType.AI) {
                updateDice(true, false, false);
                playTask = new PlayTask();
                playTask.execute(SPlayActivity.this);
            } else if (drsGame.getCurPlayerType() == DRSGame.PlayerType.INTERPEOPLE) {
                updateDice(true, false, false);
            } else if (drsGame.getCurPlayerType() == DRSGame.PlayerType.PEOPLE) {
                updateDice(true, false, true);
            }
        }
    }

    //飞机点击槽函数
//    public void slotClickAir(int iplayer, int iAir){
//        if(drsGame.turnState == DRSGame.TurnState.WAIT_AIR && drsGame.cur_player == iplayer){
//            ArrayList<Integer> airs = drsGame.getCandidateAir(cur_dice);
//            if(airs.contains(iAir)){
//                cur_air = iAir;
//                clickAirTask = new ClickAirTask();
//                clickAirTask.execute(SPlayActivity.this);
//            }
//        }
//    }

    //设置骰子按钮和飞机按钮是否可点击
    public void setButtonEnable() {
        if (drsGame.turnState == DRSGame.TurnState.WAIT_DICE) {
            btn_dice.setEnabled(true);
            for (int i = 0; i < 4; ++i)
                btn_airs[i].setEnabled(false);
        } else if (drsGame.turnState == DRSGame.TurnState.WAIT_AIR) {
            btn_dice.setEnabled(false);
            for (int i = 0; i < 4; ++i)
                btn_airs[i].setEnabled(false);
            ArrayList<Integer> candidateAirs = drsGame.getCandidateAir(cur_dice);
            for (int i = 0; i < candidateAirs.size(); ++i)
                btn_airs[candidateAirs.get(i)].setEnabled(true);
        } else {
            btn_dice.setEnabled(false);
            for (int i = 0; i < 4; ++i)
                btn_airs[i].setEnabled(false);
        }
    }

    //设置游戏结束
    public void setGameEnd() {
        //need a game record save
        try {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(System.currentTimeMillis());
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            int hour = c.get(Calendar.HOUR);
            int minute = c.get(Calendar.MINUTE);
            int second = c.get(Calendar.SECOND);

            String filename = String.format("record-%d-%d-%d-%d-%d-%d.txt", year, month, day, hour, minute, second);
            FileOutputStream out = openFileOutput(filename, Context.MODE_PRIVATE);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

            DRSGame.GameRecord gameRecord = drsGame.gameRecord;
            for (int i = 0; i < 4; ++i)
                writer.write(gameRecord.names[i] + "\n");
            for (int i = 0; i < gameRecord.iplayers.size(); ++i)
                writer.write(String.format("%d %d %d\n", gameRecord.iplayers.get(i), gameRecord.dices.get(i), gameRecord.iAirs.get(i)));

            writer.close();
        } catch (IOException e) {
            Log.e("SPlayActivity", "JH:game end IO error!");
        }

        String winner = drsGame.playerNames[drsGame.whoWin()];
        AlertDialog.Builder builder = new AlertDialog.Builder(SPlayActivity.this);
        builder.setTitle("游戏结束");
        builder.setMessage(winner + "赢了!");
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
    public void setAirImage(int iplayer, int iAir) {
        Log.d("SPlayActivity", String.format("JH:setAirImage %d %d", iplayer, iAir));
        double x;
        double y;
        if (drsGame.airPos[iplayer][iAir] == drsGame.AIROFF) {
            x = drsGame.ppos_off[drsGame.playerPos[iplayer]][iAir][0];
            y = drsGame.ppos_off[drsGame.playerPos[iplayer]][iAir][1];
        } else if (drsGame.airPos[iplayer][iAir] == drsGame.AIRREADY) {
            x = drsGame.ppos_ready[drsGame.playerPos[iplayer]][0];
            y = drsGame.ppos_ready[drsGame.playerPos[iplayer]][1];
        } else if (drsGame.airPos[iplayer][iAir] == drsGame.AIRWIN) {
            x = drsGame.ppos_win[drsGame.playerPos[iplayer]][0];
            y = drsGame.ppos_win[drsGame.playerPos[iplayer]][1];
        } else if (drsGame.airPos[iplayer][iAir] == drsGame.AIREND) {
            x = drsGame.ppos_off[drsGame.playerPos[iplayer]][iAir][0];
            y = drsGame.ppos_off[drsGame.playerPos[iplayer]][iAir][1];
        } else {
            x = drsGame.ppos_apos[drsGame.getapos(iplayer, iAir)][0];
            y = drsGame.ppos_apos[drsGame.getapos(iplayer, iAir)][1];
        }
        x = x / 100 * layout_airs.getWidth();
        y = y / 100 * layout_airs.getHeight();
        float scaleX = Float.valueOf(getResources().getString(R.string.image_air_scaleX));
        float scaleY = Float.valueOf(getResources().getString(R.string.image_air_scaleY));
        x -= image_airs[drsGame.playerPos[iplayer]][iAir].getWidth() * (1 - scaleX) / 2;
        y -= image_airs[drsGame.playerPos[iplayer]][iAir].getHeight() * (1 - scaleY) / 2;
        image_airs[drsGame.playerPos[iplayer]][iAir].setX((float) x);
        image_airs[drsGame.playerPos[iplayer]][iAir].setY((float) y);

        //Log.i("SPlayActivity", String.format("JH:%d %d %f %f", layout_airs.getWidth(), layout_airs.getHeight(), x, y));
        //Log.i("SPlayActivity", String.format("JH:%d %d %f %f", layout_airs.getWidth(), layout_airs.getHeight(), x, y));
        //image_airs[0][0].layout((int)x, (int)y, 0, 0);
    }

    //获取end像素坐标
    public Pair<Float, Float> getAirEndXYPos(int iplayer, int iAir) {
        double x;
        double y;
        if (drsGame.airPos[iplayer][iAir] == drsGame.AIROFF) {
            x = drsGame.ppos_off[drsGame.playerPos[iplayer]][iAir][0];
            y = drsGame.ppos_off[drsGame.playerPos[iplayer]][iAir][1];
        } else if (drsGame.airPos[iplayer][iAir] == drsGame.AIRREADY) {
            x = drsGame.ppos_ready[drsGame.playerPos[iplayer]][0];
            y = drsGame.ppos_ready[drsGame.playerPos[iplayer]][1];
        } else if (drsGame.airPos[iplayer][iAir] == drsGame.AIRWIN) {
            x = drsGame.ppos_win[drsGame.playerPos[iplayer]][0];
            y = drsGame.ppos_win[drsGame.playerPos[iplayer]][1];
        } else if (drsGame.airPos[iplayer][iAir] == drsGame.AIREND) {
            x = drsGame.ppos_off[drsGame.playerPos[iplayer]][iAir][0];
            y = drsGame.ppos_off[drsGame.playerPos[iplayer]][iAir][1];
        } else {
            x = drsGame.ppos_apos[drsGame.getapos(iplayer, iAir)][0];
            y = drsGame.ppos_apos[drsGame.getapos(iplayer, iAir)][1];
        }
        x = x / 100 * layout_airs.getWidth();
        y = y / 100 * layout_airs.getHeight();
        float scaleX = Float.valueOf(getResources().getString(R.string.image_air_scaleX));
        float scaleY = Float.valueOf(getResources().getString(R.string.image_air_scaleY));
        x -= image_airs[drsGame.playerPos[iplayer]][iAir].getWidth() * (1 - scaleX) / 2;
        y -= image_airs[drsGame.playerPos[iplayer]][iAir].getHeight() * (1 - scaleY) / 2;
        return Pair.create((float) x, (float) y);
    }

    //获取beg像素坐标
    public Pair<Float, Float> getAirBegXYPos(int iplayer, int iAir) {
        float x = image_airs[drsGame.playerPos[iplayer]][iAir].getX();
        float y = image_airs[drsGame.playerPos[iplayer]][iAir].getY();
        return Pair.create(x, y);
    }

    //获取像素坐标
    public Pair<Float, Float> getAirXYPos(int iplayer, int iAir, int rpos) {
        double x;
        double y;
        if (rpos == drsGame.AIROFF) {
            x = drsGame.ppos_off[drsGame.playerPos[iplayer]][iAir][0];
            y = drsGame.ppos_off[drsGame.playerPos[iplayer]][iAir][1];
        } else if (rpos == drsGame.AIRREADY) {
            x = drsGame.ppos_ready[drsGame.playerPos[iplayer]][0];
            y = drsGame.ppos_ready[drsGame.playerPos[iplayer]][1];
        } else if (rpos == drsGame.AIRWIN) {
            x = drsGame.ppos_win[drsGame.playerPos[iplayer]][0];
            y = drsGame.ppos_win[drsGame.playerPos[iplayer]][1];
        } else if (rpos == drsGame.AIREND) {
            x = drsGame.ppos_off[drsGame.playerPos[iplayer]][iAir][0];
            y = drsGame.ppos_off[drsGame.playerPos[iplayer]][iAir][1];
        } else {
            x = drsGame.ppos_apos[drsGame.rpos2apos(iplayer, rpos)][0];
            y = drsGame.ppos_apos[drsGame.rpos2apos(iplayer, rpos)][1];
            Log.d("SPlayActivity", "JH:apos:" + String.valueOf(drsGame.rpos2apos(iplayer, rpos)));
        }
        x = x / 100 * layout_airs.getWidth();
        y = y / 100 * layout_airs.getHeight();
        float scaleX = Float.valueOf(getResources().getString(R.string.image_air_scaleX));
        float scaleY = Float.valueOf(getResources().getString(R.string.image_air_scaleY));
        x -= image_airs[drsGame.playerPos[iplayer]][iAir].getWidth() * (1 - scaleX) / 2;
        y -= image_airs[drsGame.playerPos[iplayer]][iAir].getHeight() * (1 - scaleY) / 2;
        return Pair.create((float) x, (float) y);
    }

    //更新标题
    public void updateTitle() {
        String name = drsGame.playerNames[drsGame.cur_player];
        setTitle(name + "的回合");
    }

    //更新骰子按钮和图片
    //param
    // fa:是否显示骰子gif
    // fb:是否显示骰子结果
    // fc:骰子gif是否可点
    public void updateDice(Boolean fa, Boolean fb, Boolean fc) {
        if (fa) {
            imageButton_dice.setVisibility(View.VISIBLE);
            imageButton_dice.setEnabled(fc);
        } else {
            imageButton_dice.setVisibility(View.INVISIBLE);
        }

        if (fb) {
            if (cur_dice >= 1 && cur_dice <= 6)
                image_dice.setImageResource(dice_id[cur_dice]);
            image_dice.setVisibility(View.VISIBLE);
        } else {
            image_dice.setVisibility(View.INVISIBLE);
        }
    }

    //执行一次AI的飞行,返回事件集
    public DRSGame.StepEvent st_play() {
        //assert now turn to ai
        DRSGame.StepEvent events;
        int dice = drsGame.getDice();
        ArrayList<Integer> airs = drsGame.getCandidateAir(dice);

        if (airs.size() == 0) {
            events = new DRSGame.StepEvent();
            events.init(0);
        } else {
            cur_dice = dice;
            cur_air = airs.get(0);
            events = drsGame.doStep(cur_air, cur_dice);
        }

        return events;
    }

    public AnimatorSet makeAnimatorSet(DRSGame.StepEvent events) {
        AnimatorSet animatorSet = new AnimatorSet();
        Animator animatorLast = null;
        int rposLast = -1;
        for (int i = 0; i < events.num_event; ++i) {
            if (events.hits[i]) {
                int iplayer = events.players[i];
                int iAir = events.airs[i];
                Pair<Float, Float> begPos = getAirBegXYPos(iplayer, iAir);
                Pair<Float, Float> endPos = getAirEndXYPos(iplayer, iAir);
                ObjectAnimator animatorX = ObjectAnimator.ofFloat(image_airs[drsGame.playerPos[iplayer]][iAir], "translationX", begPos.first, endPos.first);
                ObjectAnimator animatorY = ObjectAnimator.ofFloat(image_airs[drsGame.playerPos[iplayer]][iAir], "translationY", begPos.second, endPos.second);
                animatorX.setDuration(1000);
                animatorY.setDuration(1000);
                if (animatorLast == null) {
                    animatorSet.play(animatorX);
                    animatorSet.play(animatorY).with(animatorX);
                    animatorLast = animatorX;
                } else {
                    animatorSet.play(animatorX).after(animatorLast);
                    animatorSet.play(animatorY).with(animatorX);
                    animatorLast = animatorX;
                }
            } else {
                int iplayer = drsGame.cur_player;
                int iAir = drsGame.cur_air;
                int rpos = events.poses[i];
                Pair<Float, Float> begPos;

                Log.d("SPlayActivity", String.format("JH:%d %d %d", i, rposLast, rpos));

                if (rposLast == -1)
                    begPos = getAirXYPos(iplayer, iAir, rpos - 1);
                else
                    begPos = getAirXYPos(iplayer, iAir, rposLast);
                Pair<Float, Float> endPos = getAirXYPos(iplayer, iAir, rpos);
                rposLast = rpos;

                ObjectAnimator animatorX = ObjectAnimator.ofFloat(image_airs[drsGame.playerPos[iplayer]][iAir], "translationX", begPos.first, endPos.first);
                ObjectAnimator animatorY = ObjectAnimator.ofFloat(image_airs[drsGame.playerPos[iplayer]][iAir], "translationY", begPos.second, endPos.second);
                Log.d("SPlayActivity", String.format("JH:%f %f %f %f", begPos.first, begPos.second, endPos.first, endPos.second));
                animatorX.setDuration(1000);
                animatorY.setDuration(1000);
                if (animatorLast == null) {
                    animatorSet.play(animatorX);
                    animatorSet.play(animatorY).with(animatorX);
                    animatorLast = animatorX;
                } else {
                    animatorSet.play(animatorX).after(animatorLast);
                    animatorSet.play(animatorY).with(animatorX);
                    animatorLast = animatorX;
                }
            }
        }
        return animatorSet;
    }

    public Animation makeAnimation(int ix) {
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
        animation = new TranslateAnimation(begPos.first, endPos.first, begPos.second, endPos.second);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ++cur_event_ix;
                if (cur_event_ix == cur_events.num_event) {
                    s1 = 0;
                } else {
                    Animation animation1 = makeAnimation(cur_event_ix);
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

    //执行动画
    public void startAnimation(Animation animation) {
        int iplayer;
        int iAir;
        if (cur_events.hits[cur_event_ix]) {
            iplayer = cur_events.players[cur_event_ix];
            iAir = cur_events.airs[cur_event_ix];
        } else {
            iplayer = drsGame.cur_player;
            iAir = drsGame.cur_air;
        }
        image_airs[drsGame.playerPos[iplayer]][iAir].startAnimation(animation);
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("SPlay Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    //AI任务类,处理AI回合
    class PlayTask extends AsyncTask<SPlayActivity, DRSGame.StepEvent, Boolean> {
        int ANITYPE_DICE = -1;
        int ANITYPE_AIR = -2;

        SPlayActivity sPlayActivity;
        int aniType;
        //int s1;//信号量

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Boolean doInBackground(SPlayActivity... params) {
            sPlayActivity = params[0];

            while (sPlayActivity.drsGame.getCurPlayerType() == DRSGame.PlayerType.AI) {
                SystemClock.sleep(500);
                DRSGame.StepEvent events = sPlayActivity.st_play();
                aniType = ANITYPE_DICE;
                doEvent(events);
                SystemClock.sleep(500);
                aniType = ANITYPE_AIR;
                doEvent(events);

                if (sPlayActivity.drsGame.gameState == DRSGame.GameState.END)
                    return false;
                if (sPlayActivity.drsGame.cur_dice != 6)
                    drsGame.nextStep();
            }

            return true;
        }

        @Override
        protected void onProgressUpdate(DRSGame.StepEvent... values) {
            DRSGame.StepEvent events = values[0];
            if (aniType == ANITYPE_DICE) {
                sPlayActivity.updateTitle();
                sPlayActivity.btn_dice.setText("Dice" + String.valueOf(sPlayActivity.cur_dice));
                sPlayActivity.updateDice(false, true, false);
                s1 = 0;
            } else if (aniType == ANITYPE_AIR) {
                sPlayActivity.updateDice(false, false, false);
                if (events.num_event > 0) {
                    //AnimatorSet animatorSet = makeAnimatorSet(events);
                    //animatorSet.start();
                    cur_events = events;
                    cur_event_ix = 0;

                    Animation animation = makeAnimation(0);
                    startAnimation(animation);
                    /*
                    sPlayActivity.setAirImage(sPlayActivity.drsGame.cur_player, sPlayActivity.drsGame.cur_air);
                    for (int i = 0; i < events.num_event; ++i)
                        if (events.hits[i])
                            sPlayActivity.setAirImage(events.players[i], events.airs[i]);*/
                } else {
                    s1 = 0;
                }
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            sPlayActivity.btn_dice.setText("Dice");
            if (result) {
                sPlayActivity.setButtonEnable();
                sPlayActivity.updateTitle();
                if (sPlayActivity.drsGame.getCurPlayerType() == DRSGame.PlayerType.PEOPLE)
                    sPlayActivity.updateDice(true, false, true);
                else
                    sPlayActivity.updateDice(true, false, false);
            } else
                sPlayActivity.setGameEnd();
        }

        public void doEvent(DRSGame.StepEvent events) {
            s1 = 1;
            publishProgress(events);
            while (s1 != 0)
                SystemClock.sleep(50);
        }
    }

    //骰子点击任务类,处理骰子点击后的操作
    class ClickDiceTask extends AsyncTask<SPlayActivity, Void, ArrayList<Integer>> {
        SPlayActivity sPlayActivity;
        //int s1;//信号量

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected ArrayList<Integer> doInBackground(SPlayActivity... params) {
            SystemClock.sleep(500);
            sPlayActivity = params[0];

            if (sPlayActivity.cur_dice == -1)
                sPlayActivity.cur_dice = sPlayActivity.drsGame.getDice();

            publishProgress();
            SystemClock.sleep(1500);

            ArrayList<Integer> airs = drsGame.getCandidateAir(sPlayActivity.cur_dice);
            if (airs.size() == 0) {
                sPlayActivity.drsGame.nextStep();
            } else {
                sPlayActivity.drsGame.turnState = DRSGame.TurnState.WAIT_AIR;
            }

            return airs;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            sPlayActivity.updateDice(false, true, false);
        }

        @Override
        protected void onPostExecute(ArrayList<Integer> result) {
            sPlayActivity.btn_dice.setText("Dice" + String.valueOf(sPlayActivity.cur_dice));
            sPlayActivity.setButtonEnable();
            sPlayActivity.updateTitle();
            if (result.size() == 0) {
                if (sPlayActivity.drsGame.getCurPlayerType() == DRSGame.PlayerType.AI) {
                    sPlayActivity.updateDice(true, false, false);
                    sPlayActivity.playTask = new PlayTask();
                    sPlayActivity.playTask.execute(sPlayActivity);
                } else if (sPlayActivity.drsGame.getCurPlayerType() == DRSGame.PlayerType.PEOPLE) {
                    sPlayActivity.updateDice(true, false, true);
                } else if (sPlayActivity.drsGame.getCurPlayerType() == DRSGame.PlayerType.INTERPEOPLE) {
                    sPlayActivity.updateDice(true, false, false);
                }
            } else {
                sPlayActivity.updateDice(false, true, false);
            }
        }
    }

    //飞机点击任务类,用于处理点击飞机按钮后的操作
    class ClickAirTask extends AsyncTask<SPlayActivity, DRSGame.StepEvent, Boolean> {
        SPlayActivity sPlayActivity;
        //int s1;//信号量

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Boolean doInBackground(SPlayActivity... params) {
            SystemClock.sleep(500);
            sPlayActivity = params[0];

            DRSGame.StepEvent events = sPlayActivity.drsGame.doStep(sPlayActivity.cur_air, sPlayActivity.cur_dice);
            s1 = 1;
            publishProgress(events);
            while (s1 != 0)
                SystemClock.sleep(50);

            Log.d("SPlayActivity", "JH:click air bg");

            if (sPlayActivity.drsGame.gameState == DRSGame.GameState.END)
                return false;
            if (sPlayActivity.drsGame.cur_dice != 6)
                drsGame.nextStep();
            else
                sPlayActivity.drsGame.turnState = DRSGame.TurnState.WAIT_DICE;

            return true;
        }

        @Override
        protected void onProgressUpdate(DRSGame.StepEvent... values) {
            sPlayActivity.updateDice(false, false, false);
            DRSGame.StepEvent events = values[0];

            cur_events = events;
            cur_event_ix = 0;

            Animation animation = makeAnimation(0);
            startAnimation(animation);

            //AnimatorSet animatorSet = makeAnimatorSet(events);
            //animatorSet.start();

            /*sPlayActivity.setAirImage(sPlayActivity.drsGame.cur_player, sPlayActivity.drsGame.cur_air);
            for(int i=0;i<events.num_event;++i)
                if(events.hits[i])
                    sPlayActivity.setAirImage(events.players[i], events.airs[i]);*/
        }

        @Override
        protected void onPostExecute(Boolean result) {
            sPlayActivity.btn_dice.setText("Dice");
            if (result) {
                sPlayActivity.setButtonEnable();
                sPlayActivity.updateTitle();
                if (sPlayActivity.drsGame.getCurPlayerType() == DRSGame.PlayerType.AI) {
                    sPlayActivity.updateDice(true, false, false);
                    sPlayActivity.playTask = new PlayTask();
                    sPlayActivity.playTask.execute(sPlayActivity);
                } else if (sPlayActivity.drsGame.getCurPlayerType() == DRSGame.PlayerType.PEOPLE) {
                    sPlayActivity.updateDice(true, false, true);
                } else if (sPlayActivity.drsGame.getCurPlayerType() == DRSGame.PlayerType.INTERPEOPLE) {
                    sPlayActivity.updateDice(true, false, false);
                }
            } else
                sPlayActivity.setGameEnd();
        }
    }
}
