package cz.incad.cdk.cdkharvester.changeindex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.incad.kramerius.virtualcollections.CDKSource;

public class ResultsUtils {

	public static boolean collectionExists(JSONObject result) {
		JSONObject jsonObject = result.getJSONObject("response");
		JSONArray jsonArray = jsonObject.getJSONArray("docs");
		if (jsonArray.length() == 1) {
			return jsonArray.getJSONObject(0).has("collection");
		}
		return false;
	}

	public static List<String> disectCollections(JSONObject result) {
		List<String> collections = new ArrayList<String>();
		JSONObject jsonObject = result.getJSONObject("response");
		JSONArray jsonArray = jsonObject.getJSONArray("docs");
		if (jsonArray.length() == 1) {
			JSONArray jArray = jsonArray.getJSONObject(0).getJSONArray("collection");
			for (int i = 0,ll=jArray.length(); i < ll; i++) {
				String item = jArray.getString(i);
				collections.add(item);
			}
		}
		return collections;
	}

	public static boolean docsExists(JSONObject result) {
		JSONObject jsonObject = result.getJSONObject("response");
		JSONArray jsonArray = jsonObject.getJSONArray("docs");
		return jsonArray.length() > 0;
	}

	public static List<CDKSource> disectSources(JSONObject result, List<CDKSource> sourcesList) {
		List<String> disectCollections = disectCollections(result);
		Map<String, CDKSource> map = sourcesList.stream().collect(Collectors.toMap(CDKSource::getPid,Function.identity()));
		return disectCollections.stream().filter(c -> map.containsKey(c)).map(c-> map.get(c)).collect(Collectors.toList());
	}
}
