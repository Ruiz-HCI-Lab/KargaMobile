package org.ruizlab.phoni.kargamobile;

import static android.content.Context.SENSOR_SERVICE;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.ArrayList;

public class Analytics extends Worker implements SensorEventListener {
    public Analytics(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        ArrayList<String> performanceIndicators = new ArrayList<>();
    }

    @NonNull
    @Override
    public Result doWork() {
        SensorManager mSensorManager = null;
        Sensor mTemperature;

        mSensorManager = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
        mTemperature = mSensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE);

        mSensorManager.registerListener(this, mTemperature, SensorManager.SENSOR_DELAY_NORMAL);

        while (((Global) this.getApplicationContext()).mapperIsRunning())
        {

        }

        return Result.success();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float temperature = event.values[0];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
