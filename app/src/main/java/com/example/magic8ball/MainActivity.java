package com.example.magic8ball;

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

    // Válaszlista – magyar skála szerint
    private final String[] answers = {
            "Biztosan így van", "Határozottan igen", "Valószínűleg",
            "Kérdezd meg később", "Nem tudom megmondani", "Ne számíts rá",
            "Erősen kétséges", "Igen", "Nem", "Jelek szerint igen"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        questionEditText = findViewById(R.id.questionEditText);
        answerTextView = findViewById(R.id.answerTextView);
        Button generateButton = findViewById(R.id.generateButton);

        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateAnswer();
            }
        });
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
}
