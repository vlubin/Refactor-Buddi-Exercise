/*
 * Created on Oct 3, 2006 by wyatt
 */
package org.homeunix.drummer.plugins.exports;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.homeunix.drummer.Const;
import org.homeunix.drummer.controller.Translate;
import org.homeunix.drummer.controller.TranslateKeys;
import org.homeunix.drummer.plugins.interfaces.BuddiExportPlugin;
import org.homeunix.drummer.prefs.PrefsInstance;
import org.homeunix.drummer.view.AbstractFrame;
import org.homeunix.drummer.view.ReportFrameLayout;
import org.homeunix.thecave.moss.util.Log;

import edu.stanford.ejalbert.BrowserLauncher;

public class ExportHTML implements BuddiExportPlugin {

	public void exportData(AbstractFrame frame) {
		if (Const.DEVEL) Log.debug("Exporting HTML");
		if (frame instanceof ReportFrameLayout){
			String htmlReport = ((ReportFrameLayout) frame).getHTMLPage();

			File tempFile = new File(
//					(!reportPath.matches("^\\S*[ ]\\S*$") ? reportPath : "")
					new File(
							PrefsInstance.getInstance().getPrefs().getDataFile()
					).getParent() 
					+ File.separatorChar
					+ "report_" 
					+ (int) (Math.random() * 1000) 
					+ ".html"
			);
			if (Const.DEVEL) Log.debug("Tempfile: " + tempFile.getAbsolutePath());

			try{
				PrintStream out = new PrintStream(new FileOutputStream(tempFile));
				out.println(htmlReport);
				out.close();

				tempFile.deleteOnExit();

				if (Const.DEVEL) Log.debug("Opening file...");
				BrowserLauncher bl = new BrowserLauncher(null);
				bl.openURLinBrowser("file://" + tempFile.getAbsolutePath());
				if (Const.DEVEL) Log.debug("Finished opening file...");
			}
			catch (Exception ex){
				Log.error(ex);
			}
		}
		else {
			Log.error("This is not an instance of ReportFrameLayout");
		}
	}
	
	public Class[] getCorrectWindows() {
		Class[] windows = new Class[0];
		return windows;
	}

	public String getDescription() {
		return Translate.getInstance().get(TranslateKeys.EXPORT_TO_HTML);
	}
}