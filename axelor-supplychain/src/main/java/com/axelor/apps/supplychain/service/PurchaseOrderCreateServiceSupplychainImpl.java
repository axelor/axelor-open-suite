package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.service.PurchaseOrderCreateServiceImpl;
import com.axelor.apps.purchase.service.config.PurchaseConfigService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.auth.db.User;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PurchaseOrderCreateServiceSupplychainImpl extends PurchaseOrderCreateServiceImpl
    implements PurchaseOrderCreateSupplychainService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AccountConfigService accountConfigService;

  @Inject
  public PurchaseOrderCreateServiceSupplychainImpl(
      SequenceService sequenceService,
      PurchaseConfigService purchaseConfigService,
      AccountConfigService accountConfigService) {
    super(sequenceService, purchaseConfigService);
    this.accountConfigService = accountConfigService;
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
      StockLocation stockLocation,
      LocalDate orderDate,
      PriceList priceList,
      Partner supplierPartner,
      TradingName tradingName)
      throws AxelorException {
    return createPurchaseOrder(
        buyerUser,
        company,
        contactPartner,
        currency,
        deliveryDate,
        internalReference,
        externalReference,
        stockLocation,
        orderDate,
        priceList,
        supplierPartner,
        tradingName,
        null);
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
      StockLocation stockLocation,
      LocalDate orderDate,
      PriceList priceList,
      Partner supplierPartner,
      TradingName tradingName,
      FiscalPosition fiscalPosition)
      throws AxelorException {

    LOG.debug(
        "Creation of a purchase order : Company = {},  External reference = {}, Supplier = {}",
        company.getName(),
        externalReference,
        supplierPartner.getFullName());

    PurchaseOrder purchaseOrder =
        super.createPurchaseOrder(
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
            tradingName,
            fiscalPosition);

    purchaseOrder.setStockLocation(stockLocation);

    purchaseOrder.setPaymentMode(supplierPartner.getOutPaymentMode());
    purchaseOrder.setPaymentCondition(supplierPartner.getPaymentCondition());

    if (purchaseOrder.getPaymentMode() == null) {
      purchaseOrder.setPaymentMode(
          accountConfigService.getAccountConfig(company).getOutPaymentMode());
    }

    if (purchaseOrder.getPaymentCondition() == null) {
      purchaseOrder.setPaymentCondition(
          accountConfigService.getAccountConfig(company).getDefPaymentCondition());
    }

    purchaseOrder.setTradingName(tradingName);

    return purchaseOrder;
  }
}
