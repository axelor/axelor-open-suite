package com.axelor.apps.businessproduction.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.businessproduction.exception.BusinessProductionExceptionMessage;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMarginService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineDiscountService;
import com.axelor.apps.sale.service.saleorderline.creation.SaleOrderLineCreateService;
import com.axelor.apps.sale.service.saleorderline.pack.SaleOrderLinePackService;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineComplementaryProductService;
import com.axelor.apps.supplychain.service.AccountingSituationSupplychainService;
import com.axelor.apps.supplychain.service.PartnerLinkSupplychainService;
import com.axelor.apps.supplychain.service.TrackingNumberSupplychainService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderStockService;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.repo.AppSaleRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SaleOrderServiceBusinessProductionImpl extends SaleOrderServiceSupplychainImpl {

  protected final AppSaleService appSaleService;

  @Inject
  public SaleOrderServiceBusinessProductionImpl(
      AppBaseService appBaseService,
      SaleOrderLineRepository saleOrderLineRepo,
      SaleOrderRepository saleOrderRepo,
      SaleOrderComputeService saleOrderComputeService,
      SaleOrderMarginService saleOrderMarginService,
      SaleConfigService saleConfigService,
      SaleOrderLineCreateService saleOrderLineCreateService,
      SaleOrderLineComplementaryProductService saleOrderLineComplementaryProductService,
      SaleOrderLinePackService saleOrderLinePackService,
      SaleOrderLineDiscountService saleOrderLineDiscountService,
      AppSupplychainService appSupplychainService,
      SaleOrderStockService saleOrderStockService,
      AccountingSituationSupplychainService accountingSituationSupplychainService,
      TrackingNumberSupplychainService trackingNumberSupplychainService,
      PartnerLinkSupplychainService partnerLinkSupplychainService,
      AppSaleService appSaleService) {
    super(
        appBaseService,
        saleOrderLineRepo,
        saleOrderRepo,
        saleOrderComputeService,
        saleOrderMarginService,
        saleConfigService,
        saleOrderLineCreateService,
        saleOrderLineComplementaryProductService,
        saleOrderLinePackService,
        saleOrderLineDiscountService,
        appSupplychainService,
        saleOrderStockService,
        accountingSituationSupplychainService,
        trackingNumberSupplychainService,
        partnerLinkSupplychainService);
    this.appSaleService = appSaleService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public boolean enableEditOrder(SaleOrder saleOrder) throws AxelorException {
    if (saleOrder.getProject() != null
        && appSaleService.getAppSale().getListDisplayTypeSelect()
            == AppSaleRepository.APP_SALE_LINE_DISPLAY_TYPE_MULTI) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BusinessProductionExceptionMessage.SALE_ORDER_EDIT_SO_LINK_TO_PROJECT_ERROR));
    }
    return super.enableEditOrder(saleOrder);
  }
}
