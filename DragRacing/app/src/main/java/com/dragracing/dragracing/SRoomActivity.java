package com.dragracing.dragracing;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.Serializable;

//单机游戏房间中的界面

public class SRoomActivity extends AppCompatActivity {
    final DRSGame.PlayerType[] playerType = new DRSGame.PlayerType[4];//四个位置的玩家类型
    int[] btn_names = {R.id.button1_sroom,R.id.button2_sroom,R.id.button3_sroom,R.id.button4_sroom};//四个位置对应的按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sroom);

        playerType[0] = DRSGame.PlayerType.PEOPLE;
        for(int i=1;i<4;++i)
            playerType[i] = DRSGame.PlayerType.EMPTY;

        //四个位置的按钮事件设置
        for(int i=0;i<4;++i){
            Button btn = (Button)this.findViewById(btn_names[i]);
            final int ix = i;
            //点击事件
            btn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    if(playerType[ix] != DRSGame.PlayerType.EMPTY)
                        return;
                    for(int j=0;j<4;++j)
                        if(playerType[j] == DRSGame.PlayerType.PEOPLE){
                            playerType[j] = DRSGame.PlayerType.EMPTY;
                            break;
                        }
                    playerType[ix] = DRSGame.PlayerType.PEOPLE;

                    Log.i("SRoomActivity", "JH:select a people");

                    updateBtnText();
                }
            });
            //长按事件
            btn.setOnLongClickListener(new View.OnLongClickListener(){
                @Override
                public boolean onLongClick(View view){
                    if(playerType[ix] == DRSGame.PlayerType.EMPTY) {
                        playerType[ix] = DRSGame.PlayerType.AI;
                        Log.i("SRoomActivity", "JH:add a ai");
                    }
                    else if(playerType[ix] == DRSGame.PlayerType.AI){
                        playerType[ix] = DRSGame.PlayerType.EMPTY;
                        Log.i("SRoomActivity", "JH:remove a ai");
                    }

                    updateBtnText();
                    return true;
                }
            });
        }

        //开始游戏按钮
        Button btn_beggame = (Button)this.findViewById(R.id.button_beggame_sroom);
        btn_beggame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(SRoomActivity.this, SPlayActivity.class);
                intent.putExtra("playerType", (Serializable)playerType);
                startActivity(intent);
            }
        });

        updateBtnText();
    }

    //更新按钮上文本
    void updateBtnText(){
        for(int i=0;i<4;++i) {
            Button btn = (Button) this.findViewById(btn_names[i]);
            if(playerType[i] == DRSGame.PlayerType.EMPTY) btn.setText("空位");
            else if(playerType[i] == DRSGame.PlayerType.PEOPLE) btn.setText("你");
            else if(playerType[i] == DRSGame.PlayerType.AI) btn.setText("AI");
        }
    }
}
