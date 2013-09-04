/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.base.service.formula.loader;

import groovy.lang.GroovyClassLoader;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.service.formula.Condition1Lvl;
import com.axelor.apps.base.service.formula.Condition2Lvl;

@Singleton
public final class Loader extends GroovyClassLoader {
	
	private static final Logger LOG = LoggerFactory.getLogger(Loader.class);
	
	private static volatile Loader instance = null;
	
	/**
	 * Constructeur.
	 * Initialise l'instance pour les formules de prix unitaires d'affichage.
	 */
	@Inject
	private Loader(){

		LOG.debug("Création du chargeur de formule");
		
	}
	
	/**
	 * Méthode d'accès au singleton
	 * @return DisplayFormulaCall
	 */
	private static Loader get(){
		
		if(instance == null){
			synchronized(Loader.class){
				
				if(instance == null) { instance = new Loader(); }
				
			}
		}
		
		return instance;
		
	}
	
	public static Loader loader(){
		
		return get();
		
	}
	
	public void removeClassCache(Class<?> klass){
		
		removeClassCacheEntry(klass.getName());
		
	}

	public static Class<?> loaderFormula(String code) throws InstantiationException, IllegalAccessException {
		
		return loader().parseClass(code);
		
	}

	@SuppressWarnings("unchecked")
	public static <P> Condition1Lvl<P> loaderCondition1Lvl(String code) throws InstantiationException, IllegalAccessException{
		
		Class<?> classLoader = loader().parseClass(code);
		return (Condition1Lvl<P>) classLoader.newInstance();
		
	}

	@SuppressWarnings("unchecked")
	public static <P1, P2> Condition2Lvl<P1, P2> loaderCondition2Lvl(String code) throws InstantiationException, IllegalAccessException{
		
		Class<?> classLoader = loader().parseClass(code);
		return (Condition2Lvl<P1, P2>) classLoader.newInstance();
		
	}
	
}
