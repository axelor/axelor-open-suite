package com.axelor.apps.base.service.formula.call;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codehaus.groovy.control.CompilationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.service.formula.Condition1Lvl;
import com.axelor.apps.base.service.formula.generator.condition.ConditionDiscountGenerator;
import com.axelor.apps.base.service.formula.loader.Loader;
import com.axelor.exception.AxelorException;

/**
 * Singleton d'accès aux formules des conditions des prix unitaires.
 * 
 * @author guerrier
 * @version 1.0
 *
 */
@Singleton
public final class ConditionDiscountCall {
	
	private static final Logger LOG = LoggerFactory.getLogger(ConditionDiscountCall.class);
	
	private static volatile ConditionDiscountCall instance = null;

	private Condition1Lvl<Invoice> condition;
	
	/**
	 * Constructeur.
	 * Initialise l'instance pour les formules des conditions des prix unitaires.
	 */
	@Inject
	private ConditionDiscountCall(){
		
		LOG.info("NEW CONDITION DISCOUNT");
		
		try {
			condition = Loader.loaderCondition1Lvl((new ConditionDiscountGenerator()).generate());
		} 
		catch (Exception e) {
			throw new RuntimeException("Impossible de charger les conditions sur les formules de prix unitaire", e);
		}
		
	}
	
	/**
	 * Méthode d'accès au singleton
	 * @return ConditionFormulaCall
	 */
	private static ConditionDiscountCall get(){
		
		if(instance == null){
			synchronized(ConditionDiscountCall.class){
				
				if(instance == null) { instance = new ConditionDiscountCall(); }
				
			}
		}
		
		return instance;
		
	}
	
	/**
	 * Récupérer l'instance des formules des conditions des prix unitaires.
	 * 
	 * @return
	 */
	public static Condition1Lvl<Invoice> condition(){
		
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
		
		LOG.info("RESET CONDITION DISCOUNT");

		Loader.loader().removeClassCache(get().condition.getClass());
		get().condition = Loader.loaderCondition1Lvl((new ConditionDiscountGenerator()).generate());
		
	}
	
}
