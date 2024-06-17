package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.sale.db.PackLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineCreateServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.apps.supplychain.db.SupplyChainConfig;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.axelor.apps.supplychain.service.config.SupplyChainConfigService;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class SaleOrderLineCreateSupplychainServiceImpl extends SaleOrderLineCreateServiceImpl {

  protected AnalyticLineModelService analyticLineModelService;
  protected SupplyChainConfigService supplyChainConfigService;
  protected ReservedQtyService reservedQtyService;

  @Inject
  public SaleOrderLineCreateSupplychainServiceImpl(
      SaleOrderLineService saleOrderLineService,
      AppSaleService appSaleService,
      AppBaseService appBaseService,
      SaleOrderLineComputeService saleOrderLineComputeService,
      AnalyticLineModelService analyticLineModelService,
      SupplyChainConfigService supplyChainConfigService,
      ReservedQtyService reservedQtyService) {
    super(saleOrderLineService, appSaleService, appBaseService, saleOrderLineComputeService);
    this.analyticLineModelService = analyticLineModelService;
    this.supplyChainConfigService = supplyChainConfigService;
    this.reservedQtyService = reservedQtyService;
  }

  @Override
  public SaleOrderLine createSaleOrderLine(
      PackLine packLine,
      SaleOrder saleOrder,
      BigDecimal packQty,
      BigDecimal conversionRate,
      Integer sequence)
      throws AxelorException {

    SaleOrderLine soLine =
        super.createSaleOrderLine(packLine, saleOrder, packQty, conversionRate, sequence);

    if (soLine != null && soLine.getProduct() != null) {
      soLine.setSaleSupplySelect(soLine.getProduct().getSaleSupplySelect());

      AnalyticLineModel analyticLineModel = new AnalyticLineModel(soLine, null);
      analyticLineModelService.getAndComputeAnalyticDistribution(analyticLineModel);

      if (ObjectUtils.notEmpty(soLine.getAnalyticMoveLineList())) {
        soLine
            .getAnalyticMoveLineList()
            .forEach(analyticMoveLine -> analyticMoveLine.setSaleOrderLine(soLine));
      }

      try {
        SupplyChainConfig supplyChainConfig =
            supplyChainConfigService.getSupplyChainConfig(saleOrder.getCompany());

        if (supplyChainConfig.getAutoRequestReservedQty()) {
          reservedQtyService.requestQty(soLine);
        }
      } catch (AxelorException e) {
        TraceBackService.trace(e);
      }
    }
    return soLine;
  }
}
