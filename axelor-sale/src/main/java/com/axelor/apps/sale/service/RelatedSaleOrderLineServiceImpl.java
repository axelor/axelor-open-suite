package com.axelor.apps.sale.service;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RelatedSaleOrderLineServiceImpl implements RelatedSaleOrderLineService {

  protected final SaleOrderLineRepository saleOrderLineRepository;
  protected final SaleOrderRepository saleOrderRepository;
  protected final TaxService taxService;
  protected final AppBaseService appBaseService;
  protected final SaleOrderLineService saleOrderLineService;

  @Inject
  public RelatedSaleOrderLineServiceImpl(
      SaleOrderLineRepository saleOrderLineRepository,
      SaleOrderRepository saleOrderRepository,
      TaxService taxService,
      AppBaseService appBaseService,
      SaleOrderLineService saleOrderLineService) {
    this.saleOrderLineRepository = saleOrderLineRepository;
    this.saleOrderRepository = saleOrderRepository;
    this.taxService = taxService;
    this.appBaseService = appBaseService;
    this.saleOrderLineService = saleOrderLineService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateRelatedSOLinesOnPriceChange(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    saleOrderLine = calculateChildrenTotalsAndPrices(saleOrderLine, saleOrder);
    saleOrderLineRepository.save(saleOrderLine);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateRelatedSOLinesOnQtyChange(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    saleOrderLine = updateSubLinesQty(saleOrderLine, saleOrder);
    saleOrderLine.setIsProcessedLine(true);
    saleOrderLineRepository.save(saleOrderLine);
  }

  protected SaleOrderLine updateSubLinesQty(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    List<SaleOrderLine> subLines = saleOrderLine.getSaleOrderLineList();
    if (subLines == null || subLines.isEmpty()) {
      saleOrderLine.setQtyBeforeUpdate(saleOrderLine.getQty());
      return saleOrderLine;
    }
    BigDecimal qty = saleOrderLine.getQty();
    BigDecimal oldQty = saleOrderLine.getQtyBeforeUpdate();

    for (SaleOrderLine subLine : subLines) {
      subLine.setQty(
          subLine.getQtyBeforeUpdate().multiply(qty).divide(oldQty, 2, RoundingMode.HALF_UP));
      computeAllValues(subLine, saleOrder);
      updateSubLinesQty(subLine, saleOrder);
    }

    saleOrderLine.setQtyBeforeUpdate(saleOrderLine.getQty());

    return saleOrderLine;
  }

  @Override
  public SaleOrder getSaleOrderFromContext(Context context) {
    Context parentContext = context.getParent();
    if (parentContext == null) {
      return null;
    }
    if (parentContext.getContextClass().equals(SaleOrder.class)) {
      return parentContext.asType(SaleOrder.class);
    }
    return getSaleOrderFromContext(parentContext);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateRelatedOrderLines(SaleOrder saleOrder) throws AxelorException {

    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    if (saleOrderLineList != null && !saleOrderLineList.isEmpty()) {
      for (int i = 0; i < saleOrderLineList.size(); i++) {
        SaleOrderLine saleOrderLine = saleOrderLineList.get(i);
        SaleOrderLine newSaleOrderLine =
            calculateAllParentsTotalsAndPrices(saleOrderLine, saleOrder);
        saleOrderLineList.set(i, newSaleOrderLine);
      }
    }
    saleOrderRepository.save(saleOrder);
  }

  protected SaleOrderLine calculateAllParentsTotalsAndPrices(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    if (saleOrderLine.getSaleOrderLineList() == null
        || saleOrderLine.getSaleOrderLineList().isEmpty()) {
      saleOrderLine.setPriceBeforeUpdate(saleOrderLine.getPrice());
      saleOrderLine.setQtyBeforeUpdate(saleOrderLine.getQty());
      setDefaultSaleOrderLineProperties(saleOrderLine, saleOrder);
      return saleOrderLine;
    }

    BigDecimal total = BigDecimal.ZERO;
    for (SaleOrderLine subline : saleOrderLine.getSaleOrderLineList()) {
      if (subline.getSaleOrderLineList() != null
          && !saleOrderLine.getSaleOrderLineList().isEmpty()) {
        calculateAllParentsTotalsAndPrices(subline, saleOrder);
        total = total.add(subline.getExTaxTotal());
      } else {
        total = total.add(subline.getExTaxTotal());
      }
    }

    saleOrderLine.setPrice(total.divide(saleOrderLine.getQty(), 2, RoundingMode.HALF_UP));

    computeAllValues(saleOrderLine, saleOrder);
    saleOrderLine.setPriceBeforeUpdate(saleOrderLine.getPrice());
    saleOrderLine.setQtyBeforeUpdate(saleOrderLine.getQty());
    setDefaultSaleOrderLineProperties(saleOrderLine, saleOrder);
    return saleOrderLine;
  }

  protected void setDefaultSaleOrderLineProperties(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    if (saleOrderLine.getParentSaleOrderLine() == null
        && saleOrder.getCompany().getSaleConfig().getCountType() == 1) {
      saleOrderLine.setIsNotCountable(false);
    }
    if (saleOrderLine.getParentSaleOrderLine() != null
        && saleOrder.getCompany().getSaleConfig().getCountType() == 1) {
      saleOrderLine.setIsNotCountable(true);
    }
    if (saleOrderLine.getParentSaleOrderLine() == null
        && saleOrder.getCompany().getSaleConfig().getCountType() == 2) {
      saleOrderLine.setIsNotCountable(true);
    }
    if (saleOrderLine.getParentSaleOrderLine() != null
        && saleOrder.getCompany().getSaleConfig().getCountType() == 2) {
      saleOrderLine.setIsNotCountable(false);
    }

    if (saleOrderLine.getParentSaleOrderLine() == null
        && (saleOrderLine.getSaleOrderLineList() == null
            || saleOrderLine.getSaleOrderLineList().isEmpty())
        && saleOrder.getCompany().getSaleConfig().getCountType() == 2) {
      saleOrderLine.setIsNotCountable(false);
    }

    if (saleOrderLine.getSaleOrder() == null) {
      saleOrderLine.setSaleOrder(saleOrder);
    }
    if (saleOrderLine.getIsProcessedLine()) {
      saleOrderLine.setIsProcessedLine(false);
    }
    if (saleOrderLine.getIsDisabledFromCalculation()) {
      saleOrderLine.setIsDisabledFromCalculation(false);
    }
  }

  protected SaleOrderLine calculateChildrenTotalsAndPrices(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    List<SaleOrderLine> subLines = saleOrderLine.getSaleOrderLineList();
    BigDecimal oldQty = saleOrderLine.getQty();
    BigDecimal oldPrice = saleOrderLine.getPriceBeforeUpdate();
    if (subLines == null || subLines.isEmpty()) {
      saleOrderLine.setPriceBeforeUpdate(saleOrderLine.getPrice());
      return saleOrderLine;
    }

    for (SaleOrderLine subLine : subLines) {
      BigDecimal subLineTotal = subLine.getExTaxTotal();
      subLine.setPrice(
          saleOrderLine
              .getExTaxTotal()
              .multiply(subLineTotal)
              .divide(
                  subLine.getQty().multiply(oldQty.multiply(oldPrice)), 2, RoundingMode.HALF_UP));
      computeAllValues(subLine, saleOrder);
      calculateChildrenTotalsAndPrices(subLine, saleOrder);
    }

    saleOrderLine.setPriceBeforeUpdate(saleOrderLine.getPrice());
    return saleOrderLine;
  }

  protected SaleOrderLine computeAllValues(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    BigDecimal exTaxPrice = saleOrderLine.getPrice();
    Set<TaxLine> taxLineSet = saleOrderLine.getTaxLineSet();
    BigDecimal inTaxPrice =
        taxService.convertUnitPrice(
            false, taxLineSet, exTaxPrice, appBaseService.getNbDecimalDigitForUnitPrice());
    saleOrderLine.setInTaxPrice(inTaxPrice);
    Map<String, BigDecimal> map = saleOrderLineService.computeValues(saleOrder, saleOrderLine);
    saleOrderLine.setInTaxTotal(map.get("inTaxTotal"));
    saleOrderLine.setExTaxTotal(map.get("exTaxTotal"));
    saleOrderLine.setPriceDiscounted(map.get("priceDiscounted"));
    saleOrderLine.setCompanyExTaxTotal(map.get("companyExTaxTotal"));
    saleOrderLine.setCompanyInTaxTotal(map.get("companyInTaxTotal"));
    saleOrderLine.setSubTotalCostPrice(map.get("subTotalCostPrice"));
    return saleOrderLine;
  }

  public SaleOrderLine setLineIndex(SaleOrderLine saleOrderLine, Context context) {
    if (saleOrderLine.getLineIndex() == null) {
      if (context.getParent() != null
          && context.getParent().getContextClass().equals(SaleOrder.class)) {
        SaleOrder parent = context.getParent().asType(SaleOrder.class);
        String nextIndex =
            parent.getSaleOrderLineList().stream()
                .filter(slo -> slo.getLineIndex() != null)
                .map(slo -> slo.getLineIndex().split("\\.")[0])
                .mapToInt(Integer::parseInt)
                .boxed()
                .collect(Collectors.maxBy(Integer::compareTo))
                .map(max -> String.valueOf(max + 1))
                .orElse("1");
        saleOrderLine.setLineIndex(nextIndex);
      }

      if (context.getParent() != null
          && context.getParent().getContextClass().equals(SaleOrderLine.class)) {
        SaleOrderLine parent = context.getParent().asType(SaleOrderLine.class);
        saleOrderLine.setLineIndex(
            parent.getLineIndex() + "." + (parent.getSaleOrderLineListSize() + 1));
      }
    }
    return saleOrderLine;
  }
}
