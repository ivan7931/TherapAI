package com.therapai.servicies;

public class ChatService {
    private final IAServer iaServer;

    public ChatService(IAServer iaServer) {
        this.iaServer = iaServer;
    }

    public String sendMessageToAI(String userMessage) {
        return iaServer.askGemini(userMessage);
    }
}
