package sbte;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PrefManager {
	private static JSONParser jp = new JSONParser();
	private JSONObject json = null;
	private File prefsFile;
	private String readFile() {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(prefsFile))){
			String focus;
            while ((focus = br.readLine()) != null) 
            {
               sb.append(focus).append("\n");
            }
		}catch (IOException e) {e.printStackTrace(); }
		return sb.toString();
	}
	public PrefManager(File f) {
		prefsFile = f;
		json = new JSONObject();
		if (!prefsFile.exists()) return;

		try { json = (JSONObject) jp.parse(readFile().toString()); } catch (ParseException e) { e.printStackTrace(); }
	}
	public void save() {
		if (!prefsFile.exists()) {
//			File parent = prefsFile.getParentFile();
//			if (!parent.exists()) parent.mkdir();
			try { prefsFile.createNewFile(); BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(prefsFile, false), StandardCharsets.UTF_8)); bw.write("{\n\n}"); bw.close(); } catch (IOException e) { e.printStackTrace(); }
		}
		//do not save if equal
		String wr = this.toString();
		String rd = readFile();
		JSONObject j1 = null, j2 = null;
		try { j1 = (JSONObject) jp.parse(wr); } catch (ParseException e) { e.printStackTrace(); }
		try { j2 = (JSONObject) jp.parse(rd); } catch (ParseException e) { e.printStackTrace(); }
		if (j1.toString().equals(j2.toString())) { return; }
		
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(prefsFile, false), StandardCharsets.UTF_8));
			bw.write(wr);
			bw.close();
		} catch (IOException e) { e.printStackTrace(); }
	}
	public void put(String key, Object value) {
		json.put(key, value);
	}
	public void putArray(String key, Object[] value) {
		JSONArray j = new JSONArray();
		for (Object o: value) j.add(o);
		json.put(key, j);
	}
	public String[] getStringArray(String key) {
		JSONArray j = (JSONArray) json.get(key);
		String[] op = new String[j.size()];
		for (int i = 0; i < op.length; i++) {
			op[i] = (String) j.get(i);
		}
		return op;
	}
	public long[] getLongArray(String key) {
		JSONArray j = (JSONArray) json.get(key);
		long[] op = new long[j.size()];
		for (int i = 0; i < op.length; i++) {
			op[i] = (long) j.get(i);
		}
		return op;
	}
	public Boolean getBoolean(String key) {
		return (Boolean) json.get(key);
	}
	public long getLong(String key) {
		return (long) json.get(key);
	}
	public String getString(String key) {
		return (String) json.get(key);
	}
	public Boolean isNull(String key) {
		return json.get(key) == null;
	}
	public String toString() {
		//indenting
		class A{ //nested functions
			String op;
			public A() {
				StringBuilder sb = new StringBuilder("{\r\n");
				int count=0; for (Object o: json.keySet()) count++;
				for (int i = 0; i < count; i++) { String s = getOneObject(i); sb.append("	" + s.substring(1, s.length()-1)); if (i!=count-1) sb.append(","); sb.append("\n"); }
				sb.append("}");
				op = sb.toString();
			}
			public String getOneObject(int index) {
				int i = 0;
				for (Object o:json.keySet()) {
					if (i==index) {
						JSONObject j = new JSONObject();
						String os = o.toString();
						j.put(os, json.get(os));
						String op = j.toString();
						j = new JSONObject();
						j.put("", os);
						String jsonkey = j.toString().substring(4).replaceFirst(".$","") + ":";
						op = new StringBuilder(op).insert(jsonkey.length() + 1, " ").toString();
						return op;
					}
					i++;
				}
				return null;
			}
			public String Output() {
				return op;
			}
		}
		return new A().Output();
	}
	public File toFile() {
		return prefsFile;
	}
}
