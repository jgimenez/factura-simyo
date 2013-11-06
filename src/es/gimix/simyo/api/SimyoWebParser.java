package es.gimix.simyo.api;

import java.util.Date;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import es.gimix.simyo.PhoneNumberList;
import es.gimix.simyo.Summary;
import es.gimix.util.Logging;

public class SimyoWebParser {
	
	public PhoneNumberList getNumberList(Document doc) throws SimyoAPIException {
		PhoneNumberList numbers = new PhoneNumberList();
		for (Element el : doc.select("#phone_number_selector option")) {
			numbers.add(el.attr("value"));
		}
		return numbers;
	}
	
	/** Gets a Summary object out of a Response */
	public Summary getSummary(Document currentDoc) throws SimyoAPIException {
		Logging.log("Parsing page: " + currentDoc.title());
		Summary summary = new Summary();
		// phone number
		summary.number = getPhoneNumber(currentDoc);
		// amounts (euros)
		Elements consumos = currentDoc.select("#consumo_total table tr");
		if(consumos.size() == 0) { // prepaid
			consumos = currentDoc.select("#consumo_detallado2 table tr");
			for(int i=0; i<consumos.size(); i++) {
				Element detalleTr = consumos.get(i);
				String concept = detalleTr.child(0).text();
				String quantity = detalleTr.child(1).text();
				String euros = ""; // not available

				if(concept.startsWith("Datos")) concept = "Datos";
				summary.concepts.put(concept, new Summary.AmountEuros(quantity, euros));
			}
			// total
			summary.total = "Prepago";
			summary.totalLabel = "Tarifa";
		} else {
			for(int i=0; i<consumos.size(); i++) {
				Element detalleTr = consumos.get(i);
				String concept = detalleTr.child(0).text();
				String[] quantityAndEuros = detalleTr.child(1).html().split("<br[ ]*/>");
				String quantity = quantityAndEuros[0].trim();
				String euros = quantityAndEuros[1].trim();

				if(concept.startsWith("Datos")) concept = "Datos";
				summary.concepts.put(concept, new Summary.AmountEuros(quantity, euros));
			}
			// total
			summary.total = currentDoc.select("#consumo_total h2 span").first().text();
			summary.totalLabel = currentDoc.select("#consumo_total h2").first().ownText();			
		}
		summary.timestamp = new Date();
		summaryCheck(summary); // perform basic checks
		return summary;
	}
	
	/** Fills in a Summary with balance out of a Response */
	public Summary fillInSummaryPrepaidBalance(Summary summary, Document currentDoc) throws SimyoAPIException {
		Logging.log("Parsing page: " + currentDoc.title());
		summary.total = currentDoc.select("#consumo_total h2 span").first().text();
		summary.totalLabel = currentDoc.select("#consumo_total h2").first().ownText();			
		return summary;
	}

	private void summaryCheck(Summary summary) throws SimyoAPIException {
		try {
			if(summary.total == null ||
					summary.concepts.size() < 4 ||
					summary.number.length() < 9)
				throw new SimyoAPIException("Invalid data");
		} catch (Exception e) {
			throw new SimyoAPIException("Invalid data");
		}
	}
	
	private String getPhoneNumber(Document currentDoc) {
		// phone number
		Elements activeNumbers = currentDoc.select("#phone_number_selector option[selected]"); // one or zero
		if(activeNumbers.size() == 0) // no selection, then it is the first one
			activeNumbers = currentDoc.select("#phone_number_selector option");
		Element number = activeNumbers.first(); // always one
		return number.attr("value");
	}

}
