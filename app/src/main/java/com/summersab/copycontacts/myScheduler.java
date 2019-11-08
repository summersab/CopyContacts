package com.summersab.copycontacts;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

public class myScheduler {
    public static void scheduleAlarm(Context context) {
        ((AlarmManager) context.getSystemService("alarm")).setInexactRepeating(2, SystemClock.elapsedRealtime() + 3600000, 3600000, PendingIntent.getBroadcast(context, 0, new Intent(context, AlarmReceiver.class), 0));
    }
}
