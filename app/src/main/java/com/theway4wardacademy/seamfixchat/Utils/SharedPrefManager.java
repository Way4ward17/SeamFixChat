package com.theway4wardacademy.seamfixchat.Utils;

import android.content.Context;
import android.content.SharedPreferences;


/**
 * Created by Way4wardPC on 10/12/2018.
 */
public class SharedPrefManager {

    private static SharedPrefManager mInstance;
    private  Context mCtx;
    private  final String SHARED_PREF_NAME = "myshared";


    public String getTopic(){
        SharedPreferences sharedPreferences =  mCtx.getSharedPreferences(
                SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString("TOPIC", null);
    }

    public boolean saveTopic(String id){
        SharedPreferences sharedPreferences =  mCtx.getSharedPreferences(
                "TOPIC", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("id", id);

        editor.apply();
        return true;
    }

}

