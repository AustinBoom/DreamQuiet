//Aight so what this does is keeps the program alive until onDestroy is called.
package com.example.dreamquiet;

import android.app.Service;
        import android.content.Intent;
        import android.os.IBinder;
        import android.widget.Toast;
public class RunCountdownClock extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {  //if program fails, pass in some parameters here and make timer in this class.
        //Toast.makeText(this, "Service started.", Toast.LENGTH_LONG).show();
        testoutput();
        return START_STICKY;
    }

    public void testoutput(){
        System.out.println("ME BEGINS!");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
        Toast.makeText(this, "Goodnight :)", Toast.LENGTH_LONG).show();
    }
}