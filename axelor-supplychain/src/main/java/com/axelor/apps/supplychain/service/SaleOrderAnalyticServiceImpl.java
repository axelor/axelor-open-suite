package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.sale.db.SaleConfig;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class SaleOrderAnalyticServiceImpl implements SaleOrderAnalyticService {

  /**
   * Method that checks if any lines of saleOrder has a null analyticDistributiontemplate (if it
   * required by Company's sale config). If it is the case, the method will throws a
   * AxelorException.
   *
   * @throws AxelorException
   */
  @Override
  public void checkSaleOrderAnalyticDistributionTemplate(SaleOrder saleOrder)
      throws AxelorException {
    Objects.requireNonNull(saleOrder);
    if (Optional.ofNullable(saleOrder.getCompany())
        .map(Company::getSaleConfig)
        .map(SaleConfig::getIsAnalyticDistributionRequired)
        .orElse(false)) {
      List<String> productsWithError =
          saleOrder.getSaleOrderLineList().stream()
              .filter(line -> line.getAnalyticDistributionTemplate() == null)
              .map(SaleOrderLine::getProductName)
              .collect(Collectors.toList());

      if (!productsWithError.isEmpty()) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(IExceptionMessage.SALE_ORDER_ANALYTIC_DISTRIBUTION_ERROR),
            productsWithError);
      }
    }
  }
}
