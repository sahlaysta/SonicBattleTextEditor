package sbte;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import org.json.simple.parser.ParseException;

public class JSONTools {
	public static String toJSONValue(String string) { //the goal is to create the JSON file value of a String object
		org.json.simple.JSONObject j = new org.json.simple.JSONObject();
		j.put("", string); //put an empty key with the String as the value in a json object. it will look like this {"":"foo"}
		return
		j.toString().
		substring(5). //remove the first 4 characters. now it looks like foo"}
		replaceFirst("..$",""); //remove the last 2 characters
	}
	public static String formattedJSON(org.json.simple.JSONObject e) { // return a formatted and styled JSON object to String
		com.google.gson.JsonObject jsonObject = com.google.gson.JsonParser.parseString(e.toString()).getAsJsonObject(); //relies on Google's GSON for pretty-printing
		com.google.gson.Gson gson = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
		String prettyJsonString = gson.toJson(jsonObject);
		
		return prettyJsonString;
	}
	public static org.json.simple.JSONObject getPrefsJson() {
		File prefsJson = new File(Main.getRunningDirectory(), "prefs.json");
		if (!prefsJson.exists()) return new org.json.simple.JSONObject();
		
		String jsonToParse = null;
		try {
			jsonToParse = Main.readFileToString(prefsJson);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		org.json.simple.JSONObject output = null;
		try {
			output = (org.json.simple.JSONObject) new org.json.simple.parser.JSONParser().parse(jsonToParse);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return output;
	}
	public static void savePrefsJson(org.json.simple.JSONObject jsonObject) {
		File prefsJson = new File(Main.getRunningDirectory(), "prefs.json");
		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(prefsJson), StandardCharsets.UTF_8)){
			writer.write(formattedJSON(jsonObject));
			writer.close();
		} catch (IOException e) { e.printStackTrace(); }
	}
}
