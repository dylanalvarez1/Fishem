package com.example.geeksquad.finalproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class CreditsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);
    }

    public void onButtonClick(View view) {
        Intent i = new Intent(getBaseContext(), MainActivity.class);
        startActivity(i);
    }
}
