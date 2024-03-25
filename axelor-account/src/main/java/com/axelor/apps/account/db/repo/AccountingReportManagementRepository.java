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
package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.account.service.AccountingReportService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.persistence.PersistenceException;
import org.apache.commons.collections.CollectionUtils;

public class AccountingReportManagementRepository extends AccountingReportRepository {

  @Inject protected AccountingReportService accountingReportService;

  @Override
  public AccountingReport save(AccountingReport accountingReport) {
    try {

      if (accountingReport.getRef() == null) {

        String seq = accountingReportService.getSequence(accountingReport);
        accountingReportService.setSequence(accountingReport, seq);
      }

      return super.save(accountingReport);
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
  }

  @Override
  public AccountingReport copy(AccountingReport entity, boolean deep) {

    AccountingReport copy = super.copy(entity, deep);

    copy.setRef(null);
    copy.setStatusSelect(this.STATUS_DRAFT);
    copy.setPublicationDateTime(null);
    copy.setTotalDebit(BigDecimal.ZERO);
    copy.setTotalCredit(BigDecimal.ZERO);
    copy.setBalance(BigDecimal.ZERO);

    return copy;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    // Concatenate company names into company field for custom reports
    if (context.containsKey("_isCustom")) {
      boolean isCustom = (boolean) context.get("_isCustom");

      if (isCustom) {
        if (json.containsKey("companySet")) {
          List<Map<String, Object>> companySet = (List<Map<String, Object>>) json.get("companySet");

          if (CollectionUtils.isNotEmpty(companySet)) {
            String companyStr =
                companySet.stream()
                    .map(it -> (String) it.get("name"))
                    .collect(Collectors.joining(","));
            json.put("$companyStr", companyStr);
          }
        }
      } else if (json.containsKey("company")) {
        Map<String, Object> company = (Map<String, Object>) json.get("company");
        json.put("$companyStr", company.get("name"));
      }
    }

    return super.populate(json, context);
  }
}
