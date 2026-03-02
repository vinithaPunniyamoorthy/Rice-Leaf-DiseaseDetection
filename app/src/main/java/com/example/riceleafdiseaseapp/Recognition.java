package com.example.riceleafdiseaseapp;

public class Recognition {
    private String name;
    private float confidence;

    public Recognition(String name, float confidence) {
        this.name = name;
        this.confidence = confidence;
    }

    public String getName() { return name; }
    public float getConfidence() { return confidence; }
}
