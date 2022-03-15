package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.production.service.SaleOrderWorkflowServiceProductionImpl;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.productionorder.ProductionOrderSaleOrderService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.apps.supplychain.service.AccountingSituationSupplychainService;
import com.axelor.apps.supplychain.service.PartnerSupplychainService;
import com.axelor.apps.supplychain.service.SaleOrderAnalyticService;
import com.axelor.apps.supplychain.service.SaleOrderPurchaseService;
import com.axelor.apps.supplychain.service.SaleOrderStockService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SaleOrderWorkflowServiceBusinessProjectImpl
    extends SaleOrderWorkflowServiceProductionImpl {

  protected AppBusinessProjectService appBusinessProjectService;
  protected SaleOrderBusinessProjectService saleOrderBusinessProjectService;

  @Inject
  public SaleOrderWorkflowServiceBusinessProjectImpl(
      SequenceService sequenceService,
      PartnerRepository partnerRepo,
      SaleOrderRepository saleOrderRepo,
      AppSaleService appSaleService,
      UserService userService,
      SaleOrderLineService saleOrderLineService,
      SaleOrderStockService saleOrderStockService,
      SaleOrderPurchaseService saleOrderPurchaseService,
      AppSupplychainService appSupplychainService,
      AccountingSituationSupplychainService accountingSituationSupplychainService,
      ProductionOrderSaleOrderService productionOrderSaleOrderService,
      AppProductionService appProductionService,
      PartnerSupplychainService partnerSupplychainService,
      SaleOrderAnalyticService saleOrderAnalyticService,
      AppBusinessProjectService appBusinessProjectService,
      SaleOrderBusinessProjectService saleOrderBusinessProjectService) {
    super(
        sequenceService,
        partnerRepo,
        saleOrderRepo,
        appSaleService,
        userService,
        saleOrderLineService,
        saleOrderStockService,
        saleOrderPurchaseService,
        appSupplychainService,
        accountingSituationSupplychainService,
        productionOrderSaleOrderService,
        appProductionService,
        partnerSupplychainService,
        saleOrderAnalyticService);
    this.appBusinessProjectService = appBusinessProjectService;
    this.saleOrderBusinessProjectService = saleOrderBusinessProjectService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void confirmSaleOrder(SaleOrder saleOrder) throws AxelorException {
    super.confirmSaleOrder(saleOrder);

    if (appBusinessProjectService.isApp("business-project")
        && appBusinessProjectService.getAppBusinessProject().getAutomaticProject()) {
      saleOrderBusinessProjectService.generateProject(saleOrder);
    }
  }
}
