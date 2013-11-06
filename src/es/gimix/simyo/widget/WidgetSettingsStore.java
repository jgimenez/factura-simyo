package es.gimix.simyo.widget;

import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;

public class WidgetSettingsStore {
	private static final String PREF_NUMBER_FOR_ID_ = "numberForId_";
	
	private static HashMap<Integer,String> mNumberForIdMap = new HashMap<Integer, String>();

	private final SharedPreferences sharedPrefs;
	
	public WidgetSettingsStore(Context context) {
		sharedPrefs = context.getSharedPreferences("simyo", Context.MODE_PRIVATE);
	}
	
	public void putNumberForId(int id, String number) {
		mNumberForIdMap.put(id, number);
		sharedPrefs.edit()
				.putString(PREF_NUMBER_FOR_ID_ + id, number)
				.commit();
	}
	
	public String getNumberForId(int id) {
		String result = mNumberForIdMap.get(id);
		if(result == null) {
			result = sharedPrefs.getString(PREF_NUMBER_FOR_ID_ + id, null);
			if(result == null) return null;
			mNumberForIdMap.put(id, result);
		}
		return result;
	}
}
