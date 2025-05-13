package com.example.magic8ball;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private EditText questionEditText;
    private TextView answerTextView;
    private Button generateButton;

    // Válaszlista – magyarul
    private final String[] answers = {
            "Biztosan így van", "Határozottan igen", "Valószínűleg",
            "Kérdezd meg később", "Nem tudom megmondani", "Ne számíts rá",
            "Erősen kétséges", "Igen", "Nem", "Jelek szerint igen"
    };

    // Rázásérzékelés
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private SensorEventListener sensorListener;
    private float lastX, lastY, lastZ;
    private long lastShakeTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        questionEditText = findViewById(R.id.questionEditText);
        answerTextView = findViewById(R.id.answerTextView);
        generateButton = findViewById(R.id.generateButton);

        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateAnswer();
            }
        });

        // Rázásérzékelés beállítása
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        sensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float deltaX = x - lastX;
                float deltaY = y - lastY;
                float deltaZ = z - lastZ;

                lastX = x;
                lastY = y;
                lastZ = z;

                double delta = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

                if (delta > 12) { // Rázási küszöb
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastShakeTime > 1000) {
                        lastShakeTime = currentTime;
                        generateAnswer();
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // Nem szükséges kezelés
            }
        };
    }

    private void generateAnswer() {
        String question = questionEditText.getText().toString().trim();

        if (question.isEmpty()) {
            answerTextView.setText("Írj be egy kérdést!");
            return;
        }

        Random random = new Random();
        int index = random.nextInt(answers.length);
        String selectedAnswer = answers[index];

        answerTextView.setText(selectedAnswer);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorListener);
    }
}
