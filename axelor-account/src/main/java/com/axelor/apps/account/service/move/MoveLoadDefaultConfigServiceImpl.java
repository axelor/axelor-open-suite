package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.JournalType;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.FiscalPositionAccountService;
import com.axelor.apps.base.db.Partner;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.List;

public class MoveLoadDefaultConfigServiceImpl implements MoveLoadDefaultConfigService {

  protected FiscalPositionAccountService fiscalPositionAccountService;
  protected AccountingSituationService accountingSituationService;

  @Inject
  public MoveLoadDefaultConfigServiceImpl(
      FiscalPositionAccountService fiscalPositionAccountService,
      AccountingSituationService accountingSituationService) {
    this.fiscalPositionAccountService = fiscalPositionAccountService;
    this.accountingSituationService = accountingSituationService;
  }

  @Override
  public Account getAccountingAccountFromAccountConfig(Move move) {
    AccountingSituation accountSituation =
        accountingSituationService.getAccountingSituation(move.getPartner(), move.getCompany());
    Account accountingAccount = null;

    JournalType journalType = move.getJournal().getJournalType();
    if (journalType != null) {
      if (journalType.getTechnicalTypeSelect()
          == JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE) {
        accountingAccount = accountSituation.getDefaultExpenseAccount();
      } else if (journalType.getTechnicalTypeSelect()
          == JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE) {
        accountingAccount = accountSituation.getDefaultIncomeAccount();
      } else if (journalType.getTechnicalTypeSelect()
          == JournalTypeRepository.TECHNICAL_TYPE_SELECT_TREASURY) {
        if (move.getPaymentMode() != null) {
          if (move.getPaymentMode().getInOutSelect().equals(PaymentModeRepository.IN)) {
            accountingAccount = accountSituation.getCustomerAccount();
          } else if (move.getPaymentMode().getInOutSelect().equals(PaymentModeRepository.OUT)) {
            accountingAccount = accountSituation.getSupplierAccount();
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
  public TaxLine getTaxLine(Move move, MoveLine moveLine, Account accountingAccount) {
    TaxLine taxLine = null;
    Partner partner = move.getPartner();
    if (ObjectUtils.isEmpty(partner.getFiscalPosition())) {
      if (accountingAccount != null && accountingAccount.getDefaultTax() != null) {
        taxLine = accountingAccount.getDefaultTax().getActiveTaxLine();
        if (taxLine == null || !taxLine.getStartDate().isBefore(moveLine.getDate())) {
          taxLine =
              findValidTaxLineForMoveLine(
                  accountingAccount.getDefaultTax().getTaxLineList(), moveLine);
        }
      }
    } else {
      for (TaxEquiv taxEquiv : partner.getFiscalPosition().getTaxEquivList()) {
        if (accountingAccount != null
            && taxEquiv.getFromTax().equals(accountingAccount.getDefaultTax())) {
          taxLine = taxEquiv.getToTax().getActiveTaxLine();
          if (taxLine == null || !taxLine.getStartDate().isBefore(moveLine.getDate())) {
            taxLine = findValidTaxLineForMoveLine(taxEquiv.getToTax().getTaxLineList(), moveLine);
          }
          break;
        }
      }
    }
    return taxLine;
  }

  protected TaxLine findValidTaxLineForMoveLine(List<TaxLine> taxLineList, MoveLine moveLine) {
    return taxLineList.stream()
        .filter(
            tl ->
                !moveLine.getDate().isBefore(tl.getEndDate())
                    && !tl.getStartDate().isAfter(moveLine.getDate()))
        .findFirst()
        .orElse(null);
  }
}
