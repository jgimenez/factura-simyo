package es.gimix.simyo.controller;

import java.io.IOException;
import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.widget.Toast;
import es.gimix.simyo.FacturaSimyoApp;
import es.gimix.simyo.PhoneNumberList;
import es.gimix.simyo.SimyoStore;
import es.gimix.simyo.Summary;
import es.gimix.simyo.SummaryList;
import es.gimix.simyo.api.SimyoAPIException;
import es.gimix.simyo.api.SimyoApi;
import es.gimix.simyo.api.SimyoCredentialsException;
import es.gimix.simyo.settings.TimeCalculator;
import es.gimix.simyo.widget.SimyoNotificationManager;
import es.gimix.util.Logging;
import es.gimix.util.ResultOrException;

/** {@link BroadcastReceiver} that issues API queries on demand. Used by the UI but also for scheduled
 * alerts or system events. */
public class QueryReceiver extends BroadcastReceiver {
	public static final String FORCE_UPADTE = "es.gimix.simyo.FORCE_UPDATE";
	public static final String OPPORTUNISTIC_UPDATE = "es.gimix.simyo.OPPORTUNISTIC_UPDATE";
	public static final String STATUS_CANCELLED = "es.gimix.simyo.status.CANCELLED";
	public static final String STATUS_UPDATED = "es.gimix.simyo.status.UPDATED";
	public static final String STATUS_WORKING = "es.gimix.simyo.status.WORKING";
	public static final String EXTRA_SUMMARY = "es.gimix.simyo.extra.SUMMARY";
	
	private static boolean mWorking = false;
	
	private Context context;

	@Override public void onReceive(Context context, Intent intent) {
		this.context = context;
		String action = intent.getAction();
		if(FORCE_UPADTE.equals(action))
			forceUpdate();
		else if(OPPORTUNISTIC_UPDATE.equals(action))
			opportunisticUpdate();
    }
	
	public void opportunisticUpdate() {
		// check latest update and see whether wifi or 3g update is needed
		SimyoStore store = new SimyoStore(context);
		TimeCalculator calculator = new TimeCalculator(context);
		
		Date latestUpdate = store.getLatestUpdateTimestamp();
		
		if(calculator.is3gSetOff(latestUpdate)) { // needs update, no matter which network
			es.gimix.util.Logging.log("Scheduling refresh, even if 3G");
			forceUpdate();			
		} else if(WifiConnectivityReceiver.isWifiConnected(context) && 
				calculator.isWifiSetOff(latestUpdate)) { // needs wifi update, check wifi state
			es.gimix.util.Logging.log("Scheduling refresh, because it is wifi");
			forceUpdate();
		} else {
			es.gimix.util.Logging.log("Skipping refresh");
		}
	}
	
	public void forceUpdate() {
		if(isWorking()) {
			es.gimix.util.Logging.log("Already working, not refreshing again");
			return;
		}
		new AsyncTask<Void,Void,ResultOrException<SummaryList>>() {
			CredentialStore credentialStore = new CredentialStore(context);
			SimyoStore qc = new SimyoStore(context);
			WakeLock wakeLock;
			
			@Override protected void onPreExecute() {
				notifyWorking();
				if(credentialStore.needCredentials()) {
					FacturaSimyoApp.getInstance().getNotificationManager().notifyLoginError(null);
					notifyCancel();
					cancel(true);
					return;
				}
				es.gimix.util.Logging.log("Refreshing now");
				PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
				wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "es.gimix.simyo");
				wakeLock.acquire();
				es.gimix.util.Logging.log("Got wake lock");
			}

			@Override protected ResultOrException<SummaryList> doInBackground(Void... params) {
				SimyoApi simyoApi = credentialStore.getSimyoApiInstance();
				try {
					String credentialError = qc.getCredentialError();
					if(credentialError != null)
						throw new SimyoCredentialsException(credentialError);
					PhoneNumberList numbers = simyoApi.getNumberList();
					qc.putNumberList(numbers);
                    SummaryList result = new SummaryList();
					for(String number : numbers) {
						try {
							Summary summary = simyoApi.getSummary(number);
							qc.putSummary(summary);
	                        result.add(summary);
						} catch(SimyoAPIException ex) {
                            Logging.log(ex.toString());
                            //Logging.sendLogs();
						}
					}
                    return new ResultOrException<SummaryList>(result);
				} catch (SimyoCredentialsException e) {
					qc.putCredentialError(e.getMessage());
					return new ResultOrException<SummaryList>(e);
				} catch (Exception e) {
					Logging.log(e.toString());
					//Logging.sendLogs();
					return new ResultOrException<SummaryList>(e);
				}
			}
			
			@Override protected void onPostExecute(ResultOrException<SummaryList> result) {
				wakeLock.release();
				try {
					es.gimix.util.Logging.log("Sending result back");
					notifyResult(result.get());
				} catch (SimyoCredentialsException e) {
					es.gimix.util.Logging.log("Credentials error");
					notifyCancel();
					notifyCredentialError(e.getMessage());
				} catch (Exception e) {
					es.gimix.util.Logging.log("Error during aquisition");
					e.printStackTrace();
					notifyCancel();
					if((e instanceof IOException) && 
							((IOException)e).getMessage() != null &&
							((IOException)e).getMessage().startsWith("50")) {
						showNoServiceToast();
					}
				}
			}
			
			@Override protected void onCancelled() {
				notifyCancel();
			}
		}.execute();
	}
	
	private void notifyWorking() {
		mWorking = true;
		context.sendBroadcast(new Intent(STATUS_WORKING));
	}

	private void notifyCancel() {
		mWorking = false;
		context.sendBroadcast(new Intent(STATUS_CANCELLED));
	}

	private void notifyCredentialError(String message) {
		new SimyoNotificationManager(context).notifyLoginError(message);
	}
	
	private void notifyResult(SummaryList summaries) {
		mWorking = false;
		Intent i = new Intent(STATUS_UPDATED);
		i.putExtra(EXTRA_SUMMARY, summaries.toJson());
		context.sendBroadcast(i);
	}
	
	public boolean isWorking() {
		return mWorking;
	}
	
	private void showNoServiceToast() {
		CharSequence text = "El servidor de Simyo no responde.";
		int duration = Toast.LENGTH_LONG;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}
}
