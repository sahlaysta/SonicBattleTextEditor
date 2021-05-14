package sbte.gui;

import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import sbte.utilities.FileTools;

public class Localization {
	public static HashMap<String, String> getMap(String language) {
		String jsonToParse = getLocalizationFile();
		JSONObject json = parseJsonString(jsonToParse);
		
		String langKey = null;
		for (Object o: json.keySet()) {
			String key = o.toString();
			if (key.equals(language)) {
				langKey = key;
				break;
			}
		}
		
		if (langKey == null) {
			if (language.contains("-")) {
				language = language.substring(0, language.indexOf("-"));
				language += "-*";
			} else {
				language += "-*";
			}
		}
		
		for (Object o: json.keySet()) {
			String key = o.toString();
			if (key.equals(language)) {
				langKey = key;
				break;
			}
		}
		
		if (langKey == null) {
			langKey = "en-*"; //default language
		}

		JSONObject languageJson = (JSONObject) json.get(langKey);
		HashMap<String, String> languageMap = new HashMap<>();
		for (Object o: languageJson.keySet()) {
			String key = o.toString();
			String value = languageJson.get(key).toString();
			languageMap.put(key, value);
		}
		
		languageMap.put("thisKey", langKey);
		
		return languageMap;
	}
	public static String getLocalizationFile() {
		String output = FileTools.readResourceToString("Localization.json");
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
