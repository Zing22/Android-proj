package com.dragracing.dragracing;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

//设置界面

public class SettingActivity extends AppCompatActivity {
    static public String TAG = "SettingActivity";
    Button btn_ok;
    Button btn_clear;
    EditText et_input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        btn_ok = (Button)this.findViewById(R.id.button_ok_setting);
        btn_clear = (Button)this.findViewById(R.id.button_clear_setting);
        et_input = (EditText)this.findViewById(R.id.playenameinput);

        //获取配置名字
        try {
            FileInputStream in = openFileInput("setting.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String text = reader.readLine();
            //Log.d(TAG, "JH:123"+text);
            et_input.setText(text);
        }catch (IOException e) {
            Log.w(TAG, "JH:file setting not found!");
            try{
                FileOutputStream out = openFileOutput("setting.txt", Context.MODE_PRIVATE);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
                writer.write("玩家");
                writer.close();
                et_input.setText("玩家");
            }catch (IOException e1){
                Log.e(TAG, "JH:create file setting error!");
            }
        }

        //设置事件
        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_input.setText("");
            }
        });
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setTitle("Setting");
                try{
                    FileOutputStream out = openFileOutput("setting.txt", Context.MODE_PRIVATE);
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
                    writer.write(et_input.getText().toString());
                    writer.close();
                    builder.setMessage("设置成功");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            SettingActivity.this.finish();
                        }
                    });
                }catch (IOException e1){
                    Log.e(TAG, "JH:modify name error!");
                    builder.setMessage("设置失败,请重试!");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                }
                builder.create().show();
            }
        });

    }
}
