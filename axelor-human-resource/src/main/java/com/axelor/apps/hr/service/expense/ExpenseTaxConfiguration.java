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
package com.axelor.apps.hr.service.expense;

import com.axelor.apps.account.db.TaxLine;
import java.util.Objects;

public class ExpenseTaxConfiguration {

  protected TaxLine taxLine;
  protected int vatSystem;

  public ExpenseTaxConfiguration(TaxLine taxLine, int vatSystem) {
    this.taxLine = taxLine;
    this.vatSystem = vatSystem;
  }

  public TaxLine getTaxLine() {
    return taxLine;
  }

  public int getVatSystem() {
    return vatSystem;
  }

  @Override
  public int hashCode() {
    return Objects.hash(taxLine != null ? taxLine.getId() : 0, vatSystem);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }

    if (!(o instanceof ExpenseTaxConfiguration)) {
      return false;
    }

    ExpenseTaxConfiguration other = (ExpenseTaxConfiguration) o;

    return this.vatSystem == other.vatSystem && Objects.equals(this.taxLine, other.taxLine);
  }
}
