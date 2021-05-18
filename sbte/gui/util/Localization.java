package sbte.gui.util;

import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import sbte.util.FileTools;

public final class Localization {
	private static final String DEFAULT_LANGUAGE = "en-*";
	private static final String SEARCH_NOT_FOUND = (String)null;
	public static HashMap<String, String> getMap(String arg0) {
		/*
		 * Guess the language with the String parameter
		 * Example: parameter = "fr-FR" (french language)
		 * Search localization.json for "fr-FR"
		 * If that failed, mutilate to "fr-*"
		 * Search localization.json for "fr-*"
		 * If that failed, use the default language "en-*" (English)
		 */
		
		final String jsonToParse = getLocalizationFile();
		final JSONObject json = parseJsonString(jsonToParse);
		String language = new String(arg0);
	
		String searchResult = SEARCH_NOT_FOUND;
		
		for (Object o: json.keySet()) {
			String key = o.toString();
			if (key.equals(language)) {
				searchResult = key;
				break;
			}
		}
		
		if (searchResult == SEARCH_NOT_FOUND) {
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
				searchResult = key;
				break;
			}
		}
		
		if (searchResult == SEARCH_NOT_FOUND) {
			searchResult = DEFAULT_LANGUAGE; //default language
		}

		JSONObject languageJson = (JSONObject) json.get(searchResult);
		HashMap<String, String> languageMap = new HashMap<>();
		for (Object o: languageJson.keySet()) {
			String key = o.toString();
			String value = languageJson.get(key).toString();
			languageMap.put(key, value);
		}
		
		languageMap.put("thisKey", searchResult);
		
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
