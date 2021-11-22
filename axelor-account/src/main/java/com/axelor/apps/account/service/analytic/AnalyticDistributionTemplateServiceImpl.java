package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.AnalyticDistributionLine;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Company;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AnalyticDistributionTemplateServiceImpl
    implements AnalyticDistributionTemplateService {
  protected AccountConfigService accountConfigService;
  protected AnalyticDistributionLineService analyticDistributionLineService;

  @Inject
  public AnalyticDistributionTemplateServiceImpl(
      AccountConfigService accountConfigService,
      AnalyticDistributionLineService analyticDistributionLineService) {
    this.accountConfigService = accountConfigService;
    this.analyticDistributionLineService = analyticDistributionLineService;
  }

  public BigDecimal getPercentage(
      AnalyticDistributionLine analyticDistributionLine, AnalyticAxis analyticAxis) {
    if (analyticDistributionLine.getAnalyticAxis() != null
        && analyticAxis != null
        && analyticDistributionLine.getAnalyticAxis() == analyticAxis) {
      return analyticDistributionLine.getPercentage();
    }
    return BigDecimal.ZERO;
  }

  public List<AnalyticAxis> getAllAxis(AnalyticDistributionTemplate analyticDistributionTemplate) {
    List<AnalyticAxis> axisList = new ArrayList<AnalyticAxis>();
    for (AnalyticDistributionLine analyticDistributionLine :
        analyticDistributionTemplate.getAnalyticDistributionLineList()) {
      if (!axisList.contains(analyticDistributionLine.getAnalyticAxis())) {
        axisList.add(analyticDistributionLine.getAnalyticAxis());
      }
    }
    return axisList;
  }

  @Override
  public boolean validateTemplatePercentages(
      AnalyticDistributionTemplate analyticDistributionTemplate) {
    List<AnalyticDistributionLine> analyticDistributionLineList =
        analyticDistributionTemplate.getAnalyticDistributionLineList();
    List<AnalyticAxis> axisList = getAllAxis(analyticDistributionTemplate);
    BigDecimal sum;
    for (AnalyticAxis analyticAxis : axisList) {
      sum = BigDecimal.ZERO;
      for (AnalyticDistributionLine analyticDistributionLine : analyticDistributionLineList) {
        sum = sum.add(getPercentage(analyticDistributionLine, analyticAxis));
      }
      if (sum.intValue() != 100) {
        return false;
      }
    }
    return true;
  }

  @Override
  public AnalyticDistributionTemplate createDistributionTemplateFromAccount(Account account)
      throws AxelorException {
    Company company = account.getCompany();
    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
    AnalyticDistributionTemplate analyticDistributionTemplate = new AnalyticDistributionTemplate();
    analyticDistributionTemplate.setName(account.getName());
    analyticDistributionTemplate.setCompany(account.getCompany());
    analyticDistributionTemplate.setArchived(true);
    analyticDistributionTemplate.setAnalyticDistributionLineList(
        new ArrayList<AnalyticDistributionLine>());
    for (AnalyticAxisByCompany analyticAxisByCompany :
        accountConfig.getAnalyticAxisByCompanyList()) {
      analyticDistributionTemplate.addAnalyticDistributionLineListItem(
          analyticDistributionLineService.createAnalyticDistributionLine(
              analyticAxisByCompany.getAnalyticAxis(), null, null, BigDecimal.valueOf(100)));
    }
    return analyticDistributionTemplate;
  }
}
