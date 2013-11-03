package com.musichackparis.andromin;

import com.musichackparis.andromin.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class AndrominActivity extends Activity implements View.OnClickListener, SensorEventListener
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.androminactivity_fullscreen);
        findViewById(R.id.sine).setOnClickListener(this);
        findViewById(R.id.saw).setOnClickListener(this);
        findViewById(R.id.square).setOnClickListener(this);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mSensor1 = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mSensor2 = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        wavePlayer = new WavePlayer();
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.sine: wavePlayer.playStopToggle(this); break;
            case R.id.saw: wavePlayer.playStopToggle(this); break;
            case R.id.square: wavePlayer.playStopToggle(this); break;
        }
    }

    protected void onResume()
    {
        super.onResume();
        mSensorManager.registerListener(this, mSensor1, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mSensor2, SensorManager.SENSOR_DELAY_GAME);
    }

    protected void onPause()
    {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {/*not used*/}

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        switch (event.sensor.getType())
        {
            case Sensor.TYPE_ORIENTATION:   wavePlayer.setPitchData(event.values[1], this); break;
            case Sensor.TYPE_ACCELEROMETER: wavePlayer.setGainData(Math.abs(event.values[0]) + Math.abs(event.values[1]) + Math.abs(event.values[2]), this); break;
        }
    }

    private  WavePlayer wavePlayer = null;
    private  SensorManager mSensorManager = null;
    private  Sensor mSensor1 = null;
    private  Sensor mSensor2 = null;
}
