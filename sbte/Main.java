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
	 * - Popup window message adjustment ("OK" string de-hardcode)
	 * - Recently opened menu fix (extra empty item bug when adding new file)
	 * - Search window: small readjustment (set minimum size of window + lowered bottem-left "found" text for consistency)
	 * - Yes or no dialog prompt fix (closing the window prompt no longer equals yes)
	 */
}
