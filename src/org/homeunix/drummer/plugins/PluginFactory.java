/*
 * Created on Sep 14, 2006 by wyatt
 */
package org.homeunix.drummer.plugins;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.jar.JarEntry;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.roydesign.ui.JScreenMenuItem;
import net.sourceforge.buddi.api.manager.DataManager;
import net.sourceforge.buddi.api.manager.ImportManager;
import net.sourceforge.buddi.api.plugin.BuddiExportPluginAPI;
import net.sourceforge.buddi.api.plugin.BuddiImportPluginAPI;
import net.sourceforge.buddi.api.plugin.BuddiPluginAPI;
import net.sourceforge.buddi.impl_2_2.manager.DataManagerImpl;
import net.sourceforge.buddi.impl_2_2.manager.ImportManagerImpl;

import org.homeunix.drummer.Const;
import org.homeunix.drummer.controller.SourceController;
import org.homeunix.drummer.controller.Translate;
import org.homeunix.drummer.model.Account;
import org.homeunix.drummer.model.Category;
import org.homeunix.drummer.model.Transaction;
import org.homeunix.drummer.plugins.BuddiPluginAPIImpl.BuddiExportPluginAPIImpl;
import org.homeunix.drummer.plugins.BuddiPluginAPIImpl.BuddiImportPluginAPIImpl;
import org.homeunix.drummer.plugins.BuddiPluginHelper.DateChoice;
import org.homeunix.drummer.plugins.BuddiPluginHelper.DateRangeType;
import org.homeunix.drummer.plugins.BuddiPluginImpl.BuddiExportPluginImpl;
import org.homeunix.drummer.plugins.BuddiPluginImpl.BuddiGraphPluginImpl;
import org.homeunix.drummer.plugins.BuddiPluginImpl.BuddiImportPluginImpl;
import org.homeunix.drummer.plugins.BuddiPluginImpl.BuddiReportPluginImpl;
import org.homeunix.drummer.plugins.BuddiPluginImpl.BuddiRunnablePluginImpl;
import org.homeunix.drummer.plugins.interfaces.BuddiExportPlugin;
import org.homeunix.drummer.plugins.interfaces.BuddiGraphPlugin;
import org.homeunix.drummer.plugins.interfaces.BuddiImportPlugin;
import org.homeunix.drummer.plugins.interfaces.BuddiPanelPlugin;
import org.homeunix.drummer.plugins.interfaces.BuddiPlugin;
import org.homeunix.drummer.plugins.interfaces.BuddiGenericAPIPlugin;
import org.homeunix.drummer.plugins.interfaces.BuddiReportPlugin;
import org.homeunix.drummer.plugins.interfaces.BuddiRunnablePlugin;
import org.homeunix.drummer.prefs.Plugin;
import org.homeunix.drummer.prefs.PrefsInstance;
import org.homeunix.drummer.view.AccountListPanel;
import org.homeunix.drummer.view.DocumentManager;
import org.homeunix.drummer.view.MainFrame;
import org.homeunix.drummer.view.TransactionsFrame;
import org.homeunix.drummer.view.components.CustomDateDialog;
import org.homeunix.thecave.moss.gui.abstractwindows.AbstractFrame;
import org.homeunix.thecave.moss.jar.JarLoader;
import org.homeunix.thecave.moss.util.Log;
import org.homeunix.thecave.moss.util.Version;


public class PluginFactory<T extends BuddiGenericAPIPlugin> {

    protected static List<JScreenMenuItem> getExportMenuItemsNonAPI(final AbstractFrame frame){
        List<JScreenMenuItem> exportMenuItems = new LinkedList<JScreenMenuItem>();

        PluginFactory<BuddiExportPlugin> factory = new PluginFactory<BuddiExportPlugin>();
        List<BuddiExportPlugin> exportPlugins = factory.getPluginObjects(BuddiExportPluginImpl.class);

        //Iterate through each plugin, and create a menu item for each.
        for (BuddiExportPlugin plugin : exportPlugins) {
            boolean correctWindow = true;
            
            if (plugin.getCorrectWindows() != null && frame != null){
                correctWindow = false;
                for (Class c : plugin.getCorrectWindows()) {
                    if (frame.getClass().equals(c)) {
                        correctWindow = true;
                        break;
                    }
                }
            }
            if (correctWindow){
                JScreenMenuItem menuItem = new JScreenMenuItem(plugin.getDescription());

                //This is where the user's custom code is actually run
                addExportActionListener(plugin, menuItem, frame);
                exportMenuItems.add(menuItem);
            }
        }

        return exportMenuItems;
    }

