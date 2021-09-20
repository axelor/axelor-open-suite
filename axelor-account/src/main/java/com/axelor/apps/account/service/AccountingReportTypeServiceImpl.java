package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AccountingReportType;
import com.axelor.apps.account.module.AccountModule;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaStore;
import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;

@Alternative
@Priority(AccountModule.PRIORITY)
public class AccountingReportTypeServiceImpl implements AccountingReportTypeService {
  @Override
  public void setDefaultName(AccountingReportType accountingReportType) {
    if (accountingReportType.getTypeSelect() != null) {
      String name =
          I18n.get(
              MetaStore.getSelectionItem(
                      "accounting.report.type.select",
                      accountingReportType.getTypeSelect().toString())
                  .getTitle());
      accountingReportType.setName(name);
    }
  }
}
