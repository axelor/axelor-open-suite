package com.axelor.apps.repo;

import com.axelor.apps.base.db.ProductType;
import com.axelor.apps.budget.db.repo.SaleOrderBudgetRepository;
import com.axelor.apps.sale.db.PriceStudy;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderConstructionRepository extends SaleOrderBudgetRepository {

  @Override
  public SaleOrder save(SaleOrder saleOrder) {

    populatePriceStudyList(saleOrder);

    return super.save(saleOrder);
  }

  protected void populatePriceStudyList(SaleOrder saleOrder) {

    if (CollectionUtils.isNotEmpty(saleOrder.getPriceStudyList())) {
      saleOrder.getPriceStudyList().clear();
    } else {
      saleOrder.setPriceStudyList(new ArrayList<>());
    }

    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    if (CollectionUtils.isEmpty(saleOrderLineList)) {
      return;
    }

    Map<ProductType, List<SaleOrderLine>> groupedByProductType =
        saleOrderLineList.stream()
            .filter(line -> line.getProductType() != null)
            .collect(Collectors.groupingBy(line -> line.getProductType()));

    groupedByProductType.forEach(
        (productType, lines) -> {
          BigDecimal sumOfPurchasePrices =
              lines.stream()
                  .map(SaleOrderLine::getPurchasePrice)
                  .reduce(BigDecimal.ZERO, BigDecimal::add);
          BigDecimal sumOfCostPrices =
              lines.stream()
                  .map(SaleOrderLine::getCostPrice)
                  .reduce(BigDecimal.ZERO, BigDecimal::add);
          BigDecimal sumOfPrices =
              lines.stream().map(SaleOrderLine::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
          BigDecimal averageFG = BigDecimal.ZERO;
          BigDecimal averageGrossMargin = BigDecimal.ZERO;
          if (!lines.isEmpty()) {
            averageFG =
                lines.stream()
                    .map(SaleOrderLine::getGeneralExpenses)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(lines.size()), 2, BigDecimal.ROUND_HALF_EVEN);
            averageGrossMargin =
                lines.stream()
                    .map(SaleOrderLine::getGrossMarging)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(lines.size()), 2, BigDecimal.ROUND_HALF_EVEN);
          }

          PriceStudy newPriceStudy = new PriceStudy();
          newPriceStudy.setCostPrice(sumOfCostPrices);
          newPriceStudy.setPurchasePrice(sumOfPurchasePrices);
          newPriceStudy.setPrice(sumOfPrices);
          newPriceStudy.setAverageFG(averageFG);
          newPriceStudy.setAverageGrossMarge(averageGrossMargin);
          newPriceStudy.setProductType(productType);

          saleOrder.addPriceStudyListItem(newPriceStudy);
        });
  }
}
