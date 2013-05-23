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
 * @author guerrier
 * @version 1.0
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
