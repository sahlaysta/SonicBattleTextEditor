package sbte.GUI;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;

import sbte.JSONTools;
import sbte.SonicBattleTextParser;

public class ListModel extends DefaultListModel{
	public List<String> content = new ArrayList<>();
	private final SonicBattleTextParser sbtp;
	public ListModel(SonicBattleTextParser arg0) {
		sbtp = arg0;
	}
	public void add (byte[] line) {
		String s = sbtp.parseString(line);
		content.add(s);
		super.addElement(JSONTools.toJSONValue(s));
	}
}
