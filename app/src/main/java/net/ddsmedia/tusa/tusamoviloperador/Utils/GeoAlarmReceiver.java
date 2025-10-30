package net.ddsmedia.tusa.tusamoviloperador.Utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class GeoAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        /*PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "YOUR TAG");
        //Acquire the lock
        wl.acquire();

        //You can do the processing here.
        Bundle extras = intent.getExtras();
        StringBuilder msgStr = new StringBuilder();

        if(extras != null && extras.getBoolean(ONE_TIME, Boolean.FALSE)){
            //Make sure this intent has been sent by the one-time timer button.
            msgStr.append("One time Timer : ");
        }
        Format formatter = new SimpleDateFormat("hh:mm:ss a");
        msgStr.append(formatter.format(new Date()));

        Toast.makeText(context, msgStr, Toast.LENGTH_LONG).show();

        //Release the lock
        wl.release();*/

        //Toast.makeText(context,"Alarma!!! "+,Toast.LENGTH_SHORT).show();


        String msg = "Geo Service alive => ALARMA";
        //Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        Log.i("GEOAPP_ALARMA",msg);

        Intent myIntent = new Intent(context, SaveGeoService.class);
        //context.startService(myIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(myIntent);
        } else {
            context.startService(myIntent);
        }
    }

    public void SetAlarm(Context context)
    {
        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, GeoAlarmReceiver.class);
        //intent.putExtra(ONE_TIME, Boolean.FALSE);

        PendingIntent pi = null;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pi = PendingIntent.getBroadcast
                    (context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }
        else
        {
            pi = PendingIntent.getBroadcast
                    (context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        //PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);

        //After after 5 seconds
        //am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 5 , pi);

        am.setRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime(),
                Globals.FOUR_MINUTES, pi);

                //AlarmManager.INTERVAL_FIFTEEN_MINUTES, pi);
    }

    public void CancelAlarm(Context context)
    {
        Intent intent = new Intent(context, GeoAlarmReceiver.class);

        PendingIntent sender = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            sender = PendingIntent.getBroadcast
                    (context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }
        else
        {
            sender = PendingIntent.getBroadcast
                    (context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        //PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
        Log.i("GEOAPP_ALARMA","Alarma apagada...");
    }




}
