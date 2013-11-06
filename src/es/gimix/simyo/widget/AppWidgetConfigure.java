package es.gimix.simyo.widget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.actionbarsherlock.app.SherlockActivity;

import es.gimix.simyo.PhoneNumberList;
import es.gimix.simyo.R;
import es.gimix.simyo.SimyoStore;
import es.gimix.simyo.controller.CredentialStore;
import es.gimix.simyo.controller.QueryReceiver;
import es.gimix.simyo.settings.CredentialsActivity;
import es.gimix.util.Logging;

/**
 * The configuration screen for the WidgetProvider.
 */
public class AppWidgetConfigure extends SherlockActivity {
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	private SimyoStore qc;
	private WidgetSettingsStore settings;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);

        // Find the widget id from the intent. 
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null)
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        // If they gave us an intent without the widget id, just bail.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
        	Logging.log("Invalid widget id");
            finish();
        }
        // draw window
        setTitle(R.string.widget_config_title);
    }
    
    @Override protected void onResume() {
    	super.onResume();
        //TODO refactor this
        // if password needed, launch config activity
        qc = new SimyoStore(this);
        settings = new WidgetSettingsStore(this);
		Intent credentialsIntent = new Intent(this, CredentialsActivity.class);
        if(new CredentialStore(this).needCredentials()) {
    		startActivityForResult(credentialsIntent,0);
        } else {
			String credentialError = qc.getCredentialError();
			if(credentialError != null) {
				startActivityForResult(credentialsIntent,0);
			} else {
				// check if cache is empty
				PhoneNumberList numbers = qc.getNumberList();
				if(numbers==null || numbers.size()==0) {
	        		this.sendBroadcast(new Intent(QueryReceiver.FORCE_UPADTE));
				} else {
			        // populate view
					String currentNumber = settings.getNumberForId(mAppWidgetId);
					if(!numbers.contains(currentNumber)) // check first one if none checked
						currentNumber = numbers.get(0);
			        setContentView(R.layout.widget_config_activity);
		        	RadioGroup group = (RadioGroup) findViewById(R.id.radioGroup);
			        for(String number : numbers) {
						String label = "+" + number; 
						if(label.startsWith("+34")) label = label.substring(3); //TODO refactor
			        	RadioButton button = new RadioButton(this);
			        	button.setText(label);
			        	button.setTag(number);
			        	button.setChecked(number.equals(currentNumber));
			        	button.setOnClickListener(mOnClickListener);
			        	group.addView(button);
			        }
				}
			}
        }
    }

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
        	// store selection
            String number = (String)((RadioButton) v).getTag();
            settings.putNumberForId(mAppWidgetId, number);
            
            // update
            sendBroadcast(new Intent(QueryReceiver.STATUS_CANCELLED));
            
            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };
}
