package com.musichackparis.andromin;

import com.musichackparis.andromin.util.SystemUiHider;

import android.app.Activity;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class AndrominActivity extends Activity implements View.OnClickListener, SensorEventListener
{

    private  SensorManager mSensorManager = null;
    private  Sensor mSensor1 = null;
    private  Sensor mSensor2 = null;

    private WavePlayer wavePlayer;

    /** type of curve that has been selected */
    private int selectedGenerator;

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
        //noinspection deprecation
        mSensor1 = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mSensor2 = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        wavePlayer = new WavePlayer(this);

        Typeface myTypeface = Typeface.createFromAsset(getAssets(), "Airstream.ttf");
        overrideButtonTypeFace(R.id.saw, myTypeface);
        overrideButtonTypeFace(R.id.sine, myTypeface);
        overrideButtonTypeFace(R.id.square, myTypeface);
    }

    private void overrideButtonTypeFace(int id, Typeface typeface) {
        Button myTextView = (Button)findViewById(id);
        myTextView.setTypeface(typeface);
    }

    private void toggleButtons(ToggleButton selectedButton) {
        for (int i : new int[] { R.id.sine, R.id.saw, R.id.square }) {
            if (selectedButton.getId() == i) {
                // not really working here:
                // android:drawableLeft="@drawable/ic_lock_silent_mode_off"
                selectedButton.setCompoundDrawablesWithIntrinsicBounds(selectedButton.isChecked() ? getResources().getDrawable(R.drawable.ic_lock_silent_mode_off) : null, null, null, null);
            } else {
                ToggleButton btn = (ToggleButton) findViewById(i);
                btn.setChecked(false);
                btn.setCompoundDrawables(null, null, null, null);
            }
        }
    }

    @Override
    public void onClick(View v)
    {
        selectedGenerator = v.getId();
        toggleButtons((ToggleButton) v);

        /*
        switch(v.getId())
        {
            case R.id.sine: wavePlayer.playStopToggle(); break;
            case R.id.saw: wavePlayer.playStopToggle(); break;
            case R.id.square: wavePlayer.playStopToggle(); break;
        }
        */
        wavePlayer.playStopToggle();
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
        wavePlayer.stop();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {/*not used*/}

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        switch (event.sensor.getType())
        {
            //noinspection deprecation
            case Sensor.TYPE_ORIENTATION:   wavePlayer.setPitchData(event.values[1], this); break;
            case Sensor.TYPE_ACCELEROMETER: wavePlayer.setGainData(Math.abs(event.values[0]) + Math.abs(event.values[1]) + Math.abs(event.values[2]), this); break;
        }
    }

    public int getSelectedGenerator() {
        return selectedGenerator;
    }
}