    static protected DataManager createDataManager(AbstractFrame frame)
    {
        // TODO: Implement more of this functionality
        Account selectedAccount = getSelectedAccount(frame);
        Category selectedCategory = null;
        Transaction selectedTransaction = null;

        DataManager dataManager = new DataManagerImpl(
                selectedAccount, selectedCategory, selectedTransaction);
        
        return dataManager;
    }

    static protected ImportManager createImportManager(AbstractFrame frame)
    {
        // TODO: Implement more of this functionality
        Account selectedAccount = getSelectedAccount(frame);
        Category selectedCategory = null;
        Transaction selectedTransaction = null;
        
        ImportManager importManager = new ImportManagerImpl(
                selectedAccount, selectedCategory, selectedTransaction);
        
        return importManager;
    }
    
    protected static Account getSelectedAccount(AbstractFrame frame) {
        Account selectedAccount = null;
        if (frame instanceof TransactionsFrame)
        {
            // Doesn't work because account isn't set yet
            // selectedAccount = ((TransactionsFrame) frame).getAccount();
            selectedAccount = MainFrame.getInstance().getAccountListPanel().getSelectedAccount();
        }
        else if (frame instanceof MainFrame)
        {
            MainFrame mainFrame = MainFrame.getInstance();
            if (null != mainFrame)
            {
                AccountListPanel accountListPanel = MainFrame.getInstance().getAccountListPanel();
                if (null != accountListPanel)
                {
                    selectedAccount = accountListPanel.getSelectedAccount();
                }
            }
        }
        return selectedAccount;
    }
    
    protected static List<JScreenMenuItem> getExportMenuItemsAPI(final AbstractFrame frame){
        List<JScreenMenuItem> exportMenuItems = new LinkedList<JScreenMenuItem>();

        PluginFactory<BuddiExportPluginAPI> factory = new PluginFactory<BuddiExportPluginAPI>();
        List<BuddiExportPluginAPI> exportPlugins = factory.getPluginObjects(BuddiExportPluginAPIImpl.class);

        DataManager dataManager = createDataManager(frame);
        
        //Iterate through each plugin, and create a menu item for each.
        for (BuddiExportPluginAPI plugin : exportPlugins) {
            JScreenMenuItem menuItem = new JScreenMenuItem(plugin.getDescription(dataManager));

            //This is where the user's custom code is actually run
            addExportAPIActionListener(plugin, menuItem, frame);
            exportMenuItems.add(menuItem);
        }

        return exportMenuItems;
    }

    public static List<JScreenMenuItem> getExportMenuItems(final AbstractFrame frame){
        List<JScreenMenuItem> exportMenuItems = getExportMenuItemsNonAPI(frame);
        exportMenuItems.addAll(getExportMenuItemsAPI(frame));
        return exportMenuItems;
    }
        
    /**
     * Returns a list of all BuddiRunnablePlugins in the system.
     * 
     * @return
     */
    public static List<BuddiRunnablePlugin> getRunnablePlugins(){
        PluginFactory<BuddiRunnablePlugin> factory = new PluginFactory<BuddiRunnablePlugin>();
        List<BuddiRunnablePlugin> runnablePlugins = factory.getPluginObjects(BuddiRunnablePluginImpl.class);

        return runnablePlugins;
    }


	public static List<JScreenMenuItem> getImportMenuItemsNonAPI(final AbstractFrame frame){
		List<JScreenMenuItem> importMenuItems = new LinkedList<JScreenMenuItem>();

		PluginFactory<BuddiImportPlugin> factory = new PluginFactory<BuddiImportPlugin>();
		List<BuddiImportPlugin> importPlugins = factory.getPluginObjects(BuddiImportPluginImpl.class);

		//Iterate through each plugin, and create a menu item for each.
		for (BuddiImportPlugin plugin : importPlugins) {
			boolean correctWindow = true;
			
			if (plugin.getCorrectWindows() != null && frame != null){
				correctWindow = false;
				for (Class c : plugin.getCorrectWindows()) {
					if (frame.getClass().equals(c)) {
						correctWindow = true;
						break;
					}
				}
			}
			if (correctWindow){
				JScreenMenuItem menuItem = new JScreenMenuItem(plugin.getDescription());

				//This is where the user's custom code is actually run
				addImportActionListener(plugin, menuItem, frame);
				importMenuItems.add(menuItem);
			}
		}

		return importMenuItems;
	}

