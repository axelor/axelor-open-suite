/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Optional;
import java.util.Set;

@Singleton
public class FiscalPositionServiceImpl implements FiscalPositionService {

  protected TaxService taxService;

  @Inject
  public FiscalPositionServiceImpl(TaxService taxService) {
    this.taxService = taxService;
  }

  @Override
  public Set<Tax> getTaxSet(FiscalPosition fiscalPosition, Set<Tax> taxSet) {
    TaxEquiv taxEquiv = getTaxEquiv(fiscalPosition, taxSet);
    return taxEquiv == null ? taxSet : taxEquiv.getToTaxSet();
  }

  @Override
  public TaxEquiv getTaxEquiv(FiscalPosition fiscalPosition, Set<Tax> taxSet) {
    if (fiscalPosition != null
        && fiscalPosition.getTaxEquivList() != null
        && ObjectUtils.notEmpty(taxSet)) {
      for (TaxEquiv taxEquiv : fiscalPosition.getTaxEquivList()) {
        if (ObjectUtils.notEmpty(taxEquiv.getFromTaxSet())
            && taxEquiv.getFromTaxSet().equals(taxSet)
            && ObjectUtils.notEmpty(taxEquiv.getToTaxSet())) {
          return taxEquiv;
        }
      }
    }

    return null;
  }

  @Override
  public TaxEquiv getTaxEquivFromOrToTaxSet(
      FiscalPosition fiscalPosition, Set<TaxLine> taxLineSet) {
    Set<Tax> taxSet = taxService.getTaxSet(taxLineSet);
    if (fiscalPosition == null
        || ObjectUtils.isEmpty(fiscalPosition.getTaxEquivList())
        || ObjectUtils.isEmpty(taxSet)) {
      return null;
    }
    return Optional.ofNullable(getTaxEquiv(fiscalPosition, taxSet))
        .orElse(
            fiscalPosition.getTaxEquivList().stream()
                .filter(
                    taxEquiv ->
                        ObjectUtils.notEmpty(taxEquiv.getToTaxSet())
                            && taxEquiv.getToTaxSet().equals(taxSet)
                            && ObjectUtils.notEmpty(taxEquiv.getFromTaxSet()))
                .findFirst()
                .orElse(null));
  }

  @Override
  public TaxEquiv getTaxEquivFromTaxLines(FiscalPosition fiscalPosition, Set<TaxLine> taxLineSet) {
    return getTaxEquiv(fiscalPosition, taxService.getTaxSet(taxLineSet));
  }
}
