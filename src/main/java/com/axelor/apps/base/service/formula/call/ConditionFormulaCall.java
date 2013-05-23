package com.axelor.apps.base.service.formula.call;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.exception.AxelorException;

/**
 * Singleton d'accès aux formules des conditions des prix unitaires.
 * 
 * @author guerrier
 * @version 1.0
 *
 */
@Singleton
public final class ConditionFormulaCall {
	
	private static final Logger LOG = LoggerFactory.getLogger(ConditionFormulaCall.class);
	
	private static volatile ConditionFormulaCall instance = null;

	/**
	 * Constructeur.
	 * Initialise l'instance pour les formules des conditions des prix unitaires.
	 */
	@Inject
	private ConditionFormulaCall(){
		
		LOG.info("NEW CONDITION FORMULA");
		
		try {
//			condition = Loader.loaderCondition2Lvl(GeneralService.getFormulaGenerator().getConditionFormula());
		} 
		catch (Exception e) {
			throw new RuntimeException("Impossible de charger les conditions sur les formules de prix unitaire", e);
		}
		
	}
	
	/**
	 * Méthode d'accès au singleton
	 * @return ConditionFormulaCall
	 */
	private static ConditionFormulaCall get(){
		
		if(instance == null){
			synchronized(ConditionFormulaCall.class){
				
				if(instance == null) { instance = new ConditionFormulaCall(); }
				
			}
		}
		
		return instance;
		
	}
	
	
	/**
	 * Réinitialiser l'instance des formules des conditions des prix unitaires. 
	 * 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws AxelorException 
	 */
	public static void reset() throws InstantiationException, IllegalAccessException, AxelorException {
		
		LOG.info("RESET CONDITION FORMULA");

//		Loader.loader().removeClassCache(get().condition.getClass());
//		get().condition = Loader.loaderCondition2Lvl(GeneralService.getFormulaGenerator().getConditionFormula());
		
	}
	
}
