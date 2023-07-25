/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticDistributionLine;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.exception.AxelorException;
import com.axelor.meta.CallMethod;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface AnalyticMoveLineService {

  public AnalyticMoveLineRepository getAnalyticMoveLineRepository();

  public BigDecimal computeAmount(AnalyticMoveLine analyticMoveLine);

  public List<AnalyticMoveLine> generateLines(
      AnalyticDistributionTemplate analyticDistributionTemplate,
      BigDecimal total,
      int typeSelect,
      LocalDate date);

  public AnalyticDistributionTemplate getAnalyticDistributionTemplate(
      Partner partner, Product product, Company company, boolean isPurchase) throws AxelorException;

  public void updateAnalyticMoveLine(
      AnalyticMoveLine analyticMoveLine, BigDecimal total, LocalDate date);

  public boolean validateLines(List<AnalyticDistributionLine> analyticDistributionLineList);

  @CallMethod
  boolean validateAnalyticMoveLines(List<AnalyticMoveLine> analyticDistributionLineList);

  AnalyticMoveLine computeAnalyticMoveLine(
      MoveLine moveLine, Company company, AnalyticAccount analyticAccount) throws AxelorException;

  AnalyticMoveLine computeAnalyticMoveLine(
      InvoiceLine invoiceLine, Invoice invoice, Company company, AnalyticAccount analyticAccount)
      throws AxelorException;

  AnalyticMoveLine reverse(AnalyticMoveLine analyticMoveLine, AnalyticAccount analyticAccount);

  AnalyticMoveLine reverseAndPersist(
      AnalyticMoveLine analyticMoveLine, AnalyticAccount analyticAccount);

  AnalyticMoveLine generateAnalyticMoveLine(
      AnalyticMoveLine analyticMoveLine, AnalyticAccount analyticAccount);

  String getAnalyticAxisDomain(Company company);
}
