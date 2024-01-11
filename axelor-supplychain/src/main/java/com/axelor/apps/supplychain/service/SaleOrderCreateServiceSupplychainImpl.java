/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.TaxNumber;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.service.DMSService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderCreateServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderService;
import com.axelor.apps.stock.db.Incoterm;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.team.db.Team;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaleOrderCreateServiceSupplychainImpl extends SaleOrderCreateServiceImpl {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AccountConfigService accountConfigService;
  protected SaleOrderRepository saleOrderRepository;
  protected AppBaseService appBaseService;
  protected SaleOrderSupplychainService saleOrderSupplychainService;

  @Inject
  public SaleOrderCreateServiceSupplychainImpl(
      PartnerService partnerService,
      SaleOrderRepository saleOrderRepo,
      AppSaleService appSaleService,
      AppBaseService appBaseService,
      SaleOrderService saleOrderService,
      SaleOrderComputeService saleOrderComputeService,
      AccountConfigService accountConfigService,
      SaleOrderRepository saleOrderRepository,
      SaleOrderSupplychainService saleOrderSupplychainService,
      DMSService dmsService) {

    super(
        partnerService,
        saleOrderRepo,
        appSaleService,
        saleOrderService,
        saleOrderComputeService,
        dmsService);

    this.accountConfigService = accountConfigService;
    this.saleOrderRepository = saleOrderRepository;
    this.appBaseService = appBaseService;
    this.saleOrderSupplychainService = saleOrderSupplychainService;
  }

  @Override
  public SaleOrder createSaleOrder(
      User salespersonUser,
      Company company,
      Partner contactPartner,
      Currency currency,
      LocalDate estimatedShippingDate,
      String internalReference,
      String externalReference,
      PriceList priceList,
      Partner clientPartner,
      Team team,
      TaxNumber taxNumber,
      FiscalPosition fiscalPosition,
      TradingName tradingName)
      throws AxelorException {

    if (!appSaleService.isApp("supplychain")) {
      return super.createSaleOrder(
          salespersonUser,
          company,
          contactPartner,
          currency,
          estimatedShippingDate,
          internalReference,
          externalReference,
          priceList,
          clientPartner,
          team,
          taxNumber,
          fiscalPosition,
          tradingName);
    }
    return createSaleOrder(
        salespersonUser,
        company,
        contactPartner,
        currency,
        estimatedShippingDate,
        internalReference,
        externalReference,
        null,
        priceList,
        clientPartner,
        team,
        taxNumber,
        fiscalPosition,
        tradingName,
        null,
        null,
        null);
  }

  public SaleOrder createSaleOrder(
      User salespersonUser,
      Company company,
      Partner contactPartner,
      Currency currency,
      LocalDate estimatedShippingDate,
      String internalReference,
      String externalReference,
      StockLocation stockLocation,
      PriceList priceList,
      Partner clientPartner,
      Team team,
      TaxNumber taxNumber,
      FiscalPosition fiscalPosition)
      throws AxelorException {

    return createSaleOrder(
        salespersonUser,
        company,
        contactPartner,
        currency,
        estimatedShippingDate,
        internalReference,
        externalReference,
        stockLocation,
        priceList,
        clientPartner,
        team,
        taxNumber,
        fiscalPosition,
        null,
        null,
        null,
        null);
  }

  public SaleOrder createSaleOrder(
      User salespersonUser,
      Company company,
      Partner contactPartner,
      Currency currency,
      LocalDate estimatedShippingDate,
      String internalReference,
      String externalReference,
      StockLocation stockLocation,
      PriceList priceList,
      Partner clientPartner,
      Team team,
      TaxNumber taxNumber,
      FiscalPosition fiscalPosition,
      TradingName tradingName,
      Incoterm incoterm,
      Partner invoicedPartner,
      Partner deliveredPartner)
      throws AxelorException {

    logger.debug(
        "Creation of a sale order : Company = {},  External reference = {}, Customer = {}",
        company.getName(),
        externalReference,
        clientPartner.getFullName());

    SaleOrder saleOrder =
        super.createSaleOrder(
            salespersonUser,
            company,
            contactPartner,
            currency,
            estimatedShippingDate,
            internalReference,
            externalReference,
            priceList,
            clientPartner,
            team,
            taxNumber,
            fiscalPosition,
            tradingName);

    if (stockLocation == null) {
      stockLocation = saleOrderSupplychainService.getStockLocation(clientPartner, company);
    }

    saleOrder.setStockLocation(stockLocation);

    saleOrder.setPaymentMode(clientPartner.getInPaymentMode());
    saleOrder.setPaymentCondition(clientPartner.getPaymentCondition());
    saleOrder.setIncoterm(incoterm);
    saleOrder.setInvoicedPartner(invoicedPartner);
    saleOrder.setDeliveredPartner(deliveredPartner);

    if (saleOrder.getPaymentMode() == null) {
      saleOrder.setPaymentMode(
          this.accountConfigService.getAccountConfig(company).getInPaymentMode());
    }

    if (saleOrder.getPaymentCondition() == null) {
      saleOrder.setPaymentCondition(
          this.accountConfigService.getAccountConfig(company).getDefPaymentCondition());
    }

    saleOrder.setShipmentMode(clientPartner.getShipmentMode());
    saleOrder.setFreightCarrierMode(clientPartner.getFreightCarrierMode());

    return saleOrder;
  }

  @Transactional(rollbackOn = {Exception.class})
  public SaleOrder mergeSaleOrders(
      List<SaleOrder> saleOrderList,
      Currency currency,
      Partner clientPartner,
      Company company,
      StockLocation stockLocation,
      Partner contactPartner,
      PriceList priceList,
      Team team,
      TaxNumber taxNumber,
      FiscalPosition fiscalPosition,
      Incoterm incoterm,
      Partner invoicedPartner,
      Partner deliveredPartner)
      throws AxelorException {

    StringBuilder numSeq = new StringBuilder();
    StringBuilder externalRef = new StringBuilder();
    StringBuilder internalNote = new StringBuilder();
    for (SaleOrder saleOrderLocal : saleOrderList) {
      if (numSeq.length() > 0) {
        numSeq.append("-");
      }
      numSeq.append(saleOrderLocal.getSaleOrderSeq());

      if (externalRef.length() > 0) {
        externalRef.append("|");
      }
      if (saleOrderLocal.getExternalReference() != null) {
        externalRef.append(saleOrderLocal.getExternalReference());
      }
      if (internalNote.length() > 0) {
        internalNote.append("<br>");
      }
      if (saleOrderLocal.getInternalNote() != null) {
        internalNote.append(saleOrderLocal.getInternalNote());
      }
    }

    SaleOrder saleOrderMerged =
        this.createSaleOrder(
            AuthUtils.getUser(),
            company,
            contactPartner,
            currency,
            null,
            numSeq.toString(),
            externalRef.toString(),
            stockLocation,
            priceList,
            clientPartner,
            team,
            taxNumber,
            fiscalPosition,
            null,
            incoterm,
            invoicedPartner,
            deliveredPartner);

    saleOrderMerged.setInternalNote(internalNote.toString());

    super.attachToNewSaleOrder(saleOrderList, saleOrderMerged);

    saleOrderComputeService.computeSaleOrder(saleOrderMerged);

    saleOrderRepository.save(saleOrderMerged);

    dmsService.addLinkedDMSFiles(saleOrderList, saleOrderMerged);

    super.removeOldSaleOrders(saleOrderList);

    return saleOrderMerged;
  }
}
