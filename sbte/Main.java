package sbte;

import sbte.gui.GUI;

public class Main {
	public static void main (String[] args) {
		GUI gui = new GUI();
		gui.setVisible(true);
	}
	
	/*
	 * SonicBattleTextEditor 3.3.0 changelog
	 * - Text preview window
	 * - Properties (Right-click line for properties) (unfinished)
	 * - hextable.json update (added missing characters)
	 * - Popup window message adjustment ("OK" string de-hardcode)
	 * - Recently opened files menu fix (extra empty item bug when adding new file)
	 * - Search window: small readjustment (set minimum size of window + lowered bottem-left "found" text for consistency)
	 * - Yes or no dialog prompt fix (closing the window prompt no longer equals yes)
	 * - Preferences fix (forgetting chosen files)
	 * - Search GUI and Go To GUI preserve their text field entry on close and reopen
	 * - Clicking the list auto-transfers focus to textbox
	 * - Fixed list ctr-click glitch
	 * - Optimization (serial version uids)
	 */
}
