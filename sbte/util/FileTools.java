package sbte.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import sbte.Main;

public final class FileTools { //Global file IO utiilties
	public static String readFileToString(File f) throws FileNotFoundException {
		FileInputStream fis = new FileInputStream(f);
		return readInputStreamToString(fis);
	}
	public static void writeStringToFile(File f, String content) throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8);
		writer.write(content);
		writer.close();
	}
	public static String readResourceToString(String resource) {
		return readInputStreamToString(Main.class.getResourceAsStream(resource));
	}
	private static String readInputStreamToString(InputStream is) {
		String output = null;
		try {
			output = new BufferedReader(new InputStreamReader(is, "UTF-8")).lines().collect(Collectors.joining());
		} catch (UnsupportedEncodingException e) {}
		
		return output;
	}
	public static byte[] readFileToByteArray(File file) throws IOException {
		return Files.readAllBytes(file.toPath());
	}
	public static void writeByteArrayToFile(File file, byte[] content) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(file)) {
			   fos.write(content);
			   fos.close();
		}
	}
	public static final File RUNNING_DIRECTORY = retrieveRunningDirectory();
	private static File retrieveRunningDirectory() {
		File dir = null;
		try {
			dir = Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toFile().getParentFile();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return dir;
	}
}
