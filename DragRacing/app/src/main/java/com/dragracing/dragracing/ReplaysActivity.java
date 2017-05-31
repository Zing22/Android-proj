package com.dragracing.dragracing;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;

public class ReplaysActivity extends AppCompatActivity {
    static public String TAG = "ReplaysAcitivy";
    LinearLayout ll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replays);

        ll = (LinearLayout)this.findViewById(R.id.linear_replays);

        try {
            FileInputStream in = openFileInput("record.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String name;
            while((name=reader.readLine())!=null){
                Button btn = new Button(this);
                final String filename = name;
                btn.setText(filename);
                btn.setAllCaps(false);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.setClass(ReplaysActivity.this, ReplayActivity.class);
                        intent.putExtra("filename", filename);
                        startActivity(intent);
                    }
                });
                ll.addView(btn);
            }
        }catch (IOException e) {
            Log.e(TAG, "JH:file record read error!");
        }

//        btn = (Button)this.findViewById(R.id.button);
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showDialog();
//            }
//        });
//
//        MyTask myTask = new MyTask();
//        myTask.execute();
    }

//    public void showDialog(){
//        AlertDialog.Builder builder = new AlertDialog.Builder(ReplaysActivity.this);
//        builder.setTitle("提示");
//        builder.setMessage("哈哈?");
//        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                dialogInterface.dismiss();
//            }
//        });
//        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                dialogInterface.dismiss();
//                ReplaysActivity.this.finish();
//            }
//        });
//        builder.setView(new EditText(this));
//        builder.create().show();
//    }
//
//    class MyTask extends AsyncTask<Void, Void, Boolean> {
//        int k;
//
//        @Override
//        protected void onPreExecute(){
//
//        }
//        @Override
//        protected Boolean doInBackground(Void... params){
//            for(k=0;k<40;++k){
//                SystemClock.sleep(1000);
//                publishProgress();
//            }
//
//            return true;
//        }
//        @Override
//        protected void onProgressUpdate(Void... values){
//            btn.setText(String.valueOf(k));
//        }
//        @Override
//        protected void onPostExecute(Boolean result) {
//
//        }
//    }
}
