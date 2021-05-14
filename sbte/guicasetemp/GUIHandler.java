package sbte.guicasetemp;

import java.io.IOException;
import java.util.List;

import sbte.guicasetemp.GUIActions.ROMArgs;
import sbte.guicasetemp.GUIActions.ROMListener;
import sbte.parser.SonicBattleROMReader;
import sbte.parser.SonicBattleROMSaver;
import sbte.parser.SonicBattleROMReader.ROM;
import sbte.parser.SonicBattleROMReader.SonicBattleLine;

public class GUIHandler implements ROMListener {
	public void ROMopened(ROMArgs args) {
		List<SonicBattleLine> sonicBattleLines = null;
		ROM rom = null;
		try {
			rom = new ROM(args.selectedPath);
			sonicBattleLines = SonicBattleROMReader.readUSAROM(rom);
		} catch (Exception e) {
			args.source.showMsg(args.source.localization.get("open") + ": " + args.selectedPath.toString(), e.toString(), GUI.Msg.ERROR_MESSAGE);
			return;
		}

		if (args.source.isOpen) args.source.close();
		args.source.open(rom, sonicBattleLines);
	}

	public void ROMsaved(ROMArgs args) {
		try {
			SonicBattleROMSaver.saveToUSAROM(args.selectedPath, args.source.rom, args.source.getSonicBattleLines());
		} catch (IOException e) {
			args.source.showMsg(args.source.localization.get("save") + ": " + args.selectedPath.toString(), e.toString(), GUI.Msg.ERROR_MESSAGE);
			return;
		}
		
		args.source.isSaved = true;
		args.source.showMsg(args.source.localization.get("save"), args.source.localization.get("saved"), GUI.Msg.INFORMATION_MESSAGE);
	}
}
