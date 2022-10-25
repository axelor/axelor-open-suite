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

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.AnalyticDistributionLine;
import com.axelor.apps.account.db.AnalyticJournal;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.tool.StringTool;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class AnalyticDistributionLineServiceImpl implements AnalyticDistributionLineService {

  protected AccountConfigRepository accountConfigRepository;

  @Inject
  public AnalyticDistributionLineServiceImpl(AccountConfigRepository accountConfigRepository) {
    this.accountConfigRepository = accountConfigRepository;
  }

  @Override
  public AnalyticDistributionLine createAnalyticDistributionLine(
      AnalyticAxis analyticAxis,
      AnalyticAccount analyticAccount,
      AnalyticJournal analyticJournal,
      BigDecimal percentage) {
    AnalyticDistributionLine analyticDistributionLine = new AnalyticDistributionLine();
    analyticDistributionLine.setAnalyticAxis(analyticAxis);
    analyticDistributionLine.setAnalyticAccount(analyticAccount);
    analyticDistributionLine.setAnalyticJournal(analyticJournal);
    analyticDistributionLine.setPercentage(percentage);
    return analyticDistributionLine;
  }

  @Override
  public String getAxisDomain(AnalyticDistributionLine analyticDistributionLine, Company company) {
    String idListStr = "0";
    if (analyticDistributionLine != null && company != null) {
      AccountConfig accountConfig = accountConfigRepository.findByCompany(company);
      List<AnalyticAxis> analyticAxisList =
          accountConfig.getAnalyticAxisByCompanyList().stream()
              .map(AnalyticAxisByCompany::getAnalyticAxis)
              .collect(Collectors.toList());
      if (CollectionUtils.isNotEmpty(analyticAxisList)) {
        idListStr = StringTool.getIdListString(analyticAxisList);
      }
    }
    return "self.id IN (" + idListStr + ")";
  }
}
