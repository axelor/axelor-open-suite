package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.VatLine;
import com.axelor.apps.supplychain.db.SalesOrder;
import com.axelor.apps.supplychain.db.SalesOrderLine;
import com.axelor.apps.supplychain.db.SalesOrderLineVat;
import com.axelor.apps.supplychain.db.SalesOrderSubLine;
import com.google.inject.Inject;

public class SalesOrderLineVatService {

	private static final Logger LOG = LoggerFactory.getLogger(SalesOrderLineVatService.class); 
	
	@Inject
	private SalesOrderToolService salesOrderToolService;
	
	
	
	
	/**
	 * Créer les lignes de TVA du devis. La création des lignes de TVA se
	 * basent sur les lignes de devis ainsi que les sous-lignes de devis de
	 * celles-ci.
	 * Si une ligne de devis comporte des sous-lignes de devis, alors on se base uniquement sur celles-ci.
	 * 
	 * @param invoice
	 *            La facture.
	 * 
	 * @param invoiceLines
	 *            Les lignes de facture.
	 * 
	 * @param invoiceLineTaxes
	 *            Les lignes des taxes de la facture.
	 * 
	 * @return La liste des lignes de TVA de la facture.
	 */
	public List<SalesOrderLineVat> createsSalesOrderLineVat(SalesOrder salesOrder, List<SalesOrderLine> salesOrderLineList) {
		
		List<SalesOrderLineVat> vatLines = new ArrayList<SalesOrderLineVat>();
		Map<VatLine, SalesOrderLineVat> map = new HashMap<VatLine, SalesOrderLineVat>();
		
		if (salesOrderLineList != null && !salesOrderLineList.isEmpty()) {

			LOG.debug("Création des lignes de tva pour les lignes de factures.");
			
			for (SalesOrderLine salesOrderLine : salesOrderLineList) {
				
				if(salesOrderLine.getSalesOrderSubLineList() != null && !salesOrderLine.getSalesOrderSubLineList().isEmpty())  {
					
					for(SalesOrderSubLine salesOrderSubLine : salesOrderLine.getSalesOrderSubLineList())  {
						VatLine vatLine = salesOrderSubLine.getVatLine();
						LOG.debug("TVA {}", vatLine);
						
						if (map.containsKey(vatLine)) {
						
							SalesOrderLineVat salesOrderLineVat = map.get(vatLine);
							
							salesOrderLineVat.setExTaxBase(salesOrderLineVat.getExTaxBase().add(salesOrderSubLine.getExTaxTotal()));
							
						}
						else {
							
							SalesOrderLineVat salesOrderLineVat = new SalesOrderLineVat();
							salesOrderLineVat.setSalesOrder(salesOrder);
							
							salesOrderLineVat.setExTaxBase(salesOrderSubLine.getExTaxTotal());
							
							salesOrderLineVat.setVatLine(vatLine);
							map.put(vatLine, salesOrderLineVat);
							
						}
					}
				}
				else  {
				
					VatLine vatLine = salesOrderLine.getVatLine();
					LOG.debug("TVA {}", vatLine);
					
					if (map.containsKey(vatLine)) {
					
						SalesOrderLineVat salesOrderLineVat = map.get(vatLine);
						
						salesOrderLineVat.setExTaxBase(salesOrderLineVat.getExTaxBase().add(salesOrderLine.getExTaxTotal()));
						
					}
					else {
						
						SalesOrderLineVat salesOrderLineVat = new SalesOrderLineVat();
						salesOrderLineVat.setSalesOrder(salesOrder);
						
						salesOrderLineVat.setExTaxBase(salesOrderLine.getExTaxTotal());
						
						salesOrderLineVat.setVatLine(vatLine);
						map.put(vatLine, salesOrderLineVat);
						
					}
				}
			}
		}
			
		for (SalesOrderLineVat vatLine : map.values()) {
			
			// Dans la devise de la facture
			BigDecimal vatExTaxBase = vatLine.getExTaxBase();
			BigDecimal vatTotal = salesOrderToolService.computeAmount(vatExTaxBase, vatLine.getVatLine().getValue());
			vatLine.setVatTotal(vatTotal);
			vatLine.setInTaxTotal(vatExTaxBase.add(vatTotal));
			
			vatLines.add(vatLine);

			LOG.debug("Ligne de TVA : Total TVA => {}, Total HT => {}", new Object[] {vatLine.getVatTotal(), vatLine.getInTaxTotal()});
			
		}

		return vatLines;
	}

	
	
}
