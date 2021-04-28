package sbte;

import java.io.File;

import sbte.GUI.GUI;
import sbte.GUI.GUIActions.ROMListener;

public class Main {
	public static void main (String[] args) {
		GUI gui = new GUI();
		gui.setVisible(true);
		
		gui.actions.openROMListener.addListener(new ROMEvent());
		
		SonicBattleTextParser sbtp = new SonicBattleTextParser();
	}
	
	public static class ROMEvent implements ROMListener {
		public void ROMopened(File rom) {
			System.out.println(rom);
		}

		public void ROMsaved(File rom) {
			System.out.println(rom);
		}
	}
}
