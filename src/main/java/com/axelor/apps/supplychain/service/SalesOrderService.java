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

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.IPartner;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.UserInfo;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.supplychain.db.ILocation;
import com.axelor.apps.supplychain.db.IPurchaseOrder;
import com.axelor.apps.supplychain.db.Location;
import com.axelor.apps.supplychain.db.SalesOrder;
import com.axelor.apps.supplychain.db.SalesOrderLine;
import com.axelor.apps.supplychain.db.SalesOrderLineTax;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SalesOrderService {

	private static final Logger LOG = LoggerFactory.getLogger(SalesOrderService.class); 

	@Inject
	private SalesOrderLineService salesOrderLineService;

	@Inject
	private CurrencyService currencyService;

	@Inject
	private SalesOrderLineTaxService salesOrderLineTaxService;
	
	@Inject
	private SequenceService sequenceService;


	public SalesOrder _computeSalesOrderLineList(SalesOrder salesOrder) throws AxelorException  {

		if(salesOrder.getSalesOrderLineList() != null)  {
			for(SalesOrderLine salesOrderLine : salesOrder.getSalesOrderLineList())  {
				salesOrderLine.setExTaxTotal(salesOrderLineService.computeSalesOrderLine(salesOrderLine));
				salesOrderLine.setCompanyExTaxTotal(salesOrderLineService.getCompanyExTaxTotal(salesOrderLine.getExTaxTotal(), salesOrder));
			}
		}

		return salesOrder;
	}


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void computeSalesOrder(SalesOrder salesOrder) throws AxelorException  {

		this.initSalesOrderLineTaxList(salesOrder);

		this._computeSalesOrderLineList(salesOrder);

		this._populateSalesOrder(salesOrder);

		this._computeSalesOrder(salesOrder);

		salesOrder.save();
	}


	/**
	 * Peupler un devis.
	 * <p>
	 * Cette fonction permet de déterminer les tva d'un devis. 
	 * </p>
	 * 
	 * @param salesOrder
	 * 
	 * @throws AxelorException
	 */
	public void _populateSalesOrder(SalesOrder salesOrder) throws AxelorException {

		LOG.debug("Peupler un devis => lignes de devis: {} ", new Object[] { salesOrder.getSalesOrderLineList().size() });

		// create Tva lines
		salesOrder.getSalesOrderLineTaxList().addAll(salesOrderLineTaxService.createsSalesOrderLineTax(salesOrder, salesOrder.getSalesOrderLineList()));

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
		salesOrder.setTaxTotal(BigDecimal.ZERO);
		salesOrder.setInTaxTotal(BigDecimal.ZERO);

		for (SalesOrderLineTax salesOrderLineVat : salesOrder.getSalesOrderLineTaxList()) {

			// Dans la devise de la comptabilité du tiers
			salesOrder.setExTaxTotal(salesOrder.getExTaxTotal().add( salesOrderLineVat.getExTaxBase() ));
			salesOrder.setTaxTotal(salesOrder.getTaxTotal().add( salesOrderLineVat.getTaxTotal() ));
			salesOrder.setInTaxTotal(salesOrder.getInTaxTotal().add( salesOrderLineVat.getInTaxTotal() ));

		}

		salesOrder.setAmountRemainingToBeInvoiced(salesOrder.getInTaxTotal());

		LOG.debug("Montant de la facture: HTT = {},  HT = {}, Taxe = {}, TTC = {}",
				new Object[] { salesOrder.getExTaxTotal(), salesOrder.getTaxTotal(), salesOrder.getInTaxTotal() });

	}


	/**
	 * Permet de réinitialiser la liste des lignes de TVA
	 * @param salesOrder
	 * 			Un devis
	 */
	public void initSalesOrderLineTaxList(SalesOrder salesOrder) {

		if (salesOrder.getSalesOrderLineTaxList() == null) { salesOrder.setSalesOrderLineTaxList(new ArrayList<SalesOrderLineTax>()); }

		else { salesOrder.getSalesOrderLineTaxList().clear(); }

	}

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Partner validateCustomer(SalesOrder salesOrder)  {
		
		Partner clientPartner = Partner.find(salesOrder.getClientPartner().getId());
		clientPartner.setCustomerTypeSelect(IPartner.CUSTOMER_TYPE_SELECT_YES);
		
		return clientPartner.save();
	}
	
	
	public Location getLocation(Company company)  {
		
		return Location.filter("self.company = ?1 and self.isDefaultLocation = ?2 and self.typeSelect = ?3", 
				company, true, ILocation.INTERNAL).fetchOne();
	}
	
	
	public String getSequence(Company company) throws AxelorException  {
		String seq = sequenceService.getSequence(IAdministration.SALES_ORDER, company, false);
		if (seq == null)  {
			throw new AxelorException(String.format("La société %s n'a pas de séquence de configurée pour les devis clients",company.getName()),
							IException.CONFIGURATION_ERROR);
		}
		return seq;
	}
	

	public SalesOrder createSalesOrder(Project project, UserInfo buyerUserInfo, Company company, Partner contactPartner, Currency currency, 
			LocalDate deliveryDate, String internalReference, String externalReference, int invoicingTypeSelect, Location location, LocalDate orderDate, 
			PriceList priceList, Partner clientPartner) throws AxelorException  {
		
		LOG.debug("Création d'une commande fournisseur : Société = {},  Reference externe = {}, Client = {}",
				new Object[] { company.getName(), externalReference, clientPartner.getFullName() });
		
		SalesOrder salesOrder = new SalesOrder();
		salesOrder.setProject(project);
		salesOrder.setCompany(company);
		salesOrder.setContactPartner(contactPartner);
		salesOrder.setCurrency(currency);
		salesOrder.setExternalReference(externalReference);
		salesOrder.setInvoicingTypeSelect(invoicingTypeSelect);
		salesOrder.setLocation(location);
		salesOrder.setOrderDate(orderDate);
		salesOrder.setPriceList(priceList);
		salesOrder.setSalesOrderLineList(new ArrayList<SalesOrderLine>());
		
		salesOrder.setSalesOrderSeq(this.getSequence(company));
		salesOrder.setStatusSelect(IPurchaseOrder.STATUS_DRAFT);
		salesOrder.setClientPartner(clientPartner);
		
		return salesOrder;
	}
	
}



