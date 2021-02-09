package com.theway4wardacademy.seamfixchat.Utils;

import android.app.Application;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;


public class App extends Application {


    String topic;
    String clientId = MqttClient.generateClientId();
    MqttAndroidClient client;


    @Override
    public void onCreate() {
        super.onCreate();

    }

}

