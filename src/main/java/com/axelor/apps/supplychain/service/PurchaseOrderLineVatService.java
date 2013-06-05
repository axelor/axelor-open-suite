package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.VatLine;
import com.axelor.apps.supplychain.db.PurchaseOrder;
import com.axelor.apps.supplychain.db.PurchaseOrderLine;
import com.axelor.apps.supplychain.db.PurchaseOrderLineVat;
import com.google.inject.Inject;

public class PurchaseOrderLineVatService {

	private static final Logger LOG = LoggerFactory.getLogger(PurchaseOrderLineVatService.class); 
	
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
	public List<PurchaseOrderLineVat> createsPurchaseOrderLineVat(PurchaseOrder purchaseOrder, List<PurchaseOrderLine> purchaseOrderLineList) {
		
		List<PurchaseOrderLineVat> vatLines = new ArrayList<PurchaseOrderLineVat>();
		Map<VatLine, PurchaseOrderLineVat> map = new HashMap<VatLine, PurchaseOrderLineVat>();
		
		if (purchaseOrderLineList != null && !purchaseOrderLineList.isEmpty()) {

			LOG.debug("Création des lignes de tva pour les lignes de commande.");
			
			for (PurchaseOrderLine purchaseOrderLine : purchaseOrderLineList) {
				
				VatLine vatLine = purchaseOrderLine.getVatLine();
				LOG.debug("TVA {}", vatLine);
				
				if (map.containsKey(vatLine)) {
				
					PurchaseOrderLineVat purchaseOrderLineVat = map.get(vatLine);
					
					purchaseOrderLineVat.setExTaxBase(purchaseOrderLineVat.getExTaxBase().add(purchaseOrderLine.getExTaxTotal()));
					
				}
				else {
					
					PurchaseOrderLineVat purchaseOrderLineVat = new PurchaseOrderLineVat();
					purchaseOrderLineVat.setPurchaseOrder(purchaseOrder);
					
					purchaseOrderLineVat.setExTaxBase(purchaseOrderLine.getExTaxTotal());
					
					purchaseOrderLineVat.setVatLine(vatLine);
					map.put(vatLine, purchaseOrderLineVat);
					
				}
			}
		}
			
		for (PurchaseOrderLineVat vatLine : map.values()) {
			
			// Dans la devise de la commande
			BigDecimal vatExTaxBase = vatLine.getExTaxBase();
			BigDecimal vatTotal = purchaseOrderToolService.computeAmount(vatExTaxBase, vatLine.getVatLine().getValue());
			vatLine.setVatTotal(vatTotal);
			vatLine.setInTaxTotal(vatExTaxBase.add(vatTotal));
			
			vatLines.add(vatLine);

			LOG.debug("Ligne de TVA : Total TVA => {}, Total HT => {}", new Object[] {vatLine.getVatTotal(), vatLine.getInTaxTotal()});
			
		}

		return vatLines;
	}

	
	
}
