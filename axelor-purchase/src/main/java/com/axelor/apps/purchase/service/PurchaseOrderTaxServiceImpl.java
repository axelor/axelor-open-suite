package com.axelor.apps.purchase.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseConfigRepository;
import com.axelor.apps.purchase.service.config.PurchaseConfigService;
import com.google.inject.Inject;

public class PurchaseOrderTaxServiceImpl implements PurchaseOrderTaxService {

  protected final PurchaseConfigService purchaseConfigService;

  @Inject
  public PurchaseOrderTaxServiceImpl(PurchaseConfigService purchaseConfigService) {
    this.purchaseConfigService = purchaseConfigService;
  }

  @Override
  public void setPurchaseOrderInAti(PurchaseOrder purchaseOrder) throws AxelorException {
    Integer atiChoice =
        purchaseConfigService
            .getPurchaseConfig(purchaseOrder.getCompany())
            .getPurchaseOrderInAtiSelect();
    purchaseOrder.setInAti(
        atiChoice == PurchaseConfigRepository.PURCHASE_ATI_ALWAYS
            || atiChoice == PurchaseConfigRepository.PURCHASE_ATI_DEFAULT);
  }
}
