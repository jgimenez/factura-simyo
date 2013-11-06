package es.gimix.simyo.api;

import java.io.IOException;
import java.util.Date;

import es.gimix.simyo.PhoneNumberList;
import es.gimix.simyo.Summary;

/** Fake SimyoApi that does not connect to a server but rather responds immediately
 * with some hard-coded data.
 */
public class SimyoApiStub extends SimyoApi {
	public static SimyoApiStub getInstance(String username, String password) {
		return new SimyoApiStub(username, password);
	}

    protected SimyoApiStub(String username, String password) {
        super(username, password);
    }
	
	public PhoneNumberList getNumberList() throws IOException, SimyoAPIException {
		try { Thread.sleep(1000); } catch (InterruptedException e) {}
        PhoneNumberList result = new PhoneNumberList();
        result.add("600111111");
        result.add("600222222");
        result.add("Línea de datos - 99999999999");
		return result;
	}
	
	public Summary getSummary(String number) throws IOException, SimyoAPIException {
		try { Thread.sleep(1000); } catch (InterruptedException e) {}
        if("600111111".equals(number)) return getSummary1();
        if("600222222".equals(number)) return getSummary2();
        if("Línea de datos - 99999999999".equals(number)) return getSummary3();
        throw new RuntimeException();
	}

	private Summary getSummary1() throws IOException, SimyoAPIException {
		Summary summary = new Summary();
		// phone number
		summary.number = "600111111";
		// amounts (euros)
        summary.concepts.put("Llamadas", new Summary.AmountEuros("1h 34m 1s", "12,5 €"));
        summary.concepts.put("SMS", new Summary.AmountEuros("3 SMS", "0,32 €"));
        summary.concepts.put("MMS", new Summary.AmountEuros("1 MMS", "0,34 €"));
        summary.concepts.put("Datos", new Summary.AmountEuros("79,25 MB", "0 €"));
		// total
		summary.total = "13,42 €";
		summary.totalLabel = "Total";
		summary.timestamp = new Date();
		summaryCheck(summary); // perform basic checks
		return summary;
	}

	private Summary getSummary2() throws IOException, SimyoAPIException {
		Summary summary = new Summary();
		// phone number
		summary.number = "600222222";
		// amounts (euros)
        summary.concepts.put("Llamadas", new Summary.AmountEuros("54m 25s", "8,5 €"));
        summary.concepts.put("SMS", new Summary.AmountEuros("1 SMS", "0,12 €"));
        summary.concepts.put("MMS", new Summary.AmountEuros("0 MMS", "0 €"));
        summary.concepts.put("Datos", new Summary.AmountEuros("420,12 MB", "1,02 €"));
		// total
		summary.total = "9,72 €";
		summary.totalLabel = "Saldo";
		summary.timestamp = new Date();
		summaryCheck(summary); // perform basic checks
		return summary;
	}

	private Summary getSummary3() throws IOException, SimyoAPIException {
		Summary summary = new Summary();
		// phone number
		summary.number = "Línea de datos - 99999999999";
		// amounts (euros)
        summary.concepts.put("Llamadas", new Summary.AmountEuros("54m 25s", "8,5 €"));
        summary.concepts.put("SMS", new Summary.AmountEuros("1 SMS", "0,12 €"));
        summary.concepts.put("SMS Premium", new Summary.AmountEuros("1 SMS", "1,2 €"));
        summary.concepts.put("MMS", new Summary.AmountEuros("0 MMS", "0 €"));
        summary.concepts.put("Datos", new Summary.AmountEuros("420,12 MB", "1,02 €"));
		// total
		summary.total = "9,72 €";
		summary.totalLabel = "Saldo";
		summary.timestamp = new Date();
		summaryCheck(summary); // perform basic checks
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
}
