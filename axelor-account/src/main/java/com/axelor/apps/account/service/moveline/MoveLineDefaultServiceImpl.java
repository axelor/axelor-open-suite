package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.move.MoveLoadDefaultConfigService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import org.apache.commons.collections.CollectionUtils;

import java.math.BigDecimal;

public class MoveLineDefaultServiceImpl implements MoveLineDefaultService {
  protected AppAccountService appAccountService;
  protected MoveLoadDefaultConfigService moveLoadDefaultConfigService;

  @Inject
  public MoveLineDefaultServiceImpl(
      AppAccountService appAccountService,
      MoveLoadDefaultConfigService moveLoadDefaultConfigService) {
    this.appAccountService = appAccountService;
    this.moveLoadDefaultConfigService = moveLoadDefaultConfigService;
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

    if (accountingAccount != null) {
      moveLine.setAccount(accountingAccount);

      if (!accountingAccount.getUseForPartnerBalance()) {
        moveLine.setPartner(null);
      }

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
    if (move == null
        || !move.getGetInfoFromFirstMoveLineOk()
        || CollectionUtils.isEmpty(move.getMoveLineList())) {
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
  public void setIsOtherCurrency(MoveLine moveLine, Move move) {
    if (move == null) {
      return;
    }

    moveLine.setIsOtherCurrency(!move.getCurrency().equals(move.getCompanyCurrency()));
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
}
