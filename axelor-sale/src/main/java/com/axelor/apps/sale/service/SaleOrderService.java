/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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

import org.joda.time.LocalDate;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.Team;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public interface SaleOrderService {


	public SaleOrder _computeSaleOrderLineList(SaleOrder saleOrder) throws AxelorException;


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public SaleOrder computeSaleOrder(SaleOrder saleOrder) throws AxelorException;


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
	public void _populateSaleOrder(SaleOrder saleOrder) throws AxelorException;


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
	public void _computeSaleOrder(SaleOrder saleOrder) throws AxelorException;


	/**
	 * Permet de réinitialiser la liste des lignes de TVA
	 * @param saleOrder
	 * 			Un devis
	 */
	public void initSaleOrderLineTaxList(SaleOrder saleOrder);


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Partner validateCustomer(SaleOrder saleOrder);


	public String getSequence(Company company) throws AxelorException;


	public SaleOrder createSaleOrder(User buyerUser, Company company, Partner contactPartner, Currency currency,
			LocalDate deliveryDate, String internalReference, String externalReference, LocalDate orderDate,
			PriceList priceList, Partner clientPartner, Team team) throws AxelorException;

	public SaleOrder createSaleOrder(Company company) throws AxelorException;

	public void cancelSaleOrder(SaleOrder saleOrder);

	public void finalizeSaleOrder(SaleOrder saleOrder) throws Exception;
	
	public void confirmSaleOrder(SaleOrder saleOrder) throws Exception;

	public void saveSaleOrderPDFAsAttachment(SaleOrder saleOrder) throws AxelorException;

	public String getLanguageForPrinting(SaleOrder saleOrder);
	
	public String getFileName(SaleOrder saleOrder);

	@Transactional
	public SaleOrder createTemplate(SaleOrder context);

	@Transactional
	public SaleOrder createSaleOrder(SaleOrder context);

	public SaleOrder computeEndOfValidityDate(SaleOrder saleOrder);
	
	public String getReportLink(SaleOrder saleOrder, String name, String language, String format) throws AxelorException;
}



