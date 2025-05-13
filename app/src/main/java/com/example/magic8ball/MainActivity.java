package com.example.magic8ball;

import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private EditText questionEditText;
    private TextView answerTextView;
    private Button generateButton;
    private RadioGroup modeRadioGroup, languageRadioGroup;
    private RadioButton shakeModeRadioButton, buttonModeRadioButton;
    private RadioButton hungarianRadioButton, englishRadioButton;
    private ProgressBar shakeProgressBar;

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

    private final String[] positiveAnswers_en = {
            "It is certain", "Definitely yes", "Yes", "Signs point to yes", "You may rely on it",
            "Most likely", "Outlook good", "Without a doubt"
    };
    private final String[] neutralAnswers_en = {
            "Reply hazy, try again", "Ask again later", "Better not tell you now",
            "Cannot predict now", "Concentrate and ask again"
    };
    private final String[] negativeAnswers_en = {
            "Don't count on it", "My reply is no", "Outlook not so good",
            "Very doubtful", "My sources say no"
    };

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private SensorEventListener sensorListener;
    private float lastX, lastY, lastZ;
    private long lastShakeTime;

    private boolean measuringShake = false;
    private double maxShakeValue = 0;
    private long shakeStartTime = 0;

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
        //languageRadioGroup = findViewById(R.id.languageRadioGroup);
        shakeModeRadioButton = findViewById(R.id.shakeModeRadioButton);
        buttonModeRadioButton = findViewById(R.id.buttonModeRadioButton);
        //hungarianRadioButton = findViewById(R.id.hungarianRadioButton);
        //englishRadioButton = findViewById(R.id.englishRadioButton);
        shakeProgressBar = findViewById(R.id.shakeProgressBar);

        random = new Random();
        mediaPlayer = MediaPlayer.create(this, R.raw.magic_sound);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        generateButton.setOnClickListener(view -> {
            if (buttonModeRadioButton.isChecked()) {
                generateAnswer("button", 0);
            }
        });
/*
        languageRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.hungarianRadioButton) {
                setLocale("hu");
            } else if (checkedId == R.id.englishRadioButton) {
                setLocale("en");
            }
        });
*/
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        sensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (!shakeModeRadioButton.isChecked()) return;

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

                if (measuringShake) {
                    if (delta > maxShakeValue) {
                        maxShakeValue = delta;
                        runOnUiThread(() -> shakeProgressBar.setProgress((int) Math.min(100, delta * 5)));
                    }

                    if (System.currentTimeMillis() - shakeStartTime > 3000) {
                        measuringShake = false;
                        runOnUiThread(() -> shakeProgressBar.setVisibility(View.GONE));
                        generateAnswer("shake", maxShakeValue);
                    }
                } else if (delta > 12) {
                    measuringShake = true;
                    maxShakeValue = delta;
                    shakeStartTime = System.currentTimeMillis();
                    runOnUiThread(() -> {
                        shakeProgressBar.setProgress((int) Math.min(100, delta * 5));
                        shakeProgressBar.setVisibility(View.VISIBLE);
                    });
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };
    }

    private void generateAnswer(String source, double intensity) {
        String question = questionEditText.getText().toString().trim();

        if (question.isEmpty()) {
            answerTextView.setText(getString(R.string.hint_question));
            return;
        }

        String[] positives = hungarianRadioButton.isChecked() ? positiveAnswers : positiveAnswers_en;
        String[] neutrals = hungarianRadioButton.isChecked() ? neutralAnswers : neutralAnswers_en;
        String[] negatives = hungarianRadioButton.isChecked() ? negativeAnswers : negativeAnswers_en;

        String selectedAnswer;
        if ("shake".equals(source)) {
            if (intensity > 15) {
                selectedAnswer = positives[random.nextInt(positives.length)];
            } else if (intensity > 12) {
                selectedAnswer = neutrals[random.nextInt(neutrals.length)];
            } else {
                selectedAnswer = negatives[random.nextInt(negatives.length)];
            }
        } else {
            int category = random.nextInt(3);
            if (category == 0) {
                selectedAnswer = positives[random.nextInt(positives.length)];
            } else if (category == 1) {
                selectedAnswer = neutrals[random.nextInt(neutrals.length)];
            } else {
                selectedAnswer = negatives[random.nextInt(negatives.length)];
            }
        }

        answerTextView.setText(selectedAnswer);
        if (mediaPlayer != null) mediaPlayer.start();
        if (vibrator != null) vibrator.vibrate(500);
    }

    private void setLocale(String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        recreate();
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
