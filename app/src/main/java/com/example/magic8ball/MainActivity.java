package com.example.magic8ball;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private EditText questionEditText;
    private TextView answerTextView;
    private Button generateButton;

    private RadioGroup modeRadioGroup;
    private RadioButton shakeModeRadioButton;
    private RadioButton buttonModeRadioButton;

    private final String[] positiveAnswers = {
            "Biztosan így van", "Határozottan igen", "Igen", "Jelek szerint igen", "Bízhatsz benne",
            "Valószínűleg", "Jó kilátások", "Igen, egészen biztosan"
    };

    private final String[] neutralAnswers = {
            "Kérdezd meg később", "Nem tudom megmondani", "Koncentrálj és kérdezd újra",
            "Jobb, ha most nem válaszolok", "Nem egyértelmű"
    };

    private final String[] negativeAnswers = {
            "Ne számíts rá", "Erősen kétséges", "Nem", "Kilátások nem jók", "Forrásaim szerint nem"
    };

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private SensorEventListener sensorListener;
    private float lastX, lastY, lastZ;
    private long lastShakeTime;

    private Random random;
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        questionEditText = findViewById(R.id.questionEditText);
        answerTextView = findViewById(R.id.answerTextView);
        generateButton = findViewById(R.id.generateButton);
        modeRadioGroup = findViewById(R.id.modeRadioGroup);
        shakeModeRadioButton = findViewById(R.id.shakeModeRadioButton);
        buttonModeRadioButton = findViewById(R.id.buttonModeRadioButton);

        random = new Random();
        mediaPlayer = MediaPlayer.create(this, R.raw.magic_sound);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (buttonModeRadioButton.isChecked()) {
                    generateAnswer("button", 0); // csak gomb módban aktív
                }
            }
        });

        // Szenzor inicializálás
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        sensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (!shakeModeRadioButton.isChecked()) return; // csak rázás módban aktív

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

                if (delta > 12) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastShakeTime > 1000) {
                        lastShakeTime = currentTime;
                        generateAnswer("shake", delta);
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };
    }

    private void generateAnswer(String source, double intensity) {
        String question = questionEditText.getText().toString().trim();

        if (question.isEmpty()) {
            answerTextView.setText("Írj be egy kérdést!");
            return;
        }

        String selectedAnswer;

        if ("shake".equals(source)) {
            if (intensity > 15) {
                selectedAnswer = positiveAnswers[random.nextInt(positiveAnswers.length)];
            } else if (intensity > 12) {
                selectedAnswer = neutralAnswers[random.nextInt(neutralAnswers.length)];
            } else {
                selectedAnswer = negativeAnswers[random.nextInt(negativeAnswers.length)];
            }
        } else {
            int category = random.nextInt(3);
            if (category == 0) {
                selectedAnswer = positiveAnswers[random.nextInt(positiveAnswers.length)];
            } else if (category == 1) {
                selectedAnswer = neutralAnswers[random.nextInt(neutralAnswers.length)];
            } else {
                selectedAnswer = negativeAnswers[random.nextInt(negativeAnswers.length)];
            }
        }

        answerTextView.setText(selectedAnswer);

        if (mediaPlayer != null) mediaPlayer.start();
        if (vibrator != null) vibrator.vibrate(500);
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

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }
}
