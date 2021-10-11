package com.github.sahlaysta.sonicbattletexteditor.ui.localization;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.github.sahlaysta.jsonevent.JsonEventArgs;
import com.github.sahlaysta.jsonevent.JsonEventReader;
import com.github.sahlaysta.jsonevent.JsonEventType;

/** Localization.json parser class */
public final class Localization {
	
	//Invisible constructor
	private Localization() {}
	
	/** The map of parsed Localization.json file */
	public static final Map<String, Map<String, String>> LANGUAGE_MAPS;
	static {
		//Static block to set and parse LANGUAGE_MAPS from Localization.json
		LANGUAGE_MAPS = new LinkedHashMap<>();
		JsonEventReader jer = new JsonEventReader(
				new InputStreamReader(Localization.class.getResourceAsStream("Localization.json"),
				StandardCharsets.UTF_8)
			);
		try {
			//Read Localization.json and populate LANGUAGE_MAPS HashMaps
			JsonEventArgs jea = jer.next();
			while ((jea = jer.next()).type != JsonEventType.JSON_OBJECT_END) {
				String languageName = jea.elmntStr; //the language name, e.g. "en-US"
				jer.next();
				
				HashMap<String, String> languageMap = new HashMap<>(); //String HashMap of the language
				while ((jea = jer.next()).type != JsonEventType.JSON_OBJECT_END)
					languageMap.put(jea.elmntStr, jer.next().elmntStr); //put a single key-value-pair
				LANGUAGE_MAPS.put(languageName, languageMap);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/** Get a language map from LANGUAGE_MAPS using the passed language key */
	public static final Map<String, String> getLanguageMap(String language) {
		return LANGUAGE_MAPS.getOrDefault(language, mutate(language));
	}
	/*
	 * Mutate the language key String and try to get a value from
	 * LANGUAGE_MAPS.
	 * 
	 * For example, try to find "fr-FR". If that is not found, try to find
	 * "fr-*". If that also is not found, use the default language English "en-US"
	 */
	private static final Map<String, String> mutate(String language) {
		int indexOfHyphen = language.indexOf('-');
		return
				indexOfHyphen == -1
				? LANGUAGE_MAPS.getOrDefault(language + "-*", LANGUAGE_MAPS.get("en-US"))
				: LANGUAGE_MAPS.getOrDefault(language.substring(0, indexOfHyphen) + "-*", LANGUAGE_MAPS.get("en-US"));
	}
}