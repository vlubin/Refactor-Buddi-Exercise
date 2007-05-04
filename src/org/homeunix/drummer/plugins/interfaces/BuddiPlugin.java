/*
 * Created on Sep 14, 2006 by wyatt
 */
package org.homeunix.drummer.plugins.interfaces;

import org.homeunix.thecave.moss.util.Version;

public interface BuddiPlugin extends BuddiGenericAPIPlugin {
	/**
	 * Returns the description text, as seen on the main window 
	 * under Reports tab, or in the menu
	 * @return
	 */
	public String getDescription();
	
	/**
	 * Returns the minimum version required to run this plugin, 
	 * or null if there is no minimum (not recommended unless
	 * the plugin uses no API calls which have been changed 
	 * since plugins were introduced).
	 * 
	 * @return The org.homeunix.thecave.moss.util.Version object 
	 * containing the minimum version number. 
	 */
	public Version getMinimumVersion();
	
	/**
	 * Should this plugin be activated?  Most people can just
	 * put true here; if there is some logic which determines if this
	 * is to be shown or not, though, you can add it here.
	 * @return
	 */
	public boolean isPluginActive();
}
