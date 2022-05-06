package org.cas.lib.cdl;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

public enum TitleParts {

	periodical {
		@Override
		public String part(JSONObject object) {
			return object.getString("title");
		}
	},
	periodicalvolume {
		@Override
		public String part(JSONObject object) {
			if (object.has("details")) {
				JSONObject jsonObject = object.getJSONObject("details");
				StringBuilder builder = new StringBuilder();
				
				//{"volumeNumber":"1","year":"1861"}
				if (hasValue("year", jsonObject)) {
					builder.append(jsonObject.getString("year"));
				}
				if (hasValue("volumeNumber",jsonObject)) {
					if (builder.length() > 0) builder.append(' ');
					builder.append("Volume:").append(jsonObject.getString("volumeNumber"));
				}
				return builder.toString();
			}
			return null;
		}
	},
	periodicalitem {
		@Override
		public String part(JSONObject object) {
			if (object.has("details")) {
				StringBuilder builder = new StringBuilder();
				boolean partNumberExists = false;
				JSONObject jsonObject = object.getJSONObject("details");
				if (hasValue("partNumber", jsonObject)) {
					partNumberExists = true;
					builder.append("Number:"+jsonObject.getString("partNumber"));
				}
				if (!partNumberExists && hasValue("date", jsonObject)) {
					if (builder.length() > 0) builder.append(' ');
					builder.append(jsonObject.getString("date"));
				}
				return builder.toString(); 
			}
			return null;
		}
	},
	article {

		@Override
		public String part(JSONObject object) {
			if (object.has("title"))
				return object.getString("title");
			else 
				return null;
		}
		
	},
	supplement {
		@Override
		public String part(JSONObject object) {
			if (object.has("title"))
				return object.getString("title");
			else 
				return null;
		}
	};
	
	public abstract String part(JSONObject object);
	
	public boolean hasValue(String key, JSONObject jsonObject) {
		if (jsonObject.has(key)) {
			String val = jsonObject.getString(key);
			return val != null && !val.trim().equals("");
		} return false;
	}
	
	public static String buildTitle(List<JSONObject> items) {
		List<String> titles = new ArrayList<String>();
		for (JSONObject jsonObject : items) {
			String model = jsonObject.getString("model");
			TitleParts part = TitleParts.valueOf(model);
			String partOftitle = part.part(jsonObject);                                               
			if (partOftitle != null && part != article) {
				if (!titles.contains(partOftitle)) {
					titles.add(partOftitle);
				}
			}
		}
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < titles.size(); i++) {
			if (i > 0) builder.append(" | ");
			builder.append(titles.get(i));
		}
		return builder.toString();
	}
}
