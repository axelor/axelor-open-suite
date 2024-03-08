package com.axelor.apps.sale.service;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.SubProduct;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleConfigRepository;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.shiro.util.CollectionUtils;

public class RelatedSaleOrderLineServiceImpl implements RelatedSaleOrderLineService {

  protected final SaleOrderLineRepository saleOrderLineRepository;
  protected final SaleOrderRepository saleOrderRepository;
  protected final TaxService taxService;
  protected final AppBaseService appBaseService;
  protected final SaleOrderLineService saleOrderLineService;
  protected final AppSaleService appSaleService;

  @Inject
  public RelatedSaleOrderLineServiceImpl(
      SaleOrderLineRepository saleOrderLineRepository,
      SaleOrderRepository saleOrderRepository,
      TaxService taxService,
      AppBaseService appBaseService,
      SaleOrderLineService saleOrderLineService,
      AppSaleService appSaleService) {
    this.saleOrderLineRepository = saleOrderLineRepository;
    this.saleOrderRepository = saleOrderRepository;
    this.taxService = taxService;
    this.appBaseService = appBaseService;
    this.saleOrderLineService = saleOrderLineService;
    this.appSaleService = appSaleService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void populateSOLines(SaleOrder saleOrder) throws AxelorException {

    if (!appSaleService.getAppSale().getIsSubLinesEnabled()) {
      return;
    }

    updateRelatedOrderLines(saleOrder);
    saleOrder.getSaleOrderLineList().clear();

    for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineDisplayList()) {
      if (!saleOrderLine.getIsNotCountable()) {
        saleOrder.addSaleOrderLineListItem(saleOrderLine);
      }
    }
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
  @Transactional(rollbackOn = {Exception.class})
  public void updateRelatedOrderLines(SaleOrder saleOrder) throws AxelorException {
    if (!appSaleService.getAppSale().getIsSubLinesEnabled()) {
      return;
    }

    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineDisplayList();
    if (CollectionUtils.isEmpty(saleOrderLineList)) {
      return;
    }
    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      calculateAllParentsTotalsAndPrices(saleOrderLine, saleOrder);
    }
  }

  protected void calculateAllParentsTotalsAndPrices(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    if (saleOrderLine.getSaleOrderLineList() == null
        || saleOrderLine.getSaleOrderLineList().isEmpty()) {
      saleOrderLine.setPriceBeforeUpdate(saleOrderLine.getPrice());
      saleOrderLine.setQtyBeforeUpdate(saleOrderLine.getQty());
      setDefaultSaleOrderLineProperties(saleOrderLine, saleOrder);
      return;
    }

    BigDecimal total = computeTotal(saleOrderLine, saleOrder);
    saleOrderLine.setPrice(
        total.divide(
            saleOrderLine.getQty(),
            appBaseService.getNbDecimalDigitForUnitPrice(),
            RoundingMode.HALF_UP));

    computeAllValues(saleOrderLine, saleOrder);
    saleOrderLine.setPriceBeforeUpdate(saleOrderLine.getPrice());
    saleOrderLine.setQtyBeforeUpdate(saleOrderLine.getQty());
    setDefaultSaleOrderLineProperties(saleOrderLine, saleOrder);
  }

  protected BigDecimal computeTotal(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    BigDecimal total = BigDecimal.ZERO;
    for (SaleOrderLine subline : saleOrderLine.getSaleOrderLineList()) {
      calculateAllParentsTotalsAndPrices(subline, saleOrder);
      total = total.add(subline.getExTaxTotal());
    }
    return total;
  }

