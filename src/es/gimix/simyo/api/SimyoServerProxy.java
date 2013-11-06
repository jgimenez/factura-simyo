package es.gimix.simyo.api;

import java.io.IOException;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import es.gimix.simyo.FacturaSimyoApp;
import es.gimix.simyo.R;
import es.gimix.util.Logging;

public class SimyoServerProxy {
	private static final String BASE_URL = "https://www.simyo.es";
 	private static final String LOGIN_FORM_HTML = "/simyo/publicarea/login/login.htm";
 	private static final String LOGIN_FORM = "/simyo/publicarea/login/j_security_check";
	private static final String LINE_SELECTION_URL = "/simyo/privatearea/customer/common/select-msisdn.htm";
	private static final String SUMMARY_URL = "/simyo/privatearea/customer/consumption-panel.htm";
	private static final String BALANCE_URL = "/simyo/privatearea/ajax/customer/account-available.htm";

	private String currentUrl;
	private Connection c;
	private Response currentResponse;
	private Document currentDoc;

	private final String username;
	private final String password;

	public SimyoServerProxy(String username, String password) {
		try {
			es.gimix.net.PinningCertManager.install(FacturaSimyoApp.getInstance(),R.raw.trust);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		System.setProperty("http.keepAlive", "false"); // android bug in Eclair
		// keep for later
		this.username = username;
		this.password = password;
	}

	public Document getPageForNumberList() throws IOException, SimyoAPIException {
		try {
			currentUrl = null;
			goTo();
			return currentDoc;
		} catch (IOException e) {
			if (e.getMessage().contains("authentication challenge"))
				throw new SimyoCredentialsException(
						"Por favor, revisa que has introducido bien tus datos de usuario y clave.");
			else
				throw e;
		}
	}

	public Document getPageForNumber(String number)
			throws IOException, SimyoCredentialsException {
		try {
			doSelectNumberForm(number);
			currentResponse = c.execute();
			currentUrl = currentResponse.url().toString();
			currentDoc = currentResponse.parse();
			return currentDoc;
		} catch (IOException e) {
			if (e.getMessage().contains("authentication challenge"))
				throw new SimyoCredentialsException(
						"Por favor, revisa que has introducido bien tus datos de usuario y clave.");
			else
				throw e;
		}
	}
	
	public Document getBalance() throws IOException, SimyoCredentialsException {
		try {
			c.request().clearData();
			c.method(Connection.Method.GET);
			c.url(BASE_URL + BALANCE_URL);
			currentResponse = c.execute();
			currentUrl = currentResponse.url().toString();
			currentDoc = currentResponse.parse();
			return currentDoc;
		} catch (IOException e) {
			if (e.getMessage().contains("authentication challenge"))
				throw new SimyoCredentialsException(
						"Por favor, revisa que has introducido bien tus datos de usuario y clave.");
			else
				throw e;
		}		
	}

	/**
	 * Navigates to a specified destination, handling authentication if
	 * necessary
	 */
	void goTo() throws IOException, SimyoAPIException {
		if (currentUrl == null) { // needs to go somewhere
			Logging.log("Place: getting login form");
			doGetLoginForm();
		} else if (currentUrl.startsWith(BASE_URL + LOGIN_FORM_HTML)) { // needs to go somewhere
			Logging.log("Place: faking login form");
			doLoginForm();
		} else if (currentUrl.startsWith(BASE_URL + LOGIN_FORM)) {
			Logging.log("Place: login error");
			throw new SimyoCredentialsException(
					"Por favor, revisa que has introducido bien tus datos de usuario y clave.");
		} else if (currentUrl.startsWith(BASE_URL + SUMMARY_URL)) {
			Logging.log("Place: summary");
			return;
		} else { // try to get to the summary URL
			c.request().clearData();
			c.method(Connection.Method.GET);
			c.url(BASE_URL + SUMMARY_URL);
		}
		currentResponse = c.execute();
		currentUrl = currentResponse.url().toString();
		currentDoc = currentResponse.parse();
		Logging.log("Loaded: " + currentUrl);
		goTo();
	}
	
	/** Gets login form, execute c.execute() */
	private void doGetLoginForm() throws IOException {
		c = Jsoup.connect(BASE_URL + LOGIN_FORM_HTML);
		c.timeout(180000);
		c.method(Connection.Method.GET);
	}

	/** Configures connection for login, execute c.execute() */
	private void doLoginForm() throws IOException {
		c = Jsoup.connect(BASE_URL + LOGIN_FORM);
		c.timeout(180000);
		c.data("j_username", username);
		c.data("j_password", password);
		c.data("x", "0");
		c.data("y", "0");
		c.method(Connection.Method.POST);
	}

	/** Configures connection for login, execute c.execute() */
	private void doSelectNumberForm(String number) throws IOException {
		Logging.trace(currentResponse.body());
		String action = BASE_URL + LINE_SELECTION_URL;
		c.referrer(currentUrl);
		c.url(action);
		c.request().clearData();
		c.data("forwardUrl", SUMMARY_URL);
		c.data("msisdn", number);
		c.method(Connection.Method.POST);
	}

}
