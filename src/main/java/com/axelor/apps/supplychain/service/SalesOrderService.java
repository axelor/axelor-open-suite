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
import java.util.List;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IProduct;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.supplychain.service.StockMoveService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.organisation.db.PlanningLine;
import com.axelor.apps.organisation.db.Task;
import com.axelor.apps.supplychain.db.ILocation;
import com.axelor.apps.supplychain.db.ISalesOrder;
import com.axelor.apps.supplychain.db.Location;
import com.axelor.apps.supplychain.db.SalesOrder;
import com.axelor.apps.supplychain.db.SalesOrderLine;
import com.axelor.apps.supplychain.db.SalesOrderLineVat;
import com.axelor.apps.supplychain.db.SalesOrderSubLine;
import com.axelor.apps.supplychain.db.StockMove;
import com.axelor.apps.supplychain.db.StockMoveLine;
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
	private SalesOrderLineVatService salesOrderLineVatService;

	@Inject
	private StockMoveService stockMoveService;
	
	@Inject
	private StockMoveLineService stockMoveLineService;

	public SalesOrder _computeSalesOrderLines(SalesOrder salesOrder) throws AxelorException  {

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

		this.initSalesOrderLineVats(salesOrder);

		this._computeSalesOrderLines(salesOrder);

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
		salesOrder.getSalesOrderLineVatList().addAll(salesOrderLineVatService.createsSalesOrderLineVat(salesOrder, salesOrder.getSalesOrderLineList()));

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
		salesOrder.setVatTotal(BigDecimal.ZERO);
		salesOrder.setInTaxTotal(BigDecimal.ZERO);

		for (SalesOrderLineVat salesOrderLineVat : salesOrder.getSalesOrderLineVatList()) {

			// Dans la devise de la comptabilité du tiers
			salesOrder.setExTaxTotal(salesOrder.getExTaxTotal().add( salesOrderLineVat.getExTaxBase() ));
			salesOrder.setVatTotal(salesOrder.getVatTotal().add( salesOrderLineVat.getVatTotal() ));
			salesOrder.setInTaxTotal(salesOrder.getInTaxTotal().add( salesOrderLineVat.getInTaxTotal() ));

		}

		salesOrder.setAmountRemainingToBeInvoiced(salesOrder.getInTaxTotal());

		LOG.debug("Montant de la facture: HTT = {},  HT = {}, TVA = {}, TTC = {}",
				new Object[] { salesOrder.getExTaxTotal(), salesOrder.getVatTotal(), salesOrder.getInTaxTotal() });

	}


	/**
	 * Permet de réinitialiser la liste des lignes de TVA
	 * @param salesOrder
	 * 			Un devis
	 */
	public void initSalesOrderLineVats(SalesOrder salesOrder) {

		if (salesOrder.getSalesOrderLineVatList() == null) { salesOrder.setSalesOrderLineVatList(new ArrayList<SalesOrderLineVat>()); }

		else { salesOrder.getSalesOrderLineVatList().clear(); }

	}


	/**
	 * Méthode permettant de créer un StockMove à partir d'un SalesOrder.
	 * @param salesOrder l'objet salesOrder
	 * @throws AxelorException Aucune séquence de StockMove (Livraison) n'a été configurée
	 */
	public void createStocksMovesFromSalesOrder(SalesOrder salesOrder) throws AxelorException {
		
		if(salesOrder.getSalesOrderLineList() != null && salesOrder.getCompany() != null) {
			
			Company company = salesOrder.getCompany();
			
			Location toLocation = Location.all().filter("self.isDefaultLocation = true and self.company = ?1 and self.typeSelect = ?2", company, ILocation.EXTERNAL).fetchOne();
			
			if(toLocation == null)  {
				toLocation = company.getCustomerVirtualLocation();
			}
			if(toLocation == null)  {
				throw new AxelorException(String.format("%s Veuillez configurer un entrepot virtuel client pour la société %s ",
						GeneralService.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
			}
			
			StockMove stockMove = stockMoveService.createStockMove(salesOrder.getDeliveryAddress(), company, salesOrder.getClientPartner(), salesOrder.getLocation(), toLocation);
			stockMove.setStockMoveLineList(new ArrayList<StockMoveLine>());
			
			for(SalesOrderLine salesOrderLine: salesOrder.getSalesOrderLineList()) {
				
				Product product = salesOrderLine.getProduct();
				// Check if the company field 'hasOutSmForStorableProduct' = true and productTypeSelect = 'storable' or 'hasOutSmForNonStorableProduct' = true and productTypeSelect = 'service' or productTypeSelect = 'other'
				if(product != null && ((company.getHasOutSmForStorableProduct() && product.getProductTypeSelect().equals(IProduct.STORABLE)) || (company.getHasOutSmForNonStorableProduct() && !product.getProductTypeSelect().equals(IProduct.STORABLE)))) {
					
					StockMoveLine stockMoveLine = stockMoveLineService.createStockMoveLine(product, salesOrderLine.getQty(), salesOrderLine.getUnit(), salesOrderLine.getPrice(), stockMove, salesOrderLine.getProductVariant(), 1);
					if(stockMoveLine != null) {
						stockMove.getStockMoveLineList().add(stockMoveLine);
					}
				}	
			}
			if(stockMove.getStockMoveLineList() != null && !stockMove.getStockMoveLineList().isEmpty()){
				stockMoveService.validate(stockMove);
			}
		}
	}
}

