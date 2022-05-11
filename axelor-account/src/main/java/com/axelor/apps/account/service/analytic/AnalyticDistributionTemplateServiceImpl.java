/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.AnalyticDistributionLine;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Company;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

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
    List<AnalyticAxis> axisList = new ArrayList<>();
    if (analyticDistributionTemplate != null
        && !CollectionUtils.isEmpty(
            analyticDistributionTemplate.getAnalyticDistributionLineList())) {
      for (AnalyticDistributionLine analyticDistributionLine :
          analyticDistributionTemplate.getAnalyticDistributionLineList()) {
        if (!axisList.contains(analyticDistributionLine.getAnalyticAxis())) {
          axisList.add(analyticDistributionLine.getAnalyticAxis());
        }
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
      if (analyticDistributionTemplate.getCompany() != null
          && !CollectionUtils.isEmpty(analyticDistributionLineList)) {
        boolean checkAxis = false;
        boolean checkJournal = false;
        for (AnalyticDistributionLine analyticDistributionLine : analyticDistributionLineList) {
          if (analyticDistributionLine.getAnalyticAxis() != null
              && (analyticDistributionTemplate.getCompany()
                      != analyticDistributionLine.getAnalyticAxis().getCompany()
                  || analyticDistributionLine.getAnalyticAxis().getCompany() == null)) {
            checkAxis = true;
          }
          if (analyticDistributionTemplate.getCompany()
                  != analyticDistributionLine.getAnalyticJournal().getCompany()
              || analyticDistributionLine.getAnalyticAxis().getCompany() == null) {
            checkJournal = true;
          }
        }
        printCheckAnalyticDistributionTemplateCompany(checkAxis, checkJournal);
      }
    }
  }

  protected void printCheckAnalyticDistributionTemplateCompany(
      boolean checkAxis, boolean checkJournal) throws AxelorException {
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

  public AnalyticDistributionTemplate createDistributionTemplateFromAccount(Account account)
      throws AxelorException {
    if (account.getCompany() != null && account.getName() != null) {
      Company company = account.getCompany();
      AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
      AnalyticDistributionTemplate analyticDistributionTemplate =
          new AnalyticDistributionTemplate();
      analyticDistributionTemplate.setName(account.getName());
      analyticDistributionTemplate.setCompany(account.getCompany());
      analyticDistributionTemplate.setArchived(true);
      analyticDistributionTemplate.setAnalyticDistributionLineList(
          new ArrayList<AnalyticDistributionLine>());
      for (AnalyticAxisByCompany analyticAxisByCompany :
          accountConfig.getAnalyticAxisByCompanyList()) {
        analyticDistributionTemplate.addAnalyticDistributionLineListItem(
            analyticDistributionLineService.createAnalyticDistributionLine(
                analyticAxisByCompany.getAnalyticAxis(),
                null,
                accountConfig.getAnalyticJournal(),
                BigDecimal.valueOf(100)));
      }
      return analyticDistributionTemplate;
    }
    return null;
  }
}
