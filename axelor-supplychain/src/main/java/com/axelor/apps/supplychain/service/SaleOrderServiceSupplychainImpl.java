/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
import java.math.BigDecimal;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.General;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.Team;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.SaleOrderLineService;
import com.axelor.apps.sale.service.SaleOrderLineTaxService;
import com.axelor.apps.sale.service.SaleOrderServiceImpl;
import com.axelor.apps.stock.db.Location;
import com.axelor.apps.supplychain.db.Timetable;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SaleOrderServiceSupplychainImpl extends SaleOrderServiceImpl {
	
	private final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );
	
	protected SaleOrderStockService saleOrderStockService;
	protected SaleOrderPurchaseService saleOrderPurchaseService;
	protected General general;
	protected AccountConfigService accountConfigService;


	@Inject
	public SaleOrderServiceSupplychainImpl(SaleOrderLineService saleOrderLineService, SaleOrderLineTaxService saleOrderLineTaxService, 	
			SequenceService sequenceService, PartnerService partnerService, PartnerRepository partnerRepo, SaleOrderRepository saleOrderRepo,
			GeneralService generalService, UserService userService, SaleOrderStockService saleOrderStockService, 
			SaleOrderPurchaseService saleOrderPurchaseService, AccountConfigService accountConfigService) {
		
		super(saleOrderLineService, saleOrderLineTaxService, sequenceService,
				partnerService, partnerRepo, saleOrderRepo, generalService, userService);
		
		this.saleOrderStockService = saleOrderStockService;
		this.saleOrderPurchaseService = saleOrderPurchaseService;
		this.general = generalService.getGeneral();
		this.accountConfigService = accountConfigService;
		
	}
	
	
	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void confirmSaleOrder(SaleOrder saleOrder) throws Exception  {

		super.confirmSaleOrder(saleOrder);
		
		if(general.getPurchaseOrderGenerationAuto())  {
			saleOrderPurchaseService.createPurchaseOrders(saleOrder);
		}
		if(general.getCustomerStockMoveGenerationAuto())  {
			saleOrderStockService.createStocksMovesFromSaleOrder(saleOrder);
		}
		
	}
	
	
	public SaleOrder createSaleOrder(User buyerUser, Company company, Partner contactPartner, Currency currency,
			LocalDate deliveryDate, String internalReference, String externalReference, Location location, LocalDate orderDate,
			PriceList priceList, Partner clientPartner, Team team) throws AxelorException  {

		logger.debug("Création d'une commande fournisseur : Société = {},  Reference externe = {}, Client = {}",
				new Object[] { company.getName(), externalReference, clientPartner.getFullName() });

		SaleOrder saleOrder = super.createSaleOrder(buyerUser, company, contactPartner, currency, deliveryDate, internalReference,
				externalReference, orderDate, priceList, clientPartner, team);

		if(location == null)  {
			location = saleOrderStockService.getLocation(company);
		}
		
		saleOrder.setLocation(location);

		saleOrder.setPaymentMode(clientPartner.getInPaymentMode());
		saleOrder.setPaymentCondition(clientPartner.getPaymentCondition());
		
		if (saleOrder.getPaymentMode() == null) {
			saleOrder.setPaymentMode(
					this.accountConfigService
					.getAccountConfig(company)
					.getInPaymentMode()
				);
		}

		if (saleOrder.getPaymentCondition() == null) {
			saleOrder.setPaymentCondition(
					this.accountConfigService
					.getAccountConfig(company)
					.getDefPaymentCondition()
				);
		}
		
		
		return saleOrder;
	}
	
	public SaleOrder getClientInformations(SaleOrder saleOrder){
		Partner client = saleOrder.getClientPartner();
		PartnerService partnerService = Beans.get(PartnerService.class);
		if(client != null){
			saleOrder.setPaymentCondition(client.getPaymentCondition());
			saleOrder.setPaymentMode(client.getInPaymentMode());
			saleOrder.setMainInvoicingAddress(partnerService.getInvoicingAddress(client));
			saleOrder.setDeliveryAddress(partnerService.getDeliveryAddress(client));
			saleOrder.setPriceList(client.getSalePriceList());
		}
		return saleOrder;
	}

	public void updateAmountToBeSpreadOverTheTimetable(SaleOrder saleOrder) {
		List<Timetable> timetableList = saleOrder.getTimetableList();
		BigDecimal totalHT = saleOrder.getExTaxTotal();
		BigDecimal sumTimetableAmount = BigDecimal.ZERO;
		if (timetableList != null)
		for (Timetable timetable : timetableList) {
			sumTimetableAmount = sumTimetableAmount.add(timetable.getAmount().multiply(timetable.getQty()));
		}
		saleOrder.setAmountToBeSpreadOverTheTimetable(totalHT.subtract(sumTimetableAmount));
	}

	@Transactional
	protected void updateCustomerCreditLines(SaleOrder saleOrder) throws Exception {
		Partner partner = saleOrder.getClientPartner();
		if (partner != null) {
			Beans.get(CustomerCreditLineService.class).updateLinesFromOrder(partner, saleOrder);
		}
	}

	@Override
	public void finalizeSaleOrder(SaleOrder saleOrder) throws Exception {
		updateCustomerCreditLines(saleOrder);
		_finalizeSaleOrder(saleOrder);
	}

}
