package com.theway4wardacademy.seamfixchat.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.theway4wardacademy.seamfixchat.Utils.SharedPrefManager;


public class Splashscreen extends Activity {

    SharedPrefManager sharedPrefManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPrefManager = new SharedPrefManager(this);
        checkLogin();

    }




    private void checkLogin() {

        if (sharedPrefManager.getIsUserSubscribed()) {
            Intent i = new Intent(Splashscreen.this, Message_Activity.class);
            startActivity(i);
            finish();
        } else {
            Intent i = new Intent(Splashscreen.this, MainActivity.class);
            startActivity(i);
            finish();
        }

    }
}

