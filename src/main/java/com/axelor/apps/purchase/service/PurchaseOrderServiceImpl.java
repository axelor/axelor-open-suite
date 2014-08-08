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
package com.axelor.apps.purchase.service;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.IPartner;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.auth.db.User;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.purchase.db.IPurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.PurchaseOrderLineTax;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PurchaseOrderServiceImpl implements PurchaseOrderService {

	private static final Logger LOG = LoggerFactory.getLogger(PurchaseOrderServiceImpl.class); 

	@Inject
	private PurchaseOrderLineService purchaseOrderLineService;
	
	@Inject
	private CurrencyService currencyService;
	
	@Inject
	private PurchaseOrderLineTaxService purchaseOrderLineVatService;
	
	@Inject
	private SequenceService sequenceService;
	
	@Override
	public PurchaseOrder _computePurchaseOrderLines(PurchaseOrder purchaseOrder) throws AxelorException  {
		
		if(purchaseOrder.getPurchaseOrderLineList() != null)  {
			for(PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList())  {
				purchaseOrderLine.setExTaxTotal(purchaseOrderLineService.computePurchaseOrderLine(purchaseOrderLine));
				purchaseOrderLine.setCompanyExTaxTotal(purchaseOrderLineService.getCompanyExTaxTotal(purchaseOrderLine.getExTaxTotal(), purchaseOrder));
			}
		}
		
		return purchaseOrder;
	}
	
	@Override
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
	@Override
	public void _populatePurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException {
		
		LOG.debug("Peupler une facture => lignes de devis: {} ", new Object[] { purchaseOrder.getPurchaseOrderLineList().size() });
		
		// create Tva lines
		purchaseOrder.getPurchaseOrderLineTaxList().addAll(purchaseOrderLineVatService.createsPurchaseOrderLineTax(purchaseOrder, purchaseOrder.getPurchaseOrderLineList()));
		
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
	@Override
	public void _computePurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException {
		
		purchaseOrder.setExTaxTotal(BigDecimal.ZERO);
		purchaseOrder.setTaxTotal(BigDecimal.ZERO);
		purchaseOrder.setInTaxTotal(BigDecimal.ZERO);
		
		for (PurchaseOrderLineTax purchaseOrderLineVat : purchaseOrder.getPurchaseOrderLineTaxList()) {
			
			// Dans la devise de la comptabilité du tiers
			purchaseOrder.setExTaxTotal(purchaseOrder.getExTaxTotal().add( purchaseOrderLineVat.getExTaxBase() ));
			purchaseOrder.setTaxTotal(purchaseOrder.getTaxTotal().add( purchaseOrderLineVat.getTaxTotal() ));
			purchaseOrder.setInTaxTotal(purchaseOrder.getInTaxTotal().add( purchaseOrderLineVat.getInTaxTotal() ));
			
		}
		
		LOG.debug("Montant de la facture: HTT = {},  HT = {}, TVA = {}, TTC = {}",
			new Object[] { purchaseOrder.getExTaxTotal(), purchaseOrder.getTaxTotal(), purchaseOrder.getInTaxTotal() });
		
	}

	
	/**
	 * Permet de réinitialiser la liste des lignes de TVA
	 * @param purchaseOrder
	 * 			Une commande.
	 */
	@Override
	public void initPurchaseOrderLineVats(PurchaseOrder purchaseOrder) {
		
		if (purchaseOrder.getPurchaseOrderLineTaxList() == null) { purchaseOrder.setPurchaseOrderLineTaxList(new ArrayList<PurchaseOrderLineTax>()); }
		
		else { purchaseOrder.getPurchaseOrderLineTaxList().clear(); }
		
	}
	
	
	@Override
	public PurchaseOrder createPurchaseOrder(User buyerUser, Company company, Partner contactPartner, Currency currency, 
			LocalDate deliveryDate, String internalReference, String externalReference, int invoicingTypeSelect, LocalDate orderDate, 
			PriceList priceList, Partner supplierPartner) throws AxelorException  {
		
		LOG.debug("Création d'une commande fournisseur : Société = {},  Reference externe = {}, Fournisseur = {}",
				new Object[] { company.getName(), externalReference, supplierPartner.getFullName() });
		
		PurchaseOrder purchaseOrder = new PurchaseOrder();
		purchaseOrder.setBuyerUser(buyerUser);
		purchaseOrder.setCompany(company);
		purchaseOrder.setContactPartner(contactPartner);
		purchaseOrder.setCurrency(currency);
		purchaseOrder.setDeliveryDate(deliveryDate);
		purchaseOrder.setInternalReference(internalReference);
		purchaseOrder.setExternalReference(externalReference);
		purchaseOrder.setOrderDate(orderDate);
		purchaseOrder.setPriceList(priceList);
		purchaseOrder.setPurchaseOrderLineList(new ArrayList<PurchaseOrderLine>());
		
		purchaseOrder.setPurchaseOrderSeq(this.getSequence(company));
		purchaseOrder.setStatusSelect(IPurchaseOrder.STATUS_DRAFT);
		purchaseOrder.setSupplierPartner(supplierPartner);
		
		return purchaseOrder;
	}
	
	@Override
	public String getSequence(Company company) throws AxelorException  {
		String seq = sequenceService.getSequenceNumber(IAdministration.PURCHASE_ORDER, company);
		if (seq == null)  {
			throw new AxelorException(String.format("La société %s n'a pas de séquence de configurée pour les commandes fournisseur", company.getName()),
							IException.CONFIGURATION_ERROR);
		}
		return seq;
	}
	
	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Partner validateSupplier(PurchaseOrder purchaseOrder)  {
		
		Partner supplierPartner = Partner.find(purchaseOrder.getSupplierPartner().getId());
		supplierPartner.setSupplierTypeSelect(IPartner.SUPPLIER_TYPE_SELECT_APPROVED);
		
		return supplierPartner.save();
	}
	
	
	
	
	
}
