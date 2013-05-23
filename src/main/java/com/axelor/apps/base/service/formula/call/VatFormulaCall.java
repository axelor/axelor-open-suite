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
