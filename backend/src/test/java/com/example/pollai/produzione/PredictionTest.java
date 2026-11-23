package com.example.pollai.produzione;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PredictionTest {

    @Test
    void readsAndUpdatesPrediction() {
        Prediction prediction = new Prediction();

        assertEquals(0, prediction.getPrediction());
        prediction.setPrediction(24);
        assertEquals(24, prediction.getPrediction());
    }
}
