package com.example.geeksquad.finalproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //The good one
    public void onFishingButtonClick(View view) {
        Intent i = new Intent(getBaseContext(), RotationActivity.class);
        startActivity(i);
    }

    //Open tutorial
    public void onTutorialButtonClick(View view) {
        Intent i = new Intent(getBaseContext(), TutorialActivity.class);
        startActivity(i);
    }

    //Open credits
    public void onCreditsButtonClick(View view) {
        Intent i = new Intent(getBaseContext(), CreditsActivity.class);
        startActivity(i);
    }
}