    public static List<JScreenMenuItem> getImportMenuItemsAPI(final AbstractFrame frame){
        List<JScreenMenuItem> importMenuItems = new LinkedList<JScreenMenuItem>();

        PluginFactory<BuddiImportPluginAPI> factory = new PluginFactory<BuddiImportPluginAPI>();
        List<BuddiImportPluginAPI> importPlugins = factory.getPluginObjects(BuddiImportPluginAPIImpl.class);

        ImportManager importManager = createImportManager(frame);
        
        //Iterate through each plugin, and create a menu item for each.
        for (BuddiImportPluginAPI plugin : importPlugins) {
            JScreenMenuItem menuItem = new JScreenMenuItem(plugin.getDescription(importManager));

            //This is where the user's custom code is actually run
            addImportAPIActionListener(plugin, menuItem, frame);
            importMenuItems.add(menuItem);
        }

        return importMenuItems;
    }

    public static List<JScreenMenuItem> getImportMenuItems(final AbstractFrame frame){
        List<JScreenMenuItem> importMenuItems = getImportMenuItemsNonAPI(frame);
        importMenuItems.addAll(getImportMenuItemsAPI(frame));
        return importMenuItems;
    }
        
	public static List<JPanel> getReportPanelLaunchers(){
		return getPanelLaunchers(BuddiReportPluginImpl.class);
	}

	public static List<JPanel> getGraphPanelLaunchers(){
		return getPanelLaunchers(BuddiGraphPluginImpl.class);
	}

	private static List<JPanel> getPanelLaunchers(Class<? extends BuddiPanelPlugin> pluginType){
		List<JPanel> panelItems = new LinkedList<JPanel>();

		PluginFactory<BuddiPanelPlugin> factory = new PluginFactory<BuddiPanelPlugin>();
		List<BuddiPanelPlugin> panelPlugins = factory.getPluginObjects(pluginType);

		//Iterate through each plugin, and create a menu item for each.
		for (BuddiPanelPlugin plugin : panelPlugins) {
			if (plugin.isPluginActive()){
				JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

				//Select the correct options for the dropdown, based on the plugin
				Vector<DateChoice> dateChoices;
				if (plugin.getDateRangeType().equals(DateRangeType.INTERVAL))
					dateChoices = BuddiPluginHelper.getInterval();
				else if (plugin.getDateRangeType().equals(DateRangeType.START_ONLY))
					dateChoices = BuddiPluginHelper.getStartOnly();
				else if (plugin.getDateRangeType().equals(DateRangeType.END_ONLY))
					dateChoices = BuddiPluginHelper.getEndOnly();
				else
					dateChoices = new Vector<DateChoice>();

				//Get the combobox 
				final JComboBox dateSelect = new JComboBox(dateChoices);
				dateSelect.setPreferredSize(new Dimension(Math.max(150, dateSelect.getPreferredSize().width), dateSelect.getPreferredSize().height));

				//Add the launchers
				addPanelActionListener(plugin, dateSelect);

				panel.add(new JLabel(Translate.getInstance().get(plugin.getDescription())));
				panel.add(dateSelect);

				panelItems.add(panel);
			}
		}

		return panelItems;
	}

