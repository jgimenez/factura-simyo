package es.gimix.simyo.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import es.gimix.simyo.FacturaSimyoApp;
import es.gimix.simyo.R;
import es.gimix.simyo.Summary;

/** Not really a JUnit test, but rather some checks */
public class SimyoApiTest {
	private static final String SUMMARY_URL = "https://www.simyo.es/simyo/portal/customer/action/my-panel";

	public void testSSL() throws IOException {
		Connection c = Jsoup.connect(SUMMARY_URL);
		Document d = c.get();
		System.out.println(d.title());
	}
	
	public void testContractOneLine() throws IOException {
		Document doc = Jsoup.parse(new File("../doc/my-panel-657.html"), "UTF-8", "https://www.simyo.es/simyo/portal/customer/action/my-panel");
		Summary summary = new Summary();
		System.out.println(doc.title());
		// phone number
		Element number = doc.select("#activeMsisdn option[selected]").first();
		summary.number = "+" + number.text();
		if(summary.number.startsWith("+34")) summary.number = summary.number.substring(3);
		// amounts (euros)
		Elements rows = doc.select("#consumo_total tr");
		for(Element tr : rows) {
			String concept = tr.child(0).text();
			String amount = tr.child(1).text();
			summary.concepts.put(concept, new Summary.AmountEuros(null, amount));
		}
		// quantities (minutes/instances)
		Elements rows2 = doc.select("#consumo_detallado2 tr");
		for(Element tr : rows2) {
			String concept = tr.child(0).text();
			String quantity = tr.child(1).text();
			if(summary.concepts.get(concept)==null)
				summary.concepts.put(concept, new Summary.AmountEuros(quantity, null));
			else
				summary.concepts.get(concept).amount = quantity;
		}
		// total
		Elements total = doc.select("#consumo_total h2 span");
		summary.total = total.text();
	}

	public void testContractMultipleLines() throws IOException {
		Document doc = Jsoup.parse(new File("../doc/my-panel-111.html"), "UTF-8", "https://www.simyo.es/simyo/portal/customer/action/my-panel");
		Summary summary = new Summary();
		System.out.println(doc.title());
		// phone number
		Element number = doc.select("#activeMsisdn option").first();
		summary.number = "+" + number.text();
		if(summary.number.startsWith("+34")) summary.number = summary.number.substring(3);
		// amounts (euros)
		Elements rows = doc.select("#consumo_total tr");
		for(Element tr : rows) {
			String concept = tr.child(0).text();
			String amount = tr.child(1).text();
			summary.concepts.put(concept, new Summary.AmountEuros(null, amount));
		}
		// quantities (minutes/instances)
		Elements rows2 = doc.select("#consumo_detallado2 tr");
		for(Element tr : rows2) {
			String concept = tr.child(0).text();
			String quantity = tr.child(1).text();
			if(summary.concepts.get(concept)==null)
				summary.concepts.put(concept, new Summary.AmountEuros(quantity, null));
			else
				summary.concepts.get(concept).amount = quantity;
		}
		// total
		Elements total = doc.select("#consumo_total h2 span");
		summary.total = total.text();
		// number list
		ArrayList<String> numbers = new ArrayList<String>();
		for(Element el : doc.select("#activeMsisdn option")) {
			numbers.add(el.text());
		}
		System.out.println(numbers);
	}

	
	public static void main(String args[]) throws Exception {
		es.gimix.net.PinningCertManager.install(FacturaSimyoApp.getInstance(),R.raw.trust);
		SimyoApiTest a = new SimyoApiTest();
		a.testContractOneLine();
		a.testContractMultipleLines();
		a.testSSL();
	}
}
