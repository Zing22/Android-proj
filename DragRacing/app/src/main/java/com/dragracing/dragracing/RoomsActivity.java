package com.dragracing.dragracing;

import android.content.Intent;
import android.provider.SyncStateContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.Random;

public class RoomsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);

        final LinearLayout ll = (LinearLayout)this.findViewById(R.id.linear_rooms);

        for(int i=0;i<10;++i){
            LayoutInflater inflater = getLayoutInflater();
            Button btn = (Button)inflater.inflate(R.layout.button_room, null);
            Random r = new Random();
            final int sa=r.nextInt(100);
            btn.setText("房间" + String.valueOf(sa));
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setClass(RoomsActivity.this, RoomActivity.class);
                    Data.setRoom_num(sa);
                    startActivity(intent);
                }
            });
            ll.addView(btn);
        }

//        Button btn1 = (Button)this.findViewById(R.id.button);
//        btn1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent();
//                intent.setClass(RoomsActivity.this, RoomActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        Button btn_newroom = (Button)this.findViewById(R.id.button_newroom);
//        final LinearLayout ll = (LinearLayout)this.findViewById(R.id.linear_rooms);
//        btn_newroom.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Button btn_new = new Button(RoomsActivity.this);
//                btn_new.setText("new btn");
//                ll.addView(btn_new);
//            }
//        });
    }
}
