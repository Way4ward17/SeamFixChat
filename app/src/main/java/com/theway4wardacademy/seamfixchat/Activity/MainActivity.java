package com.theway4wardacademy.seamfixchat.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.theway4wardacademy.seamfixchat.R;
import com.theway4wardacademy.seamfixchat.Utils.SharedPrefManager;
import com.theway4wardacademy.seamfixchat.Utils.Util;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.json.JSONException;
import org.json.JSONObject;

import static com.theway4wardacademy.seamfixchat.Utils.Util.CHAT_SERVER;

public class MainActivity extends AppCompatActivity {

    Button submitBtn;
    ProgressBar progress;
    EditText username, topic;

    SharedPrefManager sharedPrefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPrefs = new SharedPrefManager(this);
        progress = findViewById(R.id.progress);
        submitBtn = findViewById(R.id.login);
        username = findViewById(R.id.username);
        topic = findViewById(R.id.topic);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress.setVisibility(View.VISIBLE);
                if (!validateUsername()) {
                    progress.setVisibility(View.GONE);
                    return;
                }

                if (!Util.isNetworkAvailable(getApplicationContext())) {
                    Toast.makeText(getApplicationContext(), getString(R.string.label_no_internet_available), Toast.LENGTH_SHORT).show();
                    progress.setVisibility(View.GONE);
                    return;
                }

                sharedPrefs.setUsername(username.getText().toString() + "_" + System.currentTimeMillis());
                subscribeTopic();
            }
        });
    }





    private void subscribeTopic() {
        final MqttAndroidClient mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), CHAT_SERVER, sharedPrefs.getUsername());
        Log.e("starting ","subscription");
        try {
            mqttAndroidClient.connect(Util.mqttConnectOptions(getApplicationContext()), null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);

                    try {
                        mqttAndroidClient.subscribe(sharedPrefs.getTopic(), 1, null, new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                if (!sharedPrefs.getIsUserSubscribed()) {
                                    if (!sharedPrefs.getIsNewJoineeBroadCasted()) {
                                        //send broadcast to other users
                                        publishUserMessage( "User " + "\'" +  sharedPrefs.getUsername().substring(0, sharedPrefs.getUsername().lastIndexOf("_")) + "\'" + " has joined the chat", "alert", mqttAndroidClient);
                                        sharedPrefs.setIsNewJoineeBroadcasted(true);
                                        sharedPrefs.saveTopic("testtopic/"+topic.getText().toString());
                                        progress.setVisibility(View.GONE);
                                    }


                                    Log.e("subscription","success");
                                    Toast.makeText(getApplicationContext(),  getString(R.string.label_subscribed_to_topic, sharedPrefs.getTopic()), Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(MainActivity.this, Message_Activity.class);
                                    startActivity(intent);
                                    finish();
                                    sharedPrefs.setIsUserSubscribed(true);
                                }
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                Log.e("subscription","fail");
                                sharedPrefs.setIsUserSubscribed(false);
                                progress.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(), getString(R.string.label_something_went_wrong), Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (MqttException e) {
                        progress.setVisibility(View.GONE);
                        Log.e("subscribe exception", e.toString());
                        Toast.makeText(getApplicationContext(), getString(R.string.label_something_went_wrong), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e("Failed to connect to: ", CHAT_SERVER);
                    progress.setVisibility(View.GONE);
                    sharedPrefs.setIsUserSubscribed(false);
                }
            });


        } catch (MqttException ex){
            Log.e("connect exception", ex.toString());
            progress.setVisibility(View.GONE);
            sharedPrefs.setIsUserSubscribed(false);
        }

    }

    private boolean validateUsername(){
        if (username.getText().toString().trim().isEmpty()) {
            username.setError(getString(R.string.label_empty_text_field_required));
            username.requestFocus();

            if(topic.getText().toString().trim().isEmpty()){
            topic.setError(getString(R.string.label_empty_text_field_required));
            topic.requestFocus();
            return false;
            }
            return false;
        }
        return  true;
    }

    private void publishUserMessage(String content, String contentType, MqttAndroidClient mqttAndroidClient){
        MqttMessage message = new MqttMessage();

        try {
            int msgID = (int) System.currentTimeMillis();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("message_id", msgID);
            jsonObject.put("sender", sharedPrefs.getUsername());
            jsonObject.put("message", content);
            jsonObject.put("datetime", Util.getCurrentDateTimeInUTC());
            jsonObject.put("content_type", contentType);

            message.setId(msgID);
            message.setQos(0);
            message.setPayload(jsonObject.toString().getBytes());

            mqttAndroidClient.publish(sharedPrefs.getTopic(), message);

            try {
                if (!mqttAndroidClient.isConnected()) {
                    Log.e(" messages in buffer.", mqttAndroidClient.getBufferedMessageCount() + "");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}





