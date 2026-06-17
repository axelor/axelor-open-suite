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

import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.repo.AnalyticLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.CurrencyScaleService;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class AnalyticLineComputeServiceImpl implements AnalyticLineComputeService {

  protected AnalyticMoveLineService analyticMoveLineService;
  protected AnalyticMoveLineRepository analyticMoveLineRepository;
  protected CurrencyScaleService currencyScaleService;

  @Inject
  public AnalyticLineComputeServiceImpl(
      AnalyticMoveLineService analyticMoveLineService,
      AnalyticMoveLineRepository analyticMoveLineRepository,
      CurrencyScaleService currencyScaleService) {
    this.analyticMoveLineService = analyticMoveLineService;
    this.analyticMoveLineRepository = analyticMoveLineRepository;
    this.currencyScaleService = currencyScaleService;
  }

  @Override
  public AnalyticLine computeAnalyticDistribution(
      AnalyticLine analyticLine, BigDecimal amount, LocalDate date) {

    List<AnalyticMoveLine> analyticMoveLineList = analyticLine.getAnalyticMoveLineList();

    if (analyticMoveLineList == null || analyticMoveLineList.isEmpty()) {
      createAnalyticDistributionWithTemplate(analyticLine, amount, date);
    } else {
      for (AnalyticMoveLine analyticMoveLine : analyticMoveLineList) {
        analyticMoveLineService.updateAnalyticMoveLine(analyticMoveLine, amount, date);
      }
    }
    updateAccountTypeOnAnalytic(analyticLine, analyticMoveLineList);

    return analyticLine;
  }

  @Override
  public AnalyticLine createAnalyticDistributionWithTemplate(
      AnalyticLine analyticLine, BigDecimal amount, LocalDate date) {

    List<AnalyticMoveLine> analyticMoveLineList =
        analyticMoveLineService.generateLines(
            analyticLine.getAnalyticDistributionTemplate(),
            amount,
            AnalyticMoveLineRepository.STATUS_REAL_ACCOUNTING,
            date);

    if (analyticLine.getAnalyticMoveLineList() == null) {
      analyticLine.setAnalyticMoveLineList(new ArrayList<>());
    } else {
      analyticLine.getAnalyticMoveLineList().clear();
    }
    for (AnalyticMoveLine analyticMoveLine : analyticMoveLineList) {
      analyticLine.addAnalyticMoveLineListItem(analyticMoveLine);
    }
    return analyticLine;
  }

  @Override
  public AnalyticLine analyzeAnalyticLine(
      AnalyticLine analyticLine, Company company, BigDecimal amount, LocalDate date)
      throws AxelorException {
    if (analyticLine == null) {
      return null;
    }

    if (analyticLine.getAnalyticMoveLineList() == null) {
      analyticLine.setAnalyticMoveLineList(new ArrayList<>());
    } else {
      analyticLine.getAnalyticMoveLineList().clear();
    }

    AnalyticAccount[] axisAccounts = {
      analyticLine.getAxis1AnalyticAccount(),
      analyticLine.getAxis2AnalyticAccount(),
      analyticLine.getAxis3AnalyticAccount(),
      analyticLine.getAxis4AnalyticAccount(),
      analyticLine.getAxis5AnalyticAccount()
    };

    for (AnalyticAccount analyticAccount : axisAccounts) {
      if (analyticAccount != null) {
        AnalyticMoveLine analyticMoveLine =
            computeAnalyticMoveLine(analyticLine, company, analyticAccount, amount, date);
        analyticLine.addAnalyticMoveLineListItem(analyticMoveLine);
      }
    }
    return analyticLine;
  }

  @Override
  public AnalyticLine clearAnalyticAccounting(AnalyticLine analyticLine) {
    analyticLine.setAxis1AnalyticAccount(null);
    analyticLine.setAxis2AnalyticAccount(null);
    analyticLine.setAxis3AnalyticAccount(null);
    analyticLine.setAxis4AnalyticAccount(null);
    analyticLine.setAxis5AnalyticAccount(null);

    if (!CollectionUtils.isEmpty(analyticLine.getAnalyticMoveLineList())) {
      analyticLine.getAnalyticMoveLineList().clear();
    }

    return analyticLine;
  }

  @Override
  public AnalyticLine clearAnalyticAccountingIfEmpty(AnalyticLine analyticLine) {
    if (analyticLine.getAxis1AnalyticAccount() == null
        && analyticLine.getAxis2AnalyticAccount() == null
        && analyticLine.getAxis3AnalyticAccount() == null
        && analyticLine.getAxis4AnalyticAccount() == null
        && analyticLine.getAxis5AnalyticAccount() == null) {
      if (!CollectionUtils.isEmpty(analyticLine.getAnalyticMoveLineList())) {
        analyticLine.getAnalyticMoveLineList().clear();
      }
    }

    return analyticLine;
  }

  @Override
  public void updateAccountTypeOnAnalytic(
      AnalyticLine analyticLine, List<AnalyticMoveLine> analyticMoveLineList) {

    if (analyticMoveLineList == null || analyticMoveLineList.isEmpty()) {
      return;
    }

    for (AnalyticMoveLine analyticMoveLine : analyticMoveLineList) {
      if (analyticLine.getAccount() != null) {
        analyticMoveLine.setAccount(analyticLine.getAccount());
        analyticMoveLine.setAccountType(analyticLine.getAccount().getAccountType());
      }
    }
  }

  @Override
  public void copyAnalyticMoveLines(
      AnalyticLine oldLine, AnalyticLine newLine, BigDecimal newLineAmount) {
    initializeAnalyticFields(oldLine, newLine);

    if (oldLine.getAnalyticMoveLineList() != null) {
      for (AnalyticMoveLine oldAnalyticMoveLine : oldLine.getAnalyticMoveLineList()) {
        AnalyticMoveLine analyticMoveLine =
            analyticMoveLineRepository.copy(oldAnalyticMoveLine, false);

        if (newLineAmount.signum() != 0 && analyticMoveLine.getPercentage().signum() != 0) {
          int signum =
              analyticMoveLine.getAmount().signum() != 0
                  ? analyticMoveLine.getAmount().signum()
                  : 1;
          BigDecimal amount =
              newLineAmount.multiply(
                  analyticMoveLine
                      .getPercentage()
                      .multiply(BigDecimal.valueOf(signum))
                      .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP));
          analyticMoveLine.setAmount(
              currencyScaleService.getCompanyScaledValue(analyticMoveLine, amount));
        }

        analyticMoveLine.setTypeSelect(AnalyticMoveLineRepository.STATUS_REAL_ACCOUNTING);

        newLine.addAnalyticMoveLineListItem(analyticMoveLine);
      }
    }
  }

  protected void initializeAnalyticFields(AnalyticLine oldLine, AnalyticLine newLine) {
    newLine.setAnalyticDistributionTemplate(oldLine.getAnalyticDistributionTemplate());

    newLine.setAxis1AnalyticAccount(oldLine.getAxis1AnalyticAccount());
    newLine.setAxis2AnalyticAccount(oldLine.getAxis2AnalyticAccount());
    newLine.setAxis3AnalyticAccount(oldLine.getAxis3AnalyticAccount());
    newLine.setAxis4AnalyticAccount(oldLine.getAxis4AnalyticAccount());
    newLine.setAxis5AnalyticAccount(oldLine.getAxis5AnalyticAccount());
  }

  protected AnalyticMoveLine computeAnalyticMoveLine(
      AnalyticLine analyticLine,
      Company company,
      AnalyticAccount analyticAccount,
      BigDecimal amount,
      LocalDate date)
      throws AxelorException {
    AnalyticMoveLine analyticMoveLine =
        analyticMoveLineService.computeAnalytic(company, analyticAccount);
    analyticMoveLineService.setAnalyticCurrency(company, analyticMoveLine);

    analyticMoveLine.setDate(date);
    if (analyticLine.getAccount() != null) {
      analyticMoveLine.setAccount(analyticLine.getAccount());
      analyticMoveLine.setAccountType(analyticLine.getAccount().getAccountType());
    }
    analyticMoveLine.setAmount(amount);
    return analyticMoveLine;
  }
}
