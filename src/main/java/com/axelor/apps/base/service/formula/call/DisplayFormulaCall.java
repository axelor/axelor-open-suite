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

import java.math.BigDecimal;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.PricingListVersion;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.formula.Formula1Lvl;
import com.axelor.apps.base.service.formula.loader.Loader;
import com.axelor.exception.AxelorException;

/**
 * Singleton d'accès aux formules de prix unitaire d'affichage.
 * 
 * @author guerrier
 * @version 1.0
 *
 */
@Singleton
public final class DisplayFormulaCall {
	
	private static final Logger LOG = LoggerFactory.getLogger(DisplayFormulaCall.class);
	
	private static volatile DisplayFormulaCall instance = null;
	
	private Class<Formula1Lvl<BigDecimal, PricingListVersion>> klass;
	
	/**
	 * Constructeur.
	 * Initialise l'instance pour les formules de prix unitaires d'affichage.
	 */
	@SuppressWarnings("unchecked")
	@Inject
	private DisplayFormulaCall(){
		
		LOG.info("NEW DISPLAY FORMULA");
		
		try { klass = (Class<Formula1Lvl<BigDecimal, PricingListVersion>>) Loader.loaderFormula(GeneralService.getFormulaGenerator().getDisplayFormula()); } 
		catch (Exception e) { throw new RuntimeException("Impossible de charger les formules d'affichages des prix unitaires", e); }
		
	}
	
	/**
	 * Méthode d'accès au singleton
	 * @return DisplayFormulaCall
	 */
	private static DisplayFormulaCall get(){
		
		if(instance == null){
			synchronized(DisplayFormulaCall.class){
				
				if(instance == null) { instance = new DisplayFormulaCall(); }
				
			}
		}
		
		return instance;
		
	}
	
	/**
	 * Récupérer l'instance des formules pour le prix unitaire d'affichage.
	 * 
	 * @return
	 */
	public static Formula1Lvl<BigDecimal, PricingListVersion> newInstance(){
		
		try { return get().klass.newInstance(); } 
		catch (Exception e) { throw new RuntimeException("Impossible d'instancier les formules d'affichages des prix unitaires", e); }
		
	}
	
	/**
	 * Réinitialiser l'instance des formules de prix unitaire d'affichage. 
	 * 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws AxelorException 
	 */
	@SuppressWarnings("unchecked")
	public static void reset() throws InstantiationException, IllegalAccessException, AxelorException {

		LOG.info("RESET DISPLAY FORMULA");

		Loader.loader().removeClassCache(get().klass);
		get().klass = (Class<Formula1Lvl<BigDecimal, PricingListVersion>>) Loader.loaderFormula(GeneralService.getFormulaGenerator().getDisplayFormula());
		
	}
	
}
