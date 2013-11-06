package es.gimix.simyo.controller;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class RefreshAlarm {
	
	/** Sets up an alarm to set off every 1 hour and check if updates are needed */
	public static void setUpAlarm(Context context) {
    	// update intent
    	Intent updateIntent = new Intent(QueryReceiver.OPPORTUNISTIC_UPDATE);
    	PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, updateIntent, 0);
    	// set up alarm to trigger an update
    	AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    	alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0, AlarmManager.INTERVAL_HOUR, pendingIntent);
	}

}
