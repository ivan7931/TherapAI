package com.therapai.servicies;

import com.therapai.models.SensitiveAnalysisModel;

import java.util.Arrays;
import java.util.List;

public class SensitiveMessageDetector {

    private static final List<String> RIESGO_ALTO = Arrays.asList(
            "suicid", "matarme", "quitarme la vida", "no quiero vivir",
            "acabar con todo", "morir", "autolesion", "cortarme",
            "hacerme daño", "lastimarme", "no vale la pena vivir"
    );

    private static final List<String> RIESGO_MEDIO = Arrays.asList(
            "no puedo más", "no puedo mas", "desesperado", "desesperada",
            "me rindo", "no veo salida", "todo me supera",
            "me siento vacío", "me siento vacio", "odio mi vida",
            "me siento atrapado", "me siento atrapada"
    );

    private static final List<String> RIESGO_BAJO = Arrays.asList(
            "ansiedad", "estresado", "estresada",
            "triste", "tristeza", "agotado", "agotada",
            "preocupado", "preocupada", "me siento mal",
            "me siento solo", "me siento sola"
    );

    public SensitiveAnalysisModel analyze(String message) {
        String lower = message.toLowerCase();

        for (String word : RIESGO_ALTO) {
            if (lower.contains(word)) {
                return new SensitiveAnalysisModel(true, "riesgo_alto", 0.95);
            }
        }

        for (String word : RIESGO_MEDIO) {
            if (lower.contains(word)) {
                return new SensitiveAnalysisModel(true, "riesgo_medio", 0.75);
            }
        }

        for (String word : RIESGO_BAJO) {
            if (lower.contains(word)) {
                return new SensitiveAnalysisModel(true, "riesgo_bajo", 0.50);
            }
        }

        return new SensitiveAnalysisModel(false, "normal", 0.0);
    }
}
