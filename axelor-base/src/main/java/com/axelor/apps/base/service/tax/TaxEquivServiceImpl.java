/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
