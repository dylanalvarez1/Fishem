package com.example.geeksquad.finalproject;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import static java.lang.Math.PI;

public class RotationActivity extends AppCompatActivity implements SensorEventListener {
    SensorManager sensorManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rotation);
        Sensor mRotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sensorManager.registerListener(this, mRotationVectorSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int number) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final int eventType = event.sensor.getType();

        if (eventType != Sensor.TYPE_ROTATION_VECTOR) return;

        long timeNow            = System.nanoTime();

        float mOrientationData[] = new float[3];
        calcOrientation(mOrientationData, event.values.clone());

        // Do what you want with mOrientationData
    }

    private void calcOrientation(float[] orientation, float[] incomingValues) {
        // Get the quaternion
        float[] quatF = new float[4];
        SensorManager.getQuaternionFromVector(quatF, incomingValues);

        // Get the rotation matrix
        //
        // This is a variant on the code presented in
        // http://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToMatrix/
        // which has been altered for scaling and (I think) a different axis arrangement. It
        // tells you the rotation required to get from the between the phone's axis
        // system and the earth's.
        //
        // Phone axis system:
        // https://developer.android.com/guide/topics/sensors/sensors_overview.html#sensors-coords
        //
        // Earth axis system:
        // https://developer.android.com/reference/android/hardware/SensorManager.html#getRotationMatrix(float[], float[], float[], float[])
        //
        // Background information:
        // https://en.wikipedia.org/wiki/Rotation_matrix
        //
        float[][] rotMatF = new float[3][3];
        rotMatF[0][0] = quatF[1]*quatF[1] + quatF[0]*quatF[0] - 0.5f;
        rotMatF[0][1] = quatF[1]*quatF[2] - quatF[3]*quatF[0];
        rotMatF[0][2] = quatF[1]*quatF[3] + quatF[2]*quatF[0];
        rotMatF[1][0] = quatF[1]*quatF[2] + quatF[3]*quatF[0];
        rotMatF[1][1] = quatF[2]*quatF[2] + quatF[0]*quatF[0] - 0.5f;
        rotMatF[1][2] = quatF[2]*quatF[3] - quatF[1]*quatF[0];
        rotMatF[2][0] = quatF[1]*quatF[3] - quatF[2]*quatF[0];
        rotMatF[2][1] = quatF[2]*quatF[3] + quatF[1]*quatF[0];
        rotMatF[2][2] = quatF[3]*quatF[3] + quatF[0]*quatF[0] - 0.5f;

        // Get the orientation of the phone from the rotation matrix
        //
        // There is some discussion of this at
        // http://stackoverflow.com/questions/30279065/how-to-get-the-euler-angles-from-the-rotation-vector-sensor-type-rotation-vecto
        // in particular equation 451.
        //
        final float rad2deg = (float)(180.0 / PI);
        orientation[0] = (float)Math.atan2(-rotMatF[1][0], rotMatF[0][0]) * rad2deg;
        orientation[1] = (float)Math.atan2(-rotMatF[2][1], rotMatF[2][2]) * rad2deg;
        orientation[2] = (float)Math.asin ( rotMatF[2][0])                * rad2deg;
        if (orientation[0] < 0) orientation[0] += 360;

        TextView current1 = findViewById(R.id.currentX);
        TextView current2 = findViewById(R.id.currentY);
        TextView current3 = findViewById(R.id.currentZ);

        current1.setText("orientation 1: " + orientation[0]);
        current2.setText("orientation 2: " + orientation[1]);
        current3.setText("orientation 3: " + orientation[2]);

    }
}
