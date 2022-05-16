package org.ruizlab.phoni.kargamobile;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Analytics extends Worker{

    public Analytics(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        ArrayList<String> analyticValues = new ArrayList<>();
        long startTime, endTime, elapsedTime;
        float maxRam = 0;
        float totalRam = 0;
        float currentRam;
        float maxTemp = 0;
        float totalTemp = 0;
        float currentTemp;
        int counter = 0;
        startTime = System.currentTimeMillis();

        /*
        List of analytics:
            [0]- A. Sequence file
            [1]- B. Reference file
            [2]- E. Total wall time
            [3]- F. Total CPU time
            [4]- G. Max RAM usage
            [5]- H. Average RAM usage
            [6]- I. Max temperature
            [7]- J. Average temperature
        */

        try {
            String fileLocation = getApplicationContext().getExternalFilesDir(null) + "/" + ((Global)this.getApplicationContext()).getSequenceFilename() + "_Analytics.csv";
            FileWriter fileWriter = new FileWriter(fileLocation);
            BufferedWriter writer = new BufferedWriter(fileWriter);
            writer.write("Sequence File,Reference File,Total Wall Time,Total CPU Time,Max RAM Usage,Average RAM Usage,Max Temperature,Average Temperature\r\n");
            analyticValues.add(((Global)this.getApplicationContext()).getSequenceFilename()); //0
            analyticValues.add(((Global)this.getApplicationContext()).getReferenceFilename()); //1

            System.out.println("Analytics started with Sequence File: "+ analyticValues.get(0) +", Reference File: "+ analyticValues.get(1));

            while (((Global) this.getApplicationContext()).mapperIsRunning())
            {
                currentRam = (float) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
                if (maxRam < currentRam) {
                    maxRam = currentRam;
                }
                totalRam += currentRam;
                currentTemp = ((Global)getApplicationContext()).getTemp();
                if (maxTemp < currentTemp) {
                    maxTemp = currentTemp;
                }
                totalTemp += currentTemp;
                counter++;
                Thread.sleep(3000);

                System.out.println("Analytics read #"+ counter +". Current ram: "+ currentRam +", Max ram: "+ maxRam + ", Current temp: "+ currentTemp +", Max temp: "+ maxTemp);
            }

            totalRam = totalRam/counter;
            totalTemp = totalTemp/counter;

            endTime = System.currentTimeMillis();
            elapsedTime = endTime-startTime;
            analyticValues.add(""+elapsedTime/1000); //2
            analyticValues.add(""+elapsedTime/1000); //3
            analyticValues.add(""+maxRam); //4
            analyticValues.add(""+totalRam); //5
            analyticValues.add(""+maxTemp); //6
            analyticValues.add(""+totalTemp); //7

            for (int i = 0; i < 7; i++) {
                writer.write(analyticValues.get(i)+",");
            }
            writer.write(analyticValues.get(7)+"\r\n");
            writer.close();
            return Result.success();
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return Result.failure();
        }
    }

}
