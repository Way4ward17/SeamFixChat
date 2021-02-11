package com.theway4wardacademy.seamfixchat.Models;

public class ChatItemModel {
    public static final String TABLE_NAME = "ChatModel";

    public static final String KEY_SENDER_ID = "SenderID";
    public static final String KEY_MESSAGE_ID = "MessageID";
    public static final String KEY_MESSAGE_CONTENT = "MessageContent";
    public static final String KEY_MESSAGE_SENT_DATETIME = "MessageSentDateTime";
    public static final String KEY_MESSAGE_CONTENT_TYPE = "MessageContentType";

    //only for sent messages it'll be set to 1(true) and 0(false). For received and alert it'll be 0 always.
    public static final String KEY_IS_MESSAGE_SENT_SUCCESSFULLY = "IsMessageSentSuccessfully";
    public static final String KEY_IS_MESSAGE_PUBLISHED_SUCCESSFULLY = "IsMessagePublishedSuccessFully";

    private String sender;
    private long messageID;
    private String message;
    private String sentDateTime;
    private boolean messageSentStatusSuccess;

    private String contentType;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public long getMessageID() {
        return messageID;
    }

    public void setMessageID(long messageID) {
        this.messageID = messageID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSentDateTime() {
        return sentDateTime;
    }

    public void setSentDateTime(String sentDateTime) {
        this.sentDateTime = sentDateTime;
    }

    public boolean isMessageSentStatusSuccess() {
        return messageSentStatusSuccess;
    }

    public void setMessageSentStatusSuccess(boolean messageSentStatusSuccess) {
        this.messageSentStatusSuccess = messageSentStatusSuccess;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