	/**
	 * Returns a list of all the class names (in filesystem format, 
	 * i.e. org/homeunix/drummer/Test.class) of classes which
	 * implement the BuddiPlugin interface.  This is a relatively
	 * expensive operation - we have to instantiate each class
	 * within the jar file to be sure it is the correct type -
	 * so use this method with care. 
	 * @param jarFile
	 * @return
	 */
	public static Vector<String> getAllPluginsFromJar(File jarFile){
		Vector<String> classNames = new Vector<String>();

		for (JarEntry entry : JarLoader.getAllClasses(jarFile)) {
			try {
				Log.debug("Loading " + entry.getName() + " (" + filesystemToClass(entry.getName()) + ")");
				Object o = JarLoader.getObject(jarFile, filesystemToClass(entry.getName()));
                if (o instanceof BuddiPlugin) {
                    Log.info("Found BuddiPlugin: " + entry.getName());
                    
                    Version pluginVersion = ((BuddiPlugin) o).getMinimumVersion(); 
                    if (pluginVersion == null 
                            || pluginVersion.compareTo(new Version(Const.VERSION)) > 0) 
                        classNames.add(entry.getName());
                    else
                        Log.error("This plugin is for a newer version of Buddi.  To use it, please upgrade to version " + pluginVersion + " or later.");
                }
                else if (o instanceof BuddiPluginAPI) {
                    classNames.add(entry.getName());
                    Log.info("Found BuddiPluginAPI: " + entry.getName());
                    // TODO: should probably check versions here as well
                }
                else if (o instanceof BuddiGenericAPIPlugin) {
                    classNames.add(entry.getName());
                    Log.info("Found BuddiPluginGeneric: " + entry.getName());
                    // TODO: should probably check versions here as well
                }
				else {
					Log.info("Not of type BuddiPluginGeneric: " + o);
				}
			}
			catch (Exception e){
				Log.warning("Exception when instantiating plugin " + entry.getName() + ": if Const.DEVEL is set, stack trace follows:");
				if (Const.DEVEL)
					e.printStackTrace();
			}
			catch (AbstractMethodError ame){				
				Log.critical("Error when instantiating plugin " + entry.getName() + ".  This is probably due to loading a very old plugin in a new version of Buddi.  For debugging, stack trace follows:");
				ame.printStackTrace();
			}
			catch (Error e){
				Log.critical("Error when instantiating plugin " + entry.getName() + ": stack trace follows:");
				e.printStackTrace();
			}
		}

		return classNames;
	}


	/**
	 * Returns a list of plugin objects, after instantiating and checking 
	 * type.
	 * @param pluginType The class of the type of objects required.  This is needed to allow type checking on a generic.
	 * @return List of plugin objects of type pluginType.
	 */
	@SuppressWarnings("unchecked")
	private List<T> getPluginObjects(Class<? extends T> pluginTypeClass){
		List<T> pluginEntries = new LinkedList<T>();

		Object pluginType;
		try{
			pluginType = pluginTypeClass.newInstance();
		}
		catch(Exception e){
			Log.critical("Cannot create plugin type object to compare instances against.  I cannot load any plugins:" + e);
			return pluginEntries;
		}

		try{
			for (String pluginClass : Const.BUILT_IN_PLUGINS) {
				Object pluginObject = Class.forName(pluginClass).newInstance();

				if (pluginType instanceof BuddiExportPlugin
						&& pluginObject instanceof BuddiExportPlugin){
					if (Const.DEVEL) Log.info("Adding Export Plugin " + pluginObject);
					pluginEntries.add((T) pluginObject);
				}
				else if (pluginType instanceof BuddiImportPlugin
						&& pluginObject instanceof BuddiImportPlugin){
					if (Const.DEVEL) Log.info("Adding Import Plugin " + pluginObject);
					pluginEntries.add((T) pluginObject);
				}
				else if (pluginType instanceof BuddiReportPlugin
						&& pluginObject instanceof BuddiReportPlugin){
					if (Const.DEVEL) Log.info("Adding Report Plugin " + pluginObject);
					pluginEntries.add((T) pluginObject);
				}
				else if (pluginType instanceof BuddiGraphPlugin
						&& pluginObject instanceof BuddiGraphPlugin){
					if (Const.DEVEL) Log.info("Adding Graph Plugin " + pluginObject);
					pluginEntries.add((T) pluginObject);
				}
			}
		}
		catch(Exception e){
			Log.warning("Loading built in plugins: " + e);
		}

		// TODO Currently we load every jar separately.  This can potentially be slow - perhaps we should test first.
		Vector<Plugin> badPlugins = new Vector<Plugin>();
		for (Object o1 : PrefsInstance.getInstance().getPrefs().getLists().getPlugins()) {
			if (o1 instanceof Plugin){
				Plugin plugin = (Plugin) o1;

				File jarFile = new File(plugin.getJarFile());
				Translate.getInstance().loadPluginLanguages(jarFile, PrefsInstance.getInstance().getPrefs().getLanguage());
				String className = plugin.getClassName();
				className = filesystemToClass(className);
				try{
					Object pluginObject = JarLoader.getObject(jarFile, className);

                    if (pluginType instanceof BuddiExportPlugin
                            && pluginObject instanceof BuddiExportPlugin){
                        pluginEntries.add((T) pluginObject);
                    }
                    else if (pluginType instanceof BuddiImportPlugin
                            && pluginObject instanceof BuddiImportPlugin){
                        pluginEntries.add((T) pluginObject);
                    }
                    else if (pluginType instanceof BuddiPanelPlugin
                            && pluginObject instanceof BuddiPanelPlugin){
                        pluginEntries.add((T) pluginObject);
                    }
                    
                    else if (pluginType instanceof BuddiExportPluginAPI
                            && pluginObject instanceof BuddiExportPluginAPI){
                        if (Const.DEVEL) Log.info("Adding Custom Export API Plugin " + pluginObject);
                        pluginEntries.add((T) pluginObject);
                    }
                    else if (pluginType instanceof BuddiImportPluginAPI
                            && pluginObject instanceof BuddiImportPluginAPI){
                        if (Const.DEVEL) Log.info("Adding Custom Import API Plugin " + pluginObject);
                        pluginEntries.add((T) pluginObject);
                    }
//                    else if (pluginType instanceof BuddiPanelPluginAPI
//                            && pluginObject instanceof BuddiPanelPluginAPI){
//                        pluginEntries.add((T) pluginObject);
//                    }
				}
				catch(Exception e){
					Log.warning("Loading custom plugins: " + e);
					badPlugins.add(plugin);
				}
			}
		}

		//If there were any bad plugins, we remove them.
		if (badPlugins.size() > 0){
			Log.warning("Removing bad plugins: " + badPlugins);
			PrefsInstance.getInstance().getPrefs().getLists().getPlugins().removeAll(badPlugins);
		}

		return pluginEntries;

	}

