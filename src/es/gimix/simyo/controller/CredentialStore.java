package es.gimix.simyo.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import es.gimix.simyo.api.SimyoApi;

/** Stores and retrieves access credentials. Call {@link #needCredentials()} to check if you need
 * to ask for them in the UI. You can not get the password, but rather use {@link #getSimyoApiInstance()}
 * to get a working instance. */
public class CredentialStore {
	public static final String PREF_USERNAME = "username";
	public static final String PREF_PASSWORD = "password";

	private final SharedPreferences sharedPrefs;
	
	public CredentialStore(Context context) {
		sharedPrefs = context.getSharedPreferences("simyo", Context.MODE_PRIVATE);
	}
	
	public void put(String username, String password) {
		sharedPrefs.edit()
				.putString(PREF_USERNAME, username)
				.putString(PREF_PASSWORD, password)
				.commit();
	}

	public SimyoApi getSimyoApiInstance() {
		return SimyoApi.getInstance(getUsername(), getPassword());
	}
	
	public String getUsername() {
		return sharedPrefs.getString(PREF_USERNAME, "");
	}

	private String getPassword() {
		return sharedPrefs.getString(PREF_PASSWORD, "");
	}
	
	public boolean needCredentials() {
        return TextUtils.isEmpty(getUsername()) || TextUtils.isEmpty(getPassword());
	}
}
