package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.supplychain.db.SalesOrder;
import com.axelor.apps.supplychain.db.SalesOrderLine;
import com.axelor.apps.supplychain.db.SalesOrderLineVat;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SalesOrderService {

	private static final Logger LOG = LoggerFactory.getLogger(SalesOrderService.class); 

	@Inject
	private SalesOrderLineService salesOrderLineService;
	
	@Inject
	private CurrencyService currencyService;
	
	@Inject
	private SalesOrderLineVatService salesOrderLineVatService;
	
	public SalesOrder _computeSalesOrderLines(SalesOrder salesOrder)  {
		
		if(salesOrder.getSalesOrderLineList() != null)  {
			for(SalesOrderLine salesOrderLine : salesOrder.getSalesOrderLineList())  {
				salesOrderLine.setExTaxTotal(salesOrderLineService.computeSalesOrderLine(salesOrderLine));
			}
		}
		
		return salesOrder;
	}


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void computeSalesOrder(SalesOrder salesOrder) throws AxelorException  {
		
		this.initSalesOrderLineVats(salesOrder);
		
		this._computeSalesOrderLines(salesOrder);
		
		this._populateSalesOrder(salesOrder);
		
		this._computeSalesOrder(salesOrder);
		
		salesOrder.save();
	}
	
	
	/**
	 * Peupler un devis.
	 * <p>
	 * Cette fonction permet de déterminer les tva d'un devis à partir des lignes de factures passées en paramètres. 
	 * </p>
	 * 
	 * @param invoice
	 * @param contractLine
	 * @param invoiceLines
	 * @param invoiceLineTaxes
	 * @param standard
	 * 
	 * @throws AxelorException
	 */
	public void _populateSalesOrder(SalesOrder salesOrder) throws AxelorException {
		
		LOG.debug("Peupler une facture => lignes de devis: {} ", new Object[] { salesOrder.getSalesOrderLineList().size() });
		
		// create Tva lines
		salesOrder.getSalesOrderLineVatList().addAll(salesOrderLineVatService.createsSalesOrderLineVat(salesOrder, salesOrder.getSalesOrderLineList()));
		
	}
	
	/**
	 * Calculer le montant d'une facture.
	 * <p> 
	 * Le calcul est basé sur les lignes de TVA préalablement créées.
	 * </p>
	 * 
	 * @param invoice
	 * @param vatLines
	 * @throws AxelorException 
	 */
	public void _computeSalesOrder(SalesOrder salesOrder) throws AxelorException {
		
		salesOrder.setExTaxTotal(BigDecimal.ZERO);
		salesOrder.setVatTotal(BigDecimal.ZERO);
		salesOrder.setInTaxTotal(BigDecimal.ZERO);
		
		for (SalesOrderLineVat salesOrderLineVat : salesOrder.getSalesOrderLineVatList()) {
			
			// Dans la devise de la comptabilité du tiers
			salesOrder.setExTaxTotal(salesOrder.getExTaxTotal().add( salesOrderLineVat.getExTaxBase() ));
			salesOrder.setVatTotal(salesOrder.getVatTotal().add( salesOrderLineVat.getVatTotal() ));
			salesOrder.setInTaxTotal(salesOrder.getInTaxTotal().add( salesOrderLineVat.getInTaxTotal() ));
			
		}
		
		salesOrder.setAmountRemainingToBeInvoiced(salesOrder.getInTaxTotal());
		
		LOG.debug("Montant de la facture: HTT = {},  HT = {}, TVA = {}, TTC = {}",
			new Object[] { salesOrder.getExTaxTotal(), salesOrder.getVatTotal(), salesOrder.getInTaxTotal() });
		
	}

	
	/**
	 * Permet de réinitialiser la liste des lignes de TVA
	 * @param salesOrder
	 * 			Un devis
	 */
	public void initSalesOrderLineVats(SalesOrder salesOrder) {
		
		if (salesOrder.getSalesOrderLineVatList() == null) { salesOrder.setSalesOrderLineVatList(new ArrayList<SalesOrderLineVat>()); }
		
		else { salesOrder.getSalesOrderLineVatList().clear(); }
		
	}

}


