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
package com.axelor.apps.bankpayment.db.repo;

import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.google.inject.Inject;
import java.util.Map;

public class BankReconciliationLineManagementRepository extends BankReconciliationLineRepository {

  protected CurrencyScaleService currencyScaleService;

  @Inject
  public BankReconciliationLineManagementRepository(CurrencyScaleService currencyScaleService) {
    this.currencyScaleService = currencyScaleService;
  }

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    Long bankReconciliationLineId = (Long) json.get("id");
    BankReconciliationLine bankReconciliationLine = find(bankReconciliationLineId);

    json.put("$currencyNumberOfDecimals", currencyScaleService.getScale(bankReconciliationLine));

    return super.populate(json, context);
  }
}
