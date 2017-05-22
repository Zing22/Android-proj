package com.dragracing.dragracing;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

//主界面

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //单机游戏按钮事件
        Button btn0 = (Button)this.findViewById(R.id.button_sgame);
        btn0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, SRoomActivity.class);
                startActivity(intent);
            }
        });

        //联机游戏按钮事件
        Button btn1 = (Button)this.findViewById(R.id.button_game);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, RoomsActivity.class);
                startActivity(intent);
            }
        });

        //设置按钮事件
        Button btn2 = (Button)this.findViewById(R.id.button_settings);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });

        //关于按钮事件
        Button btn3 = (Button)this.findViewById(R.id.button_about);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Boolean fa = Data.createSocket();
                if(!fa){
                    Toast.makeText(MainActivity.this, "Connect failed!", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(MainActivity.this, "Connect success!", Toast.LENGTH_SHORT).show();
                }

                Data.emitRandomName();
            }
        });
    }
}
