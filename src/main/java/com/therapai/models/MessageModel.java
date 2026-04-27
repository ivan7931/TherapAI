package com.therapai.models;

public class MessageModel {

    private String chatId;
    private String message;
    private long timestamp;
    private String sender; //Para saber si el mensaje ha sido enviado por la ia o un user.

    public MessageModel() {
    }

    public MessageModel(String chatId, String message, long timestamp, String sender) {
        this.chatId = chatId;
        this.message = message;
        this.timestamp = timestamp;
        this.sender = sender;
    }

    public String getChatId() {
        return chatId;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSender() {
        return sender;
    }
}
