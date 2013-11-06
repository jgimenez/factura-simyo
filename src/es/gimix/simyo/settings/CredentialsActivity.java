package es.gimix.simyo.settings;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;

import es.gimix.simyo.FacturaSimyoApp;
import es.gimix.simyo.R;
import es.gimix.simyo.SimyoStore;
import es.gimix.simyo.controller.CredentialStore;
import es.gimix.simyo.controller.QueryReceiver;

/**
 * Activity which displays login screen to the user.
 */
public class CredentialsActivity extends SherlockActivity {
	public static final String PREF_LOGIN_ERROR = "login_error";

	private CredentialStore credentialStore;

    private EditText mUsernameEdit;
    private EditText mPasswordEdit;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        // dismiss notification
        FacturaSimyoApp.getInstance().getNotificationManager().cancelNeedCredentials();
        // get credentials
        credentialStore = new CredentialStore(this);
        // draw window
        setContentView(R.layout.login_activity);
        getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        // set title
        this.setTitle(R.string.login_notification_ticker);
        
        mUsernameEdit = (EditText) findViewById(R.id.username_edit);
        mPasswordEdit = (EditText) findViewById(R.id.password_edit);
        mUsernameEdit.setText(credentialStore.getUsername());
    }
    
    /**
     * Handles onClick event on the Submit button. Sends username/password to
     * the server for authentication.
     * 
     * @param view The Submit button for which this method is invoked
     */
    public void handleLogin(View view) {
        String username = mUsernameEdit.getText().toString();
        String password = mPasswordEdit.getText().toString();
        if(TextUtils.isEmpty(username) || TextUtils.isEmpty(password))  {// do not save, is an error
    		Toast.makeText(this, R.string.login_activity_loginfail_infomissing, Toast.LENGTH_LONG).show();
        	return;
        }
        // save
        credentialStore.put(username, password);
        new SimyoStore(this).putCredentialError(null);
		// send update request to widget
		Intent intent = new Intent(QueryReceiver.FORCE_UPADTE);
		this.sendBroadcast(intent);
		// display reloading message
		Toast.makeText(this, R.string.login_activity_reloading, Toast.LENGTH_LONG).show();
		finish();
    }
}