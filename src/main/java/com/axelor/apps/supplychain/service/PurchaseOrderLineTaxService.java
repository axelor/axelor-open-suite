/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
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
import com.axelor.apps.supplychain.db.PurchaseOrder;
import com.axelor.apps.supplychain.db.PurchaseOrderLine;
import com.axelor.apps.supplychain.db.PurchaseOrderLineTax;
import com.google.inject.Inject;

public class PurchaseOrderLineTaxService {

	private static final Logger LOG = LoggerFactory.getLogger(PurchaseOrderLineTaxService.class); 
	
	@Inject
	private PurchaseOrderToolService purchaseOrderToolService;
	
	
	
	/**
	 * Créer les lignes de TVA de la commande. La création des lignes de TVA se
	 * basent sur les lignes de commande.
	 * 
	 * @param purchaseOrder
	 *            La commande.
	 *            
	 * @param purchaseOrderLineList
	 *            Les lignes de commandes.
	 *            
	 * @return La liste des lignes de TVA de la commande.
	 */
	public List<PurchaseOrderLineTax> createsPurchaseOrderLineTax(PurchaseOrder purchaseOrder, List<PurchaseOrderLine> purchaseOrderLineList) {
		
		List<PurchaseOrderLineTax> purchaseOrderLineTaxList = new ArrayList<PurchaseOrderLineTax>();
		Map<TaxLine, PurchaseOrderLineTax> map = new HashMap<TaxLine, PurchaseOrderLineTax>();
		
		if (purchaseOrderLineList != null && !purchaseOrderLineList.isEmpty()) {

			LOG.debug("Création des lignes de tva pour les lignes de commande.");
			
			for (PurchaseOrderLine purchaseOrderLine : purchaseOrderLineList) {
				
				TaxLine taxLine = purchaseOrderLine.getTaxLine();
				LOG.debug("TVA {}", taxLine);
				
				if (map.containsKey(taxLine)) {
				
					PurchaseOrderLineTax purchaseOrderLineVat = map.get(taxLine);
					
					purchaseOrderLineVat.setExTaxBase(purchaseOrderLineVat.getExTaxBase().add(purchaseOrderLine.getExTaxTotal()));
					
				}
				else {
					
					PurchaseOrderLineTax purchaseOrderLineTax = new PurchaseOrderLineTax();
					purchaseOrderLineTax.setPurchaseOrder(purchaseOrder);
					
					purchaseOrderLineTax.setExTaxBase(purchaseOrderLine.getExTaxTotal());
					
					purchaseOrderLineTax.setTaxLine(taxLine);
					map.put(taxLine, purchaseOrderLineTax);
					
				}
			}
		}
			
		for (PurchaseOrderLineTax purchaseOrderLineTax : map.values()) {
			
			// Dans la devise de la commande
			BigDecimal exTaxBase = purchaseOrderLineTax.getExTaxBase();
			BigDecimal taxTotal = purchaseOrderToolService.computeAmount(exTaxBase, purchaseOrderLineTax.getTaxLine().getValue());
			purchaseOrderLineTax.setTaxTotal(taxTotal);
			purchaseOrderLineTax.setInTaxTotal(exTaxBase.add(taxTotal));
			
			purchaseOrderLineTaxList.add(purchaseOrderLineTax);

			LOG.debug("Ligne de TVA : Total TVA => {}, Total HT => {}", new Object[] {purchaseOrderLineTax.getTaxTotal(), purchaseOrderLineTax.getInTaxTotal()});
			
		}

		return purchaseOrderLineTaxList;
	}

	
	
}
