package es.gimix.simyo.api;

import java.io.File;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.os.Environment;

public class SimyoFileProxy extends SimyoServerProxy {
	
	SimyoFileProxy(String username, String password) {
		super(username, password);
	}
	
	public Document getPageForNumberList() throws IOException, SimyoAPIException {
		File input = new File(Environment.getExternalStorageDirectory()+"/consumption-panel.html");
		Document doc = Jsoup.parse(input, "UTF-8", "https://www.simyo.es/simyo/privatearea/customer/consumption-panel.htm");
		return doc;
	}

	public Document getPageForNumber(String number) throws IOException, SimyoCredentialsException {
		File input = new File(Environment.getExternalStorageDirectory()+"/consumption-panel.html");
		Document doc = Jsoup.parse(input, "UTF-8", "https://www.simyo.es/simyo/privatearea/customer/consumption-panel.htm");
		return doc;
	}

}
