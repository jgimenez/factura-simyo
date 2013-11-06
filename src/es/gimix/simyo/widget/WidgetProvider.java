package es.gimix.simyo.widget;

import org.json.JSONException;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;
import es.gimix.simyo.PhoneNumberList;
import es.gimix.simyo.R;
import es.gimix.simyo.SimyoStore;
import es.gimix.simyo.Summary;
import es.gimix.simyo.SummaryList;
import es.gimix.simyo.controller.QueryReceiver;

public class WidgetProvider extends AppWidgetProvider {
	private Context context;
	private SimyoStore qc;
	private WidgetSettingsStore settings;
	protected boolean big = false;
	
	@Override public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		// refresh view
		updateAllSummaries(false);
		// query the receiver for an update
        context.sendBroadcast(new Intent(QueryReceiver.OPPORTUNISTIC_UPDATE));
    }
	
	@Override public void onReceive(Context context, Intent intent) {
        this.context = context;
        this.qc = new SimyoStore(context);
        this.settings = new WidgetSettingsStore(context);
		if(QueryReceiver.STATUS_UPDATED.equals(intent.getAction())) {
			try {
				SummaryList summaries = SummaryList.fromJson(intent.getStringExtra(QueryReceiver.EXTRA_SUMMARY));
				onNewConsumptionSummaries(summaries);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else if(QueryReceiver.STATUS_WORKING.equals(intent.getAction())) {
			onUpdateWorking();
		} else if(QueryReceiver.STATUS_CANCELLED.equals(intent.getAction())) {
			onUpdateCancelled();
		} else {
			super.onReceive(context, intent);
		}
	}
    
    private void onNewConsumptionSummaries(SummaryList summaries) {
    	updateAllSummaries(false);
    }
    
    private void onUpdateCancelled() {
    	updateAllSummaries(false);
    }

    private void onUpdateWorking() {
    	updateAllSummaries(true);
    }
    
    private void updateAllSummaries(boolean working) {
        // Tell the AppWidgetManager to perform an update on the current app widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisWidget = new ComponentName(context, getClass());
        int[] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(thisWidget);
        for(int id : ids) {
        	RemoteViews views = getViewsForId(id, working);
    		appWidgetManager.updateAppWidget(id, views);
        }
    }
    
    private RemoteViews getViewsForId(int id, boolean working) {
    	String number = settings.getNumberForId(id);
    	Summary summary;
    	if(number == null) {
    		PhoneNumberList list = qc.getNumberList();
    		if(list == null || list.size() == 0) { // no numbers
    			summary = null;
    		} else { // there are numbers, but none configured, just pick one
        		summary = qc.getSummary(list.get(0));
    		}
    	} else { // configured number
    		summary = qc.getSummary(number);
    	}
		// Get the layout for the App Widget and attach an on-click listener
		// to the whole widget
    	RemoteViews views = WidgetRemoteViews.getRemoteViews(summary, working, big);
    	Intent intent = new Intent(context, AppWidgetConfigure.class);
    	intent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
    	intent.setData(Uri.parse("id:" + id)); // not needed, just make the intent appear different so the PendingIntent is not reused
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
    	PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.whole_widget, pendingIntent);

    	return views;
    }
}
