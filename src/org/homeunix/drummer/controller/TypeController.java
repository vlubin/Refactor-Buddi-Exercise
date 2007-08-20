/*
 * Created on Apr 7, 2007 by wyatt
 */
package org.homeunix.drummer.controller;

import java.util.Collections;
import java.util.Vector;

import org.homeunix.drummer.model.Account;
import org.homeunix.drummer.model.DataInstance;
import org.homeunix.drummer.model.ModelFactory;
import org.homeunix.drummer.model.Type;

public class TypeController {
	
	/**
	 * Adds a new type to the data model.  This type is what will show
	 * up in the New Account window.
	 * @param name The user-readable name (or the translation key, if it is a translated term)
	 * @param credit Is this a Credit type? 
	 */
	@SuppressWarnings("unchecked")
	public static void addType(String name, boolean credit){
		Type t = ModelFactory.eINSTANCE.createType();
		t.setName(name);
		t.setCredit(credit);
		DataInstance.getInstance().getDataModel().getAllTypes().getTypes().add(t);
	}
	
	/**
	 * Adds a new type to the data model.  This type is what will show
	 * up in the New Account window.
	 * @param type The user-readable name (or the translation key, if it is a translated term) 
	 */
	@SuppressWarnings("unchecked")
	public static void addType(Type type){
		DataInstance.getInstance().getDataModel().getAllTypes().getTypes().add(type);
	}
	
	/**
	 * Returns all types in the model, sorted according to the Type comparator
	 * @return All types in the currently loaded data model
	 */
	@SuppressWarnings("unchecked")
	public static Vector<Type> getTypes(){
		Vector<Type> v = new Vector<Type>(DataInstance.getInstance().getDataModel().getAllTypes().getTypes());
		Collections.sort(v);
		return v;
	}
	
	/**
	 * Attempts to remove a Type object, after doing some checks to 
	 * ensure you can safely remove it.
	 * 
	 * @param t Type to remove
	 * @return <code>true</code> if the Type was successfully deleted, 
	 * <code>false</code> otherwise.
	 */
	public static boolean deleteType(Type t){
		for (Account a : SourceController.getAccounts()) {
			if (a.getAccountType().equals(t)){
				return false;
			}
		}
		
		return DataInstance.getInstance().getDataModel().getAllTypes().getTypes().remove(t);
	}
}
