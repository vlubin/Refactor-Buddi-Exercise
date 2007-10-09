/*
 * Created on Aug 6, 2007 by wyatt
 */
package org.homeunix.thecave.buddi.view.menu.items;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;

import net.java.dev.SwingWorker;

import org.homeunix.thecave.buddi.i18n.BuddiKeys;
import org.homeunix.thecave.buddi.i18n.keys.ButtonKeys;
import org.homeunix.thecave.buddi.model.Document;
import org.homeunix.thecave.buddi.model.prefs.PrefsModel;
import org.homeunix.thecave.buddi.plugin.api.BuddiExportPlugin;
import org.homeunix.thecave.buddi.plugin.api.BuddiImportPlugin;
import org.homeunix.thecave.buddi.plugin.api.BuddiSynchronizePlugin;
import org.homeunix.thecave.buddi.plugin.api.MenuPlugin;
import org.homeunix.thecave.buddi.plugin.api.exception.PluginException;
import org.homeunix.thecave.buddi.plugin.api.exception.PluginMessage;
import org.homeunix.thecave.buddi.plugin.api.model.impl.MutableDocumentImpl;
import org.homeunix.thecave.buddi.plugin.api.util.TextFormatter;
import org.homeunix.thecave.buddi.view.MainFrame;
import org.homeunix.thecave.buddi.view.TransactionFrame;
import org.homeunix.thecave.moss.swing.MossDocumentFrame;
import org.homeunix.thecave.moss.swing.MossMenuItem;
import org.homeunix.thecave.moss.swing.MossSmartFileChooser;
import org.homeunix.thecave.moss.swing.MossStatusDialog;
import org.homeunix.thecave.moss.util.Log;

public class PluginMenuEntry extends MossMenuItem {
	public static final long serialVersionUID = 0;

	private final MenuPlugin plugin;

