/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderCreateServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
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

  @Inject
  public SaleOrderCreateServiceSupplychainImpl(
      PartnerService partnerService,
      SaleOrderRepository saleOrderRepo,
      AppSaleService appSaleService,
      SaleOrderService saleOrderService,
      SaleOrderComputeService saleOrderComputeService,
      AccountConfigService accountConfigService,
      SaleOrderRepository saleOrderRepository) {

    super(partnerService, saleOrderRepo, appSaleService, saleOrderService, saleOrderComputeService);

    this.accountConfigService = accountConfigService;
    this.saleOrderRepository = saleOrderRepository;
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
      LocalDate orderDate,
      PriceList priceList,
      Partner customerPartner,
      Team team)
      throws AxelorException {
    return createSaleOrder(
        salespersonUser,
        company,
        contactPartner,
        currency,
        deliveryDate,
        internalReference,
        externalReference,
        null,
        orderDate,
        priceList,
        customerPartner,
        team);
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
      LocalDate orderDate,
      PriceList priceList,
      Partner customerPartner,
      Team team)
      throws AxelorException {

    logger.debug(
        "Création d'une commande fournisseur : Société = {},  Reference externe = {}, Client = {}",
        company.getName(),
        externalReference,
        customerPartner.getFullName());

    SaleOrder saleOrder =
        super.createSaleOrder(
            salespersonUser,
            company,
            contactPartner,
            currency,
            deliveryDate,
            internalReference,
            externalReference,
            orderDate,
            priceList,
            customerPartner,
            team);

    if (stockLocation == null) {
      stockLocation = Beans.get(StockLocationService.class).getPickupDefaultStockLocation(company);
    }

    saleOrder.setStockLocation(stockLocation);

    saleOrder.setPaymentMode(customerPartner.getInPaymentMode());
    saleOrder.setPaymentCondition(customerPartner.getPaymentCondition());

    if (saleOrder.getPaymentMode() == null) {
      saleOrder.setPaymentMode(
          this.accountConfigService.getAccountConfig(company).getInPaymentMode());
    }

    if (saleOrder.getPaymentCondition() == null) {
      saleOrder.setPaymentCondition(
          this.accountConfigService.getAccountConfig(company).getDefPaymentCondition());
    }

    saleOrder.setShipmentMode(customerPartner.getShipmentMode());
    saleOrder.setFreightCarrierMode(customerPartner.getFreightCarrierMode());

    return saleOrder;
  }

  @Transactional(rollbackOn = {Exception.class})
  public SaleOrder mergeSaleOrders(
      List<SaleOrder> saleOrderList,
      Currency currency,
      Partner customerPartner,
      Company company,
      StockLocation stockLocation,
      Partner contactPartner,
      PriceList priceList,
      Team team)
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
            LocalDate.now(),
            priceList,
            customerPartner,
            team);

    super.attachToNewSaleOrder(saleOrderList, saleOrderMerged);

    saleOrderComputeService.computeSaleOrder(saleOrderMerged);

    saleOrderRepository.save(saleOrderMerged);

    super.removeOldSaleOrders(saleOrderList);

    return saleOrderMerged;
  }
}
