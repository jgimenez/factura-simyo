package es.gimix.simyo;

import net.robotmedia.billing.BillingController;
import android.app.Application;
import es.gimix.simyo.controller.RefreshAlarm;
import es.gimix.simyo.widget.SimyoNotificationManager;

public class FacturaSimyoApp extends Application {

	private static FacturaSimyoApp instance;
	private SimyoNotificationManager notificationManager;
	
	public FacturaSimyoApp() {
		instance = this;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		notificationManager = new SimyoNotificationManager(this);
		RefreshAlarm.setUpAlarm(this.getApplicationContext());
		
		// In-app billing config
		BillingController.setDebug(true);
		BillingController.setConfiguration(new BillingController.IConfiguration() {
			public byte[] getObfuscationSalt() {
				return new byte[] {52,-28,-83,-91,6,108,39,-28,78,107,114,-91,-54,59,85,-91,-5,92,-27,52};
			}
	
			public String getPublicKey() {
				return "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqXx5EI5+9s4f/qmj356e27XaE3B3pQQooMowmo/tMmaFRG7GdUvUNrHr0ik///cvsiG2z2f8pXnHjro/u4sC1LTDlgrDg5NLSjfKLcgjUuCeWB580DKA52Q6bkmGP9C1LWpIDIjWi9kYZvLjXvEnqv3aLo2vRIyhnviUmFQaRJQIr7V30ezFHRvCNoe+0mBnPL9WhfXs8SPl/IyNTwPUnmnbxFhi7OGXbijldg5a4oFcp5X3WSBnD1mk4Mu9pGKEAqE2PMx3MQwt/cPg1r+KqvYQivgvRnlcLiKhZYMWkGPiFsN17jywYtp8JDuhFRkc7lkB+7uxNj2Y2D6MCOo+KwIDAQAB";
			}
		});
	}
	
	public static FacturaSimyoApp getInstance() { return instance; }
	public SimyoNotificationManager getNotificationManager() { return notificationManager; }

}
