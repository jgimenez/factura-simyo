package es.gimix.simyo.api;

import java.io.IOException;
import java.util.HashMap;

import org.jsoup.nodes.Document;

import es.gimix.simyo.PhoneNumberList;
import es.gimix.simyo.Summary;

/** Entry point for communication with the server. Use {@link #getInstance(String, String)} to get one. 
 * Thread safe. */
public class SimyoApi {
	
	private static final HashMap<String, SimyoApi> _instances = new HashMap<String, SimyoApi>();

	private final SimyoServerProxy server;
	private final SimyoWebParser parser;
	
	private boolean working;

	/** Factory method for the {@link SimyoApi} objects and subclasses, that can be reused from a pool */
	public static SimyoApi getInstance(String username, String password) {
        if("test".equals(username) && "test".equals(password)) // instantiate test stub instead
            return SimyoApiStub.getInstance(username, password);
		String key = username + ":" + password;
		SimyoApi result = _instances.get(key);
		if(result == null) {
			result = new SimyoApi(username, password);
			_instances.put(key, result);
		}
		return result;
	}
	
	protected SimyoApi(String username, String password) {
        if("test".equals(username) && "test2".equals(password)) // instantiate network test stub instead
            this.server = new SimyoFileProxy(username, password);
        else
        	this.server = new SimyoServerProxy(username, password);
		this.parser = new SimyoWebParser();
	}
	
	public synchronized PhoneNumberList getNumberList() throws IOException, SimyoAPIException {
		working = true;
		try {
			Document doc = server.getPageForNumberList();
			return parser.getNumberList(doc);
		} finally {
			working = false;
		}
	}
	
	public synchronized Summary getSummary(String number) throws IOException, SimyoAPIException {
		working = true;
		try {
			Document doc = server.getPageForNumber(number);
			Summary s = parser.getSummary(doc);
			if(!number.equals(s.number))
				throw new SimyoAPIException("Unable to get summary for: " + number);
			if("Prepago".equals(s.total)) { // hack to get balance with another request
				doc = server.getBalance();
				s = parser.fillInSummaryPrepaidBalance(s, doc);
			}
			return s;
		} finally {
			working = false;
		}
	}
	
	public boolean isWorking() {
		return working;
	}
}
