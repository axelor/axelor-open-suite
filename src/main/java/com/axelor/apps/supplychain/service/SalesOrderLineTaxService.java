/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.supplychain.db.SalesOrder;
import com.axelor.apps.supplychain.db.SalesOrderLine;
import com.axelor.apps.supplychain.db.SalesOrderLineTax;
import com.axelor.apps.supplychain.db.SalesOrderSubLine;
import com.google.inject.Inject;

public class SalesOrderLineTaxService {

	private static final Logger LOG = LoggerFactory.getLogger(SalesOrderLineTaxService.class); 
	
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
	 * @return La liste des lignes de taxe de la facture.
	 */
	public List<SalesOrderLineTax> createsSalesOrderLineTax(SalesOrder salesOrder, List<SalesOrderLine> salesOrderLineList) {
		
		List<SalesOrderLineTax> salesOrderLineTaxList = new ArrayList<SalesOrderLineTax>();
		Map<TaxLine, SalesOrderLineTax> map = new HashMap<TaxLine, SalesOrderLineTax>();
		
		if (salesOrderLineList != null && !salesOrderLineList.isEmpty()) {

			LOG.debug("Création des lignes de tva pour les lignes de factures.");
			
			for (SalesOrderLine salesOrderLine : salesOrderLineList) {
				
				if(salesOrderLine.getSalesOrderSubLineList() != null && !salesOrderLine.getSalesOrderSubLineList().isEmpty())  {
					
					for(SalesOrderSubLine salesOrderSubLine : salesOrderLine.getSalesOrderSubLineList())  {
						TaxLine taxLine = salesOrderSubLine.getTaxLine();
						LOG.debug("Tax {}", taxLine);
						
						if (map.containsKey(taxLine)) {
						
							SalesOrderLineTax salesOrderLineTax = map.get(taxLine);
							
							salesOrderLineTax.setExTaxBase(salesOrderLineTax.getExTaxBase().add(salesOrderSubLine.getExTaxTotal()));
							
						}
						else {
							
							SalesOrderLineTax salesOrderLineTax = new SalesOrderLineTax();
							salesOrderLineTax.setSalesOrder(salesOrder);
							
							salesOrderLineTax.setExTaxBase(salesOrderSubLine.getExTaxTotal());
							
							salesOrderLineTax.setTaxLine(taxLine);
							map.put(taxLine, salesOrderLineTax);
							
						}
					}
				}
				else  {
				
					TaxLine taxLine = salesOrderLine.getTaxLine();
					LOG.debug("Tax {}", taxLine);
					
					if (map.containsKey(taxLine)) {
					
						SalesOrderLineTax salesOrderLineTax = map.get(taxLine);
						
						salesOrderLineTax.setExTaxBase(salesOrderLineTax.getExTaxBase().add(salesOrderLine.getExTaxTotal()));
						
					}
					else {
						
						SalesOrderLineTax salesOrderLineTax = new SalesOrderLineTax();
						salesOrderLineTax.setSalesOrder(salesOrder);
						
						salesOrderLineTax.setExTaxBase(salesOrderLine.getExTaxTotal());
						
						salesOrderLineTax.setTaxLine(taxLine);
						map.put(taxLine, salesOrderLineTax);
						
					}
				}
			}
		}
			
		for (SalesOrderLineTax salesOrderLineTax : map.values()) {
			
			// Dans la devise de la facture
			BigDecimal exTaxBase = salesOrderLineTax.getExTaxBase();
			BigDecimal taxTotal = salesOrderToolService.computeAmount(exTaxBase, salesOrderLineTax.getTaxLine().getValue());
			salesOrderLineTax.setTaxTotal(taxTotal);
			salesOrderLineTax.setInTaxTotal(exTaxBase.add(taxTotal));
			
			salesOrderLineTaxList.add(salesOrderLineTax);

			LOG.debug("Ligne de TVA : Total TVA => {}, Total HT => {}", new Object[] {salesOrderLineTax.getTaxTotal(), salesOrderLineTax.getInTaxTotal()});
			
		}

		return salesOrderLineTaxList;
	}

	
	
}
