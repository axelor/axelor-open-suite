package com.axelor.apps.bankpayment.service.bankreconciliation;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountType;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class BankReconciliationDomainServiceImpl implements BankReconciliationDomainService {

  protected BankDetailsService bankDetailsService;
  protected BankReconciliationAccountService bankReconciliationAccountService;
  protected MoveLineRepository moveLineRepository;
  protected BankReconciliationQueryService bankReconciliationQueryService;

  @Inject
  public BankReconciliationDomainServiceImpl(
      BankDetailsService bankDetailsService,
      BankReconciliationAccountService bankReconciliationAccountService,
      MoveLineRepository moveLineRepository,
      BankReconciliationQueryService bankReconciliationQueryService) {
    this.bankDetailsService = bankDetailsService;
    this.bankReconciliationAccountService = bankReconciliationAccountService;
    this.moveLineRepository = moveLineRepository;
    this.bankReconciliationQueryService = bankReconciliationQueryService;
  }

  @Override
  public String getDomainForWizard(
      BankReconciliation bankReconciliation,
      BigDecimal bankStatementCredit,
      BigDecimal bankStatementDebit) {
    if (bankReconciliation != null
        && bankReconciliation.getCompany() != null
        && bankStatementCredit != null
        && bankStatementDebit != null) {
      String query =
          "self.move.company.id = "
              + bankReconciliation.getCompany().getId()
              + " AND self.move.currency.id = "
              + bankReconciliation.getCurrency().getId()
              + " AND (self.move.statusSelect = "
              + MoveRepository.STATUS_ACCOUNTED
              + " OR self.move.statusSelect = "
              + MoveRepository.STATUS_DAYBOOK
              + ")"
              + " AND abs(self.currencyAmount) > 0 AND self.bankReconciledAmount < abs(self.currencyAmount) ";

      if (bankStatementCredit.signum() > 0) {
        query = query.concat(" AND self.debit > 0");
      }
      if (bankStatementDebit.signum() > 0) {
        query = query.concat(" AND self.credit > 0");
      }
      if (bankReconciliation.getCashAccount() != null) {
        query =
            query.concat(" AND self.account.id = " + bankReconciliation.getCashAccount().getId());
      } else {
        query =
            query.concat(
                " AND self.account.accountType.technicalTypeSelect LIKE '"
                    + AccountTypeRepository.TYPE_CASH
                    + "'");
      }
      if (bankReconciliation.getJournal() != null) {
        query =
            query.concat(" AND self.move.journal.id = " + bankReconciliation.getJournal().getId());
      } else {
        query =
            query.concat(
                " AND self.move.journal.journalType.technicalTypeSelect = "
                    + JournalTypeRepository.TECHNICAL_TYPE_SELECT_TREASURY);
      }
      return query;
    }
    return "self id in (0)";
  }

  @Override
  public String getAccountDomain(BankReconciliation bankReconciliation) {
    if (bankReconciliation != null) {
      String domain = "self.statusSelect = " + AccountRepository.STATUS_ACTIVE;
      if (bankReconciliation.getCompany() != null) {
        domain = domain.concat(" AND self.company.id = " + bankReconciliation.getCompany().getId());
      }
      if (bankReconciliation.getCashAccount() != null) {
        domain = domain.concat(" AND self.id != " + bankReconciliation.getCashAccount().getId());
      }
      if (bankReconciliation.getJournal() != null
          && !CollectionUtils.isEmpty(bankReconciliation.getJournal().getValidAccountTypeSet())) {
        domain =
            domain.concat(
                " AND (self.accountType.id IN "
                    + bankReconciliation.getJournal().getValidAccountTypeSet().stream()
                        .map(AccountType::getId)
                        .map(id -> id.toString())
                        .collect(Collectors.joining("','", "('", "')"))
                        .toString());
      } else {
        domain = domain.concat(" AND (self.accountType.id = 0");
      }
      if (bankReconciliation.getJournal() != null
          && !CollectionUtils.isEmpty(bankReconciliation.getJournal().getValidAccountSet())) {
        domain =
            domain.concat(
                " OR self.id IN "
                    + bankReconciliation.getJournal().getValidAccountSet().stream()
                        .map(Account::getId)
                        .map(id -> id.toString())
                        .collect(Collectors.joining("','", "('", "')"))
                        .toString()
                    + ")");
      } else {
        domain = domain.concat(" OR self.id = 0)");
      }
      return domain;
    }
    return "self.id = 0";
  }

  @Override
  public String getCashAccountDomain(BankReconciliation bankReconciliation) {

    String cashAccountIds = null;
    Set<String> cashAccountIdSet = new HashSet<String>();

    cashAccountIdSet.addAll(
        bankReconciliationAccountService.getAccountManagementCashAccounts(bankReconciliation));

    if (bankReconciliation.getBankDetails().getBankAccount() != null) {
      cashAccountIdSet.add(bankReconciliation.getBankDetails().getBankAccount().getId().toString());
    }

    cashAccountIds = String.join(",", cashAccountIdSet);

    return cashAccountIds;
  }

  @Override
  public String createDomainForMoveLine(BankReconciliation bankReconciliation)
      throws AxelorException {
    String domain = "";
    List<MoveLine> authorizedMoveLines =
        moveLineRepository
            .all()
            .filter(bankReconciliationQueryService.getRequestMoveLines(bankReconciliation))
            .bind(bankReconciliationQueryService.getBindRequestMoveLine(bankReconciliation))
            .fetch();

    String idList = StringHelper.getIdListString(authorizedMoveLines);
    if (idList.equals("")) {
      domain = "self.id IN (0)";
    } else {
      domain = "self.id IN (" + idList + ")";
    }
    return domain;
  }

  @Override
  public String getJournalDomain(BankReconciliation bankReconciliation) {

    String journalIds = null;
    Set<String> journalIdSet = new HashSet<String>();

    journalIdSet.addAll(
        bankReconciliationAccountService.getAccountManagementJournals(bankReconciliation));

    if (bankReconciliation.getBankDetails().getJournal() != null) {
      journalIdSet.add(bankReconciliation.getBankDetails().getJournal().getId().toString());
    }

    journalIds = String.join(",", journalIdSet);

    return journalIds;
  }

  @Override
  public String createDomainForBankDetails(BankReconciliation bankReconciliation) {

    return bankDetailsService.getActiveCompanyBankDetails(
        bankReconciliation.getCompany(), bankReconciliation.getCurrency());
  }
}
