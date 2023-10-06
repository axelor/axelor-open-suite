package com.axelor.apps.base.service.tax;

import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.service.app.AppBaseService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public class TaxEquivServiceImpl implements TaxEquivService {
  @Override
  public String getTaxDomain(TaxEquiv taxEquiv, boolean isFromTax, boolean isToTax) {
    if (!taxEquiv.getReverseCharge()) {
      return null;
    }

    BigDecimal taxRate;

    if (isFromTax) {
      taxRate =
          this.getTaxValue(taxEquiv.getToTax())
              .orElse(this.getTaxValue(taxEquiv.getReverseChargeTax()).orElse(null));
    } else if (isToTax) {
      taxRate =
          this.getTaxValue(taxEquiv.getFromTax())
              .orElse(this.getTaxValue(taxEquiv.getReverseChargeTax()).orElse(null));
    } else {
      taxRate =
          this.getTaxValue(taxEquiv.getFromTax())
              .orElse(this.getTaxValue(taxEquiv.getToTax()).orElse(null));
    }

    if (taxRate == null) {
      return null;
    } else {
      return String.format(
          "self.activeTaxLine.value = %s",
          taxRate.setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP));
    }
  }

  protected Optional<BigDecimal> getTaxValue(Tax tax) {
    return Optional.ofNullable(tax).map(Tax::getActiveTaxLine).map(TaxLine::getValue);
  }
}
