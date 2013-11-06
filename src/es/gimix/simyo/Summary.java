package es.gimix.simyo;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Summary {
	public String number;
	public LinkedHashMap<String,AmountEuros> concepts = new LinkedHashMap<String, AmountEuros>();
	public String total;
	public String totalLabel;
	public Date timestamp;
	
	public static class AmountEuros {
		public String amount;
		public String euros;
		
		public AmountEuros(String amount, String euros) {this.amount=amount; this.euros=euros;}
		
		JSONObject toJsonObject() throws JSONException {
			return new JSONObject()
					.put("amount", amount)
					.put("euros", euros);
		}
		
		static AmountEuros fromJsonObject(JSONObject json) throws JSONException {
			String amount = json.optString("amount");
			String euros = json.has("euros") ? json.getString("euros") : "";
			return new AmountEuros(amount, euros);
		}
	}
	
	public String toJson() {
		return toJsonObject().toString();
	}

	public JSONObject toJsonObject() {
		try {
			JSONObject obj = new JSONObject();
			obj.put("number", number);
			JSONArray aConcepts = new JSONArray();
			for(Entry<String, AmountEuros> e : concepts.entrySet())
				aConcepts.put(new JSONObject().put(e.getKey(), e.getValue().toJsonObject()));
			obj.put("concepts", aConcepts);
			obj.put("total", total);
			obj.put("totalLabel", totalLabel);
			obj.put("timestamp", timestamp.getTime());
			return obj;
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	public static Summary fromJson(String json) throws JSONException {
		JSONObject obj = new JSONObject(json);
		return fromJson(obj);
	}
	
	public static Summary fromJson(JSONObject obj) throws JSONException {
		Summary result = new Summary();
		result.number = obj.getString("number");
		JSONArray aConcepts = obj.getJSONArray("concepts");
		for(int i=0; i < aConcepts.length(); i++) {
			JSONObject concept = aConcepts.getJSONObject(i);
			String k = concept.names().getString(0);
			AmountEuros v = AmountEuros.fromJsonObject(concept.getJSONObject(k));
			result.concepts.put(k, v);
		}
		result.total = obj.getString("total");
		if(obj.has("totalLabel"))
			result.totalLabel = obj.getString("totalLabel");
		else
			result.totalLabel = "Total"; // for backwards compatibility
		result.timestamp = new Date(obj.getLong("timestamp"));
		return result;
	}
	
	@Override public String toString() {
		return toJson();
	}
}
