/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.cash.management.db.repo;

import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.cash.management.db.ForecastRecapLine;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class CashManagementForecastLineRecapRepository extends ForecastRecapLineRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    if (context.get("_fromForecastDashboard") != null) {
      ForecastRecapLine recapLine = find((long) json.get("id"));
      if (recapLine.getForecastRecap() != null) {
        Set<BankDetails> bankDetailsSet = recapLine.getForecastRecap().getBankDetailsSet();
        if (CollectionUtils.isNotEmpty(bankDetailsSet)) {
          json.put(
              "$ibans",
              bankDetailsSet.stream().map(BankDetails::getIban).collect(Collectors.joining(",")));
        }
      }
    }
    return super.populate(json, context);
  }
}
