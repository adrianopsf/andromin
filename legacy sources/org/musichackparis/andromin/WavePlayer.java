package org.musichackparis.andromin;

import android.content.Context;
import android.widget.Toast;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.FloatMath;

public class WavePlayer implements AudioTrack.OnPlaybackPositionUpdateListener
{
	final int REFILLSIZE = 512;
	final int SENSORREFILLSIZE = REFILLSIZE*10;
	
	private AudioTrack track = null;
	private short[] audioData = new short[REFILLSIZE];
	private float[] pitchData = new float[SENSORREFILLSIZE];
	private float[] gainData = new float[SENSORREFILLSIZE];
	private int pitchSensorTop = 0;
	private int gainSensorTop = 0;
	private int sampleRate;
	private int bufferSize;
	private float cycleState = 0;

	public WavePlayer()
	{
		super();
		
		sampleRate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC);
		int minBufferSize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
		bufferSize = minBufferSize;
		
		track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);
	}
	
	public void play(Context ctx)
	{
		Toast.makeText(ctx, "play>", Toast.LENGTH_SHORT).show();
		
		track.stop();
		
		// must fill 1 buffer for the sound to start
		int n = 0;
		for (int i=0; i<bufferSize; i+= REFILLSIZE)
		{
			refill();
			n++;
		}
		refill();
		
		// setup callback
		track.setPlaybackPositionUpdateListener(this);
		track.setNotificationMarkerPosition(REFILLSIZE*n - REFILLSIZE/2); // marker at half buffer before end
		track.play();
	}
	
	public void stop(Context ctx)
	{
		Toast.makeText(ctx, "<>stop", Toast.LENGTH_SHORT).show();
		
		track.stop();
	}
	
	public void refill()
	{
		
		int i, k, m = 0;
		float twopi = 2 * (float)Math.PI;
		float freq, gain, nbSamplesPerCycle;
		for (i=0; i<REFILLSIZE; i++)
		{
			// interpolation: use all sensor data in one buffer
			k = (int)FloatMath.floor((float)i / (float)REFILLSIZE * (float)pitchSensorTop);
			m = (int)FloatMath.floor((float)i / (float)REFILLSIZE * (float)gainSensorTop);
			freq = (float)Math.abs(((pitchData[k] < 0) ? pitchData[k]+360 : pitchData[k]) - 90) * 30;
			gain = (float)Math.abs(gainData[k] + (k>0 ? gainData[k-1] : gainData[k]) + (k>1 ? gainData[k-2] : gainData[k]) - 50) * 300;
			if (gain >= 30000)
				gain = 30000;
			nbSamplesPerCycle = (float)sampleRate / freq;
			cycleState += twopi / nbSamplesPerCycle;
			audioData[i] = (short)FloatMath.floor(FloatMath.sin(cycleState) * gain);
			if (cycleState > twopi)
				cycleState -= twopi;
		}
		if (pitchSensorTop > 0)
		{
			pitchData[0] = pitchData[pitchSensorTop-1];
			pitchSensorTop = 0;
		}
		pitchSensorTop = 0;
		if (gainSensorTop > 0)
		{
			gainData[0] = gainData[gainSensorTop-1];
			gainSensorTop = 0;
		}
		gainSensorTop = 0;
		track.write(audioData, 0, REFILLSIZE);
	}

	@Override
	public void onMarkerReached(AudioTrack track)
	{
		refill();
		//track.setPositionNotificationPeriod(REFILLSIZE / frameSize);
		int prevhead = track.getNotificationMarkerPosition();
		track.setNotificationMarkerPosition((prevhead / REFILLSIZE)*REFILLSIZE + REFILLSIZE/2);
	}

	@Override
	public void onPeriodicNotification(AudioTrack track) {/*not used*/}
	
	public void setPitchData(float data, Context ctx)
	{
		if (pitchSensorTop < SENSORREFILLSIZE)
		{
			pitchData[pitchSensorTop] = data;
			pitchSensorTop++;
		}
		else
		{
			Toast.makeText(ctx, "Overflow!", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void setGainData(float data, Context ctx)
	{
		if (pitchSensorTop < SENSORREFILLSIZE)
		{
			gainData[pitchSensorTop] = data;
			gainSensorTop++;
		}
		else
		{
			Toast.makeText(ctx, "Overflow!", Toast.LENGTH_SHORT).show();
		}
	}
}
