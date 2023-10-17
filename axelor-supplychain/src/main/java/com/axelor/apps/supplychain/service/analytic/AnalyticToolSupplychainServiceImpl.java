package com.axelor.apps.supplychain.service.analytic;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class AnalyticToolSupplychainServiceImpl implements AnalyticToolSupplychainService {

  protected AnalyticLineModelService analyticLineModelService;

  @Inject
  public AnalyticToolSupplychainServiceImpl(AnalyticLineModelService analyticLineModelService) {
    this.analyticLineModelService = analyticLineModelService;
  }

  @Override
  public void checkSaleOrderLinesAnalyticDistribution(SaleOrder saleOrder) throws AxelorException {
    if (!analyticLineModelService.analyticDistributionTemplateRequired(
        false, saleOrder.getCompany())) {
      return;
    }

    List<String> productList =
        saleOrder.getSaleOrderLineList().stream()
            .filter(
                saleOrderLine ->
                    saleOrderLine.getTypeSelect() == SaleOrderLineRepository.TYPE_NORMAL
                        && saleOrderLine.getAnalyticDistributionTemplate() == null)
            .map(SaleOrderLine::getProductName)
            .collect(Collectors.toList());

    if (!productList.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(SupplychainExceptionMessage.SALE_ORDER_ANALYTIC_DISTRIBUTION_ERROR),
          productList);
    }
  }

  @Override
  public void checkPurchaseOrderLinesAnalyticDistribution(PurchaseOrder purchaseOrder)
      throws AxelorException {
    if (!analyticLineModelService.analyticDistributionTemplateRequired(
        true, purchaseOrder.getCompany())) {
      return;
    }

    List<String> productList =
        purchaseOrder.getPurchaseOrderLineList().stream()
            .filter(
                purchaseOrderLine -> purchaseOrderLine.getAnalyticDistributionTemplate() == null)
            .map(PurchaseOrderLine::getProductName)
            .collect(Collectors.toList());

    if (!productList.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(SupplychainExceptionMessage.SALE_ORDER_ANALYTIC_DISTRIBUTION_ERROR),
          productList);
    }
  }
}
