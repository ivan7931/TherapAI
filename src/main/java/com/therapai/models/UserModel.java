package com.therapai.models;

public class UserModel {
    private String uid; //Los ids son String porque FireStore genera los ids alfanumericos
    private String email;
    private String name;
    //La contraseña no se guarda ya que FireStore la gestiona y codifica.

    //ñadimos un atributo para guardar el idToken de la sesion y poder hacer llamadas a la api
    //de firebase para poder hacer el cambio de contraseña, emailp
    private String idToken;

    public UserModel(){}

    public UserModel(String uid, String email, String name,String idToken) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.idToken = idToken;
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

    public void setName(String nuevoNombre) {
        this.name = nuevoNombre;
    }

    public void setEmail(String nuevoEmail) {
        this.email = nuevoEmail;
    }

    //getter settr idToken

    public String getIdToken() {
        return idToken;
    }
    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }
}
