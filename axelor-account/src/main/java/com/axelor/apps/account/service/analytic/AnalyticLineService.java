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
package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticJournal;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.repo.AnalyticLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface AnalyticLineService {

  AnalyticJournal getAnalyticJournal(AnalyticLine line) throws AxelorException;

  LocalDate getDate(AnalyticLine line);

  BigDecimal getAnalyticAmountFromParent(AnalyticLine line, AnalyticMoveLine analyticMoveLine);

  List<Long> getAxisDomains(AnalyticLine line, Company company, int position)
      throws AxelorException;

  boolean isAxisRequired(AnalyticLine line, Company company, int position) throws AxelorException;

  AnalyticLine checkAnalyticLineForAxis(AnalyticLine line);

  AnalyticLine printAnalyticAccount(AnalyticLine line, Company company) throws AxelorException;

  boolean checkAnalyticLinesByAxis(AnalyticLine analyticLine, int position, Company company)
      throws AxelorException;

  List<Long> getAnalyticAccountsByAxis(AnalyticLine line, AnalyticAxis analyticAxis);
}
