package sbte;

import java.io.File;

import sbte.GUI.GUI;
import sbte.GUI.GUIActions.OpenROMListener;

public class Main {
	public static void main (String[] args) {
		GUI gui = new GUI();
		gui.setVisible(true);
		
		gui.actions.openRomListener.addListener(new OpenROMEvent());
	}
	
	public static class OpenROMEvent implements OpenROMListener {
		public void ROMopened(File rom) {
			System.out.println(rom);
		}
	}
}
