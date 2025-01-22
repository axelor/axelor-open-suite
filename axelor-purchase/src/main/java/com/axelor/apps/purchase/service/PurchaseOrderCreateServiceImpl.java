/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.purchase.service;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.service.TradingNameService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.config.PurchaseConfigService;
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PurchaseOrderCreateServiceImpl implements PurchaseOrderCreateService {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected PurchaseConfigService purchaseConfigService;
  protected final PurchaseOrderTypeSelectService purchaseOrderTypeSelectService;

  @Inject
  public PurchaseOrderCreateServiceImpl(
      PurchaseConfigService purchaseConfigService,
      PurchaseOrderTypeSelectService purchaseOrderTypeSelectService) {
    this.purchaseConfigService = purchaseConfigService;
    this.purchaseOrderTypeSelectService = purchaseOrderTypeSelectService;
  }

  @Override
  public PurchaseOrder createPurchaseOrder(
      User buyerUser,
      Company company,
      Partner contactPartner,
      Currency currency,
      LocalDate deliveryDate,
      String internalReference,
      String externalReference,
      LocalDate orderDate,
      PriceList priceList,
      Partner supplierPartner,
      TradingName tradingName,
      FiscalPosition fiscalPosition)
      throws AxelorException {
    PurchaseOrder purchaseOrder =
        this.createPurchaseOrder(
            buyerUser,
            company,
            contactPartner,
            currency,
            deliveryDate,
            internalReference,
            externalReference,
            orderDate,
            priceList,
            supplierPartner,
            tradingName);
    purchaseOrder.setFiscalPosition(fiscalPosition);
    return purchaseOrder;
  }

  @Override
  public PurchaseOrder createPurchaseOrder(
      User buyerUser,
      Company company,
      Partner contactPartner,
      Currency currency,
      LocalDate deliveryDate,
      String internalReference,
      String externalReference,
      LocalDate orderDate,
      PriceList priceList,
      Partner supplierPartner,
      TradingName tradingName)
      throws AxelorException {

    logger.debug(
        "Creation of a purchase order: Company = {},  External reference = {}, Supplier partner = {}",
        company.getName(),
        externalReference,
        supplierPartner.getFullName());

    PurchaseOrder purchaseOrder = new PurchaseOrder();
    purchaseOrder.setBuyerUser(buyerUser);
    purchaseOrder.setCompany(company);
    purchaseOrder.setContactPartner(contactPartner);
    purchaseOrder.setCurrency(currency);
    purchaseOrder.setEstimatedReceiptDate(deliveryDate);
    purchaseOrder.setInternalReference(internalReference);
    purchaseOrder.setExternalReference(externalReference);
    purchaseOrder.setOrderDate(orderDate);
    purchaseOrder.setPriceList(priceList);
    purchaseOrder.setTradingName(tradingName);
    purchaseOrder.setPurchaseOrderLineList(new ArrayList<>());

    purchaseOrder.setPrintingSettings(
        Beans.get(TradingNameService.class).getDefaultPrintingSettings(null, company));

    purchaseOrder.setStatusSelect(PurchaseOrderRepository.STATUS_DRAFT);
    purchaseOrder.setSupplierPartner(supplierPartner);
    purchaseOrderTypeSelectService.setTypeSelect(purchaseOrder);
    purchaseOrder.setFiscalPosition(supplierPartner.getFiscalPosition());
    purchaseOrder.setDisplayPriceOnQuotationRequest(
        purchaseConfigService.getPurchaseConfig(company).getDisplayPriceOnQuotationRequest());
    return purchaseOrder;
  }
}
