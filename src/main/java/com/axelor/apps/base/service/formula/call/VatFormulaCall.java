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

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Vat;
import com.axelor.apps.account.db.VatManagementLine;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.formula.Formula1Lvl;
import com.axelor.apps.base.service.formula.loader.Loader;
import com.axelor.exception.AxelorException;

/**
 * Singleton d'accès aux formules de TVA.
 * 
 * @author guerrier
 * @version 1.0
 *
 */
@Singleton
public final class VatFormulaCall {
	
	private static final Logger LOG = LoggerFactory.getLogger(VatFormulaCall.class);
	
	private static volatile VatFormulaCall instance = null;

	private Formula1Lvl<Vat, List<VatManagementLine>> formula;
	
	/**
	 * Constructeur.
	 * Initialise l'instance pour les formules de TVA.
	 */
	@SuppressWarnings("unchecked")
	@Inject
	private VatFormulaCall(){
		
		LOG.info("NEW VAT FORMULA");
		
		try {
			formula = (Formula1Lvl<Vat, List<VatManagementLine>>) Loader.loaderFormula(GeneralService.getFormulaGenerator().getVatFormula()).newInstance();
		} 
		catch (Exception e) {
			throw new RuntimeException("Impossible de charger les formules TVA", e);
		}
		
	}
	
	/**
	 * Méthode d'accès au singleton
	 * @return VatFormulaCall
	 */
	private static VatFormulaCall get(){
		
		if(instance == null){
			synchronized(VatFormulaCall.class){
				
				if(instance == null) { instance = new VatFormulaCall(); }				
			}
		}
		
		return instance;
	}
	
	/**
	 * Récupérer l'instance des formules de TVA.
	 * 
	 * @return
	 */
	public static Formula1Lvl<Vat, List<VatManagementLine>> formula(){
		
		return get().formula;	
	}
	
	/**
	 * Réinitialiser l'instance des formules de TVA. 
	 * 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws AxelorException 
	 */
	@SuppressWarnings("unchecked")
	public static void reset() throws InstantiationException, IllegalAccessException, AxelorException {
		
		LOG.info("RESET VAT FORMULA");

		Loader.loader().removeClassCache(get().formula.getClass());
		get().formula = (Formula1Lvl<Vat, List<VatManagementLine>>) Loader.loaderFormula(GeneralService.getFormulaGenerator().getVatFormula()).newInstance();
		
	}
	
}
