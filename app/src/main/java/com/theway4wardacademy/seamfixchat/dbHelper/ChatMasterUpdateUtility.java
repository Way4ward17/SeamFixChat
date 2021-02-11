package com.theway4wardacademy.seamfixchat.dbHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.theway4wardacademy.seamfixchat.Models.ChatItemModel;


public class ChatMasterUpdateUtility {
    Context context;
    DBHelper dbHelper;
    SQLiteDatabase db;

    public ChatMasterUpdateUtility(Context context) {
        this.context = context;
        dbHelper = new DBHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    public long insert(ChatItemModel current) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(ChatItemModel.KEY_MESSAGE_ID, current.getMessageID());
        contentValues.put(ChatItemModel.KEY_MESSAGE_CONTENT_TYPE, current.getContentType());
        contentValues.put(ChatItemModel.KEY_MESSAGE_CONTENT, current.getMessage());
        contentValues.put(ChatItemModel.KEY_SENDER_ID, current.getSender());
        contentValues.put(ChatItemModel.KEY_MESSAGE_SENT_DATETIME, current.getSentDateTime());
        contentValues.put(ChatItemModel.KEY_IS_MESSAGE_SENT_SUCCESSFULLY, current.isMessageSentStatusSuccess() ? 1 : 0);

        return db.insert(ChatItemModel.TABLE_NAME, null, contentValues);
    }

    public int updateStatus(long messageID, boolean messageStatus) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ChatItemModel.KEY_IS_MESSAGE_SENT_SUCCESSFULLY, messageStatus ? 1 : 0);

        return db.update(ChatItemModel.TABLE_NAME, contentValues, ChatItemModel.KEY_MESSAGE_ID + "=?", new String[]{String.valueOf(messageID)});
    }

    public int updatePublishedStatus(long messageID, boolean status) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ChatItemModel.KEY_IS_MESSAGE_PUBLISHED_SUCCESSFULLY, status ? 1 : 0);

        return db.update(ChatItemModel.TABLE_NAME, contentValues, ChatItemModel.KEY_MESSAGE_ID + "=?", new String[]{String.valueOf(messageID)});
    }
}
