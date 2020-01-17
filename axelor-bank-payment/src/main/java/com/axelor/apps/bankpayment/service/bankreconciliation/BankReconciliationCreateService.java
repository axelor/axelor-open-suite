/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.service.bankreconciliation;

import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationRepository;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.db.JPA;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Query;

public class BankReconciliationCreateService {

  protected BankReconciliationRepository bankReconciliationRepository;
  protected CompanyRepository companyRepository;

  @Inject
  public BankReconciliationCreateService(
      BankReconciliationRepository bankReconciliationRepository,
      CompanyRepository companyRepository) {

    this.bankReconciliationRepository = bankReconciliationRepository;
    this.companyRepository = companyRepository;
  }

  @Transactional
  public List<BankReconciliation> createAllFromBankStatement(BankStatement bankStatement)
      throws IOException {

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
              .filter("?1 member of self.bankDetailsSet", bankDetails)
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
      BankStatement bankStatement)
      throws IOException {

    BankReconciliation bankReconciliation = new BankReconciliation();
    bankReconciliation.setCompany(company);
    bankReconciliation.setFromDate(fromDate);
    bankReconciliation.setToDate(toDate);
    bankReconciliation.setCurrency(currency);
    bankReconciliation.setBankDetails(bankDetails);
    bankReconciliation.setBankStatement(bankStatement);
    bankReconciliation.setName(this.computeName(bankReconciliation));

    return bankReconciliation;
  }

  public String computeName(BankReconciliation bankReconciliation) {

    String name = "";
    if (bankReconciliation.getCompany() != null) {
      name += bankReconciliation.getCompany().getCode();
    }
    if (bankReconciliation.getCurrency() != null) {
      if (name != "") {
        name += "-";
      }
      name += bankReconciliation.getCurrency().getCode();
    }
    if (bankReconciliation.getBankDetails() != null) {
      if (name != "") {
        name += "-";
      }
      name += bankReconciliation.getBankDetails().getAccountNbr();
    }
    if (bankReconciliation.getFromDate() != null) {
      if (name != "") {
        name += "-";
      }
      name += bankReconciliation.getFromDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
    }
    if (bankReconciliation.getToDate() != null) {
      if (name != "") {
        name += "-";
      }
      name += bankReconciliation.getToDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
    }

    return name;
  }
}
