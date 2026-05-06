package com.therapai.models;

public class SensitiveAnalysisModel {
    private boolean sensitive; //Detecta si hay riesgo o no
    private String category; //Describe el tipo de riesgo: normal, riesgo_bajo, riesgo_medio y riesgo_alto
    private double confidence; //Del 0.0 al 1.0

    public SensitiveAnalysisModel() {
    }

    public SensitiveAnalysisModel(boolean sensitive, String category, double confidence) {
        this.sensitive = sensitive;
        this.category = category;
        this.confidence = confidence;
    }

    public boolean isSensitive() {
        return sensitive;
    }

    public void setSensitive(boolean sensitive) {
        this.sensitive = sensitive;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    @Override
    public String toString() {
        return "SensitiveAnalysisModel{" +
                "sensitive=" + sensitive +
                ", category='" + category + '\'' +
                ", confidence=" + confidence +
                '}';
    }
}
