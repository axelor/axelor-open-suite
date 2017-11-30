/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.time.LocalDate;

import com.axelor.apps.purchase.db.PurchaseConfig;
import com.axelor.apps.stock.service.LocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseConfigRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SaleOrderPurchaseServiceImpl implements SaleOrderPurchaseService  {

	private static final Logger LOG = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@Inject
	private PurchaseOrderServiceSupplychainImpl purchaseOrderServiceSupplychainImpl;

	@Inject
	protected PurchaseOrderLineServiceSupplychainImpl purchaseOrderLineServiceSupplychainImpl;

	@Inject
	protected AppBaseService appBaseService;

	protected LocalDate today;

	protected User user;

	@Inject
	public SaleOrderPurchaseServiceImpl() {

		this.today = Beans.get(AppBaseService.class).getTodayDate();
		this.user = AuthUtils.getUser();
	}


	@Override
	public void createPurchaseOrders(SaleOrder saleOrder) throws AxelorException  {

		Map<Partner,List<SaleOrderLine>> saleOrderLinesBySupplierPartner = this.splitBySupplierPartner(saleOrder.getSaleOrderLineList());

		for(Partner supplierPartner : saleOrderLinesBySupplierPartner.keySet())  {

			this.createPurchaseOrder(supplierPartner, saleOrderLinesBySupplierPartner.get(supplierPartner), saleOrder);

		}

	}


	@Override
	public Map<Partner,List<SaleOrderLine>> splitBySupplierPartner(List<SaleOrderLine> saleOrderLineList) throws AxelorException  {

		Map<Partner,List<SaleOrderLine>> saleOrderLinesBySupplierPartner = new HashMap<>();

		for(SaleOrderLine saleOrderLine : saleOrderLineList)  {

			if(saleOrderLine.getSaleSupplySelect() == ProductRepository.SALE_SUPPLY_PURCHASE)  {

				Partner supplierPartner = saleOrderLine.getSupplierPartner();

				if (supplierPartner == null) {
					throw new AxelorException(saleOrderLine, IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.SO_PURCHASE_1), saleOrderLine.getProductName());
				}

				if(!saleOrderLinesBySupplierPartner.containsKey(supplierPartner))  {
					saleOrderLinesBySupplierPartner.put(supplierPartner, new ArrayList<SaleOrderLine>());
				}

				saleOrderLinesBySupplierPartner.get(supplierPartner).add(saleOrderLine);
			}

		}

		return saleOrderLinesBySupplierPartner;
	}


	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public PurchaseOrder createPurchaseOrder(Partner supplierPartner, List<SaleOrderLine> saleOrderLineList, SaleOrder saleOrder) throws AxelorException  {

		LOG.debug("Création d'une commande fournisseur pour le devis client : {}",
				new Object[] { saleOrder.getSaleOrderSeq() });

		PurchaseOrder purchaseOrder = purchaseOrderServiceSupplychainImpl.createPurchaseOrder(
				user,
				saleOrder.getCompany(),
				null,
				supplierPartner.getCurrency(),
				null,
				saleOrder.getSaleOrderSeq(),
				saleOrder.getExternalReference(),
				Beans.get(LocationService.class).getLocation(saleOrder.getCompany()),
				today,
				supplierPartner.getPurchasePriceList(),
				supplierPartner);

		PurchaseConfig purchaseConfig = Beans.get(PurchaseConfigRepository.class).all().filter("self.company = ?1", saleOrder.getCompany()).fetchOne();
		if (purchaseConfig == null) {
			throw new AxelorException(saleOrder.getCompany(), IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.PURCHASE_ORDER_MISSING_CONFIG), saleOrder.getCompany().getName());
		}
		Integer atiChoice = purchaseConfig.getPurchaseOrderInAtiSelect();
		if(atiChoice == 2 || atiChoice == 4){
			purchaseOrder.setInAti(true);
		}
		else{
			purchaseOrder.setInAti(false);
		}

		for(SaleOrderLine saleOrderLine : saleOrderLineList)  {
			purchaseOrder.addPurchaseOrderLineListItem(purchaseOrderLineServiceSupplychainImpl.createPurchaseOrderLine(purchaseOrder, saleOrderLine));
		}

		purchaseOrderServiceSupplychainImpl.computePurchaseOrder(purchaseOrder);

		Beans.get(PurchaseOrderRepository.class).save(purchaseOrder);

		return purchaseOrder;
	}
}


