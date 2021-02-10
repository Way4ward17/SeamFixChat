package com.theway4wardacademy.seamfixchat.Utils;

import android.content.Context;
import android.content.SharedPreferences;


public class SharedPrefManager {

    private  Context mCtx;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    public SharedPrefManager(Context context) {

        mCtx = context;

    }

    public boolean setIsUserSubscribed(boolean isUserSubscribed){


        SharedPreferences sharedPreferences =  mCtx.getSharedPreferences(
                "IS_USER_SUBSCRIBED", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("id", isUserSubscribed);
        editor.apply();
        return true;


       }

    public boolean setIsNewJoineeBroadcasted(boolean isNewJoineeBroadcasted) {


        SharedPreferences sharedPreferences =  mCtx.getSharedPreferences(
                "IS_USER_SUBSCRIBED", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("id", isNewJoineeBroadcasted);
        editor.apply();
        return true;
    }

    public String getUsername() {

        SharedPreferences sharedPreferences =  mCtx.getSharedPreferences(
                "USERNAME", Context.MODE_PRIVATE);
        return sharedPreferences.getString("id", null);
    }

    public boolean setUsername(String id) {
        SharedPreferences sharedPreferences =  mCtx.getSharedPreferences(
                "USERNAME", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("id", id);

        editor.apply();
        return true;
    }


    public String getTopic(){
        SharedPreferences sharedPreferences =  mCtx.getSharedPreferences(
                "TOPIC", Context.MODE_PRIVATE);
        return sharedPreferences.getString("id", "testtopic/seamfix1");
    }

    public boolean saveTopic(String id){
        SharedPreferences sharedPreferences =  mCtx.getSharedPreferences(
                "TOPIC", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("id", id);

        editor.apply();
        return true;
    }


    public boolean getIsUserSubscribed(){
        SharedPreferences sharedPreferences =  mCtx.getSharedPreferences(
                "IS_USER_SUBSCRIBED", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("id", false);
    }

    public boolean getIsNewJoineeBroadCasted() {
        SharedPreferences sharedPreferences =  mCtx.getSharedPreferences(
                "IS_NEW_JOINEE_BROADCASTED", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("id", false);

    }
}

