package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderSplitService;
import com.axelor.apps.supplychain.db.MrpLineType;
import com.axelor.studio.db.AppSale;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class MrpLineSaleOrderServiceImpl implements MrpLineSaleOrderService {

  protected final AppSaleService appSaleService;
  protected final SaleOrderSplitService saleOrderSplitService;
  protected final UnitConversionService unitConversionService;

  @Inject
  public MrpLineSaleOrderServiceImpl(
      AppSaleService appSaleService,
      SaleOrderSplitService saleOrderSplitService,
      UnitConversionService unitConversionService) {
    this.appSaleService = appSaleService;
    this.saleOrderSplitService = saleOrderSplitService;
    this.unitConversionService = unitConversionService;
  }

  @Override
  public BigDecimal getSoMrpLineQty(SaleOrderLine saleOrderLine, Unit unit, MrpLineType mrpLineType)
      throws AxelorException {
    AppSale appSale = appSaleService.getAppSale();
    boolean isSplitQuotationEnabled = appSale.getIsQuotationAndOrderSplitEnabled();
    BigDecimal qty = saleOrderLine.getQty().subtract(saleOrderLine.getDeliveredQty());
    List<Integer> statusList = StringHelper.getIntegerList(mrpLineType.getStatusSelect());
    boolean isStatusCompatible =
        CollectionUtils.isNotEmpty(statusList)
            && statusList.contains(SaleOrderRepository.STATUS_FINALIZED_QUOTATION)
            && statusList.contains(SaleOrderRepository.STATUS_ORDER_CONFIRMED);
    if (isSplitQuotationEnabled
        && isStatusCompatible
        && saleOrderLine.getSaleOrder().getStatusSelect()
            == SaleOrderRepository.STATUS_FINALIZED_QUOTATION) {
      qty = saleOrderSplitService.getQtyToOrderLeft(saleOrderLine);
    }

    if (!unit.equals(saleOrderLine.getUnit())) {
      qty =
          unitConversionService.convert(
              saleOrderLine.getUnit(),
              unit,
              qty,
              saleOrderLine.getQty().scale(),
              saleOrderLine.getProduct());
    }
    return qty;
  }
}
