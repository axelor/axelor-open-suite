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
package com.axelor.apps.sale.service;

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
import com.axelor.apps.sale.db.ISaleOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.SaleOrderLineTax;
import com.axelor.apps.supplychain.db.Location;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SaleOrderServiceImpl implements SaleOrderService {

	private static final Logger LOG = LoggerFactory.getLogger(SaleOrderServiceImpl.class); 

	@Inject
	private SaleOrderLineService saleOrderLineService;

	@Inject
	private CurrencyService currencyService;

	@Inject
	private SaleOrderLineTaxService saleOrderLineTaxService;
	
	@Inject
	private SequenceService sequenceService;


	public SaleOrder _computeSaleOrderLineList(SaleOrder saleOrder) throws AxelorException  {

		if(saleOrder.getSaleOrderLineList() != null)  {
			for(SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList())  {
				saleOrderLine.setExTaxTotal(saleOrderLineService.computeSaleOrderLine(saleOrderLine));
				saleOrderLine.setCompanyExTaxTotal(saleOrderLineService.getCompanyExTaxTotal(saleOrderLine.getExTaxTotal(), saleOrder));
			}
		}

		return saleOrder;
	}


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void computeSaleOrder(SaleOrder saleOrder) throws AxelorException  {

		this.initSaleOrderLineTaxList(saleOrder);

		this._computeSaleOrderLineList(saleOrder);

		this._populateSaleOrder(saleOrder);

		this._computeSaleOrder(saleOrder);

		saleOrder.save();
	}


	/**
	 * Peupler un devis.
	 * <p>
	 * Cette fonction permet de déterminer les tva d'un devis. 
	 * </p>
	 * 
	 * @param saleOrder
	 * 
	 * @throws AxelorException
	 */
	public void _populateSaleOrder(SaleOrder saleOrder) throws AxelorException {

		LOG.debug("Peupler un devis => lignes de devis: {} ", new Object[] { saleOrder.getSaleOrderLineList().size() });

		// create Tva lines
		saleOrder.getSaleOrderLineTaxList().addAll(saleOrderLineTaxService.createsSaleOrderLineTax(saleOrder, saleOrder.getSaleOrderLineList()));

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
	public void _computeSaleOrder(SaleOrder saleOrder) throws AxelorException {

		saleOrder.setExTaxTotal(BigDecimal.ZERO);
		saleOrder.setTaxTotal(BigDecimal.ZERO);
		saleOrder.setInTaxTotal(BigDecimal.ZERO);

		for (SaleOrderLineTax saleOrderLineVat : saleOrder.getSaleOrderLineTaxList()) {

			// Dans la devise de la comptabilité du tiers
			saleOrder.setExTaxTotal(saleOrder.getExTaxTotal().add( saleOrderLineVat.getExTaxBase() ));
			saleOrder.setTaxTotal(saleOrder.getTaxTotal().add( saleOrderLineVat.getTaxTotal() ));
			saleOrder.setInTaxTotal(saleOrder.getInTaxTotal().add( saleOrderLineVat.getInTaxTotal() ));

		}

		saleOrder.setAmountRemainingToBeInvoiced(saleOrder.getInTaxTotal());

		LOG.debug("Montant de la facture: HTT = {},  HT = {}, Taxe = {}, TTC = {}",
				new Object[] { saleOrder.getExTaxTotal(), saleOrder.getTaxTotal(), saleOrder.getInTaxTotal() });

	}


	/**
	 * Permet de réinitialiser la liste des lignes de TVA
	 * @param saleOrder
	 * 			Un devis
	 */
	public void initSaleOrderLineTaxList(SaleOrder saleOrder) {

		if (saleOrder.getSaleOrderLineTaxList() == null) { saleOrder.setSaleOrderLineTaxList(new ArrayList<SaleOrderLineTax>()); }

		else { saleOrder.getSaleOrderLineTaxList().clear(); }

	}

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Partner validateCustomer(SaleOrder saleOrder)  {
		
		Partner clientPartner = Partner.find(saleOrder.getClientPartner().getId());
		clientPartner.setCustomerTypeSelect(IPartner.CUSTOMER_TYPE_SELECT_YES);
		
		return clientPartner.save();
	}
	
	
	
	public String getSequence(Company company) throws AxelorException  {
		String seq = sequenceService.getSequenceNumber(IAdministration.SALES_ORDER, company);
		if (seq == null)  {
			throw new AxelorException(String.format("La société %s n'a pas de séquence de configurée pour les devis clients",company.getName()),
							IException.CONFIGURATION_ERROR);
		}
		return seq;
	}
	

	public SaleOrder createSaleOrder(Project project, UserInfo buyerUserInfo, Company company, Partner contactPartner, Currency currency, 
			LocalDate deliveryDate, String internalReference, String externalReference, int invoicingTypeSelect, Location location, LocalDate orderDate, 
			PriceList priceList, Partner clientPartner) throws AxelorException  {
		
		LOG.debug("Création d'une commande fournisseur : Société = {},  Reference externe = {}, Client = {}",
				new Object[] { company.getName(), externalReference, clientPartner.getFullName() });
		
		SaleOrder saleOrder = new SaleOrder();
		saleOrder.setProject(project);
		saleOrder.setCompany(company);
		saleOrder.setContactPartner(contactPartner);
		saleOrder.setCurrency(currency);
		saleOrder.setExternalReference(externalReference);
		saleOrder.setInvoicingTypeSelect(invoicingTypeSelect);
		saleOrder.setLocation(location);
		saleOrder.setOrderDate(orderDate);
		saleOrder.setPriceList(priceList);
		saleOrder.setSaleOrderLineList(new ArrayList<SaleOrderLine>());
		
		saleOrder.setSaleOrderSeq(this.getSequence(company));
		saleOrder.setStatusSelect(ISaleOrder.STATUS_DRAFT);
		saleOrder.setClientPartner(clientPartner);
		
		return saleOrder;
	}
	
}



