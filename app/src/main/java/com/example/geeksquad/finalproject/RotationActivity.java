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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Vibrator;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;



public class RotationActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private float[] accelerometerReading = new float[3];
    private float[] magnetometerReading = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];
    private boolean isVertical;
    Vibrator v;


    //Runnable and the delay before it runs again
    Handler handler;
    final int delay = 200;

    //lower alpha should equal smoother movement
    private static final float ALPHA = 0.005f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rotation);

        isVertical = false;
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
        Sensor magneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            mSensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }

        handler = new Handler();

        handler.postDelayed(new Runnable(){
            public void run(){
                //do something





                handler.postDelayed(this, delay);
            }
        }, delay);

    }

    private float[] applyLowPassFilter(float[] input, float[] output) {
        if ( output == null ) return input;

        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
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
        if(((Math.toDegrees(orientationAngles[1] * -1)) - 80) > 1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                //deprecated in API 26
                v.vibrate(500);
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

