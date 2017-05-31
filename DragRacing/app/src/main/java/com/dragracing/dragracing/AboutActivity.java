package com.dragracing.dragracing;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Calendar;

public class AboutActivity extends AppCompatActivity {
    //Button btn_about;
    //TextView tv_about;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    //private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

//        btn_about = (Button) this.findViewById(R.id.button_about2);
//        tv_about = (TextView) this.findViewById(R.id.textView_about2);
//
//        btn_about.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                slotClick();
//            }
//        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void slotClick() {
//        /*
//        AnimationSet animationSet = new AnimationSet(true);
//        for (int i=0;i<curStepEvent.num_event;i++){
//            animationSet.addAnimation(move(player,airplane,from,curStepEvent.poses[i]));
//            if(curStepEvent.hits[i] = true){
//                animationSet.addAnimation(hit(curStepEvent.players[i], curStepEvent.airs[i]));
//            }
//        }
//        this.startAnimation(animationSet);*/
//        //AnimationSet s = new AnimationSet(true);
//        ObjectAnimator animator = ObjectAnimator.ofFloat(tv_about, "translationX", 0.0f, 350.0f, 0f, 200.0f);
//        ObjectAnimator animator2 = ObjectAnimator.ofFloat(tv_about, "translationY", 0.0f, 200.0f, 300.0f, 400.0f);
//        animator.setDuration(2500);
//        animator2.setDuration(2500);
//        //Animation a = new TranslateAnimation(0,300,600,900);
//        //a.setDuration(1000);
//        //a.setFillAfter(true);
//        //Animation b = new TranslateAnimation(300,200,900,400);
//        //s.addAnimation(a);
//        //s.addAnimation(b);
//        //s.setInterpolator(new AccelerateInterpolator());
//        AnimatorSet s = new AnimatorSet();
//        //s.play(a).before(b);
//        //s.play(b).after(a);
//        //s.play(animator).before(animator2);
//        s.play(animator);
//        s.play(animator2).after(animator);
//        s.start();
//        //tv_about.startAnimation(s);

//        String filename = String.format("test.txt");
//        try {
//            Calendar c = Calendar.getInstance();
//            c.setTimeInMillis(System.currentTimeMillis());
//
//            FileOutputStream out = openFileOutput(filename, Context.MODE_PRIVATE);
//            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
//            writer.write("test");
//            writer.close();
////            Log.d("SPlayActivity", "JH:test");
//        } catch (IOException e) {
//            Log.e("SPlayActivity", "JH:game end O error!");
//        }
//
//        try {
//            FileInputStream in = openFileInput(filename);
//            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//            String text = reader.readLine();
//            Log.d("SPlayActivity", "JH:123"+text);
//        }catch (IOException e) {
//            Log.e("SPlayActivity", "JH:game end I error!");
//        }

    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
//    public Action getIndexApiAction() {
//        Thing object = new Thing.Builder()
//                .setName("About Page") // TODO: Define a title for the content shown.
//                // TODO: Make sure this auto-generated URL is correct.
//                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
//                .build();
//        return new Action.Builder(Action.TYPE_VIEW)
//                .setObject(object)
//                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
//                .build();
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//
//        // ATTENTION: This was auto-generated to implement the App Indexing API.
//        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        client.connect();
//        AppIndex.AppIndexApi.start(client, getIndexApiAction());
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//
//        // ATTENTION: This was auto-generated to implement the App Indexing API.
//        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        AppIndex.AppIndexApi.end(client, getIndexApiAction());
//        client.disconnect();
//    }

    //builder.create().show();
}


