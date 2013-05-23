package com.axelor.apps.base.service.formula.call;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.formula.Condition1Lvl;
import com.axelor.apps.base.service.formula.loader.Loader;
import com.axelor.exception.AxelorException;

/**
 * Singleton d'accès aux formules des conditions de taxes.
 * 
 * @author guerrier
 * @version 1.0
 *
 */
@Singleton
public final class ConditionTaxCall {
	
	private static final Logger LOG = LoggerFactory.getLogger(ConditionTaxCall.class);
	
	private static volatile ConditionTaxCall instance = null;
	
	private Condition1Lvl<Invoice> condition;
	
	/**
	 * Constructeur.
	 * Initialise l'instance pour les formules des conditions de taxes.
	 */
	@Inject
	private ConditionTaxCall(){
		
		LOG.info("NEW CONDITION TAX");
		
		try {
			condition = Loader.loaderCondition1Lvl(GeneralService.getFormulaGenerator().getConditionTax());
		} 
		catch (Exception e) {
			throw new RuntimeException("Impossible de charger les conditions sur les taxes", e);
		}
		
	}
	
	/**
	 * Méthode d'accès au singleton
	 * @return ConditionTaxCall
	 */
	private static ConditionTaxCall get(){
		
		if(instance == null){
			synchronized(ConditionTaxCall.class){
				
				if(instance == null) { instance = new ConditionTaxCall(); }
				
			}
		}
		
		return instance;
		
	}
	
	/**
	 * Récupérer l'instance des formules des conditions de taxes.
	 * 
	 * @return
	 */
	public static Condition1Lvl<Invoice> condition(){
		
		return get().condition;	
	}
	
	/**
	 * Réinitialiser l'instance des formules des conditions de taxes. 
	 * 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws AxelorException 
	 */
	public static void reset() throws InstantiationException, IllegalAccessException, AxelorException {
		LOG.info("RESET CONDITION TAX");

		Loader.loader().removeClassCache(get().condition.getClass());
		get().condition = Loader.loaderCondition1Lvl(GeneralService.getFormulaGenerator().getConditionTax());
		
	}
}
