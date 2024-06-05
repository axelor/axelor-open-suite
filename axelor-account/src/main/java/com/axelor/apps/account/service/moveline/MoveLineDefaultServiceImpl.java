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
package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.move.MoveLoadDefaultConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.config.CompanyConfigService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Objects;
import org.apache.commons.collections.CollectionUtils;

public class MoveLineDefaultServiceImpl implements MoveLineDefaultService {
  protected AppAccountService appAccountService;
  protected MoveLoadDefaultConfigService moveLoadDefaultConfigService;
  protected MoveLineComputeAnalyticService moveLineComputeAnalyticService;
  protected CompanyConfigService companyConfigService;

  @Inject
  public MoveLineDefaultServiceImpl(
      AppAccountService appAccountService,
      MoveLoadDefaultConfigService moveLoadDefaultConfigService,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService,
      CompanyConfigService companyConfigService) {
    this.appAccountService = appAccountService;
    this.moveLoadDefaultConfigService = moveLoadDefaultConfigService;
    this.moveLineComputeAnalyticService = moveLineComputeAnalyticService;
    this.companyConfigService = companyConfigService;
  }

  @Override
  public void setFieldsFromParent(MoveLine moveLine, Move move) {
    if (move == null) {
      return;
    }

    moveLine.setPartner(move.getPartner());
    moveLine.setDate(move.getDate());
    moveLine.setDueDate(move.getDate());
    moveLine.setOriginDate(move.getOriginDate());
    moveLine.setOrigin(move.getOrigin());
    moveLine.setDescription(move.getDescription());
  }

  @Override
  public void setAccountInformation(MoveLine moveLine, Move move) throws AxelorException {
    if (move == null || move.getPartner() == null) {
      return;
    }

    Account accountingAccount =
        moveLoadDefaultConfigService.getAccountingAccountFromAccountConfig(move);

    if (accountingAccount != null
        && (ObjectUtils.isEmpty(move.getMoveLineList())
            || move.getMoveLineList().size() == 1
                && Objects.equals(move.getMoveLineList().get(0), moveLine))) {
      moveLine.setAccount(accountingAccount);

      AnalyticDistributionTemplate analyticDistributionTemplate =
          accountingAccount.getAnalyticDistributionTemplate();

      if (accountingAccount.getAnalyticDistributionAuthorized()
          && analyticDistributionTemplate != null) {
        moveLine.setAnalyticDistributionTemplate(analyticDistributionTemplate);
      }
    }

    TaxLine taxLine = moveLoadDefaultConfigService.getTaxLine(move, moveLine, accountingAccount);
    moveLine.setTaxLine(taxLine);
  }

  @Override
  public void setFieldsFromFirstMoveLine(MoveLine moveLine, Move move) {
    if (move == null || CollectionUtils.isEmpty(move.getMoveLineList())) {
      return;
    }

    MoveLine firstMoveLine = move.getMoveLineList().get(0);

    moveLine.setPartner(firstMoveLine.getPartner());
    moveLine.setOrigin(firstMoveLine.getOrigin());
    moveLine.setDescription(firstMoveLine.getDescription());
    moveLine.setInterbankCodeLine(firstMoveLine.getInterbankCodeLine());
    moveLine.setExportedDirectDebitOk(firstMoveLine.getExportedDirectDebitOk());
  }

  @Override
  public void setIsOtherCurrency(MoveLine moveLine, Move move) throws AxelorException {
    if (move == null) {
      return;
    }

    Currency companyCurrency = companyConfigService.getCompanyCurrency(move.getCompany());

    if (move.getCurrency() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.MOVE_12),
          move.getReference());
    }

    moveLine.setIsOtherCurrency(!move.getCurrency().equals(companyCurrency));
  }

  @Override
  public void setFinancialDiscount(MoveLine moveLine) {
    if (!appAccountService.getAppAccount().getManageFinancialDiscount()
        || moveLine.getPartner() == null) {
      return;
    }

    moveLine.setFinancialDiscount(moveLine.getPartner().getFinancialDiscount());
  }

  @Override
  public void cleanDebitCredit(MoveLine moveLine) {
    if (moveLine.getDebit().signum() < 0) {
      moveLine.setDebit(BigDecimal.ZERO);
    }

    if (moveLine.getCredit().signum() < 0) {
      moveLine.setCredit(BigDecimal.ZERO);
    }
  }

  @Override
  public void setDefaultDistributionTemplate(MoveLine moveLine, Move move) throws AxelorException {
    if (move != null && moveLineComputeAnalyticService.checkManageAnalytic(move.getCompany())) {
      moveLineComputeAnalyticService.selectDefaultDistributionTemplate(moveLine);
    }
  }
}
