package com.axelor.apps.budget.service;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.budget.exception.IExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import org.apache.commons.collections.CollectionUtils;

@RequestScoped
public class BudgetAccountConfigServiceImpl implements BudgetAccountConfigService {

  @Inject
  public BudgetAccountConfigServiceImpl() {}

  @Override
  public void checkBudgetKey(AccountConfig accountConfig) throws AxelorException {
    if (accountConfig.getEnableBudgetKey()
        && !CollectionUtils.isEmpty(accountConfig.getAnalyticAxisByCompanyList())) {
      for (AnalyticAxisByCompany analyticAxisByCompany :
          accountConfig.getAnalyticAxisByCompanyList()) {
        if (analyticAxisByCompany.getIncludeInBudgetKey()) {
          return;
        }
      }
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.ERROR_CONFIG_BUDGET_KEY));
    }
  }
}
