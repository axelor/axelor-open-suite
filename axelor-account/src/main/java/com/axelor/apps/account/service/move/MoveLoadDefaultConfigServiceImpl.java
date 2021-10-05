package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.JournalType;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.service.FiscalPositionAccountService;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class MoveLoadDefaultConfigServiceImpl implements MoveLoadDefaultConfigService {

  protected FiscalPositionAccountService fiscalPositionAccountService;
  protected TaxService taxService;

  @Inject
  public MoveLoadDefaultConfigServiceImpl(
      FiscalPositionAccountService fiscalPositionAccountService, TaxService taxService) {
    this.fiscalPositionAccountService = fiscalPositionAccountService;
    this.taxService = taxService;
  }

  @Override
  public Account getAccountingAccountFromAccountConfig(Move move) {
    List<AccountingSituation> accountConfigs =
        move.getPartner().getAccountingSituationList().stream()
            .filter(
                accountingSituation -> accountingSituation.getCompany().equals(move.getCompany()))
            .collect(Collectors.toList());
    Account accountingAccount = null;

    JournalType journalType = move.getJournal().getJournalType();
    if (journalType != null && accountConfigs.size() > 0) {
      if (journalType.getTechnicalTypeSelect()
          == JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE) {
        accountingAccount = accountConfigs.get(0).getDefaultExpenseAccount();
      } else if (journalType.getTechnicalTypeSelect()
          == JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE) {
        accountingAccount = accountConfigs.get(0).getDefaultIncomeAccount();
      } else if (journalType.getTechnicalTypeSelect()
          == JournalTypeRepository.TECHNICAL_TYPE_SELECT_TREASURY) {
        if (move.getPaymentMode() != null) {
          if (move.getPaymentMode().getInOutSelect().equals(PaymentModeRepository.IN)) {
            accountingAccount = accountConfigs.get(0).getCustomerAccount();
          } else if (move.getPaymentMode().getInOutSelect().equals(PaymentModeRepository.OUT)) {
            accountingAccount = accountConfigs.get(0).getSupplierAccount();
          }
        }
      }
    }
    if (move.getPartner().getFiscalPosition() != null) {
      accountingAccount =
          fiscalPositionAccountService.getAccount(
              move.getPartner().getFiscalPosition(), accountingAccount);
    }

    return accountingAccount;
  }

  @Override
  public TaxLine getTaxLine(Move move, MoveLine moveLine, Account accountingAccount)
      throws AxelorException {
    Tax tax;
    TaxLine taxLine;
    Partner partner = move.getPartner();
    if (accountingAccount == null || accountingAccount.getDefaultTax() == null) {
      return null;
    }
    tax = accountingAccount.getDefaultTax();

    if (!ObjectUtils.isEmpty(partner) && !ObjectUtils.isEmpty(partner.getFiscalPosition())) {
      tax = fiscalPositionAccountService.getTax(partner.getFiscalPosition(), tax);
    }

    taxLine = taxService.getTaxLine(tax, moveLine.getDate());
    return taxLine;
  }
}
