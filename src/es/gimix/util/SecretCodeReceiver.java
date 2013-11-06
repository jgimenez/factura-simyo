package es.gimix.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SecretCodeReceiver extends BroadcastReceiver {

	private static String SECRET_CODE_ACTION = "android.provider.Telephony.SECRET_CODE";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(SECRET_CODE_ACTION)) {
			Logging.sendLogs();
		}
	}

}