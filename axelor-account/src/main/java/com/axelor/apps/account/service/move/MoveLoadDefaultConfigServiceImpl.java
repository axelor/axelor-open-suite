package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.service.FiscalPositionAccountService;
import com.axelor.apps.base.db.Partner;
import com.axelor.common.ObjectUtils;
import com.axelor.inject.Beans;
import java.util.List;
import java.util.stream.Collectors;

public class MoveLoadDefaultConfigServiceImpl implements MoveLoadDefaultConfigService {

  @Override
  public Account getAccountingAccountFromAccountConfig(Move move) {
    List<AccountingSituation> accountConfigs =
        move.getPartner().getAccountingSituationList().stream()
            .filter(
                accountingSituation -> accountingSituation.getCompany().equals(move.getCompany()))
            .collect(Collectors.toList());
    Account accountingAccount = null;

    if (move.getJournal().getJournalType().getTechnicalTypeSelect()
        == JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE) {
      if (accountConfigs.size() > 0) {
        accountingAccount = accountConfigs.get(0).getDefaultExpenseAccount();
      }
    } else if (move.getJournal().getJournalType().getTechnicalTypeSelect()
        == JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE) {
      if (accountConfigs.size() > 0)
        accountingAccount = accountConfigs.get(0).getDefaultIncomeAccount();
    } else if (move.getJournal().getJournalType().getTechnicalTypeSelect()
        == JournalTypeRepository.TECHNICAL_TYPE_SELECT_TREASURY) {
      if (move.getPaymentMode() != null) {
        if (move.getPaymentMode().getInOutSelect().equals(PaymentModeRepository.IN)) {
          if (accountConfigs.size() > 0) {
            accountingAccount = accountConfigs.get(0).getCustomerAccount();
          }
        } else if (move.getPaymentMode().getInOutSelect().equals(PaymentModeRepository.OUT)) {
          if (accountConfigs.size() > 0) {
            accountingAccount = accountConfigs.get(0).getSupplierAccount();
          }
        }
      }
    }

    if (move.getPartner().getFiscalPosition() != null) {
      accountingAccount =
          Beans.get(FiscalPositionAccountService.class)
              .getAccount(move.getPartner().getFiscalPosition(), accountingAccount);
    }

    return accountingAccount;
  }

  @Override
  public TaxLine getTaxLine(Move move, MoveLine moveLine, Account accountingAccount) {
    List<TaxLine> taxLineList;
    TaxLine taxLine = null;
    Partner partner = move.getPartner();
    if (ObjectUtils.isEmpty(partner.getFiscalPosition())) {
      if (accountingAccount != null)
        if (accountingAccount.getDefaultTax() != null) {
          taxLine = accountingAccount.getDefaultTax().getActiveTaxLine();
          if (taxLine == null || !taxLine.getStartDate().isBefore(moveLine.getDate())) {
            taxLineList =
                accountingAccount.getDefaultTax().getTaxLineList().stream()
                    .filter(
                        tl ->
                            !moveLine.getDate().isBefore(tl.getEndDate())
                                && !tl.getStartDate().isAfter(moveLine.getDate()))
                    .collect(Collectors.toList());
            if (taxLineList.size() > 0) taxLine = taxLineList.get(0);
          }
        }
    } else {
      for (TaxEquiv taxEquiv : partner.getFiscalPosition().getTaxEquivList()) {
        if (accountingAccount != null)
          if (taxEquiv.getFromTax().equals(accountingAccount.getDefaultTax())) {
            taxLine = taxEquiv.getToTax().getActiveTaxLine();
            if (taxLine == null || !taxLine.getStartDate().isBefore(moveLine.getDate())) {
              taxLineList =
                  taxEquiv.getToTax().getTaxLineList().stream()
                      .filter(
                          tl ->
                              !moveLine.getDate().isBefore(tl.getEndDate())
                                  && !tl.getStartDate().isAfter(moveLine.getDate()))
                      .collect(Collectors.toList());
              if (taxLineList.size() > 0) taxLine = taxLineList.get(0);
            }
            break;
          }
      }
    }
    return taxLine;
  }
}
