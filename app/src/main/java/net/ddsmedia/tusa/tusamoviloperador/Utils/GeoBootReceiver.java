package net.ddsmedia.tusa.tusamoviloperador.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class GeoBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {

            // Set the alarm here.
            GeoAlarmReceiver alarm = new GeoAlarmReceiver();
            if(alarm != null){
                alarm.SetAlarm(context);
            }else{
                //Toast.makeText(getBaseContext(), "Alarm is null", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
