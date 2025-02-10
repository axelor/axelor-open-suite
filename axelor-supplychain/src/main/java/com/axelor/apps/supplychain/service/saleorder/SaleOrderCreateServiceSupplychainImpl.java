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
package com.axelor.apps.supplychain.service.saleorder;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.TaxNumber;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.address.AddressService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderCreateServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLinePriceService;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineProductService;
import com.axelor.apps.stock.db.Incoterm;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.service.app.AppStockService;
import com.axelor.auth.db.User;
import com.axelor.team.db.Team;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaleOrderCreateServiceSupplychainImpl extends SaleOrderCreateServiceImpl
    implements SaleOrderCreateSupplychainService {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AccountConfigService accountConfigService;
  protected AppBaseService appBaseService;
  protected SaleOrderStockLocationService saleOrderStockLocationService;
  protected AppStockService appStockService;
  protected AddressService addressService;

  @Inject
  public SaleOrderCreateServiceSupplychainImpl(
      PartnerService partnerService,
      SaleOrderRepository saleOrderRepo,
      AppSaleService appSaleService,
      SaleOrderService saleOrderService,
      SaleOrderComputeService saleOrderComputeService,
      SaleOrderLineComputeService saleOrderLineComputeService,
      SaleOrderLineProductService saleOrderLineProductService,
      SaleOrderLinePriceService saleOrderLinePriceService,
      AccountConfigService accountConfigService,
      AppBaseService appBaseService,
      SaleOrderStockLocationService saleOrderStockLocationService,
      AppStockService appStockService,
      AddressService addressService) {
    super(
        partnerService,
        saleOrderRepo,
        appSaleService,
        saleOrderService,
        saleOrderComputeService,
        saleOrderLineComputeService,
        saleOrderLineProductService,
        saleOrderLinePriceService);
    this.accountConfigService = accountConfigService;
    this.appBaseService = appBaseService;
    this.saleOrderStockLocationService = saleOrderStockLocationService;
    this.appStockService = appStockService;
    this.addressService = addressService;
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

  @Override
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
      String internalNote,
      FiscalPosition fiscalPosition,
      TradingName tradingName,
      Incoterm incoterm,
      Partner invoicedPartner,
      Partner deliveredPartner)
      throws AxelorException {
    SaleOrder saleOrder =
        this.createSaleOrder(
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
            tradingName,
            incoterm,
            invoicedPartner,
            deliveredPartner);
    saleOrder.setInternalNote(internalNote);
    saleOrder.setTradingName(tradingName);
    return saleOrder;
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
      stockLocation = saleOrderStockLocationService.getStockLocation(clientPartner, company);
    }

    saleOrder.setStockLocation(stockLocation);

    saleOrder.setPaymentMode(clientPartner.getInPaymentMode());
    saleOrder.setPaymentCondition(clientPartner.getPaymentCondition());
    if (appStockService.getAppStock().getIsIncotermEnabled()) {
      saleOrder.setIncoterm(incoterm);
    }
    saleOrder.setInvoicedPartner(invoicedPartner);
    saleOrder.setDeliveredPartner(deliveredPartner);

    if (invoicedPartner != null) {
      saleOrder.setMainInvoicingAddress(partnerService.getInvoicingAddress(invoicedPartner));
      saleOrder.setMainInvoicingAddressStr(
          addressService.computeAddressStr(saleOrder.getMainInvoicingAddress()));
    }

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
}
