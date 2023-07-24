/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;

public interface AnalyticDistributionTemplateService {

  void validateTemplatePercentages(AnalyticDistributionTemplate analyticDistributionTemplate)
      throws AxelorException;

  public AnalyticDistributionTemplate personalizeAnalyticDistributionTemplate(
      AnalyticDistributionTemplate analyticDistributionTemplate, Company company)
      throws AxelorException;

  public void checkAnalyticDistributionTemplateCompany(
      AnalyticDistributionTemplate analyticDistributionTemplate) throws AxelorException;

  AnalyticDistributionTemplate createSpecificDistributionTemplate(Company company, String name)
      throws AxelorException;

  void checkAnalyticAccounts(AnalyticDistributionTemplate analyticDistributionTemplate)
      throws AxelorException;

  void verifyTemplateValues(AnalyticDistributionTemplate analyticDistributionTemplate)
      throws AxelorException;
}
