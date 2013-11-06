package es.gimix.simyo.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

import es.gimix.simyo.R;
import es.gimix.simyo.controller.CredentialStore;

public class SimyoSettings extends SherlockPreferenceActivity implements OnPreferenceClickListener, OnSharedPreferenceChangeListener {
	public static final String KEY_LOGIN = "loginPref";
	public static final String KEY_WIFI = "wifiPref";
	public static final String KEY_3G = "3gPref";
	public static final int KEY_WIFI_DEFAULT = 0; // 3 hours
	public static final int KEY_3G_DEFAULT = 8; // never

	private Preference mLoginPref;
	private TimeBarPreference mWifiPref;
	private TimeBarPreference m3gPref;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// get references to preferences
		mLoginPref = (Preference) findPreference(KEY_LOGIN);
		mWifiPref = (TimeBarPreference) findPreference(KEY_WIFI);
		m3gPref = (TimeBarPreference) findPreference(KEY_3G);
		// setup login preference
		mLoginPref.setOnPreferenceClickListener(this);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	public boolean onPreferenceClick(Preference preference) {
        // for login dialog, which is a separate activity
		if(KEY_LOGIN.equals(preference.getKey())) {
			Intent intent = new Intent(this, CredentialsActivity.class);
			startActivityForResult(intent, 1);
			return true;
		}
		return false;
	}

    @Override
    protected void onResume() {
        super.onResume();

        // Setup the initial values
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        refreshPreferenceSummaries(sharedPreferences);

        // Set up a listener whenever a key changes            
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister the listener whenever a key changes            
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);    
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    	refreshPreferenceSummaries(sharedPreferences);
    }
    
    private void refreshPreferenceSummaries(SharedPreferences sharedPreferences) {
		SharedPreferences loginPrefs = this.getSharedPreferences("simyo", Context.MODE_PRIVATE);
        String login = loginPrefs.getString(CredentialStore.PREF_USERNAME, null);
        int wifiValue = sharedPreferences.getInt(KEY_WIFI, KEY_WIFI_DEFAULT);
        int the3gValue = sharedPreferences.getInt(KEY_3G, KEY_3G_DEFAULT);
        
        mLoginPref.setSummary(login == null ? "Indique su usuario y clave" : "Usuario " + login);
        mWifiPref.setSummary(mWifiPref.getLabel(wifiValue));
        m3gPref.setSummary(m3gPref.getLabel(the3gValue));
    }

}
