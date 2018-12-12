package com.example.geeksquad.finalproject;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.os.VibrationEffect;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Range;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Vibrator;
import java.util.Random;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/*
Plan: Tilt to proper angle to cast...
You hit the cast button....animation plays or whatever
hold still....
after random amount of time, buzz! (you have a small window to shake device as much as you can)(use a handler for this "window" of time)
(shaking device adds to some value, if some value is high enough at the thresh hold, boom you caught the fish).
 */



public class RotationActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    Sensor accelerometer;
    private float[] accelerometerReading = new float[3];
    private float[] magnetometerReading = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];
    private boolean isVertical;
    private boolean isCast;
    private float deltaX = 0;
    private float deltaY = 0;
    private float deltaZ = 0;
    private float deltaXMax = 0;
    private float deltaYMax = 0;
    private float deltaZMax = 0;
    private float lastX, lastY, lastZ;
    private boolean first;
    private boolean waiting;
    private boolean bite;
    private float bitingValue = 0;
    private float bitingOffset;
    private float timeOffset;
    private int randomDelay;
    private int fishType;
    private String fishName;
    private AnimationDrawable frameAnimation;
    private Button reloadButton;
    private Button menuButton;


    AnimationDrawable rocketAnimation;


    TextView castInstr;

    private float vibrateThreshold = 0;
    Vibrator v;
    TextView maxY;

    //Runnable and the delay before it runs again
    Handler handler;
    final int delay = 2000;

    //lower alpha should equal smoother movement
    private static final float ALPHA = 0.005f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rotation);

        isVertical = false;
        isCast = false;
        first = true;
        waiting = false;
        bite = false;
        bitingValue = 0;
        Random rand = new Random();
        fishType = rand.nextInt(3);
        randomDelay = 1000 * rand.nextInt(3) + 1;
        switch(fishType) {
            case 0:
                //Hard fish
                bitingOffset = 100;
                timeOffset = 1500;
                fishName = "Shark";
                break;
            case 1:
                //Easier fish
                bitingOffset = 50;
                timeOffset = 200;
                fishName = "Red Snapper";
                break;
            case 2:
                //Easiest fish
                bitingOffset = -100;
                timeOffset = -500;
                fishName = "Boot";
                break;
            default:
                fishName = "Default";
                timeOffset = 0;
                bitingOffset = 0;

        }

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
            vibrateThreshold = accelerometer.getMaximumRange() / 2;
        }
        Sensor magneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            mSensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }

        maxY = findViewById(R.id.maxY);

        frameAnimation= new AnimationDrawable();
        frameAnimation.setOneShot(true);
        frameAnimation.addFrame(getResources().getDrawable(R.drawable.castingframe1), 100);
        frameAnimation.addFrame(getResources().getDrawable(R.drawable.castingframe2), 100);
        frameAnimation.addFrame(getResources().getDrawable(R.drawable.castingframe3), 100);
        frameAnimation.addFrame(getResources().getDrawable(R.drawable.castingframe4), 100);
        frameAnimation.addFrame(getResources().getDrawable(R.drawable.castingframe5), 100);
        frameAnimation.addFrame(getResources().getDrawable(R.drawable.castingframe6), 100);

       ImageView animateAndroid = (ImageView) findViewById(R.id.fishing_rod);
       animateAndroid.setBackgroundDrawable(frameAnimation);

        reloadButton = (Button)this.findViewById(R.id.reloadButton);
        reloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(getIntent());
            }
        });

        menuButton = (Button)this.findViewById(R.id.menuButton);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });






    }

    private float[] applyLowPassFilter(float[] input, float[] output) {
        if ( output == null ) return input;

        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    public void onCastClick(View view) {
        isCast = true;
        Button button = findViewById(R.id.castButton);
        button.setVisibility(View.INVISIBLE);
        TextView castText = findViewById(R.id.tiltText);
        castText.setVisibility(View.INVISIBLE);
        TextView castInstr = findViewById(R.id.instructions);
        castInstr.setVisibility(View.VISIBLE);
        maxY.setVisibility(View.VISIBLE);
        TextView title = findViewById(R.id.title);
        title.setText("Current Acceleration:");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(!isCast) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.length);
                //accelerometerReading = applyLowPassFilter(event.values, accelerometerReading);

            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.length);
                //magnetometerReading = applyLowPassFilter(event.values, magnetometerReading);
            }

            // Rotation matrix based on current readings from accelerometer and magnetometer.
            SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading);

            // Express the updated rotation matrix as three orientation angles.
            SensorManager.getOrientation(rotationMatrix, orientationAngles);

            TextView current1 = findViewById(R.id.currentX);
            TextView current2 = findViewById(R.id.currentY);
            TextView current3 = findViewById(R.id.currentZ);

            //current1.setText("" + rotationMatrix[0]);
            //current2.setText("" + rotationMatrix[1]);
            //current3.setText("" + rotationMatrix[2]);

            current1.setText("" + Math.round(Math.toDegrees(orientationAngles[0])));
            current2.setText("" + Math.round(Math.toDegrees(orientationAngles[1] * -1)));
            current3.setText("" + Math.round(Math.toDegrees(orientationAngles[2])));

            //if you hold your phone up, buzz
            if(((Math.toDegrees(orientationAngles[1] * -1)) - 50) > 0) {
            /*
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                //deprecated in API 26
                v.vibrate(500);
            }
            */
                Button button = findViewById(R.id.castButton);
                button.setVisibility(View.VISIBLE);

                TextView castText = findViewById(R.id.tiltText);
                castText.setVisibility(View.INVISIBLE);
            }
            else {
                Button button = findViewById(R.id.castButton);
                button.setVisibility(View.INVISIBLE);

                TextView castText = findViewById(R.id.tiltText);
                castText.setVisibility(View.VISIBLE);
            }
        }
        else {
            if(first) {
                mSensorManager.unregisterListener(this);
                mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                first = false;

                // set the last know values of x,y,z
                lastX = event.values[0];
                lastY = event.values[1];
                lastZ = event.values[2];
            }
           else if(!first && !waiting) {
                TextView current1 = findViewById(R.id.currentX);
                TextView current2 = findViewById(R.id.currentY);
                TextView current3 = findViewById(R.id.currentZ);

                //If you cast, then listen for acceleration event in the y axis above a certain threshhold
                // get the change of the x,y,z values of the accelerometer
                deltaX = Math.abs(lastX - event.values[0]);
                deltaY = Math.abs(lastY - event.values[1]);
                deltaZ = Math.abs(lastZ - event.values[2]);

                // if the change is below 2, it is just plain noise
                if (deltaX < 2)
                    deltaX = 0;
                if (deltaY < 2)
                    deltaY = 0;
                if (deltaZ < 2)
                    deltaZ = 0;

                // set the last know values of x,y,z
                lastX = event.values[0];
                lastY = event.values[1];
                lastZ = event.values[2];

                current1.setText("" + deltaX);
                current2.setText("" + deltaY);
                current3.setText("" + deltaZ);

                displayMaxValues();

                //One small vibrate means you cast successfully
                vibrate();
            }

            //You cast the rod, so any motion you do now will add to the biteValue, good or bad. This could either catch the fish or scare it off
            else {

                //If you cast, then listen for acceleration event in the y axis above a certain threshhold
                // get the change of the x,y,z values of the accelerometer
                deltaX = Math.abs(lastX - event.values[0]);
                deltaY = Math.abs(lastY - event.values[1]);
                deltaZ = Math.abs(lastZ - event.values[2]);

                // if the change is below 2, it is just plain noise
                if (deltaX < 2)
                    deltaX = 0;
                if (deltaY < 2)
                    deltaY = 0;
                if (deltaZ < 2)
                    deltaZ = 0;

                // set the last know values of x,y,z
                lastX = event.values[0];
                lastY = event.values[1];
                lastZ = event.values[2];

                bitingValue += deltaX + deltaY + deltaZ;
                //castInstr.setText("" + bitingValue);
            }

        }



    }

    public void displayMaxValues() {

        if (deltaY > deltaYMax) {
            deltaYMax = deltaY;
            //maxY.setText(Float.toString(deltaYMax));
        }

    }

    // if the change in the accelerometer value is big enough, then vibrate!
    // our threshold is MaxValue/2
    public void vibrate() {
        if (deltaYMax > 9) {
            v.vibrate(50);
            castInstr = findViewById(R.id.instructions);
            castInstr.setText("You cast the bait! Hold still to not scare any fish off.");
            findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
            frameAnimation.start();
            deltaYMax = 0;
            waiting = true;
            handler = new Handler();

            handler.postDelayed(new Runnable(){
                public void run(){
                    bitingValue = 0;
                    handler.postDelayed(new Runnable(){
                        public void run(){
                            //Create the wait for the bite
                            //I.E....if they move, let the fish get away

                            if(bitingValue > 150) {
                                castInstr.setText("You scared all the fish off, try to hold still next time");
                                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                                v.vibrate(150);
                                return;
                            }

                            handler.postDelayed(new Runnable(){
                                public void run(){
                                    //The fish bit the line! Now shake with all your strength.
                                    castInstr.setText("Its biting! Don't let it get away!");
                                    findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                                    v.vibrate(delay + (int)timeOffset);


                                    handler.postDelayed(new Runnable(){
                                        public void run(){

                                            v.vibrate(150);

                                            //Check and see if you caught the fish based on that catching variable
                                            if(bitingValue < 250 + bitingOffset) {
                                                castInstr.setText("It got away!");
                                                reloadButton.setVisibility(View.VISIBLE);
                                                menuButton.setVisibility(View.VISIBLE);
                                                return;
                                            }

                                            castInstr.setText("Great job, you caught a " + fishName);
                                            reloadButton.setVisibility(View.VISIBLE);
                                            menuButton.setVisibility(View.VISIBLE);

                                        }
                                    }, delay + (int)timeOffset);
                                }
                            }, delay + (int)timeOffset);
                        }
                    }, delay + randomDelay);

                }
            }, delay);


        }
        else if(deltaYMax > 4){
            TextView castInstr = findViewById(R.id.instructions);
            castInstr.setText("Try and cast the net a little farther");
        }
    }

    //onResume() register the accelerometer for listening the events
    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    //onPause() unregister the accelerometer for stop listening the events
    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

