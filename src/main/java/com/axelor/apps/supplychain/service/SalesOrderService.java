/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
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
 * Software distributed under the License is distributed on an “AS IS”
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
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.supplychain.db.SalesOrder;
import com.axelor.apps.supplychain.db.SalesOrderLine;
import com.axelor.apps.supplychain.db.SalesOrderLineTax;
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
	private SalesOrderLineTaxService salesOrderLineTaxService;


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

}

