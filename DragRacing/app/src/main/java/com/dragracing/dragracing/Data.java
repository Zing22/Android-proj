package com.dragracing.dragracing;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class Data {
    private static int room_num = 0;
    private static Socket mSocket;
    private static String username;

    public static String getUsername() {
        return username;
    }
    public static void setUsername(String username) {
        Data.username = username;
    }
    public static int getRoom_num() {
        return room_num;
    }
    public static void setRoom_num(int room_num) {
        Data.room_num = room_num;
    }

    public static boolean createSocket(){
        try{
            mSocket = IO.socket("http://dragracing.tech");
            //mSocket = IO.socket("http://localhost:3000");
        }
        catch (URISyntaxException e){
            Log.e("Data", "JH:create socket error");
            return false;
        }

        mSocket.on("new username", new Emitter.Listener(){
            @Override
            public void call(Object... args){
                JSONObject data = (JSONObject) args[0];
                try{
                    username = data.getString("new_name");
                }
                catch (JSONException e){
                    Log.e("Data", "JH:on new username error");
                    return;
                }
                Log.i("Data", "JH:get username " + username);

                mSocket.disconnect();//
            }
        });

        mSocket.connect();

        Log.i("Data", "JH:connect success!");

        return true;
    }

    public static void emitRandomName(){
        Log.i("Data", "JH:emit random name");
        mSocket.emit("random name", "玩家");
    }

}
