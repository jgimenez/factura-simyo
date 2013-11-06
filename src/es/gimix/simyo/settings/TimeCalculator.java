package es.gimix.simyo.settings;

import java.util.Date;

import es.gimix.simyo.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class TimeCalculator {
	private final String optionStrings[];
	private final int optionValues[];
	private final SharedPreferences sharedPreferences;
	
	public TimeCalculator(Context context) {
		optionStrings = context.getResources().getStringArray(R.array.time_strings);
		optionValues = context.getResources().getIntArray(R.array.time_values);
		if(optionStrings.length != optionValues.length)
			throw new RuntimeException("time_strings and time_values differ in length");
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public int getTimeOptionCount() {
		return optionStrings.length;
	}
	
	public String getLabelForOption(int option) {
		return optionStrings[option];
	}
	
	public int getSecondsForOption(int option) {
		return optionValues[option];
	}
	
	public int getWifiSecondsPreference() {
		int option = sharedPreferences.getInt(SimyoSettings.KEY_WIFI, SimyoSettings.KEY_WIFI_DEFAULT);
		return getSecondsForOption(option);
	}

	public int get3gSecondsPreference() {
		int option = sharedPreferences.getInt(SimyoSettings.KEY_3G, SimyoSettings.KEY_3G_DEFAULT);
		return getSecondsForOption(option);
	}
	
	public boolean isWifiSetOff(Date latestUpdate) {
		Date now = new Date();
		long timeElapsed = now.getTime() - latestUpdate.getTime();
		return timeElapsed > 1000L * getWifiSecondsPreference();
	}
	
	public boolean is3gSetOff(Date latestUpdate) {
		Date now = new Date();
		long timeElapsed = now.getTime() - latestUpdate.getTime();
		return timeElapsed > 1000L * get3gSecondsPreference();
	}

}
