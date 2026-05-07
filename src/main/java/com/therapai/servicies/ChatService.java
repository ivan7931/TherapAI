package com.therapai.servicies;

public class ChatService {
    private final IAServer iaServer;

    public ChatService(IAServer iaServer) {
        this.iaServer = iaServer;
    }

    //Metodo para enviar un mensaje a la IA
    public String sendMessageToAI(String userMessage) {
        return iaServer.askGemini(userMessage);
    }
}
