package es.gimix.simyo.controller;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/** This {@link BroadcastReceiver} can be registered at the AndroidManifest.xml
 * level, or at runtime.
 * <p>
 * To register the {@link WifiConnectivityReceiver} at the AndroidManifest
 * level, use the following code:
 * 
 * <pre>
 *      &lt;receiver android:name="WifiConnectivityReceiver"
 *          android:enabled="true" android:label="WifiConnectivityReceiver"&gt;
 *          &lt;intent-filter&gt;
 *              &lt;action android:name="android.net.conn.CONNECTIVITY_CHANGE" /&gt;
 *              &lt;action android:name="android.net.wifi.STATE_CHANGE" /&gt;
 *          &lt;/intent-filter&gt;
 *      &lt;/receiver&gt;
 * </pre>
 * 
 * To register the {@link WifiConnectivityReceiver} dynamically, create a new
 * instance and use {@link #register(Context)} and {@link #unregister(Context)}.
 * 
 * The application needs permission "android.permission.ACCESS_NETWORK_STATE"
 * (and of course "android.permission.INTERNET").
 */
public class WifiConnectivityReceiver extends BroadcastReceiver {

	//Constants --------------------------------------------------------------------
	
    private static final String TAG=WifiConnectivityReceiver.class.getSimpleName();

    //Constructor ------------------------------------------------------------------
    
    public WifiConnectivityReceiver(){}
    
    //Business methods -------------------------------------------------------------

    /** When a wifi networks connects, an intent with
     * {@link ConnectivityManager#CONNECTIVITY_ACTION} is sent twice instead of
     * once. Both intents are exactly identical. That's why we register for
     * "android.net.wifi.STATE_CHANGE", which is only called once, and check for
     * {@link WifiManager#NETWORK_STATE_CHANGED_ACTION}.
     * <p>
     * When any network disconnects, an intent with action
     * {@link ConnectivityManager#CONNECTIVITY_ACTION} is sent. In such a case,
     * we must checks that its type is {@link ConnectivityManager#TYPE_WIFI} and
     * that the network is not connected anymore.
     */
    @Override public void onReceive(Context context, Intent intent){
        String action=intent.getAction();
        
        if(WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)){
            NetworkInfo networkInfo=intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if(networkInfo.isConnected())
                onWifiConnected(context, networkInfo);
        }
        
        else if(ConnectivityManager.CONNECTIVITY_ACTION.equals(action)){
            NetworkInfo networkInfo=intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            int type=networkInfo.getType();
            if(type==ConnectivityManager.TYPE_WIFI && !networkInfo.isConnected())
                onWifiDisconnected(context, networkInfo);
        }
    }

    //Helpers -----------------------------------------------------------------------------
    
    /**
     * Called when the phone connects successfully to a wifi networks.
     * 
     * Please notice that this methods runs in the UI Thread, and that it
     * shouldn't take more then 10 seconds to execute, otherwise it will be
     * shut down by the system.
     */
    private void onWifiConnected(Context context, NetworkInfo networkInfo) {
        Log.d(TAG, "Wifi is connected: " + String.valueOf(networkInfo));
		Intent intent = new Intent(QueryReceiver.OPPORTUNISTIC_UPDATE);
		context.sendBroadcast(intent);
    }

    /**
     * Called when the phone disconnects from a previously connected wifi
     * network.
     * 
     * Please notice that this methods runs in the UI Thread, and that it
     * shouldn't take more then 10 seconds to execute, otherwise it will be
     * shut down by the system.
     */
    private void onWifiDisconnected(Context context, NetworkInfo networkInfo){
        Log.d(TAG, "Wifi is disconnected: " + String.valueOf(networkInfo));
    }

    /** Is the phone connected via wifi now?
     * <p>
     * This method can be used to check if the phone is connected to a wifi
     * network at any time, without having to register the receiver.
     */
    public static boolean isWifiConnected(Context context){
        ConnectivityManager connectivityManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=null;
        if(connectivityManager!=null)
            networkInfo=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(networkInfo==null)
        	return false;
        return networkInfo.isConnected();
    }

}