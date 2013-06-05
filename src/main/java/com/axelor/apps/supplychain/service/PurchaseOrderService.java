package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.supplychain.db.PurchaseOrder;
import com.axelor.apps.supplychain.db.PurchaseOrderLineVat;
import com.axelor.apps.supplychain.db.PurchaseOrderLine;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PurchaseOrderService {

	private static final Logger LOG = LoggerFactory.getLogger(PurchaseOrderService.class); 

	@Inject
	private PurchaseOrderLineService purchaseOrderLineService;
	
	@Inject
	private CurrencyService currencyService;
	
	@Inject
	private PurchaseOrderLineVatService purchaseOrderLineVatService;
	
	public PurchaseOrder _computePurchaseOrderLines(PurchaseOrder purchaseOrder)  {
		
		if(purchaseOrder.getPurchaseOrderLineList() != null)  {
			for(PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList())  {
				purchaseOrderLine.setExTaxTotal(purchaseOrderLineService.computePurchaseOrderLine(purchaseOrderLine));
			}
		}
		
		return purchaseOrder;
	}
	

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void computePurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException  {
		
		this.initPurchaseOrderLineVats(purchaseOrder);
		
		this._computePurchaseOrderLines(purchaseOrder);
		
		this._populatePurchaseOrder(purchaseOrder);
		
		this._computePurchaseOrder(purchaseOrder);
		
		purchaseOrder.save();
	}
	
	/**
	 * Peupler une commande.
	 * <p>
	 * Cette fonction permet de déterminer les tva d'une commande à partir des lignes de factures passées en paramètres. 
	 * </p>
	 * 
	 * @param purchaseOrder
	 * 
	 * @throws AxelorException
	 */
	public void _populatePurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException {
		
		LOG.debug("Peupler une facture => lignes de devis: {} ", new Object[] { purchaseOrder.getPurchaseOrderLineList().size() });
		
		// create Tva lines
		purchaseOrder.getPurchaseOrderLineVatList().addAll(purchaseOrderLineVatService.createsPurchaseOrderLineVat(purchaseOrder, purchaseOrder.getPurchaseOrderLineList()));
		
	}
	
	/**
	 * Calculer le montant d'une commande.
	 * <p> 
	 * Le calcul est basé sur les lignes de TVA préalablement créées.
	 * </p>
	 * 
	 * @param purchaseOrder
	 * @throws AxelorException 
	 */
	public void _computePurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException {
		
		purchaseOrder.setExTaxTotal(BigDecimal.ZERO);
		purchaseOrder.setVatTotal(BigDecimal.ZERO);
		purchaseOrder.setInTaxTotal(BigDecimal.ZERO);
		
		for (PurchaseOrderLineVat purchaseOrderLineVat : purchaseOrder.getPurchaseOrderLineVatList()) {
			
			// Dans la devise de la comptabilité du tiers
			purchaseOrder.setExTaxTotal(purchaseOrder.getExTaxTotal().add( purchaseOrderLineVat.getExTaxBase() ));
			purchaseOrder.setVatTotal(purchaseOrder.getVatTotal().add( purchaseOrderLineVat.getVatTotal() ));
			purchaseOrder.setInTaxTotal(purchaseOrder.getInTaxTotal().add( purchaseOrderLineVat.getInTaxTotal() ));
			
		}
		
		purchaseOrder.setAmountRemainingToBeInvoiced(purchaseOrder.getInTaxTotal());
		
		LOG.debug("Montant de la facture: HTT = {},  HT = {}, TVA = {}, TTC = {}",
			new Object[] { purchaseOrder.getExTaxTotal(), purchaseOrder.getVatTotal(), purchaseOrder.getInTaxTotal() });
		
	}

	
	/**
	 * Permet de réinitialiser la liste des lignes de TVA
	 * @param purchaseOrder
	 * 			Une commande.
	 */
	public void initPurchaseOrderLineVats(PurchaseOrder purchaseOrder) {
		
		if (purchaseOrder.getPurchaseOrderLineVatList() == null) { purchaseOrder.setPurchaseOrderLineVatList(new ArrayList<PurchaseOrderLineVat>()); }
		
		else { purchaseOrder.getPurchaseOrderLineVatList().clear(); }
		
	}
}
