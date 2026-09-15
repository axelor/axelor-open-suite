/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticDistributionLine;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.TradingName;
import com.axelor.meta.CallMethod;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface AnalyticMoveLineService {

  public AnalyticMoveLineRepository getAnalyticMoveLineRepository();

  public BigDecimal computeAmount(AnalyticMoveLine analyticMoveLine);

  BigDecimal computeAmount(AnalyticMoveLine analyticMoveLine, BigDecimal analyticLineAmount);

  public List<AnalyticMoveLine> generateLines(
      AnalyticDistributionTemplate analyticDistributionTemplate,
      BigDecimal total,
      int typeSelect,
      LocalDate date);

  public AnalyticDistributionTemplate getAnalyticDistributionTemplate(
      Partner partner,
      Product product,
      Company company,
      TradingName tradingName,
      Account account,
      boolean isPurchase)
      throws AxelorException;

  public void updateAnalyticMoveLine(
      AnalyticMoveLine analyticMoveLine, BigDecimal total, LocalDate date);

  /**
   * Recomputes every analytic move line of the list for the given total then reconciles the
   * per-line rounding so that, for each analytic axis distributing 100%, the sum of the line
   * amounts equals the total. Must be used instead of looping over {@link #updateAnalyticMoveLine}
   * whenever a whole distribution is recomputed, otherwise the per-line HALF_UP rounding drifts.
   */
  public void updateAnalyticMoveLineList(
      List<AnalyticMoveLine> analyticMoveLineList, BigDecimal total, LocalDate date);

  /**
   * Reconciles the per-line HALF_UP rounding of already-computed analytic move lines so that, for
   * each analytic axis distributing 100%, the sum of the line amounts equals the total (the last
   * line of each axis absorbs the residual). Use this when the line amounts are already up to date
   * and only the rounding drift needs to be corrected; use {@link #updateAnalyticMoveLineList} when
   * the amounts must be recomputed first.
   */
  public void reconcileRoundingRemainder(
      List<AnalyticMoveLine> analyticMoveLineList, BigDecimal total);

  public boolean validateLines(List<AnalyticDistributionLine> analyticDistributionLineList);

  @CallMethod
  boolean validateAnalyticMoveLines(List<AnalyticMoveLine> analyticDistributionLineList);

  AnalyticMoveLine computeAnalyticMoveLine(
      MoveLine moveLine, Company company, AnalyticAccount analyticAccount) throws AxelorException;

  AnalyticMoveLine computeAnalyticMoveLine(
      InvoiceLine invoiceLine, Invoice invoice, Company company, AnalyticAccount analyticAccount)
      throws AxelorException;

  AnalyticMoveLine computeAnalytic(Company company, AnalyticAccount analyticAccount)
      throws AxelorException;

  AnalyticMoveLine reverse(AnalyticMoveLine analyticMoveLine);

  AnalyticMoveLine reverseAndPersist(AnalyticMoveLine analyticMoveLine);

  AnalyticMoveLine generateAnalyticMoveLine(
      AnalyticMoveLine analyticMoveLine, AnalyticAccount analyticAccount, BigDecimal percentage);

  String getAnalyticAxisDomain(Company company) throws AxelorException;

  String getAnalyticJournalDomain(Company company) throws AxelorException;

  void setAnalyticCurrency(Company company, AnalyticMoveLine analyticMoveLine);
}