  protected void setDefaultSaleOrderLineProperties(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    if (saleOrderLine.getSaleOrderDisplay() == null) {
      saleOrderLine.setSaleOrderDisplay(saleOrder);
    }

    SaleOrderLine parentSaleOrderLine = saleOrderLine.getParentSaleOrderLine();
    Integer countType = saleOrder.getCompany().getSaleConfig().getCountTypeSelect();
    List<SaleOrderLine> saleOrderLineList = saleOrderLine.getSaleOrderLineList();
    if (parentSaleOrderLine == null && countType == SaleConfigRepository.COUNT_ONLY_PARENTS) {
      saleOrderLine.setIsNotCountable(false);
    }
    if (parentSaleOrderLine != null && countType == SaleConfigRepository.COUNT_ONLY_PARENTS) {
      saleOrderLine.setIsNotCountable(true);
    }
    if (parentSaleOrderLine == null && countType == SaleConfigRepository.COUNT_ONLY_CHILDREN) {
      saleOrderLine.setIsNotCountable(true);
    }
    if (parentSaleOrderLine != null
        && CollectionUtils.isEmpty(saleOrderLineList)
        && countType == SaleConfigRepository.COUNT_ONLY_CHILDREN) {
      saleOrderLine.setIsNotCountable(false);
    }

    if (parentSaleOrderLine != null
        && !CollectionUtils.isEmpty(saleOrderLineList)
        && countType == SaleConfigRepository.COUNT_ONLY_CHILDREN) {
      saleOrderLine.setIsNotCountable(true);
    }

    if (parentSaleOrderLine == null
        && CollectionUtils.isEmpty(saleOrderLine.getSaleOrderLineList())
        && countType == SaleConfigRepository.COUNT_ONLY_CHILDREN) {
      saleOrderLine.setIsNotCountable(false);
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
                  subLine.getQty().multiply(oldQty.multiply(oldPrice)),
                  appBaseService.getNbDecimalDigitForUnitPrice(),
                  RoundingMode.HALF_UP));
      computeAllValues(subLine, saleOrder);
      calculateChildrenTotalsAndPrices(subLine, saleOrder);
    }

    saleOrderLine.setPriceBeforeUpdate(saleOrderLine.getPrice());
    return saleOrderLine;
  }

  protected void computeAllValues(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    BigDecimal exTaxPrice = saleOrderLine.getPrice();
    Set<TaxLine> taxLineSet = saleOrderLine.getTaxLineSet();
    BigDecimal inTaxPrice =
        taxService.convertUnitPrice(
            false, taxLineSet, exTaxPrice, appBaseService.getNbDecimalDigitForUnitPrice());
    saleOrderLine.setInTaxPrice(inTaxPrice);
    saleOrderLineService.computeValues(saleOrder, saleOrderLine);
  }

  @Override
  public SaleOrderLine setLineIndex(SaleOrderLine saleOrderLine, Context context) {
    if (!appSaleService.getAppSale().getIsSubLinesEnabled()) {
      return saleOrderLine;
    }

    if (saleOrderLine.getLineIndex() == null) {
      Context parentContext = context.getParent();
      if (parentContext != null && parentContext.getContextClass().equals(SaleOrder.class)) {
        SaleOrder parent = parentContext.asType(SaleOrder.class);
        if (parent.getSaleOrderLineDisplayList() != null) {
          saleOrderLine.setLineIndex(calculatePrentSolLineIndex(parent));
        } else {
          saleOrderLine.setLineIndex("1");
        }
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

  protected String calculatePrentSolLineIndex(SaleOrder saleOrder) {
    return saleOrder.getSaleOrderLineDisplayList().stream()
        .filter(slo -> slo.getLineIndex() != null)
        .map(slo -> slo.getLineIndex().split("\\.")[0])
        .mapToInt(Integer::parseInt)
        .boxed()
        .collect(Collectors.maxBy(Integer::compareTo))
        .map(max -> String.valueOf(max + 1))
        .orElse("1");
  }

  @Override
  public SaleOrderLine updateOnSaleOrderLineListChange(SaleOrderLine saleOrderLine) {
    saleOrderLine.setSaleOrderLineListSize(saleOrderLine.getSaleOrderLineList().size());
    for (SaleOrderLine slo : saleOrderLine.getSaleOrderLineList()) {
      if (slo.getIsProcessedLine() || slo.getIsDisabledFromCalculation()) {
        saleOrderLine.setIsDisabledFromCalculation(true);
        break;
      }
    }
    return saleOrderLine;
  }

  public SaleOrderLine createSaleOrderline(SubProduct subProduct, SaleOrder saleOrder)
      throws AxelorException {
    SaleOrderLine saleOrderLine = new SaleOrderLine();
    saleOrderLine.setProduct(subProduct.getProduct());
    saleOrderLine.setQty(subProduct.getQty());
    saleOrderLineService.computeProductInformation(saleOrderLine, saleOrder);
    saleOrderLineService.computeValues(saleOrder, saleOrderLine);
    if (Objects.equals(saleOrderLine.getPriceBeforeUpdate(), BigDecimal.ZERO)) {
      saleOrderLine.setPriceBeforeUpdate(saleOrderLine.getPrice());
    }
    return saleOrderLine;
  }
}
