package com.axelor.apps.account.service;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.service.tax.TaxServiceImpl;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class TaxAccountServiceImpl extends TaxServiceImpl {
  @Override
  public BigDecimal getTotalTaxRateInPercentage(Set<TaxLine> taxLineSet) {
    if (CollectionUtils.isEmpty(taxLineSet)) {
      return BigDecimal.ZERO;
    }
    return taxLineSet.stream()
        .filter(Objects::nonNull)
        .filter(taxLine -> taxLine.getTax() != null && !taxLine.getTax().getIsNonDeductibleTax())
        .map(TaxLine::getValue)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
