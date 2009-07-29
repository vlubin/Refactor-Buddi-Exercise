/*
 * Created on Aug 6, 2007 by wyatt
 */
package org.homeunix.thecave.buddi.view.menu.items;

import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.homeunix.thecave.buddi.Const;
import org.homeunix.thecave.buddi.i18n.keys.MenuKeys;
import org.homeunix.thecave.buddi.model.prefs.PrefsModel;
import org.homeunix.thecave.buddi.plugin.api.util.TextFormatter;
import org.homeunix.thecave.moss.swing.MossFrame;
import org.homeunix.thecave.moss.swing.MossMenuItem;

import edu.stanford.ejalbert.BrowserLauncher;

public class HelpHelp extends MossMenuItem {
	public static final long serialVersionUID = 0;

	public HelpHelp(MossFrame frame) {
		super(frame, TextFormatter.getTranslation(MenuKeys.MENU_HELP_HELP));
	}

	public void actionPerformed(ActionEvent e) {
		try{
			BrowserLauncher bl = new BrowserLauncher(null);
			bl.openURLinBrowser(
					Const.PROJECT_URL 
					+ PrefsModel.getInstance().getLanguage().replaceAll("-.*$", "") 
					+ "/index.jsp");
		}
		catch (Exception ex){
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Exception", ex);
		}
	}
}
