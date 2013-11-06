package es.gimix.simyo;

import java.util.Date;
import java.util.HashMap;

import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;

/** Stores and retrieves business objects. For now: number list, summaries, credential error */
public class SimyoStore {
	private static final String PREF_NUMBER_LIST = "number_list";
	private static final String PREF_SUMMARY = "summary_";
	private static final String PREF_CREDENTIAL_ERROR = "login_error";
	
	private static PhoneNumberList mNumberList;
	private static HashMap<String,Summary> mSummaryMap = new HashMap<String,Summary>();
	private static String mCredentialsError;

	private final SharedPreferences sharedPrefs;
	
	public SimyoStore(Context context) {
		sharedPrefs = context.getSharedPreferences("simyo", Context.MODE_PRIVATE);
	}
	
	public void putNumberList(PhoneNumberList numbers) {
		mNumberList = numbers;
		sharedPrefs.edit()
				.putString(PREF_NUMBER_LIST, numbers.toJson())
				.commit();
	}
	
	public PhoneNumberList getNumberList() {
		if(mNumberList == null) {
			String strNumberList = sharedPrefs.getString(PREF_NUMBER_LIST, null);
			if(strNumberList == null) return null;
			try {
				mNumberList = PhoneNumberList.fromJson(strNumberList);
			} catch (JSONException e) {
				e.printStackTrace();
				return null; // just ignore cached stuff
			}
		}
		return mNumberList;
	}
	
	/** Returns latest update timestamp, or Epoch if no update is available yet. */
	public Date getLatestUpdateTimestamp() {
		PhoneNumberList numbers = getNumberList();
		long result = 0;
		if(numbers != null) {
			for(String number : numbers) {
				Summary s = getSummary(number);
				if(s != null) // better safe than sorry
					result = Math.max(result, s.timestamp.getTime());				
			}
		}
		return new Date(result);
	}
	
	public void putSummary(Summary summary) {
		synchronized (mSummaryMap) {
			mSummaryMap.put(summary.number, summary);			
		}
		sharedPrefs.edit()
				.putString(PREF_SUMMARY + summary.number, summary.toJson())
				.commit();
	}

	public Summary getSummary(String number) {
		if(mSummaryMap.get(number) == null) {
			String strSummary = sharedPrefs.getString(PREF_SUMMARY + number, null);
			if(strSummary == null) return null;
			try {
				synchronized (mSummaryMap) {
					mSummaryMap.put(number, Summary.fromJson(strSummary));
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return null; // just ignore cached stuff
			}
		}
		return mSummaryMap.get(number);
	}
	
	public void putCredentialError(String message) {
		mCredentialsError = message;
		sharedPrefs.edit()
		.putString(PREF_CREDENTIAL_ERROR, message)
		.commit();
	}
	
	public String getCredentialError() {
		if(mCredentialsError == null) {
			mCredentialsError = sharedPrefs.getString(PREF_CREDENTIAL_ERROR, null);
		}
		return mCredentialsError;
	}
}
