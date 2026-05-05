package com.therapai.models;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.therapai.servicies.FirebaseService;
import com.therapai.utils.Sesion;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;

public class ConversationModel {
    private String userId;
    private String chatId;
    private long cratedAt;
    //Posiblemente haya que añadir otro como de titulo para diferenciar chats en la lista del home.
    private String titulo;

    public ConversationModel() {
    }

    public ConversationModel(String userId, String chatId, long cratedAt,String titulo) {
        this.userId = userId;
        this.chatId = chatId;
        this.cratedAt = cratedAt;
        this.titulo = titulo;
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
    //Titulo que se mostrarra en la lista de chats
    public String getTitulo() {
        return titulo;
    }

    //lo que se va  a vaer en Listview
    @Override
    public String toString() {
        return titulo;
    }

}
