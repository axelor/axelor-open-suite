package com.axelor.apps.account.service.invoice.generator.line;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.exception.AxelorException;

public abstract class InvoiceLineManagement {

	// Logger
	private static final Logger LOG = LoggerFactory.getLogger(InvoiceLineManagement.class);
	
	abstract public List<?> creates() throws AxelorException ;
	
	/**
	 * Calculer le montant HT d'une ligne de facture.
	 * 
	 * @param quantity
	 *          Quantité à facturer.
	 * @param price
	 *          Le prix.
	 * 
	 * @return 
	 * 			Le montant HT de la ligne.
	 */
	public static BigDecimal computeAmount(BigDecimal quantity, BigDecimal price) {

		BigDecimal amount = quantity.multiply(price).setScale(2, RoundingMode.HALF_EVEN);

		LOG.debug("Calcul du montant HT avec une quantité de {} pour {} : {}", new Object[] { quantity, price, amount });

		return amount;
	}
	
}
