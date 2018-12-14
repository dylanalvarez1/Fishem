package com.example.geeksquad.finalproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class TutorialActivity extends AppCompatActivity {
    ImageView img;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        img = findViewById(R.id.imageView);
    }

    public void onMenuButtonClick(View view) {
        Intent i = new Intent(getBaseContext(), MainActivity.class);
        startActivity(i);
    }

    public void onButtonLClick(View view) {
        img.setImageResource(R.drawable.howtomenu2);
    }
    public void onButtonRClick(View view) {
        img.setImageResource(R.drawable.howtomenu1);
    }
}
