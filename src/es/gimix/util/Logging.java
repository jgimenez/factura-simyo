package es.gimix.util;

import java.io.IOException;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;

import es.gimix.simyo.FacturaSimyoApp;

public class Logging {
	private static final int MAX_ENTRIES = 20;
	private static int nextEntry = 0;
	private static String[] entryArray = new String[MAX_ENTRIES];
	
	public static void log(String text) {
		android.util.Log.d("Ojete", text);
		saveEntry(text);
	}

	public static void trace(String text) {
		android.util.Log.v("Ojete", text);
		saveEntry(text);
	}
	
	private static void saveEntry(String text) {
		entryArray[nextEntry] = text;
        nextEntry = (nextEntry + 1) % MAX_ENTRIES;
	}
	
	private static String getLastEntries() {
		StringBuilder sb = new StringBuilder();
		for(int i=1; i<=MAX_ENTRIES; i++) {
            String data = entryArray[(nextEntry+i) % MAX_ENTRIES];
            if(data!=null) {
                data = maskPersonalData(data);
                sb.append(data).append('\n');
            }
		}
		return sb.toString();
	}
	
	private static Pattern numbers = Pattern.compile("[0-9][0-9][0-9][0-9]"); // at least 4 numbers
	private static String maskPersonalData(String data) {
		return numbers.matcher(data).replaceAll("9999");
	}
	
	public static void sendLogs() {
		FacturaSimyoApp app = FacturaSimyoApp.getInstance();
		String packageName = "";
		int versionCode = -1;
		try {
			packageName = app.getPackageName();
			versionCode = app.getPackageManager().getPackageInfo(packageName, 0).versionCode;
		} catch (Exception e) {}
        log("Sending log data: " + packageName + ", version " + versionCode);
		final String logs = getLastEntries();
		new Thread(new Runnable() {			
			public void run() {
				try {
					Jsoup.connect("http://gimix.es/simyo/log").data("log", logs).post();
					entryArray = new String[MAX_ENTRIES];
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
}
