package com.theway4wardacademy.seamfixchat.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.theway4wardacademy.seamfixchat.Adapters.ChatAdapter;
import com.theway4wardacademy.seamfixchat.Models.ChatItemModel;
import com.theway4wardacademy.seamfixchat.R;
import com.theway4wardacademy.seamfixchat.Utils.SharedPrefManager;
import com.theway4wardacademy.seamfixchat.Utils.TimeDiff;
import com.theway4wardacademy.seamfixchat.Utils.Util;
import com.theway4wardacademy.seamfixchat.dbHelper.ChatMasterUpdateUtility;
import com.theway4wardacademy.seamfixchat.dbHelper.DBHelper;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Message_Activity extends AppCompatActivity {


    FloatingActionButton sendBtn;


    MqttAndroidClient client;
    TextView topic;
    SharedPrefManager sharedPrefs;
    static ChatAdapter chatRoomRecyclerAdapter;
    static List<ChatItemModel> chatItemModelList = new ArrayList<>();

    DBHelper dbHelper;
    SQLiteDatabase db;
    ChatMasterUpdateUtility chatMasterUpdateUtility;


    static boolean isDataLoadedFirstTime = false;


    RecyclerView chatHistoryRecyclerView;
    EditText messageContent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        sharedPrefs = new SharedPrefManager(this);


        topic = findViewById(R.id.username);
        topic.setText(sharedPrefs.getTopic());
        dbHelper = new DBHelper(getApplicationContext());
        db = dbHelper.getWritableDatabase();
        chatMasterUpdateUtility = new ChatMasterUpdateUtility(getApplicationContext());


        chatHistoryRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        messageContent = (EditText) findViewById(R.id.text_send);
        sendBtn = (FloatingActionButton) findViewById(R.id.btn_send);


        dbHelper = new DBHelper(getApplicationContext());
        db = dbHelper.getWritableDatabase();
        chatMasterUpdateUtility = new ChatMasterUpdateUtility(getApplicationContext());


        if (!isDataLoadedFirstTime) {
            Cursor cursor = db.rawQuery("SELECT * FROM " + ChatItemModel.TABLE_NAME, null);
            cursor.moveToFirst();

            for(int i = 0;i<cursor.getCount(); i++) {
                ChatItemModel current = new ChatItemModel();
                current.setMessageID(cursor.getInt(cursor.getColumnIndex(ChatItemModel.KEY_MESSAGE_ID)));
                current.setContentType(cursor.getString(cursor.getColumnIndex(ChatItemModel.KEY_MESSAGE_CONTENT_TYPE)));
                current.setMessage(cursor.getString(cursor.getColumnIndex(ChatItemModel.KEY_MESSAGE_CONTENT)));
                current.setSender(cursor.getString(cursor.getColumnIndex(ChatItemModel.KEY_SENDER_ID)));
                current.setSentDateTime(cursor.getString(cursor.getColumnIndex(ChatItemModel.KEY_MESSAGE_SENT_DATETIME)));
                current.setMessageSentStatusSuccess(cursor.getInt(cursor.getColumnIndex(ChatItemModel.KEY_IS_MESSAGE_SENT_SUCCESSFULLY)) == 1);


                chatItemModelList.add(current);
                cursor.moveToNext();
            }

            Collections.reverse(chatItemModelList);
            isDataLoadedFirstTime = true;
            cursor.close();
        }

        chatRoomRecyclerAdapter = new ChatAdapter(getApplicationContext(), chatItemModelList);
        chatHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, true));
        chatHistoryRecyclerView.setAdapter(chatRoomRecyclerAdapter);


        client = new MqttAndroidClient(getApplicationContext(), Util.CHAT_SERVER, sharedPrefs.getUsername());

        try{
            client.connect(Util.mqttConnectOptions(getApplicationContext()), null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.e("mqtt", "connected");
                    //initializing buffers for mqtt
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    client.setBufferOpts(disconnectedBufferOptions);

                    retryPendingPublishes();
                    ChatItemModel current = new ChatItemModel();
                    current.setMessage(getString(R.string.label_connected_to_topic, sharedPrefs.getTopic()));
                    current.setSentDateTime(Util.getCurrentDateTimeInUTC());
                    current.setSender(sharedPrefs.getUsername());
                    current.setContentType("alert");
                    chatItemModelList.add(0,current);

                    chatRoomRecyclerAdapter.notifyItemInserted(chatItemModelList.size() - 1);
                    Log.e("mqtt buffer", "initialized");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e("mqtt", "connection failed -- " + exception.toString());

                    ChatItemModel current = new ChatItemModel();
                    current.setMessage(getString(R.string.label_connection_to_topic_failed, sharedPrefs.getTopic()));
                    current.setSentDateTime(Util.getCurrentDateTimeInUTC());
                    current.setSender(sharedPrefs.getUsername());
                    current.setContentType("alert");
                    chatItemModelList.add(0,current);

                    chatRoomRecyclerAdapter.notifyDataSetChanged();
                }


            });

        } catch (MqttException e) {
            e.printStackTrace();
            ChatItemModel current = new ChatItemModel();
            current.setMessage(getString(R.string.label_connection_to_topic_failed, sharedPrefs.getTopic()));
            current.setSentDateTime(Util.getCurrentDateTimeInUTC());
            current.setSender(sharedPrefs.getUsername());
            current.setContentType("alert");
            chatItemModelList.add(0,current);

            chatRoomRecyclerAdapter.notifyDataSetChanged();
        }

        client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                sendTopicJoinedBroadCast(reconnect);
            }

            @Override
            public void connectionLost(Throwable cause) {
                showConnectionLost();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.e("MESSAGE", "RECEIVED -- " + new String(message.getPayload()));

                String messageContents = new String(message.getPayload());

                if (!messageContents.equals("User " + "\'" +  sharedPrefs.getUsername().substring(0, sharedPrefs.getUsername().lastIndexOf("_")) + "\'" + " is offline")
                        && !messageContents.equalsIgnoreCase("User " + "\'" +  sharedPrefs.getUsername().substring(0, sharedPrefs.getUsername().lastIndexOf("_")) + "\'" + " is offline")) {
                    JSONObject jsonObject = new JSONObject(messageContents);
                    int messageID = jsonObject.getInt("message_id");
                    String sender = jsonObject.getString("sender");
                    String msg = jsonObject.getString("message");
                    String dateTime = jsonObject.getString("datetime");
                    String contentType = jsonObject.getString("content_type");

                    //preventing multiple messages with same message id
                    Cursor cursor = db.rawQuery("SELECT * FROM " + ChatItemModel.TABLE_NAME + " WHERE " + ChatItemModel.KEY_MESSAGE_ID + "=" + messageID , null);
                    cursor.moveToFirst();

                    if (chatMasterUpdateUtility.updateStatus(messageID, true) > 0) {
//                        Log.e("VAL", "UPDATED --- true");
                    }else {
//                        Log.e("VAL", "UPDATE --- failed");
                    }
                    chatRoomRecyclerAdapter.notifyDataSetChanged();
//                    Log.e("ADAPTER", "NOTIFIED");

                    if (cursor.getCount() > 0) {
                        return;
                    }

                    cursor.close();


                    if (sharedPrefs.getUsername().equals(sender)) {
                        return;
                    }

                    ChatItemModel current = new ChatItemModel();
                    current.setMessage(msg);
                    current.setSentDateTime(Util.getReceivedDateTimeInCurrentLocale(dateTime));
                    current.setSender(sender);
                    current.setMessageID(messageID);
                    current.setContentType(contentType);
                    current.setMessageSentStatusSuccess(false);


                    if (chatMasterUpdateUtility.insert(current) > 0 && contentType.equalsIgnoreCase("message")) {
//                        Log.e("item insertion", "success");
                    }else {
//                        Log.e("item insertion", "fail");
                    }


                    chatItemModelList.add(0, current);
//                    Log.e("VALUE ", "ADDED");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            chatRoomRecyclerAdapter.notifyDataSetChanged();
//                            Log.e("VALUE ", "NOTIFIED ");
                        }
                    });

                }else {
                    // for wills
                    if (!messageContents.equalsIgnoreCase("User " + "\'" + sharedPrefs.getUsername().substring(0, sharedPrefs.getUsername().lastIndexOf("_")) + "\'" + " is offline")) {

                        String sender = "N/A";
                        String dateTime = "N/A";
                        String contentType = "N/A";

                        ChatItemModel current = new ChatItemModel();
                        current.setMessage(messageContents);
                        current.setSentDateTime(dateTime);
                        current.setSender(sender);
                        current.setContentType(contentType);

                        chatItemModelList.add(0, current);

                        chatRoomRecyclerAdapter.notifyDataSetChanged();
                    }

                }
                chatRoomRecyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                try {
                    chatMasterUpdateUtility.updateStatus(token.getMessage().getId(), true);
                    chatRoomRecyclerAdapter.notifyDataSetChanged();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });


        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (messageContent.getText().toString().isEmpty()) {
                    messageContent.setError(getString(R.string.label_empty_text_field_required));
                    messageContent.requestFocus();
                    return;
                }

                //publish message
                publishUserMessage(messageContent.getText().toString(), "message");

                messageContent.setText("");
                messageContent.setHint(getString(R.string.label_write_something_here));
            }
        });

    }

    public void gotoProfile(View view) {
    }





    private void subscribe(){

        int qos = 1;
        try {
            client.subscribe(sharedPrefs.getTopic(), qos);
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {

                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    Log.d("Topic", new String(topic));
                    Log.d("Message", new String(message.getPayload()));

                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }



    private void unsubscribe(){
        finish();
        try {
            IMqttToken unsubToken = client.unsubscribe(sharedPrefs.getTopic());
            unsubToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The subscription could successfully be removed from the client

                    sharedPrefs.setIsUserSubscribed(false);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // some error occurred, this is very unlikely as even if the client
                    // did not had a subscription to the topic the unsubscribe action
                    // will be successfully
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }



    private void publishUserMessage(String content, String contentType){
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

            if (!content.equals("User " + "\'" +  sharedPrefs.getUsername().substring(0, sharedPrefs.getUsername().lastIndexOf("_")) + "\'" + " is offline")
                    && !content.equalsIgnoreCase("User " + "\'" +  sharedPrefs.getUsername().substring(0, sharedPrefs.getUsername().lastIndexOf("_")) + "\'" + " is online")) {
                ChatItemModel chatItemModel = new ChatItemModel();
                chatItemModel.setMessageID(msgID);
                chatItemModel.setContentType(contentType);
                chatItemModel.setMessage(content);
                chatItemModel.setSentDateTime(jsonObject.getString("datetime"));
                chatItemModel.setSender(sharedPrefs.getUsername());
                chatItemModel.setMessageSentStatusSuccess(false);

                if (chatMasterUpdateUtility.insert(chatItemModel) > 0) {
//                    Log.e("item insertion", "success");
                }else {
//                    Log.e("item insertion", "fail");
                }

                chatItemModelList.add(0, chatItemModel);
                chatRoomRecyclerAdapter.notifyDataSetChanged();
            }

            client.publish(sharedPrefs.getTopic(), message);

            try {
                if (!client.isConnected()) {
                    Log.e(" messages in buffer.", client.getBufferedMessageCount() + "");
                }
                chatMasterUpdateUtility.updatePublishedStatus(msgID, true);
            } catch (Exception e) {
                e.printStackTrace();
                chatMasterUpdateUtility.updatePublishedStatus(msgID, false);
//                int a = sharedPrefs.getUnPublishedMessageCount() + 1;
//                sharedPrefs.setUnpublishedMessageCount(a);
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


    private void sendTopicJoinedBroadCast(boolean reconnect) {
        if (reconnect) {
            //show to user that '<topic> joined'
            ChatItemModel current = new ChatItemModel();
            current.setMessage(getString(R.string.label_reconnected_to_topic, sharedPrefs.getTopic()));
            current.setSentDateTime(Util.getCurrentDateTimeInUTC());
            current.setSender(sharedPrefs.getUsername().substring(0, sharedPrefs.getUsername().lastIndexOf("_")));
            current.setContentType("alert");
            chatItemModelList.add(0, current);
            chatRoomRecyclerAdapter.notifyDataSetChanged();
        }
//        }else {
//            //show to user that '<topic> joined'
//            ChatItemModel current = new ChatItemModel();
//            current.setMessage(getString(R.string.label_connected_to_topic, sharedPrefs.getTopic()));
//            current.setSentDateTime(Util.getCurrentDateTimeInUTC());
//            current.setSender(sharedPrefs.getUsername().substring(0, sharedPrefs.getUsername().lastIndexOf("_")));
//            current.setContentType("alert");
//            chatItemModelList.add(0,current);
//            chatRoomRecyclerAdapter.notifyDataSetChanged();
//        }

        retryPendingPublishes();

    }

    private void retryPendingPublishes() {
        Cursor cursor = db.rawQuery("SELECT * FROM " + ChatItemModel.TABLE_NAME, null);
        cursor.moveToFirst();

        for(int i = 0;i<cursor.getCount(); i++) {
            ChatItemModel current = new ChatItemModel();
            current.setMessageID(cursor.getInt(cursor.getColumnIndex(ChatItemModel.KEY_MESSAGE_ID)));
            current.setContentType(cursor.getString(cursor.getColumnIndex(ChatItemModel.KEY_MESSAGE_CONTENT_TYPE)));
            current.setMessage(cursor.getString(cursor.getColumnIndex(ChatItemModel.KEY_MESSAGE_CONTENT)));
            current.setSender(cursor.getString(cursor.getColumnIndex(ChatItemModel.KEY_SENDER_ID)));
            current.setSentDateTime(cursor.getString(cursor.getColumnIndex(ChatItemModel.KEY_MESSAGE_SENT_DATETIME)));
            current.setMessageSentStatusSuccess(cursor.getInt(cursor.getColumnIndex(ChatItemModel.KEY_IS_MESSAGE_SENT_SUCCESSFULLY)) == 1);

            int isPublished = cursor.getInt(cursor.getColumnIndex(ChatItemModel.KEY_IS_MESSAGE_PUBLISHED_SUCCESSFULLY));
//            Log.e("VAL isPublished", String.valueOf(isPublished));
            if (isPublished == 0) {
                publishPendingMessage(current);
            }

            cursor.moveToNext();
        }

        cursor.close();
    }


    private void publishPendingMessage(ChatItemModel current) {
        MqttMessage message = new MqttMessage();

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("message_id", current.getMessageID());
            jsonObject.put("sender", sharedPrefs.getUsername());
            jsonObject.put("message", current.getMessage());
            jsonObject.put("datetime", Util.getCurrentDateTimeInUTC());
            jsonObject.put("content_type", current.getContentType());

            Long l  = new Long(current.getMessageID());
            message.setId(l.intValue());
            message.setQos(0);
            message.setPayload(jsonObject.toString().getBytes());

            client.publish(sharedPrefs.getTopic(), message);

            try {
                if (!client.isConnected()) {
//                    Log.e(" messages in buffer.", mqttAndroidClient.getBufferedMessageCount() + "");
                }

                chatMasterUpdateUtility.updatePublishedStatus(l.intValue(), true);
            } catch (Exception e) {
                e.printStackTrace();
                chatMasterUpdateUtility.updatePublishedStatus(l.intValue(), false);

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


    private void showConnectionLost() {
        //show 'connection lost' to user

        ChatItemModel current = new ChatItemModel();
        current.setMessage(getString(R.string.label_connection_lost));
        current.setSentDateTime(Util.getCurrentDateTimeInUTC());
        current.setSender(sharedPrefs.getUsername());
        current.setContentType("alert");
        chatItemModelList.add(0,current);

        chatRoomRecyclerAdapter.notifyDataSetChanged();

        //mqtt will take care of sending broadcast (will) to other users
        //mqtt doesnt send will message :'(
        //publishUserMessage(sharedPrefs.getUsername().substring(0, sharedPrefs.getUsername().lastIndexOf("_")) + " disconnected", "alert");
    }


    public void close(View view) {
        finish();
    }

    public void more(View view) {

        PopupMenu popupMenu = new PopupMenu(Message_Activity.this, view);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.clear:

                        dbHelper.deleteOldTele();
                        isDataLoadedFirstTime = true;
                        sharedPrefs.setIsUserSubscribed(false);
                        Intent intent = new Intent(Message_Activity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.unsub:
                        unsubscribe();
                        dbHelper.deleteOldTele();
                        return true;
                    case R.id.exit:
                        finish();
                        dbHelper.deleteOldTele();
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.inflate(R.menu.option);
        popupMenu.show();
    }

}