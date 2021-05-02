package sbte;

import java.util.List;

import sbte.SonicBattleROMReader.SonicBattleLine;
import sbte.GUI.GUI;
import sbte.GUI.GUIActions.ROMArgs;
import sbte.GUI.GUIActions.ROMListener;

public class Main {
	public static void main (String[] args) {
		GUI gui = new GUI();
		gui.setVisible(true);
		
		gui.actions.ROMListener.addListener(new ROMHandler());
	}
	
	private static class ROMHandler implements ROMListener {
		public void ROMopened(ROMArgs args) {
			List<SonicBattleLine> sonicBattleLines = null;
			try {
				sonicBattleLines = SonicBattleROMReader.readUSAROM(args.romPath);
			} catch (Exception e) {
				args.source.showMsg(args.source.localization.get("error") + ": " + args.source.toString(), e.toString(), GUI.Msg.ERROR_MESSAGE);
				return;
			}

			if (args.source.isOpen) args.source.close();
			args.source.open(args.romPath, sonicBattleLines);
		}

		public void ROMsaved(ROMArgs args) {
			SonicBattleROMSaver.saveToUSAROM(args.source.getSonicBattleByteData());
		}
	}
}
