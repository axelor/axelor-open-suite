package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.CallTender;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.CallTenderPurchaseOrderServiceImpl;
import com.axelor.apps.purchase.service.PurchaseOrderCreateService;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.auth.AuthUtils;
import com.google.inject.Inject;

public class CallTenderPurchaseOrderSupplychainServiceImpl
    extends CallTenderPurchaseOrderServiceImpl {

  protected final PurchaseOrderCreateSupplychainService purchaseOrderCreateSupplychainService;
  protected final PurchaseOrderSupplychainService purchaseOrderSupplychainService;

  @Inject
  public CallTenderPurchaseOrderSupplychainServiceImpl(
      PurchaseOrderCreateService purchaseOrderCreateService,
      AppBaseService appBaseService,
      PartnerPriceListService partnerPriceListService,
      PurchaseOrderLineService purchaseOrderLineService,
      ProductCompanyService productCompanyService,
      PurchaseOrderRepository purchaseOrderRepository,
      PurchaseOrderService purchaseOrderService,
      PurchaseOrderCreateSupplychainService purchaseOrderCreateSupplychainService,
      PurchaseOrderSupplychainService purchaseOrderSupplychainService) {
    super(
        purchaseOrderCreateService,
        appBaseService,
        partnerPriceListService,
        purchaseOrderLineService,
        productCompanyService,
        purchaseOrderRepository,
        purchaseOrderService);
    this.purchaseOrderCreateSupplychainService = purchaseOrderCreateSupplychainService;
    this.purchaseOrderSupplychainService = purchaseOrderSupplychainService;
  }

  @Override
  protected PurchaseOrder createPurchaseOrder(
      CallTender callTender, Partner partner, Company company) throws AxelorException {
    PurchaseOrder purchaseOrder =
        purchaseOrderCreateSupplychainService.createPurchaseOrder(
            AuthUtils.getUser(),
            company,
            null,
            partner.getCurrency(),
            null,
            callTender.getCallTenderSeq(),
            null,
            purchaseOrderSupplychainService.getStockLocation(partner, company),
            appBaseService.getTodayDate(company),
            partnerPriceListService.getDefaultPriceList(partner, PriceListRepository.TYPE_PURCHASE),
            partner,
            null,
            null);
    purchaseOrder.setCallTender(callTender);
    return purchaseOrder;
  }
}