	public PluginMenuEntry(MossDocumentFrame parentFrame, MenuPlugin plugin) {
		super(parentFrame, TextFormatter.getTranslation(plugin.getName()));

		this.plugin = plugin;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		File f = null;

		if (plugin.isPromptForFile()){
			FileFilter filter = new FileFilter(){
				@Override
				public boolean accept(File f) {
					if (f.isDirectory())
						return true;
					if (plugin.getFileExtensions() != null){
						for (String s : plugin.getFileExtensions()) {
							if (f.getName().endsWith(s))
								return true;
						}
					}
					else 
						return true;
					return false;
				}

				@Override
				public String getDescription() {
					return TextFormatter.getTranslation(plugin.getDescription());
				}
			};

			String title = "Unknown Plugin Type";
			if (plugin instanceof BuddiExportPlugin)
				title = PrefsModel.getInstance().getTranslator().get(BuddiKeys.FILECHOOSER_EXPORT_FILE_TITLE);
			else if (plugin instanceof BuddiImportPlugin)
				title = PrefsModel.getInstance().getTranslator().get(BuddiKeys.FILECHOOSER_IMPORT_FILE_TITLE);
			else if (plugin instanceof BuddiSynchronizePlugin)
				title = PrefsModel.getInstance().getTranslator().get(BuddiKeys.FILECHOOSER_SYNCHRONIZE_FILE_TITLE);

			if (plugin.isFileChooserSave())
				f = MossSmartFileChooser.showSaveFileDialog(
						getFrame(),  
						filter,
						plugin.getFileExtensions() == null || plugin.getFileExtensions().length == 0 ? 
								null : 
									plugin.getFileExtensions()[0],
						title, 
						PrefsModel.getInstance().getTranslator().get(ButtonKeys.BUTTON_OK),
						PrefsModel.getInstance().getTranslator().get(ButtonKeys.BUTTON_CANCEL),
						PrefsModel.getInstance().getTranslator().get(ButtonKeys.BUTTON_REPLACE),
						PrefsModel.getInstance().getTranslator().get(BuddiKeys.MESSAGE_ERROR_CANNOT_WRITE_DATA_FILE),  
						PrefsModel.getInstance().getTranslator().get(BuddiKeys.ERROR),
						PrefsModel.getInstance().getTranslator().get(BuddiKeys.MESSAGE_PROMPT_OVERWRITE_FILE),
						PrefsModel.getInstance().getTranslator().get(BuddiKeys.MESSAGE_PROMPT_OVERWRITE_FILE_TITLE));

			else 
				f = MossSmartFileChooser.showOpenDialog(
						getFrame(),  
						filter, 
						title, 
						PrefsModel.getInstance().getTranslator().get(ButtonKeys.BUTTON_OK), 
						PrefsModel.getInstance().getTranslator().get(BuddiKeys.MESSAGE_ERROR_CANNOT_WRITE_DATA_FILE), 
						PrefsModel.getInstance().getTranslator().get(BuddiKeys.MESSAGE_ERROR_CANNOT_READ_DATA_FILE), 
						PrefsModel.getInstance().getTranslator().get(BuddiKeys.ERROR));

			if (f == null)
				return;
		}

		((MossDocumentFrame) getFrame()).getDocument().startBatchChange();

		try {
			final File fFinal = f;
			final MossStatusDialog status = new MossStatusDialog(
					getFrame(),
					TextFormatter.getTranslation(plugin.getProcessingMessage()));

			if (plugin.getProcessingMessage() != null)
				status.openWindow();

			SwingWorker worker = new SwingWorker(){
				@Override
				public Object construct() {
					try {
						plugin.processData(new MutableDocumentImpl((Document) ((MossDocumentFrame) getFrame()).getDocument()), ((MossDocumentFrame) getFrame()), fFinal);
					}
					catch (PluginMessage pm){
						return pm;
					}
					catch (PluginException pe){
						Log.error("Error processing data in plugin: ", pe);
						return null;
					}

					return new Object();
				}

				@Override
				public void finished() {
					status.closeWindow();

					((Document) ((MossDocumentFrame) getFrame()).getDocument()).updateAllBalances();
					((MossDocumentFrame) getFrame()).getDocument().finishBatchChange();

					if (getFrame() instanceof MainFrame)
						((MainFrame) getFrame()).fireStructureChanged();
					if (getFrame() instanceof TransactionFrame)
						((TransactionFrame) getFrame()).fireStructureChanged();

					MainFrame.updateAllContent();

					if (get() == null){
						String[] options = new String[1];
						options[0] = PrefsModel.getInstance().getTranslator().get(ButtonKeys.BUTTON_OK);

						JOptionPane.showOptionDialog(
								getFrame(), 
								TextFormatter.getTranslation(BuddiKeys.MESSAGE_ERROR_CHECK_LOGS_FOR_DETAILS), 
								TextFormatter.getTranslation(BuddiKeys.ERROR), 
								JOptionPane.DEFAULT_OPTION,
								JOptionPane.ERROR_MESSAGE,
								null,
								options,
								options[0]
						);
					}
					else if (get() instanceof PluginMessage){
						String[] options = new String[1];
						options[0] = PrefsModel.getInstance().getTranslator().get(ButtonKeys.BUTTON_OK);

						PluginMessage message = (PluginMessage) get();
						JOptionPane optionPane = new JOptionPane(message.getMessage(), message.getType(), JOptionPane.OK_OPTION, null, options, options[0]);
						JDialog dial = optionPane.createDialog(null, message.getTitle());
						dial.setModal(true);
						JTextArea text = new JTextArea();
						JScrollPane scroller = new JScrollPane(text);
						if (message.getLongMessage() != null){
							text.setEditable(false);
							text.setWrapStyleWord(true);
							text.setLineWrap(true);
							text.setText(message.getLongMessage());
							scroller.setPreferredSize(new Dimension(350, 200));
							scroller.setBorder(null);
							dial.getContentPane().add(scroller, BorderLayout.SOUTH);
						}

						dial.pack();

						scroller.getVerticalScrollBar().setValue(0);
						scroller.getHorizontalScrollBar().setValue(0);

						dial.setVisible(true);
						optionPane.getValue();
					}

					super.finished();
				}
			};

			worker.start();
		}
		catch (Exception ex){
			Log.error("Error encountered in plugin: " + ex);
		}
	}
}