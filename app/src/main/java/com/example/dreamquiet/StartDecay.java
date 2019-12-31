//Thread to start the volume decay
package com.example.dreamquiet;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.util.Vector;

public class StartDecay extends Service {
    protected int counter;
    protected int totalSleepTime;
    final Handler handleDecay = new Handler();


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {  //if program fails, pass in some parameters here and make timer in this class.
        //Toast.makeText(this, "Service started.", Toast.LENGTH_LONG).show(); //test if function is called
        totalSleepTime = intent.getIntExtra("sleepTime", 0); //get parameter and initialize.

        runAsForeground();  //make program run in foreground (won't stop) and displays notification.
        startVolumeDecrement();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        handleDecay.removeCallbacksAndMessages(null); //wipe out the current startVolumeDecrement
        super.onDestroy();
        stopSelf();
        Toast.makeText(this, "Goodnight :)", Toast.LENGTH_LONG).show();
    }


    protected void runAsForeground(){
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, "DecayServiceChannel")
                .setContentTitle("Foreground Service")
                .setContentText("Sleeping...")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                //.setOngoing(true) //FIXME: may need to use this, if not use AlarmManager
                .setPriority(Notification.PRIORITY_HIGH)
                //.setTicker(text)  //FIXME if I want to make the timer count down
                .build();
//        if (android.os.Build.VERSION.SDK_INT >= 26){   //if API 26 or higher startForegroundService is needed.
//            startForegroundService(notificationIntent); //if API is 26 or higher then this works..
//        }
//        //then all devices need to startForeground.
        startForeground(1, notification);

    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    "DecayServiceChannel",
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }


    //the function that lowers the volume over time.
    protected void startVolumeDecrement() {
        //FIXME make this smoothly decease volume.
        //get the audio
        final AudioManager phoneVolume = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = phoneVolume.getStreamVolume(AudioManager.STREAM_MUSIC);
        //get the vector. Yes it's ugly. enjoy it.
        Vector decayVector = new Vector();
        decayVector = populateDecay(totalSleepTime, decayVector, currentVolume);
        final Vector decay = decayVector;

        counter = 0; //Reset the counter --> safe :P

        final Runnable volDecay = new Runnable(){
            public void run(){
                if(counter >= decay.size()-1 || phoneVolume.getStreamVolume(AudioManager.STREAM_MUSIC) == 0){
                    //if current volume isn't muted then MUTE IT!
                    if(phoneVolume.getStreamVolume(AudioManager.STREAM_MUSIC) > 0){
                        counter--;
                        handleDecay.postDelayed(this, ((int)decay.get(decay.size()-1)));
                        //phoneVolume.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, AudioManager.FLAG_SHOW_UI);  //If I wanna mute.
                    }
                    //If using spotify, it will pause it! :)
                    Intent pauseSpotify = new Intent("com.spotify.mobile.android.ui.widget.PLAY");
                    pauseSpotify.setPackage("com.spotify.music");
                    sendBroadcast(pauseSpotify);
                    return;     //return is redundant but why not...
                }
                //decrease volume then recall function
                phoneVolume.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                handleDecay.postDelayed(this, ((int) decay.get(counter++)));
            }
        };
        handleDecay.postDelayed(volDecay, ((int) decay.get(counter++)));    //start the initial counter
    }

    protected Vector populateDecay(int time, Vector decayVector, int volume) {
        int dTime = time * 60 * 1000;   //convert to milliseconds.
        for (int i = 0; i < volume; ++i) {
            dTime /= 2;
            decayVector.add(dTime);
        }
        decayVector.add(0);
        return decayVector;
    }
}