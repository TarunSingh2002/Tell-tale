package com.example.myapplication;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class storyPage extends AppCompatActivity {

    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_page);
        textView = findViewById(R.id.textView);
        Intent intent = getIntent();
        if (intent != null) {
            String receivedText = intent.getStringExtra("KEY_TEXT_TO_PASS");
            if (receivedText != null) {
                textView.setText(receivedText);
            }
        }

    }
}