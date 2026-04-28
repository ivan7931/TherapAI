package com.therapai.servicies;


import org.apache.commons.text.StringEscapeUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class IAServer {
    private final String apiKey;
    //URL del modelo de IA
    private final String endPoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-001:generateContent?key=";

    public IAServer(String apiKey) {
        this.apiKey = apiKey;
    }

    public String askGemini(String prompt) {

        try {
            //Se abre la conexion HTTP endpoint + apiKey al final
            URL url = new URL(endPoint + apiKey);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            //Se crea el cuerpo del json que espera Gemini.
            String jsonInput = """
                    {
                      "contents": [
                        {
                          "parts": [
                            { "text": "%s" }
                          ]
                        }
                      ]
                    }
                    """.formatted(prompt.replace("\"", "\\\""));

            //Se escribe, es decir, se envia el json para que la IA lo procese.
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInput.getBytes());
            }

            //Codigo de estado, 200 = OK, distinto = error
            int status = conn.getResponseCode();

            //Se selecciona el Stream correcto , el normal o el de error
            InputStream stream = (status == 200)
                    ? conn.getInputStream()
                    : conn.getErrorStream();


            //Se lee la respuesta completa de la IA
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                response.append(line);
            }

            br.close();


            if (status != 200) {
                System.out.println("ERROR HTTP: " + status);
                System.out.println("RESPUESTA DEL SERVIDOR: " + response);
                return "Error al conectarse con Gemini";
            }

            //Llamamos al metodo que extrae el texto útil de la respuesta.
            return extractText(response.toString());

        } catch (Exception e) {
            e.printStackTrace();
            return "Error al conectarse con Gemini";
        }


    }

    private String extractText(String json) {
        /*
        int index = json.indexOf("\"text\":");
        if (index == -1) return "Error";

        int start = json.indexOf("\"", index + 7) + 1;
        int end = json.indexOf("\"", start);

        return json.substring(start, end);

         */
        //Buscamos el "text": en el JSON y nos vamos 8 caracteres hacia delante, 8 por los 8 de "text":", desde ahi hasta el siguiente " sera el mensaje.
        String raw = json.substring(json.indexOf("\"text\":") + 8);
        raw = raw.substring(0, raw.indexOf("\"", 1));

        //Convierte \n \" en caracteres reales.
        return StringEscapeUtils.unescapeJson(raw);
    }

}
