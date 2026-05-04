package com.therapai.utils;

import com.therapai.models.UserModel;

/***
 * Clase para guardar información del usuario que se encuentra actualmente usando la aplicacion
 */
public class Sesion {
    //atributo static para ser accesible desde cualquier parte de la app
    //podemos obtener los datos del usuario sin llamar a Firestore
    private static UserModel usuario_actual;
    public Sesion() {

    }
    public static UserModel getUsuario_actual() {
        return usuario_actual;
    }
    public static void setUsuario_actual(UserModel usuario_actual) {
        Sesion.usuario_actual = usuario_actual;
    }
    public static String getUserId(){
        if (usuario_actual == null) {
            throw new NullPointerException("usuario_actual is null");
        }
        return usuario_actual.getUid();
    }
}
