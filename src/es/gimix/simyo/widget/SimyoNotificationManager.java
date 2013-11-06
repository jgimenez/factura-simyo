package es.gimix.simyo.widget;

import es.gimix.simyo.R;
import es.gimix.simyo.settings.CredentialsActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class SimyoNotificationManager {
	private static final int ID_NEED_CREDENTIALS = 1;
	
	private Context mContext;
	private NotificationManager mNotificationManager;

	public SimyoNotificationManager(Context context) {
		mContext = context;
		mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	public void notifyLoginError(CharSequence contentText) {
		int icon = R.drawable.notification_icon;
		long when = System.currentTimeMillis();
		CharSequence tickerText = mContext.getText(R.string.login_notification_ticker);
		CharSequence contentTitle = mContext.getText(R.string.login_notification_title);
		if(contentText == null)
			contentText = mContext.getText(R.string.login_notification_text_missing);

		// pending intent
		Intent notificationIntent = new Intent(mContext, CredentialsActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		// notify
		Notification notification = new Notification(icon, tickerText, when);
		notification.setLatestEventInfo(mContext, contentTitle, contentText, contentIntent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		mNotificationManager.notify(ID_NEED_CREDENTIALS, notification);
	}

	public void cancelNeedCredentials() {
		mNotificationManager.cancel(ID_NEED_CREDENTIALS);
	}
}
