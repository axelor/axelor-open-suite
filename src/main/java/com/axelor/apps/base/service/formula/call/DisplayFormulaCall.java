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
