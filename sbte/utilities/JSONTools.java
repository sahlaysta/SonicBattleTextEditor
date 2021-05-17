package sbte.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.simple.parser.ParseException;

//makes use of Google's GSON and simple JSON for formatted JSON
public class JSONTools {
	public static String toJSONValue(String string) {
		org.json.simple.JSONObject j = new org.json.simple.JSONObject();
		j.put("", string);
		return
		j.toString().
		substring(5).
		replaceFirst("..$","");
	}
	public static String valueToString(String string) {
		org.json.simple.JSONObject j = null;
		try {
			j = (org.json.simple.JSONObject) parser.parse("{\"a\":\"" + string + "\"}");
		} catch (ParseException e) {}
		
		return j.get("a").toString();
	}
	private static String formattedJSON(org.json.simple.JSONObject e) {
		com.google.gson.JsonObject jsonObject = com.google.gson.JsonParser.parseString(e.toString()).getAsJsonObject();
		com.google.gson.Gson gson = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
		String prettyJsonString = gson.toJson(jsonObject);
		
		return prettyJsonString;
	}
	
	public static final org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser();
	public static final File prefsJson = new File(FileTools.RUNNING_DIRECTORY, "prefs.json"); //location of the preferences file
	public static org.json.simple.JSONObject getPrefsJson() throws org.json.simple.parser.ParseException {
		if (!prefsJson.exists()) return new org.json.simple.JSONObject();
		
		String jsonToParse = null;
		try {
			jsonToParse = FileTools.readFileToString(prefsJson);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		org.json.simple.JSONObject output = (org.json.simple.JSONObject) parser.parse(jsonToParse);
		return output;
	}
	public static void savePrefsJson(org.json.simple.JSONObject jsonObject) throws IOException {
		FileTools.writeStringToFile(prefsJson, formattedJSON(jsonObject));
	}
}
