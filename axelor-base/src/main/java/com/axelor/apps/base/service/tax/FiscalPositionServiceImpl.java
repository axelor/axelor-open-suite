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

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxEquiv;
import com.google.inject.Singleton;

@Singleton
public class FiscalPositionServiceImpl implements FiscalPositionService {

  @Override
  public Tax getTax(FiscalPosition fiscalPosition, Tax tax) {
    TaxEquiv taxEquiv = getTaxEquiv(fiscalPosition, tax);

    return taxEquiv == null ? tax : taxEquiv.getToTax();
  }

  @Override
  public TaxEquiv getTaxEquiv(FiscalPosition fiscalPosition, Tax tax) {
    if (fiscalPosition != null && fiscalPosition.getTaxEquivList() != null && tax != null) {
      for (TaxEquiv taxEquiv : fiscalPosition.getTaxEquivList()) {
        if (taxEquiv.getFromTax() != null
            && taxEquiv.getFromTax().equals(tax)
            && taxEquiv.getToTax() != null) {
          return taxEquiv;
        }
      }
    }

    return null;
  }
}
