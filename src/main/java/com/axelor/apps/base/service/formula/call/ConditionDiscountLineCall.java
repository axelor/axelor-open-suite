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
package com.axelor.apps.base.service.formula.call;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codehaus.groovy.control.CompilationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.service.formula.Condition2Lvl;
import com.axelor.apps.base.service.formula.generator.condition.ConditionDiscountLineGenerator;
import com.axelor.apps.base.service.formula.loader.Loader;
import com.axelor.apps.account.db.DiscountEngineLine;
import com.axelor.exception.AxelorException;

/**
 * Singleton d'accès aux formules des conditions des prix unitaires.
 * 
 */
@Singleton
public final class ConditionDiscountLineCall {
	
	private static final Logger LOG = LoggerFactory.getLogger(ConditionDiscountLineCall.class);
	
	private static volatile ConditionDiscountLineCall instance = null;

	private Condition2Lvl<DiscountEngineLine,InvoiceLine> condition;
	
	/**
	 * Constructeur.
	 * Initialise l'instance pour les formules des conditions des prix unitaires.
	 */
	@Inject
	private ConditionDiscountLineCall(){
		
		LOG.info("NEW CONDITION DISCOUNT LINE");
		
		try {
			condition = Loader.loaderCondition2Lvl((new ConditionDiscountLineGenerator()).generate());
		} 
		catch (Exception e) {
			throw new RuntimeException("Impossible de charger les conditions sur les formules de prix unitaire", e);
		}
		
	}
	
	/**
	 * Méthode d'accès au singleton
	 * @return ConditionFormulaCall
	 */
	private static ConditionDiscountLineCall get(){
		
		if(instance == null){
			synchronized(ConditionDiscountLineCall.class){
				
				if(instance == null) { instance = new ConditionDiscountLineCall(); }
				
			}
		}
		
		return instance;
		
	}
	
	/**
	 * Récupérer l'instance des formules des conditions des prix unitaires.
	 * 
	 * @return
	 */
	public static Condition2Lvl<DiscountEngineLine,InvoiceLine> condition(){
		
		return get().condition;	
	}
	
	/**
	 * Réinitialiser l'instance des formules des conditions des prix unitaires. 
	 * 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws AxelorException 
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws CompilationFailedException 
	 */
	public static void reset() throws Exception {
		
		LOG.info("RESET CONDITION DISCOUNT LINE");

		Loader.loader().removeClassCache(get().condition.getClass());
		get().condition = Loader.loaderCondition2Lvl((new ConditionDiscountLineGenerator()).generate());
		
	}
	
}
