package org.ruizlab.phoni.kargamobile;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Analytics extends Worker{

    private NotificationManager notificationManager;

    public Analytics(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {

        System.out.println("ANALYTICS STARTED");
        setForegroundAsync(createForegroundInfo());
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

                endTime = System.currentTimeMillis();
                elapsedTime = endTime-startTime;

                System.out.println("Analytics read #"+ counter +". Elapsed time: " + elapsedTime/1000 + "s, Current ram: "+ currentRam/(1024*1024)+"MB" +", Max ram: "+ maxRam/(1024*1024)+"MB" + ", Current temp: "+ currentTemp +", Max temp: "+ maxTemp);
            }

            totalRam = totalRam/counter;
            totalTemp = totalTemp/counter;

            endTime = System.currentTimeMillis();
            elapsedTime = endTime-startTime;
            analyticValues.add(""+elapsedTime/1000); //2
            analyticValues.add(""+elapsedTime/1000); //3
            analyticValues.add(""+maxRam/(1024*1024)+"MB"); //4
            analyticValues.add(""+totalRam/(1024*1024)+"MB"); //5
            analyticValues.add(""+maxTemp); //6
            analyticValues.add(""+totalTemp); //7

            for (int i = 0; i < 7; i++) {
                writer.write(analyticValues.get(i)+",");
            }
            writer.write(analyticValues.get(7)+"\r\n");
            writer.close();

            System.out.println("ANALYTICS FINISHED");
            return Result.success();
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return Result.failure();
        }
    }

    @NonNull
    private ForegroundInfo createForegroundInfo() {

        Context context = getApplicationContext();
        String id = "2";
        String title = "Analytics";

        /*
        String cancel = context.getString(R.string.cancel_download);
        // This PendingIntent can be used to cancel the worker
        PendingIntent intent = WorkManager.getInstance(context)
                .createCancelPendingIntent(getId());
         */

        createChannel(id);

        Notification notification = new NotificationCompat.Builder(context,id)
                .setContentTitle(title)
                .setTicker(title)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                //.setSmallIcon(R.drawable.ic_work_notification)
                .setOngoing(true)
                // Add the cancel action to the notification which can
                // be used to cancel the worker
                //.addAction(android.R.drawable.ic_delete, cancel, intent)
                .build();
        return new ForegroundInfo(Integer.parseInt(id),notification);
    }

    private void createChannel(String id) {
        CharSequence name = "KargaMobile - Analytics";
        String description = "Analytics Process";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(id, name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }


}
