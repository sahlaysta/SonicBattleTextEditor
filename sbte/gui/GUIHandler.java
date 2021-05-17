package sbte.gui;

import java.io.IOException;
import java.util.List;

import sbte.gui.layout.menubar.actions.GUIActions.ROMArgs;
import sbte.gui.layout.menubar.actions.GUIActions.ROMListener;
import sbte.parser.SonicBattleROMReader;
import sbte.parser.SonicBattleROMSaver;
import sbte.parser.SonicBattleROMReader.ROM;
import sbte.parser.SonicBattleROMReader.SonicBattleLine;

public final class GUIHandler implements ROMListener {
	public void romOpened(ROMArgs args) {
		List<SonicBattleLine> sonicBattleLines = null;
		ROM rom = null;
		try {
			rom = new ROM(args.selectedPath);
			sonicBattleLines = SonicBattleROMReader.readUSAROM(rom);
		} catch (Exception e) {
			args.source.showErrorMsgBox(args.source.localization.get("open") + ": " + args.selectedPath.toString(), e.toString());
			return;
		}

		if (args.source.isOpen) args.source.close();
		args.source.open(rom, sonicBattleLines);
	}

	public void romSaved(ROMArgs args) {
		try {
			SonicBattleROMSaver.saveToUSAROM(args.selectedPath, args.source.rom, args.source.getSonicBattleLines());
		} catch (IOException e) {
			args.source.showErrorMsgBox(args.source.localization.get("save") + ": " + args.selectedPath.toString(), e.toString());
			return;
		}
		
		args.source.isSaved = true;
		args.source.showMsgBox(args.source.localization.get("save"), args.source.localization.get("saved"));
	}
}
