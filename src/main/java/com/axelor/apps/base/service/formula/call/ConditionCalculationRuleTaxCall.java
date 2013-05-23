package com.axelor.apps.base.service.formula.call;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.formula.Condition1Lvl;
import com.axelor.apps.base.service.formula.loader.Loader;
import com.axelor.exception.AxelorException;

/**
 * Singleton d'accès aux formules des conditions d'assiettes.
 * 
 * @author guerrier
 * @version 1.0
 *
 */
@Singleton
public final class ConditionCalculationRuleTaxCall {
	
	private static final Logger LOG = LoggerFactory.getLogger(ConditionCalculationRuleTaxCall.class);
	
	private static volatile ConditionCalculationRuleTaxCall instance = null;
	
	private Condition1Lvl<InvoiceLine> condition;
	
	/**
	 * Constructeur.
	 * Initialise l'instance pour les formules des conditions d'assiettes.
	 */
	@Inject
	private ConditionCalculationRuleTaxCall(){
		
		LOG.info("NEW CONDITION CALCULATION RULE TAX");
		
		try {
			condition = Loader.loaderCondition1Lvl(GeneralService.getFormulaGenerator().getConditionCalculationRuleTax());
		} 
		catch (Exception e) {
			throw new RuntimeException("Impossible de charger les conditions d'assiettes de taxes", e);
		}
		
	}
	
	/**
	 * Méthode d'accès au singleton
	 * @return ConditionCalculationRuleTaxCall
	 */
	private static ConditionCalculationRuleTaxCall get(){
		
		if(instance == null){
			synchronized(ConditionCalculationRuleTaxCall.class){
				
				if(instance == null) { instance = new ConditionCalculationRuleTaxCall(); }
				
			}
		}
		
		return instance;
		
	}
	
	/**
	 * Récupérer l'instance des formules des conditions d'assiettes.
	 * 
	 * @return
	 */
	public static Condition1Lvl<InvoiceLine> condition(){
		
		return get().condition;	
	}
	
	/**
	 * Réinitialiser l'instance des formules des conditions d'assiettes. 
	 * 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws AxelorException 
	 */
	public static void reset() throws InstantiationException, IllegalAccessException, AxelorException {

		LOG.info("RESET CONDITION CALCULATION RULE TAX");

		Loader.loader().removeClassCache(get().condition.getClass());
		get().condition = Loader.loaderCondition1Lvl(GeneralService.getFormulaGenerator().getConditionCalculationRuleTax());
		
	}
}
