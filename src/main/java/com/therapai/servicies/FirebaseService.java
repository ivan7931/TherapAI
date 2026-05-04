package com.therapai.servicies;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.InputStream;

/***
 * Clase para inicializar Firebase y dar acceso a Firestore
 * Se utiliza el Firebase Admin SDK para java -> javafx no tiene sdk cliente oficial
 */
public class FirebaseService {
    //Instancia estatica para ser usada globalmante por toda la aplicacion
    private static Firestore db;

    //metodo para inicializar firebase
    public static void inicializar() {
        try {
            //Cargamos el archivo de con la clave privada de nuestro proyecto(obtenida en la consola de firebase)
            InputStream privateKey = FirebaseService.class.getClassLoader().getResourceAsStream("com\\therapai\\Firebase\\firebaseKey.json");

            //Configuramos Firebase con las credenciales
            FirebaseOptions opciones = FirebaseOptions.builder().setCredentials(GoogleCredentials.fromStream(privateKey)).build();

            //Inicializamos la aplicacion de Firebase en Java
            FirebaseApp.initializeApp(opciones);
            //Inicializamos la instancia de fireStore para realizar las operaciones de persistencia
            db = FirestoreClient.getFirestore();

            //Comprobamos que todo a ido bien
            System.out.println("Servicio de firebase runnig");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     *
     * @return instancia de la base de datos
     */
    public static Firestore getDb() {
        return db;
    }
}
