/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.service.bankreconciliation;

import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineRepository;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.db.JPA;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Query;

public class BankReconciliationCreateService {

  protected BankReconciliationRepository bankReconciliationRepository;
  protected CompanyRepository companyRepository;
  protected BankReconciliationAccountService bankReconciliationAccountService;
  protected BankStatementLineRepository bankStatementLineRepository;
  protected BankReconciliationComputeNameService bankReconciliationComputeNameService;

  @Inject
  public BankReconciliationCreateService(
      BankReconciliationRepository bankReconciliationRepository,
      CompanyRepository companyRepository,
      BankReconciliationAccountService bankReconciliationAccountService,
      BankStatementLineRepository bankStatementLineRepository,
      BankReconciliationComputeNameService bankReconciliationComputeNameService) {

    this.bankReconciliationRepository = bankReconciliationRepository;
    this.companyRepository = companyRepository;
    this.bankReconciliationAccountService = bankReconciliationAccountService;
    this.bankStatementLineRepository = bankStatementLineRepository;
    this.bankReconciliationComputeNameService = bankReconciliationComputeNameService;
  }

  @Transactional(rollbackOn = {Exception.class})
  public List<BankReconciliation> createAllFromBankStatement(BankStatement bankStatement) {

    List<BankReconciliation> bankReconciliationList = new ArrayList<>();

    List<BankDetails> bankDetailsList = getDistinctBankDetails(bankStatement);

    if (bankDetailsList == null) {
      return bankReconciliationList;
    }

    LocalDate fromDate = bankStatement.getFromDate();
    LocalDate toDate = bankStatement.getToDate();

    for (BankDetails bankDetails : bankDetailsList) {

      Company company =
          companyRepository
              .all()
              .filter("?1 member of self.bankDetailsList", bankDetails)
              .fetchOne();

      Currency currency = bankDetails.getCurrency();
      if (currency == null) {
        currency = company.getCurrency();
      }

      BankReconciliation bankReconciliation =
          createBankReconciliation(company, fromDate, toDate, currency, bankDetails, bankStatement);
      bankReconciliationRepository.save(bankReconciliation);
      bankReconciliationList.add(bankReconciliation);
    }

    return bankReconciliationList;
  }

  /**
   * Get all distinct bank details that appear on the bank statement.
   *
   * @param bankStatement
   * @return List of bank details
   */
  protected List<BankDetails> getDistinctBankDetails(BankStatement bankStatement) {

    Query q =
        JPA.em()
            .createQuery(
                "SELECT DISTINCT(BSL.bankDetails) FROM BankStatementLine BSL WHERE BSL.bankStatement = :bankStatement AND BSL.amountRemainToReconcile > 0");

    q.setParameter("bankStatement", bankStatement);

    return q.getResultList();
  }

  public BankReconciliation createBankReconciliation(
      Company company,
      LocalDate fromDate,
      LocalDate toDate,
      Currency currency,
      BankDetails bankDetails,
      BankStatement bankStatement) {

    BankReconciliation bankReconciliation = new BankReconciliation();
    bankReconciliation.setCompany(company);
    bankReconciliation.setFromDate(fromDate);
    bankReconciliation.setToDate(toDate);
    bankReconciliation.setCurrency(currency);
    bankReconciliation.setBankDetails(bankDetails);
    bankReconciliation.setBankStatement(bankStatement);
    bankReconciliation.setName(
        bankReconciliationComputeNameService.computeName(bankReconciliation));
    bankReconciliation.setJournal(bankReconciliationAccountService.getJournal(bankReconciliation));
    bankReconciliation.setCashAccount(
        bankReconciliationAccountService.getCashAccount(bankReconciliation));

    return bankReconciliation;
  }
}
