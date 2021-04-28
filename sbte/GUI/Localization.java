package sbte.GUI;

import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import sbte.FileTools;
import sbte.Main;

public class Localization {
	public static HashMap<String, String> getMap(String language) {
		String jsonToParse = getLocalizationFile();
		JSONObject json = parseJsonString(jsonToParse);
		
		JSONObject languageJson = (JSONObject) json.get(language);
		HashMap<String, String> languageMap = new HashMap<>();
		for (Object o: languageJson.keySet()) {
			String key = o.toString();
			String value = languageJson.get(key).toString();
			languageMap.put(key, value);
		}
		
		return languageMap;
	}
	private static String getLocalizationFile() {
		String output = FileTools.readInputStreamToString(Main.class.getResourceAsStream("Localization.json"));
		return output;
	}
	private static JSONObject parseJsonString(String s) {
		JSONObject output = null;
		try {
			output = (JSONObject) new JSONParser().parse(s);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return output;
	}
}
