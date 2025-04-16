package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.apps.sale.service.saleorder.status.SaleOrderConfirmService;
import com.axelor.apps.sale.service.saleorder.status.SaleOrderFinalizeService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineOnChangeService;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppBase;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderSplitServiceImpl implements SaleOrderSplitService {

  protected final SaleOrderRepository saleOrderRepository;
  protected final SaleOrderLineRepository saleOrderLineRepository;
  protected final SaleOrderLineOnChangeService saleOrderLineOnChangeService;
  protected final SaleOrderFinalizeService saleOrderFinalizeService;
  protected final SaleOrderConfirmService saleOrderConfirmService;
  protected final SaleOrderComputeService saleOrderComputeService;
  protected final AppBaseService appBaseService;

  @Inject
  public SaleOrderSplitServiceImpl(
      SaleOrderRepository saleOrderRepository,
      SaleOrderLineRepository saleOrderLineRepository,
      SaleOrderLineOnChangeService saleOrderLineOnChangeService,
      SaleOrderFinalizeService saleOrderFinalizeService,
      SaleOrderConfirmService saleOrderConfirmService,
      SaleOrderComputeService saleOrderComputeService,
      AppBaseService appBaseService) {
    this.saleOrderRepository = saleOrderRepository;
    this.saleOrderLineRepository = saleOrderLineRepository;
    this.saleOrderLineOnChangeService = saleOrderLineOnChangeService;
    this.saleOrderFinalizeService = saleOrderFinalizeService;
    this.saleOrderConfirmService = saleOrderConfirmService;
    this.saleOrderComputeService = saleOrderComputeService;
    this.appBaseService = appBaseService;
  }

  @Transactional(rollbackOn = {AxelorException.class})
  @Override
  public SaleOrder generateConfirmedSaleOrder(
      SaleOrder saleOrder, Map<Long, BigDecimal> qtyToOrderMap) throws AxelorException {

    SaleOrder confirmedSaleOrder = getConfirmedSaleOrder(saleOrder);
    addLines(qtyToOrderMap, confirmedSaleOrder);
    saleOrderComputeService.computeSaleOrder(confirmedSaleOrder);
    saleOrderFinalizeService.finalizeQuotation(confirmedSaleOrder);
    saleOrderConfirmService.confirmSaleOrder(confirmedSaleOrder);

    return confirmedSaleOrder;
  }

  protected SaleOrder getConfirmedSaleOrder(SaleOrder saleOrder) {
    SaleOrder confirmedSaleOrder = saleOrderRepository.copy(saleOrder, true);
    confirmedSaleOrder.clearSaleOrderLineList();
    confirmedSaleOrder.clearSaleOrderLineTaxList();
    confirmedSaleOrder.clearBatchSet();
    confirmedSaleOrder.setOriginSaleQuotation(saleOrder);
    return confirmedSaleOrder;
  }

  protected void addLines(Map<Long, BigDecimal> qtyToOrderMap, SaleOrder confirmedSaleOrder)
      throws AxelorException {
    AppBase appBase = appBaseService.getAppBase();
    for (Map.Entry<Long, BigDecimal> entry : qtyToOrderMap.entrySet()) {
      Long lineId = entry.getKey();
      BigDecimal qtyToOrder = entry.getValue();
      if (qtyToOrderMap.get(lineId) != null
          && qtyToOrderMap.get(lineId).compareTo(BigDecimal.ZERO) == 0) {
        continue;
      }

      SaleOrderLine saleOrderLineToCopy = saleOrderLineRepository.find(lineId);
      updateOriginSol(saleOrderLineToCopy, qtyToOrder, appBase);
      SaleOrderLine copySaleOrderLine =
          getSaleOrderLineCopy(saleOrderLineToCopy, qtyToOrder, confirmedSaleOrder);
      confirmedSaleOrder.addSaleOrderLineListItem(copySaleOrderLine);
    }

    if (CollectionUtils.isEmpty(confirmedSaleOrder.getSaleOrderLineList())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SaleExceptionMessage.SALE_QUOTATION_NO_LINE_GENERATED));
    }
  }

  protected void updateOriginSol(
      SaleOrderLine saleOrderLineToCopy, BigDecimal qtyToOrder, AppBase appBase)
      throws AxelorException {
    BigDecimal qtyToOrderLeft = getQtyToOrderLeft(saleOrderLineToCopy);
    if (qtyToOrderLeft.compareTo(qtyToOrder) < 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SaleExceptionMessage.SALE_QUOTATION_WRONG_ORDER_QTY),
          qtyToOrderLeft.setScale(appBase.getNbDecimalDigitForQty(), RoundingMode.HALF_UP),
          saleOrderLineToCopy.getProduct().getName());
    }
    saleOrderLineToCopy.setOrderedQty(saleOrderLineToCopy.getOrderedQty().add(qtyToOrder));
  }

  protected SaleOrderLine getSaleOrderLineCopy(
      SaleOrderLine saleOrderLineToCopy, BigDecimal qtyToOrder, SaleOrder confirmedSaleOrder)
      throws AxelorException {
    SaleOrderLine copySaleOrderLine = saleOrderLineRepository.copy(saleOrderLineToCopy, true);
    copySaleOrderLine.setQty(qtyToOrder);
    copySaleOrderLine.setSaleOrder(null);
    saleOrderLineOnChangeService.qtyOnChange(copySaleOrderLine, confirmedSaleOrder, null);
    saleOrderLineRepository.save(copySaleOrderLine);
    return copySaleOrderLine;
  }

  @Override
  public void checkSolOrderedQty(SaleOrder saleOrder) throws AxelorException {
    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    if (saleOrderLineList.stream()
        .allMatch(line -> line.getQty().compareTo(line.getOrderedQty()) == 0)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SaleExceptionMessage.SALE_QUOTATION_ALL_ALREADY_ORDERED));
    }
  }

  @Override
  public BigDecimal getQtyToOrderLeft(SaleOrderLine saleOrderLine) {
    return saleOrderLine.getQty().subtract(saleOrderLine.getOrderedQty());
  }
}
