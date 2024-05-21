package com.axelor.apps.purchase.service;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.service.TradingNameService;
import com.axelor.apps.base.service.administration.SequenceService;
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

  protected SequenceService sequenceService;
  protected PurchaseConfigService purchaseConfigService;

  @Inject
  public PurchaseOrderCreateServiceImpl(
      SequenceService sequenceService, PurchaseConfigService purchaseConfigService) {
    this.sequenceService = sequenceService;
    this.purchaseConfigService = purchaseConfigService;
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

    purchaseOrder.setPurchaseOrderSeq(sequenceService.getDraftSequenceNumber(purchaseOrder));

    purchaseOrder.setStatusSelect(PurchaseOrderRepository.STATUS_DRAFT);
    purchaseOrder.setSupplierPartner(supplierPartner);
    purchaseOrder.setFiscalPosition(supplierPartner.getFiscalPosition());
    purchaseOrder.setDisplayPriceOnQuotationRequest(
        purchaseConfigService.getPurchaseConfig(company).getDisplayPriceOnQuotationRequest());
    return purchaseOrder;
  }
}
