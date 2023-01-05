/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.TaxNumber;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderCreateServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
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
      SaleOrderSupplychainService saleOrderSupplychainService) {

    super(partnerService, saleOrderRepo, appSaleService, saleOrderService, saleOrderComputeService);

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
      LocalDate deliveryDate,
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
          deliveryDate,
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
        deliveryDate,
        internalReference,
        externalReference,
        null,
        priceList,
        clientPartner,
        team,
        taxNumber,
        fiscalPosition,
        tradingName);
  }

  public SaleOrder createSaleOrder(
      User salespersonUser,
      Company company,
      Partner contactPartner,
      Currency currency,
      LocalDate deliveryDate,
      String internalReference,
      String externalReference,
      StockLocation stockLocation,
      PriceList priceList,
      Partner clientPartner,
      Team team,
      TaxNumber taxNumber,
      FiscalPosition fiscalPosition,
      TradingName tradingName)
      throws AxelorException {

    logger.debug(
        "Création d'une commande fournisseur : Société = {},  Reference externe = {}, Client = {}",
        company.getName(),
        externalReference,
        clientPartner.getFullName());

    SaleOrder saleOrder =
        super.createSaleOrder(
            salespersonUser,
            company,
            contactPartner,
            currency,
            deliveryDate,
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
      FiscalPosition fiscalPosition)
      throws AxelorException {
    String numSeq = "";
    String externalRef = "";
    for (SaleOrder saleOrderLocal : saleOrderList) {
      if (!numSeq.isEmpty()) {
        numSeq += "-";
      }
      numSeq += saleOrderLocal.getSaleOrderSeq();

      if (!externalRef.isEmpty()) {
        externalRef += "|";
      }
      if (saleOrderLocal.getExternalReference() != null) {
        externalRef += saleOrderLocal.getExternalReference();
      }
    }

    SaleOrder saleOrderMerged =
        this.createSaleOrder(
            AuthUtils.getUser(),
            company,
            contactPartner,
            currency,
            null,
            numSeq,
            externalRef,
            stockLocation,
            priceList,
            clientPartner,
            team,
            taxNumber,
            fiscalPosition,
            null);
    super.attachToNewSaleOrder(saleOrderList, saleOrderMerged);

    saleOrderComputeService.computeSaleOrder(saleOrderMerged);

    saleOrderRepository.save(saleOrderMerged);

    super.removeOldSaleOrders(saleOrderList);

    return saleOrderMerged;
  }
}
