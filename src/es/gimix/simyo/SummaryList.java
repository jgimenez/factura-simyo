package es.gimix.simyo;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

/** List of phone numbers */
public class SummaryList extends ArrayList<Summary> {
	private static final long serialVersionUID = 1L;
	
	public String toJson() {
		JSONArray aSummaries = new JSONArray();
		for(Summary summary : this)
			aSummaries.put(summary.toJsonObject());
		return aSummaries.toString();
	}
	
	public static SummaryList fromJson(String json) throws JSONException {
		SummaryList result = new SummaryList();
		JSONArray aSummaries = new JSONArray(json);
		for(int i=0; i < aSummaries.length(); i++) {
			result.add(Summary.fromJson(aSummaries.getJSONObject(i)));
		}
		return result;
	}

}
