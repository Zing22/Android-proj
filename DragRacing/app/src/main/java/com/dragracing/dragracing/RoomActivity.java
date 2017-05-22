package com.dragracing.dragracing;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

//联机游戏进入房间后的界面

public class RoomActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        //设置房间名字
        setTitle("房间"+String.valueOf(Data.getRoom_num()));

        //开始游戏按钮
        Button btn_beggame = (Button)this.findViewById(R.id.button_beggame);
        btn_beggame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(RoomActivity.this, PlayActivity.class);
                startActivity(intent);
            }
        });

        //发送消息按钮
        Button btn_send = (Button)this.findViewById(R.id.button_send_room);
        final TextView tv_chat = (TextView)this.findViewById(R.id.textview_chat_room);
        final EditText et_chat = (EditText)this.findViewById(R.id.edittext_chat_room);

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_chat.setText(et_chat.getText());
                et_chat.setText("");
            }
        });
    }
}
