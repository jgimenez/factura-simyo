package es.gimix.simyo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.robotmedia.billing.BillingController;
import net.robotmedia.billing.BillingRequest.ResponseCode;
import net.robotmedia.billing.helper.AbstractBillingObserver;
import net.robotmedia.billing.model.Transaction.PurchaseState;

import org.json.JSONException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.Toast;

import cat.mobilejazz.utilities.system.AndroidFlavors;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import es.gimix.simyo.controller.CredentialStore;
import es.gimix.simyo.controller.QueryReceiver;
import es.gimix.simyo.settings.CredentialsActivity;
import es.gimix.simyo.settings.SimyoSettings;
import es.gimix.simyo.widget.WidgetRemoteViews;
import es.gimix.util.Logging;

public class FacturaSimyoActivity extends SherlockActivity {

	private SimyoStore qc;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.setTheme(R.style.Theme_HoloEverywhereDark_Sherlock);
		super.onCreate(savedInstanceState);
		qc = new SimyoStore(this);
		setContentView(R.layout.main);
		//showNewsDialog();
	}

	@Override
	protected void onResume() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(QueryReceiver.STATUS_UPDATED);
		filter.addAction(QueryReceiver.STATUS_CANCELLED);
		filter.addAction(QueryReceiver.STATUS_WORKING);
		registerReceiver(receiver, filter);

		final long MINUTE = 60000;
		super.onResume();
		onUpdateCancelled();
		// if password needed, launch config activity
		if (new CredentialStore(this).needCredentials()) {
			doEditCredentials(); // we can pass null because arg is not used
		} else {
			String credentialError = qc.getCredentialError();
			if (credentialError != null) {
				doEditCredentials(); // we can pass null because arg is not
											// used
			} else { // check if cache is old
				PhoneNumberList numbers = qc.getNumberList();
				if (numbers == null || numbers.size() == 0) {
					this.sendBroadcast(new Intent(QueryReceiver.FORCE_UPADTE));
				} else {
					Summary summary = qc.getSummary(numbers.get(0));
					if (summary == null) {
						this.sendBroadcast(new Intent(
								QueryReceiver.FORCE_UPADTE));
					} else {
						if (summary.timestamp.getTime() + 10 * MINUTE < new Date()
								.getTime()) // more than 10min ago
							this.sendBroadcast(new Intent(
									QueryReceiver.OPPORTUNISTIC_UPDATE));
					}
				}
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.main_menu, menu);
		if(AndroidFlavors.isBlackBerry()) { // hide donation menu, specific to Google Play
			MenuItem item = menu.findItem(R.id.menu_donate);
			item.setVisible(false);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
		case R.id.menu_about:
			Intent intent = new Intent(FacturaSimyoActivity.this,
					AboutActivity.class);
			startActivity(intent);
			return true;
		case R.id.menu_refresh:
			// send update request to widget
			sendBroadcast(new Intent(QueryReceiver.FORCE_UPADTE));
			// display reloading message
			Toast.makeText(FacturaSimyoActivity.this,
					R.string.login_activity_reloading, Toast.LENGTH_LONG).show();
			return true;
		case R.id.menu_settings:
			Intent intent2 = new Intent(FacturaSimyoActivity.this,
					SimyoSettings.class);
			startActivity(intent2);
			return true;
/*		case R.id.menu_donate:
			doDonate();
			return true;*/
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void doEditCredentials() {
		Intent intent = new Intent(FacturaSimyoActivity.this,
				CredentialsActivity.class);
		startActivity(intent);
	}

	// preview
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (QueryReceiver.STATUS_UPDATED.equals(intent.getAction())) {
				try {
					SummaryList summaries = SummaryList.fromJson(intent
							.getStringExtra(QueryReceiver.EXTRA_SUMMARY));
					if (summaries != null)
						onNewConsumptionSummaries(summaries);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else if (QueryReceiver.STATUS_WORKING.equals(intent.getAction())) {
				onUpdateWorking();
			} else if (QueryReceiver.STATUS_CANCELLED
					.equals(intent.getAction())) {
				onUpdateCancelled();
			}
		}
	};

	private void onNewConsumptionSummaries(SummaryList summary) {
		updateAllSummaries(false);
	}

	private void onUpdateCancelled() {
		updateAllSummaries(false);
	}

	private void onUpdateWorking() {
		updateAllSummaries(true);
	}

	private void updateAllSummaries(boolean working) {
		ArrayList<RemoteViews> views = new ArrayList<RemoteViews>();
		PhoneNumberList numbers = qc.getNumberList();
		if (numbers != null) { // first time nothing is cached
			for (String number : numbers) {
				Summary summary = qc.getSummary(number);
				views.add(WidgetRemoteViews.getRemoteViews(summary, working, true, false));
			}
		}
		if (views.size() == 0)
			views.add(WidgetRemoteViews.getRemoteViews(null, working, true, false));
		replaceSummaries(views);
	}

	private void replaceSummaries(List<RemoteViews> views) {
		ViewGroup vg = (ViewGroup) findViewById(R.id.preview);
		if (vg.getChildCount() > 0)
			vg.removeAllViews();
		for (RemoteViews v : views) {
			View inflatedView = v.apply(this, vg);
			inflatedView.getLayoutParams().height = (int) (290 * getResources()
					.getDisplayMetrics().density); // 250dp
			vg.addView(inflatedView);
		}
		vg.requestLayout();
	}
	
	/*
	//http://misha.beshkin.lv/android-alertdialog-with-checkbox/
	public static final String CHECKBOX_PREFS_NAME = "CheckBoxPref";
	public CheckBox dontShowAgain;
	
	private void showNewsDialog() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		LayoutInflater adbInflater = LayoutInflater.from(this);
		View dialogWithCheckboxLayout = adbInflater.inflate(R.layout.checkbox,
				null);
		dontShowAgain = (CheckBox) dialogWithCheckboxLayout
				.findViewById(R.id.skip);
		dialog.setView(dialogWithCheckboxLayout);
		dialog.setTitle(R.string.checkbox_title);
		dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String checkBoxResult = "NOT checked";
				if (dontShowAgain.isChecked())
					checkBoxResult = "checked";
				SharedPreferences settings = getSharedPreferences(
						CHECKBOX_PREFS_NAME, 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("skipMessage", checkBoxResult);
				// Commit the edits!
				editor.commit();
				return;
			}
		});

		SharedPreferences settings = getSharedPreferences(CHECKBOX_PREFS_NAME,
				0);
		String skipMessage = settings.getString("skipMessage", "NOT checked");
		if (!skipMessage.equalsIgnoreCase("checked"))
			dialog.show();
	}*/
	
	protected void doDonate() {
		AbstractBillingObserver mBillingObserver = new AbstractBillingObserver(this) {
			public void onBillingChecked(boolean supported) {
				Logging.log("Billing supported: " + supported);
				BillingController.requestPurchase(FacturaSimyoActivity.this, "es.gimix.simyo.donation.5", true);
			}

			public void onPurchaseStateChanged(String itemId, PurchaseState state) {
				Logging.log("Purchase state: " + state);
				if(state == PurchaseState.PURCHASED)
					Toast.makeText(FacturaSimyoActivity.this, R.string.donation_thanks, Toast.LENGTH_LONG).show();// say thanks
			}

			public void onRequestPurchaseResponse(String itemId, ResponseCode response) {
				// that's ok
			}
		};

		BillingController.registerObserver(mBillingObserver);
		BillingController.checkBillingSupported(this.getApplicationContext()).toString();
	}
}
