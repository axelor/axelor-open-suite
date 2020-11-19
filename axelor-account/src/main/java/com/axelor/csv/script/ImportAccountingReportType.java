package com.axelor.csv.script;

import com.axelor.apps.account.db.AccountingReportConfigLine;
import com.axelor.apps.account.db.AccountingReportType;
import com.axelor.apps.account.db.repo.AccountingReportConfigLineRepository;
import com.axelor.apps.account.db.repo.AccountingReportTypeRepository;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ImportAccountingReportType {

  private AccountingReportTypeRepository reportTypeRepo;
  private AccountingReportConfigLineRepository configLineRepo;

  @Inject
  public ImportAccountingReportType(
      AccountingReportTypeRepository reportTypeRepo,
      AccountingReportConfigLineRepository configLineRepo) {
    this.reportTypeRepo = reportTypeRepo;
    this.configLineRepo = configLineRepo;
  }

  @Transactional(rollbackOn = {Exception.class})
  public Object setRules(Object bean, Map<String, Object> values) throws AxelorException {
    assert bean instanceof AccountingReportType;
    AccountingReportType reportType = (AccountingReportType) bean;

    String configLineValues = (String) values.get("rules");
    if (configLineValues != null && !configLineValues.isEmpty()) {
      String[] rules = configLineValues.split("\\|");
      List<AccountingReportConfigLine> configLines = new ArrayList<>();
      for (String rule : rules) {
        configLines.add(
            configLineRepo
                .all()
                .filter("self.importId = :importId")
                .bind("importId", rule)
                .fetchOne());
      }
      reportType.setAccountingReportConfigLineList(configLines);
    }

    reportTypeRepo.save(reportType);

    return reportType;
  }
}
