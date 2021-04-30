package sbte;

import java.io.File;
import java.util.List;

import sbte.SonicBattleROMReader.SonicBattleLine;
import sbte.GUI.GUI;
import sbte.GUI.GUIActions.ROMListener;

public class Main {
	private static GUI gui;
	public static void main (String[] args) {
		gui = new GUI();
		gui.setVisible(true);
		
		gui.actions.ROMListener.addListener(new ROMHandler());
	}
	
	private static class ROMHandler implements ROMListener {
		public void ROMopened(File rom) {
			List<SonicBattleLine> sonicBattleLines = null;
			try {
				sonicBattleLines = SonicBattleROMReader.readUSAROM(rom);
			} catch (Exception e) {
				gui.showMsg(gui.localization.get("error") + ": " + rom.toString(), e.toString(), GUI.Msg.ERROR_MESSAGE);
				return;
			}

			gui.open(sonicBattleLines);
		}

		public void ROMsaved(File rom) {
			System.out.println(rom);
		}
	}
}
