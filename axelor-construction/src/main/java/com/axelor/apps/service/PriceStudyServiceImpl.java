package com.axelor.apps.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.sale.db.PriceStudy;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.apps.sale.service.saleorder.SaleOrderOnLineChangeService;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.shiro.util.CollectionUtils;

public class PriceStudyServiceImpl implements PriceStudyService {

  protected final SaleOrderOnLineChangeService saleOrderOnLineChangeService;
  protected final SaleOrderRepository saleOrderRepository;

  protected final AppBaseService appBaseService;
  protected final AnalyticLineModelService analyticLineModelService;
  protected final TaxService taxService;
  protected final SaleOrderLineService saleOrderLineService;

  @Inject
  PriceStudyServiceImpl(
      SaleOrderOnLineChangeService saleOrderOnLineChangeService,
      SaleOrderRepository saleOrderRepository,
      AppBaseService appBaseService,
      AnalyticLineModelService analyticLineModelService,
      TaxService taxService,
      SaleOrderLineService saleOrderLineService) {
    this.saleOrderRepository = saleOrderRepository;
    this.saleOrderOnLineChangeService = saleOrderOnLineChangeService;
    this.appBaseService = appBaseService;
    this.analyticLineModelService = analyticLineModelService;
    this.taxService = taxService;
    this.saleOrderLineService = saleOrderLineService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void recalculatePrices(SaleOrder saleOrder) throws AxelorException {
    SaleOrder repoSaleOrder = saleOrderRepository.find(saleOrder.getId());
    List<PriceStudy> odlPriceStudyList = repoSaleOrder.getPriceStudyList();
    List<PriceStudy> priceStudyList = saleOrder.getPriceStudyList();
    if (CollectionUtils.isEmpty(priceStudyList)) {
      return;
    }
    for (int i = 0; i < odlPriceStudyList.size(); i++) {
      PriceStudy oldPriceStudy = odlPriceStudyList.get(i);
      PriceStudy priceStudy = priceStudyList.get(i);

      if (!isEquals(priceStudy, oldPriceStudy)) {

        List<SaleOrderLine> groupedSOlinesList = priceStudyRelatedLines(repoSaleOrder, priceStudy);

        for (SaleOrderLine soLineMember : groupedSOlinesList) {
          soLineMember.setPrice(
              updateValue(
                  soLineMember.getPrice(), priceStudy.getPrice(), oldPriceStudy.getPrice()));
          soLineMember.setCostPrice(
              updateValue(
                  soLineMember.getCostPrice(),
                  priceStudy.getCostPrice(),
                  oldPriceStudy.getCostPrice()));
          soLineMember.setPurchasePrice(
              updateValue(
                  soLineMember.getPurchasePrice(),
                  priceStudy.getPurchasePrice(),
                  oldPriceStudy.getPurchasePrice()));
          soLineMember.setGeneralExpenses(
              updateValue(
                  soLineMember.getGeneralExpenses(),
                  priceStudy.getAverageFG(),
                  oldPriceStudy.getAverageFG()));
          soLineMember.setGrossMarging(
              updateValue(
                  soLineMember.getGrossMarging(),
                  priceStudy.getAverageGrossMarge(),
                  oldPriceStudy.getAverageGrossMarge()));

          taxService.convertUnitPrice(
              false,
              soLineMember.getTaxLineSet(),
              soLineMember.getPrice(),
              appBaseService.getNbDecimalDigitForUnitPrice());
          saleOrderLineService.computeValues(saleOrder, soLineMember);
          AnalyticLineModel analyticLineModel = new AnalyticLineModel(soLineMember, saleOrder);
          if (analyticLineModelService.productAccountManageAnalytic(analyticLineModel)) {
            analyticLineModelService.computeAnalyticDistribution(analyticLineModel);
          }
        }
      }
    }

    odlPriceStudyList.clear();
    odlPriceStudyList.addAll(priceStudyList);
    saleOrderOnLineChangeService.onLineChange(repoSaleOrder);
    saleOrderRepository.save(repoSaleOrder);
  }

  protected boolean isEquals(PriceStudy priceStudy, PriceStudy oldPriceStudy) {
    if (priceStudy.getPrice().equals(oldPriceStudy.getPrice())
        && priceStudy.getCostPrice().equals(oldPriceStudy.getCostPrice())
        && priceStudy.getPurchasePrice().equals(oldPriceStudy.getPurchasePrice())
        && priceStudy.getAverageFG().equals(oldPriceStudy.getAverageFG())
        && priceStudy.getAverageGrossMarge().equals(oldPriceStudy.getAverageGrossMarge())) {
      return true;
    }
    return false;
  }

  protected BigDecimal updateValue(BigDecimal currentPrice, BigDecimal coef, BigDecimal oldCeof) {
    return currentPrice
        .multiply(coef)
        .divide(oldCeof, appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_EVEN);
  }

  @Override
  public void onPriceChange(PriceStudy priceStudy) {
    int scale = appBaseService.getNbDecimalDigitForUnitPrice();
    BigDecimal margeSum = BigDecimal.ZERO;
    SaleOrder saleOrder = priceStudy.getSaleOrder();
    SaleOrder repoSaleOrder = saleOrderRepository.find(saleOrder.getId());
    List<SaleOrderLine> relatedSoLines = priceStudyRelatedLines(saleOrder, priceStudy);

    PriceStudy oldPriceStudy = getOldPriceStudy(repoSaleOrder, priceStudy);
    for (SaleOrderLine soLineMember : relatedSoLines) {
      BigDecimal newPrice =
          soLineMember
              .getPrice()
              .multiply(priceStudy.getPrice())
              .divide(oldPriceStudy.getPrice(), scale, RoundingMode.HALF_EVEN);
      BigDecimal newMarge =
          newPrice
              .divide(soLineMember.getCostPrice(), scale, RoundingMode.HALF_EVEN)
              .subtract(BigDecimal.ONE);
      margeSum = margeSum.add(newMarge);
    }

    priceStudy.setAverageGrossMarge(
        margeSum.divide(new BigDecimal(relatedSoLines.size()), scale, RoundingMode.HALF_EVEN));
  }

  @Override
  public void onGeneralExpensesChange(PriceStudy priceStudy) {
    int scale = appBaseService.getNbDecimalDigitForUnitPrice();
    BigDecimal costPriceSum = BigDecimal.ZERO;
    BigDecimal priceSum = BigDecimal.ZERO;
    SaleOrder saleOrder = priceStudy.getSaleOrder();
    SaleOrder repoSaleOrder = saleOrderRepository.find(saleOrder.getId());
    List<SaleOrderLine> relatedSoLines = priceStudyRelatedLines(saleOrder, priceStudy);

    PriceStudy oldPriceStudy = getOldPriceStudy(repoSaleOrder, priceStudy);
    for (SaleOrderLine soLineMember : relatedSoLines) {
      BigDecimal newFg =
          soLineMember
              .getGeneralExpenses()
              .multiply(
                  priceStudy
                      .getAverageFG()
                      .multiply(new BigDecimal(relatedSoLines.size()))
                      .divide(
                          oldPriceStudy
                              .getAverageFG()
                              .multiply(new BigDecimal(relatedSoLines.size())),
                          scale,
                          RoundingMode.HALF_EVEN));

      BigDecimal newCostPrice = soLineMember.getPurchasePrice().multiply(newFg);

      BigDecimal newPrice =
          newCostPrice.multiply(soLineMember.getGrossMarging().add(BigDecimal.ONE));
      costPriceSum = costPriceSum.add(newCostPrice);
      priceSum = priceSum.add(newPrice);
    }

    priceStudy.setCostPrice(
        costPriceSum.divide(new BigDecimal(relatedSoLines.size()), scale, RoundingMode.HALF_EVEN));
    priceStudy.setPrice(
        priceSum.divide(new BigDecimal(relatedSoLines.size()), scale, RoundingMode.HALF_EVEN));
  }

  @Override
  public void onMargeChange(PriceStudy priceStudy) {
    int scale = appBaseService.getNbDecimalDigitForUnitPrice();
    BigDecimal priceSum = BigDecimal.ZERO;
    SaleOrder saleOrder = priceStudy.getSaleOrder();
    SaleOrder repoSaleOrder = saleOrderRepository.find(saleOrder.getId());
    List<SaleOrderLine> relatedSoLines = priceStudyRelatedLines(saleOrder, priceStudy);

    PriceStudy oldPriceStudy = getOldPriceStudy(repoSaleOrder, priceStudy);
    for (SaleOrderLine soLineMember : relatedSoLines) {
      BigDecimal newMarge =
          soLineMember
              .getGrossMarging()
              .multiply(
                  priceStudy
                      .getAverageGrossMarge()
                      .multiply(new BigDecimal(relatedSoLines.size()))
                      .divide(
                          oldPriceStudy
                              .getAverageGrossMarge()
                              .multiply(new BigDecimal(relatedSoLines.size())),
                          scale,
                          RoundingMode.HALF_EVEN));

      BigDecimal newPrice = soLineMember.getCostPrice().multiply(newMarge.add(BigDecimal.ONE));
      priceSum = priceSum.add(newPrice);
    }

    priceStudy.setPrice(
        priceSum.divide(new BigDecimal(relatedSoLines.size()), scale, RoundingMode.HALF_EVEN));
  }

  protected List<SaleOrderLine> priceStudyRelatedLines(SaleOrder saleOrder, PriceStudy priceStudy) {
    return saleOrder.getSaleOrderLineList().stream()
        .filter(
            saleOrderLine ->
                saleOrderLine.getProductType() != null
                    && saleOrderLine.getProductType().equals(priceStudy.getProductType()))
        .collect(Collectors.toList());
  }

  protected PriceStudy getOldPriceStudy(SaleOrder repoSaleOrder, PriceStudy priceStudy) {
    return repoSaleOrder.getPriceStudyList().stream()
        .filter(ps -> ps.equals(priceStudy))
        .findFirst()
        .orElse(null);
  }
}
