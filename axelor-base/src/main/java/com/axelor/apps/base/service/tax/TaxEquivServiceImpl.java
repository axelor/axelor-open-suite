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
import com.axelor.common.ObjectUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class TaxEquivServiceImpl implements TaxEquivService {
  @Override
  public String getTaxDomain(TaxEquiv taxEquiv, boolean isFromTax, boolean isToTax) {
    if (!taxEquiv.getReverseCharge()) {
      return null;
    }

    Set<BigDecimal> taxRateSet;

    if (isFromTax) {
      taxRateSet =
          this.getTaxValues(taxEquiv.getToTaxSet())
              .orElse(this.getTaxValues(taxEquiv.getReverseChargeTaxSet()).orElse(null));
    } else if (isToTax) {
      taxRateSet =
          this.getTaxValues(taxEquiv.getFromTaxSet())
              .orElse(this.getTaxValues(taxEquiv.getReverseChargeTaxSet()).orElse(null));
    } else {
      taxRateSet =
          this.getTaxValues(taxEquiv.getFromTaxSet())
              .orElse(this.getTaxValues(taxEquiv.getToTaxSet()).orElse(null));
    }

    if (ObjectUtils.isEmpty(taxRateSet)) {
      return null;
    } else {
      return String.format(
          "self.activeTaxLine.value IN %s",
          taxRateSet.stream()
              .map(
                  value ->
                      value
                          .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP)
                          .toString())
              .collect(Collectors.joining(",", "(", ")")));
    }
  }

  protected Optional<Set<BigDecimal>> getTaxValues(Set<Tax> taxSet) {
    if (ObjectUtils.isEmpty(taxSet)) {
      return Optional.empty();
    }
    return Optional.of(
        taxSet.stream()
            .map(Tax::getActiveTaxLine)
            .filter(Objects::nonNull)
            .map(TaxLine::getValue)
            .collect(Collectors.toSet()));
  }
}
