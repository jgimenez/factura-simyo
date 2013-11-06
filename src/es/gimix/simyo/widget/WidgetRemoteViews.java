package es.gimix.simyo.widget;

import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Map.Entry;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import es.gimix.simyo.FacturaSimyoApp;
import es.gimix.simyo.R;
import es.gimix.simyo.Summary;
import es.gimix.simyo.Summary.AmountEuros;
import es.gimix.util.Logging;

/** Generates a {@link RemoteViews} out of a {@link Summary} */
public class WidgetRemoteViews {
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM HH:mm");

	private static final int[][] widgetResIds = {
		{R.id.a1, R.id.a2, R.id.a3},
		{R.id.b1, R.id.b2, R.id.b3},
		{R.id.c1, R.id.c2, R.id.c3},
		{R.id.d1, R.id.d2, R.id.d3},
		{R.id.e1, R.id.e2, R.id.e3}
	};
	private static final int[] widgetResRowIds = {R.id.rowa, R.id.rowb, R.id.rowc, R.id.rowd, R.id.rowe};
	private static final int MAX_ROWS = 5;
	
	public static RemoteViews getRemoteViews(Summary summary, boolean working) {
		return getRemoteViews(summary, working, true);
	}
	
	public static RemoteViews getRemoteViews(Summary summary, boolean working, boolean big) {
		return getRemoteViews(summary, working, big, true);		
	}
	
	//TODO(jordi): better api
	public static RemoteViews getRemoteViews(Summary summary, boolean working, boolean big, boolean handle) {
		Logging.log("Drawing: " + summary);
		Context context = FacturaSimyoApp.getInstance();
		int widgetLayoutRes = big ? R.layout.widget5_big : R.layout.widget5;
		if(!handle)
			widgetLayoutRes = R.layout.widget5_big_nodropdown;

		RemoteViews views = new RemoteViews(context.getPackageName(), widgetLayoutRes);
		if(summary != null) {
			String lastUpdatedLabel = "";
			try {
				lastUpdatedLabel = dateFormat.format(summary.timestamp);
			} catch (Exception e) {}
			// values
			Iterator<Entry<String, AmountEuros>> it = summary.concepts.entrySet().iterator();
			for(int rowId=0; rowId<MAX_ROWS; rowId++) {
				if(it.hasNext()) {
					Entry<String, AmountEuros> e = it.next();
					views.setTextViewText(widgetResIds[rowId][0], e.getKey());
					views.setTextViewText(widgetResIds[rowId][1], e.getValue().euros);
					views.setTextViewText(widgetResIds[rowId][2], e.getValue().amount);
					views.setViewVisibility(widgetResRowIds[rowId], ViewGroup.VISIBLE);
				} else {
					views.setViewVisibility(widgetResRowIds[rowId], ViewGroup.GONE);
				}
			}
			views.setTextViewText(R.id.totalLabel, summary.totalLabel);
			views.setTextViewText(R.id.total, summary.total);
			if(!working)
				views.setTextViewText(R.id.lastUpdated, lastUpdatedLabel);
			// phone number formatting
			String number = "";
			if(summary.number != null) {
				if(summary.number.matches("[0-9]+")) { // if it's really a number, format it
					number = summary.number.substring(0, 3) + " "
							+ summary.number.substring(3, 6) + " "
							+ summary.number.substring(6, 9) + " "
							+ summary.number.substring(9);
					number = number.trim();
				} else {
					number = summary.number;
				}
			}
			views.setTextViewText(R.id.number, number);
		}
		return views;
	}
}
