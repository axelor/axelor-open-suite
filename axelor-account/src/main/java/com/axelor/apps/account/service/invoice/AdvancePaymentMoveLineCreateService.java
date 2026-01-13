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
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.util.TaxConfiguration;
import com.axelor.apps.base.AxelorException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;

public interface AdvancePaymentMoveLineCreateService {
  void manageAdvancePaymentInvoiceTaxMoveLines(
      Move move,
      MoveLine defaultMoveLine,
      BigDecimal prorata,
      LocalDate paymentDate,
      Map<TaxConfiguration, Pair<BigDecimal, BigDecimal>> taxConfigurationAmountMap);

  void fillMoveWithTaxMoveLines(
      Move move, Map<TaxConfiguration, Pair<BigDecimal, BigDecimal>> taxConfigurationAmountMap)
      throws AxelorException;
}
