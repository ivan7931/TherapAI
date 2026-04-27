package com.therapai.models;

public class ConversationModel {
    private String userId;
    private String chatId;
    private long cratedAt;
    //Posiblemente haya que añadir otro como de titulo para diferenciar chats en la lista del home.

    public ConversationModel() {
    }

    public ConversationModel(String userId, String chatId, long cratedAt) {
        this.userId = userId;
        this.chatId = chatId;
        this.cratedAt = cratedAt;
    }

    public String getUserId() {
        return userId;
    }

    public String getChatId() {
        return chatId;
    }

    public long getCratedAt() {
        return cratedAt;
    }
}
