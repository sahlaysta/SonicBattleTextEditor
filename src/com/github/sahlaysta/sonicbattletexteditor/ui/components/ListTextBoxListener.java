package com.github.sahlaysta.sonicbattletexteditor.ui.components;

import java.util.EventListener;

/** Interface listener handling the events of ListTextBox<br><br>
 * 
 * List index changed- calls the <code>indexChanged</code> method<br>
 * Text box changed- calls the <code>textBoxChanged</code> method */
public interface ListTextBoxListener extends EventListener {
	/** Called when the index of the list is changed, passing the int <b>index</b> */
	void indexChanged(int index);
	
	/** Called when the text box is changed, passing the int <b>index</b>,
	 * and the String <b>str</b> of the text box content */
	void textBoxChanged(int index, String str);
}