    private static void addExportActionListener(final BuddiExportPlugin plugin, JScreenMenuItem menuItem, final AbstractFrame frame){
        menuItem.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent arg0) {
                if (plugin instanceof BuddiExportPlugin){
                    BuddiExportPlugin exportPlugin = (BuddiExportPlugin) plugin;
                    File file = null;

                    if (exportPlugin.isPromptForFile()){
                        file = DocumentManager.getInstance().showSaveFileDialog(null, exportPlugin.getFileChooserTitle(), exportPlugin.getFileFilter());
                    }

                    if (!exportPlugin.isPromptForFile() || file != null){
                        try{
                            ((BuddiExportPlugin) plugin).exportData(frame, file);
                        }
                        catch (Exception e){
                            Log.error(e);
                        }
                        catch (Error e){
                            Log.error(e);
                        }
                    }
                    else{
                        Log.warning("Plugin did not run - plugin requires valid file, which was not selected.");
                    }

                    //Update all the accounts with the new totals, etc.  
                    // Probably not needed for Import plugins, but 
                    // it's a pretty cheap operation anyway, and it 
                    // is better safe than sorry...
//                  DataInstance.getInstance().calculateAllBalances();
//                  MainFrame.getInstance().getAccountListPanel().updateContent();
                }
            }
        });
    }

    private static void addExportAPIActionListener(final BuddiExportPluginAPI plugin, JScreenMenuItem menuItem, final AbstractFrame frame){
        DataManager dataManager = createDataManager(frame);

        boolean flag = plugin.isPluginActive(dataManager);
        menuItem.setEnabled(flag);

        menuItem.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent arg0) {
                DataManager dataManager = createDataManager(frame);

                if (plugin instanceof BuddiExportPluginAPI){
                    BuddiExportPluginAPI exportPlugin = (BuddiExportPluginAPI) plugin;
                    File file = null;

                    if (exportPlugin.isPromptForFile(dataManager)){
                        file = DocumentManager.getInstance().showSaveFileDialog(null, exportPlugin.getFileChooserTitle(dataManager), exportPlugin.getFileFilter(dataManager));
                    }

                    if (!exportPlugin.isPromptForFile(dataManager) || file != null){
                        try{
                            ((BuddiExportPluginAPI) plugin).exportData(dataManager, frame, file);
                        }
                        catch (Exception e){
                            Log.error(e);
                        }
                        catch (Error e){
                            Log.error(e);
                        }
                    }
                    else{
                        Log.warning("Plugin did not run - plugin requires valid file, which was not selected.");
                    }

                    //Update all the accounts with the new totals, etc.  
                    // Probably not needed for Import plugins, but 
                    // it's a pretty cheap operation anyway, and it 
                    // is better safe than sorry...
//                  DataInstance.getInstance().calculateAllBalances();
//                  MainFrame.getInstance().getAccountListPanel().updateContent();
                }
            }
        });
    }

    private static void addImportActionListener(final BuddiImportPlugin plugin, JScreenMenuItem menuItem, final AbstractFrame frame){
        menuItem.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent arg0) {
                if (plugin instanceof BuddiImportPlugin){
                    BuddiImportPlugin importPlugin = (BuddiImportPlugin) plugin;
                    File file = null;

                    if (importPlugin.isPromptForFile()){
                        file = DocumentManager.getInstance().showLoadFileDialog(null, importPlugin.getFileChooserTitle(), importPlugin.getFileFilter());
                    }

                    if (!importPlugin.isPromptForFile() || file != null){
                        try {
                            ((BuddiImportPlugin) plugin).importData(frame, file);
                        }
                        catch (Exception e){
                            Log.error(e);
                        }
                        catch (Error e){
                            Log.error(e);
                        }
                    }
                    else {
                        Log.warning("Plugin did not run - plugin requires valid file, which was not selected.");
                    }

                    //Update all the accounts with the new totals, etc.
                    SourceController.calculateAllBalances();
                    MainFrame.getInstance().getAccountListPanel().updateContent();
                }
            }
        });
    }

    private static void addImportAPIActionListener(final BuddiImportPluginAPI plugin, JScreenMenuItem menuItem, final AbstractFrame frame){
        DataManager dataManager = createDataManager(frame);

        boolean flag = plugin.isPluginActive(dataManager);
        menuItem.setEnabled(flag);

        menuItem.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent arg0) {
                if (plugin instanceof BuddiImportPluginAPI){
                    BuddiImportPluginAPI importPlugin = (BuddiImportPluginAPI) plugin;
                    File file = null;

                    ImportManager importManager = createImportManager(frame);

                    if (importPlugin.isPromptForFile(importManager)){
                        file = DocumentManager.getInstance().showLoadFileDialog(null, importPlugin.getFileChooserTitle(importManager), importPlugin.getFileFilter(importManager));
                    }

                    if (!importPlugin.isPromptForFile(importManager) || file != null){
                        try {
                            ((BuddiImportPluginAPI) plugin).importData(importManager, frame, file);
                        }
                        catch (Exception e){
                            Log.error(e);
                        }
                        catch (Error e){
                            Log.error(e);
                        }
                    }
                    else {
                        Log.warning("Plugin did not run - plugin requires valid file, which was not selected.");
                    }

                    //Update all the accounts with the new totals, etc.
                    SourceController.calculateAllBalances();
                    MainFrame.getInstance().getAccountListPanel().updateContent();
                }
            }
        });
    }

	private static void addPanelActionListener(final BuddiPanelPlugin plugin, final JComboBox dateSelect){
		dateSelect.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				//Find out which item was clicked on
				Object o = dateSelect.getSelectedItem();

				//If the object was a date choice, access the encoded dates
				if (o instanceof DateChoice 
						&& plugin instanceof BuddiPanelPlugin){
					DateChoice choice = (DateChoice) o;

					//As long as the choice is not custom, our job is easy
					if (!choice.isCustom()){
						//Launch a new reports window with given parameters
						BuddiPluginHelper.openNewPanelPluginWindow(
								(BuddiPanelPlugin) plugin, 
								choice.getStartDate(), 
								choice.getEndDate()
						);
					}
					//If they want a custom window, it's a little 
					// harder... we need to open the custom date
					// window, which then launches the plugin.
					else{
						new CustomDateDialog(
								MainFrame.getInstance(),
								(BuddiPanelPlugin) plugin
						).openWindow();
					}
				}

				dateSelect.setSelectedItem(null);
			}
		});
	}

	/**
	 * Converts classes from filesystem type (i.e., org/homeunix/thecave/Test.class)
	 * to Class type (i.e., org.homeunix.thecave.Test).
	 * @param filesystemClassName class in Filesystem type
	 * @return
	 */
	private static String filesystemToClass(String filesystemClassName){
		return filesystemClassName.replaceAll(".class$", "").replaceAll("/", ".");
	}
}
