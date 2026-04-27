package com.therapai.models;

public class UserModel {
    private String uid; //Los ids son String porque FireStore genera los ids alfanumericos
    private String email;
    private String name;
    //La contraseña no se guarda ya que FireStore la gestiona y codifica.

    public UserModel(){}

    public UserModel(String uid, String email, String name){
        this.uid = uid;
        this.email = email;
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }
}
