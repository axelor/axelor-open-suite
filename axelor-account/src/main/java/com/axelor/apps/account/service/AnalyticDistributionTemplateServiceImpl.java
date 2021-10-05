package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticDistributionLine;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AnalyticDistributionTemplateServiceImpl
    implements AnalyticDistributionTemplateService {

  public AnalyticDistributionTemplateServiceImpl() {
    // TODO Auto-generated constructor stub
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
  public void checkAnalyticDistributionTemplateCompany(
      AnalyticDistributionTemplate analyticDistributionTemplate) throws AxelorException {
    if (analyticDistributionTemplate.getCompany() != null) {
      List<AnalyticDistributionLine> analyticDistributionLineList =
          analyticDistributionTemplate.getAnalyticDistributionLineList();
      boolean checkAxis = false;
      boolean checkJournal = false;
      for (AnalyticDistributionLine analyticDistributionLine : analyticDistributionLineList) {
        if (analyticDistributionTemplate.getCompany()
            != analyticDistributionLine.getAnalyticAxis().getCompany()) {
          checkAxis = true;
        }
        if (analyticDistributionTemplate.getCompany()
            != analyticDistributionLine.getAnalyticJournal().getCompany()) {
          checkJournal = true;
        }
      }
      printCheckAnalyticDistributionTemplateCompany(checkAxis, checkJournal);
    }
  }

  public void printCheckAnalyticDistributionTemplateCompany(boolean checkAxis, boolean checkJournal)
      throws AxelorException {
    if (checkAxis && checkJournal) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(
              IExceptionMessage.ANALYTIC_DISTRIBUTION_TEMPLATE_CHECK_COMPANY_AXIS_AND_JOURNAL));
    } else if (checkAxis) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.ANALYTIC_DISTRIBUTION_TEMPLATE_CHECK_COMPANY_AXIS));
    } else if (checkJournal) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.ANALYTIC_DISTRIBUTION_TEMPLATE_CHECK_COMPANY_JOURNAL));
    }
  }
}
