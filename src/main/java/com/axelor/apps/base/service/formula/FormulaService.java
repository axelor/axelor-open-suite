package com.axelor.apps.base.service.formula;

import java.math.BigDecimal;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.service.formula.call.CalculationRuleTaxCall;
import com.axelor.apps.base.service.formula.call.ConditionCalculationRuleTaxCall;
import com.axelor.apps.base.service.formula.call.ConditionFormulaCall;
import com.axelor.apps.base.service.formula.call.ConditionTaxCall;
import com.axelor.apps.base.service.formula.call.DisplayFormulaCall;
import com.axelor.apps.base.service.formula.call.FormulaCall;
import com.axelor.apps.base.service.formula.call.QtyFormulaCall;
import com.axelor.apps.base.service.formula.call.VatFormulaCall;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;

public class FormulaService {
	
	private static final Logger LOG = LoggerFactory.getLogger(FormulaService.class);
	
	
	/**
	 * Fonction permettant de réinitialiser l'ensemble de formules.
	 * 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws AxelorException
	 */
	public static void reset() throws InstantiationException, IllegalAccessException, AxelorException {
		
		LOG.debug("START RESET");
		
		FormulaCall.reset();
		DisplayFormulaCall.reset();
		QtyFormulaCall.reset();
		ConditionFormulaCall.reset();
		
		CalculationRuleTaxCall.reset();
		ConditionCalculationRuleTaxCall.reset();
		ConditionTaxCall.reset();
		
		VatFormulaCall.reset();
		
		LOG.debug("END RESET");
	}
	
	
	/**
	 * Fonction appelée dans les formules de prix unitaires.
	 * Joue une requête et renvoie la valeur.
	 * 
	 * @param key
	 * 		Key for details.
	 * @param sql
	 * 		SQL Request.
	 * @return
	 * @throws AxelorException
	 */
	public BigDecimal transco(String code, String label, String sql) throws AxelorException {
		
		LOG.debug("TRANSCO: KEY = {}, SQL = {}", label, sql);
		
		Query query = JPA.em().createNativeQuery(sql);
		Object value = query.getSingleResult();
		
		if (value != null){
			
			BigDecimal result = new BigDecimal(value.toString());
			
			LOG.debug("TRANSCO résultat = {}", result);
			return result;
		}
		else { throw new AxelorException(String.format("Aucun prix unitaire trouvé pour : %s", sql), IException.NO_VALUE); }
		
	}
	
}
