/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.VatExemptionReason;
import java.util.Objects;

public class TaxLineTaxExemptionKey {

  private final TaxLine taxLine;
  private final VatExemptionReason vatExemptionReason;

  public TaxLineTaxExemptionKey(TaxLine taxLine, VatExemptionReason vatExemptionReason) {
    this.taxLine = taxLine;
    this.vatExemptionReason = vatExemptionReason;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TaxLineTaxExemptionKey)) return false;
    TaxLineTaxExemptionKey that = (TaxLineTaxExemptionKey) o;
    return Objects.equals(taxLine, that.taxLine)
        && Objects.equals(vatExemptionReason, that.vatExemptionReason);
  }

  @Override
  public int hashCode() {
    return Objects.hash(taxLine, vatExemptionReason);
  }
}
