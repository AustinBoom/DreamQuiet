//Copyright Austin Huyboom

//TODO: Future possible implementations:
//Add option to pause media.
//add instructions screen
//add donation option
//add show volume widget option
//add warning when volume is increased/decreased during decay
//make volume decay a smooth transition.


package com.example.dreamquiet;
//Alt + Enter to automatically import.

import androidx.appcompat.app.AppCompatActivity;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    //Member variables:
    protected int totalSleepTime;
    protected TextView minuteTime;
    protected TextView hourTime;
    protected TextView colon;
    protected TextView centeredMinutes;
    protected int counter;
    final Handler handleDecay = new Handler();

    //Methods:
    @Override
    protected void onCreate(Bundle savedInstanceState) {    //method that does it all!
        //boiler plate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //my code:
        //startService(new Intent(MainActivity.this, RunCountdownClock.class)); //keep app open in background until the onDestroy is called.
        initializeStartTime();  //this is like a constructor but dynamic.
        useStartButton();
        useResetButton();
        useIncreaseButton();
        useDecreaseButton();

    }


    protected void initializeStartTime() {
        centeredMinutes = (TextView) findViewById(R.id.centerMinute);
        minuteTime = (TextView) findViewById(R.id.minVal);
        hourTime = (TextView) findViewById(R.id.hourVal);
        colon = (TextView) findViewById(R.id.colon);
        totalSleepTime = Integer.parseInt(minuteTime.getText().toString());
        totalSleepTime += Integer.parseInt(hourTime.getText().toString()) * 60;
        setTime();
    }


    //START BUTTON METHOD:
    protected void useStartButton() {
        //get button. The button also imports os.Bundle and widget.Button
        final Button startButton = (Button) findViewById(R.id.StartButton);
        final Button resetButton = (Button) findViewById(R.id.ResetButton);
        //make button do stuff!
            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startButton.setVisibility(View.GONE);
                    resetButton.setVisibility(View.VISIBLE);
                    startDecay();   //calls StartDecay class and starts volume decay.
                }
            });
    }

    protected void useResetButton(){
        //get button. The button also imports os.Bundle and widget.Button
        final Button startButton = (Button) findViewById(R.id.StartButton);
        final Button resetButton = (Button) findViewById(R.id.ResetButton);

        //make button do stuff!
            resetButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startButton.setVisibility(View.VISIBLE);
                    resetButton.setVisibility(View.GONE);
                    totalSleepTime = 30;
                    setTime();
                    //FIXME replenish increase and decrease buttons, and stop the countdown timer
                    stopDecay();
                    //handleDecay.removeCallbacksAndMessages(null); //wipe out the current startVolumeDecrement  //now in StartDecay destructor.
                }
            });
    }


    protected void useIncreaseButton() {
        //make the increase button
        final Button increaseTimeButt = (Button) findViewById(R.id.IncreaseTime);

        //make button do stuff!
        increaseTimeButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTime();
            }
        });
    }

    protected void useDecreaseButton() { //FIXME implement decrease button here
        //make the decrease button
        final Button decreaseTimeButt = (Button) findViewById(R.id.DecreaseTime);

        //make button do stuff!
        decreaseTimeButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subtractTime();
            }
        });
    }

    private void addTime() {
        if (totalSleepTime < 0) {  //if it gets less than zero, that's an error.
            totalSleepTime = 0;  //fix error
        } else if (totalSleepTime < 55) {
            totalSleepTime += 1;
        } else {
            totalSleepTime += 5;
        }
        setTime();
    }

    private void subtractTime() {
        if (totalSleepTime <= 0) {  //if it gets less than zero, that's an error.
            totalSleepTime = 0;
        } else if (totalSleepTime < 55) {
            totalSleepTime -= 5;
        } else {  //if greater than 55
            totalSleepTime -= 5;
        }
        setTime();
    }

    private void setTime() {
        if ((totalSleepTime >= 60) && ((totalSleepTime % 60) < 10)) {
            centeredMinutes.setText(Integer.toString(totalSleepTime % 60));
            minuteTime.setText("0" + Integer.toString(totalSleepTime % 60));
            hourTime.setText(Integer.toString(totalSleepTime / 60));
        } else {
            centeredMinutes.setText(Integer.toString(totalSleepTime % 60));
            minuteTime.setText(Integer.toString(totalSleepTime % 60));
            hourTime.setText(Integer.toString(totalSleepTime / 60));
        }
        changeHourDisplay();
    }

    private void changeHourDisplay() { //set views invisible
        if (totalSleepTime < 60) {
            centeredMinutes.setVisibility(View.VISIBLE);

            hourTime.setVisibility(View.GONE);
            colon.setVisibility(View.GONE);
            minuteTime.setVisibility(View.GONE);
        } else {
            centeredMinutes.setVisibility(View.GONE);

            hourTime.setVisibility(View.VISIBLE);
            colon.setVisibility(View.VISIBLE);
            minuteTime.setVisibility(View.VISIBLE);
        }

    }
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //Thread functions... I mean methods.
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    //StartDecay services
/*    public void startDecay(){
        Intent intent = new Intent(this, StartDecay.class);
        intent.putExtra("sleepTime",totalSleepTime);    //pass parameter to service.
        startService(intent);
    }

    public void stopDecay(){
        Intent intent = new Intent(this, StartDecay.class);
        stopService(intent);
    }*/


    public void startDecay(){
        //this is used to pass the time to the job.
        PersistableBundle bundle = new PersistableBundle();
        bundle.putInt("totsleep", totalSleepTime);

        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);

        JobInfo jobInfo = new JobInfo.Builder(11, new ComponentName(this, StartDecayJob.class))
                // only add if network access is required
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setExtras(bundle)  //this is used to pass the time to the job.
                .build();

        jobScheduler.schedule(jobInfo);
    }

    public void stopDecay(){
        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancelAll();
    }







    //RunCountdownClock services
    public void startService(){
        Intent intent = new Intent(this, RunCountdownClock.class);
        startService(intent);

    }

    public void stopService(){
        Intent intent = new Intent(this, RunCountdownClock.class);
        stopService(intent);
    }
}




//This is an alternative way to do the decay function.
//It uses the first 3 elements of the array and is more linear than handler approach.
/*    protected synchronized void startTimer(final AudioManager phoneVolume, final Vector decayVector) {
        if (t == null) {
            TimerTask decreaseVolume = new TimerTask() {
                @Override
                public void run() {
                    phoneVolume.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                }
            };

            t = new Timer();
            t.scheduleAtFixedRate(decreaseVolume, ((int) decayVector.get(counter++)), ((int)decayVector.get(counter++)));
        }
    }*/



//test array values
/*        for (int i = 0; i < decayVector.size(); ++i) {
            System.out.println(decayVector.get(i));
        }*/

//Get the current volume and output it.
//String stringVolume = Integer.toString(currentVolume);  //this string is for testing purposes
//Toast.makeText(MainActivity.this, stringVolume, Toast.LENGTH_SHORT).show(); //display the volume level. For testing purposes only.


//VOLUME LOWERING FUNCTIONS
//Lower Volume
//phoneVolume.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
//Lower Volume without UI. Use this one once testing is done.
// phoneVolume.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);


//NO LONGER USED HERE BUT USED TO BE:


//sub method of START BUTTON.
//the function that lowers the volume over time.
/*    protected void startVolumeDecrement() {
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
                    stopService();    //ENDS PROGRAM!!!!!!!!!!
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
    }*/
