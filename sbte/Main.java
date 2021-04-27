package sbte;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import sbte.GUI.GUI;

public class Main {
	private static GUI gui;
	public static void main (String[] args) {
		gui = new GUI();
		gui.setVisible(true);
	}
	public static File getRunningDirectory() {
		File dir = null;
		try {
			dir = Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toFile().getParentFile();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return dir;
	}
	//custom read file method
	public static String readInputStreamToString(InputStream is) {
		String output = null;
		try {
			output = new BufferedReader(new InputStreamReader(is, "UTF-8")).lines().collect(Collectors.joining());
		} catch (UnsupportedEncodingException e) {}
		
		return output;
	}
	public static String readFileToString(File f) throws FileNotFoundException {
		FileInputStream fis = new FileInputStream(f);
		
		return readInputStreamToString(fis);
	}
}
