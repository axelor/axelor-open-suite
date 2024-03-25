package com.axelor.apps.account.db.repo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class AccountingReportTypeManagementRepository extends AccountingReportTypeRepository {
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    // Concatenate company names into company field for custom reports
    if (json.containsKey("typeSelect")) {
      int typeSelect = (int) json.get("typeSelect");

      if (typeSelect == AccountingReportRepository.REPORT_CUSTOM_STATE) {
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
