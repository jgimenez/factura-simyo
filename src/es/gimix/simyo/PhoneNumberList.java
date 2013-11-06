package es.gimix.simyo;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

/** List of phone numbers */
public class PhoneNumberList extends ArrayList<String> {
	private static final long serialVersionUID = 1L;
	
	public String toJson() {
		JSONArray aNumbers = new JSONArray();
		for(String number : this)
			aNumbers.put(number);
		return aNumbers.toString();
	}
	
	public static PhoneNumberList fromJson(String json) throws JSONException {
		PhoneNumberList result = new PhoneNumberList();
		JSONArray aNumbers = new JSONArray(json);
		for(int i=0; i < aNumbers.length(); i++) {
			result.add(aNumbers.getString(i));
		}
		return result;
	}

}
