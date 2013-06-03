package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.VatLine;
import com.axelor.apps.account.service.AccountManagementService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.supplychain.db.SalesOrder;
import com.axelor.apps.supplychain.db.SalesOrderLine;
import com.axelor.apps.supplychain.db.SalesOrderSubLine;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class SalesOrderLineService {

	private static final Logger LOG = LoggerFactory.getLogger(SalesOrderLineService.class); 
	
	@Inject
	private CurrencyService currencyService;
	
	@Inject
	private AccountManagementService accountManagementService;
	
	
	/**
	 * Calculer le montant HT d'une ligne de devis.
	 * 
	 * @param quantity
	 *          Quantité.
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
	
	
	public BigDecimal getUnitPrice(SalesOrder salesOrder, SalesOrderLine salesOrderLine) throws AxelorException  {
		
		Product product = salesOrderLine.getProduct();
		
		return currencyService.getAmountCurrencyConverted(
			product.getSaleCurrency(), salesOrder.getCurrency(), product.getSalePrice(), salesOrder.getCreationDate());  
		
	}
	
	
	public VatLine getVatLine(SalesOrder salesOrder, SalesOrderLine salesOrderLine) throws AxelorException  {
		
		return accountManagementService.getVatLine(
				salesOrder.getCreationDate(), salesOrderLine.getProduct(), salesOrder.getCompany(), false);
		
	}
	
	
	public BigDecimal computeSalesOrderLine(SalesOrderLine salesOrderLine)  {
		
		BigDecimal exTaxTotal = BigDecimal.ZERO;
		
		if(salesOrderLine.getSalesOrderSubLineList() != null && !salesOrderLine.getSalesOrderSubLineList().isEmpty())  {
			for(SalesOrderSubLine salesOrderSubLine : salesOrderLine.getSalesOrderSubLineList())  {
				exTaxTotal = exTaxTotal.add(salesOrderSubLine.getExTaxTotal());
			}
		}
		else  {
			return salesOrderLine.getExTaxTotal();
		}
		
		return exTaxTotal;
	}

	
}
