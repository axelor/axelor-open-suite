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
package com.axelor.apps.bankpayment.service.bankstatementline;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineRepository;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.db.Query;
import com.google.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class BankStatementLineFetchServiceImpl implements BankStatementLineFetchService {

  protected BankStatementLineRepository bankStatementLineRepository;

  @Inject
  public BankStatementLineFetchServiceImpl(
      BankStatementLineRepository bankStatementLineRepository) {
    this.bankStatementLineRepository = bankStatementLineRepository;
  }

  @Override
  public List<BankStatementLine> getBankStatementLines(BankStatement bankStatement) {
    return bankStatementLineRepository.findByBankStatement(bankStatement).fetch();
  }

  @Override
  public List<BankDetails> getBankDetailsFromStatementLines(BankStatement bankStatement) {
    return bankStatementLineRepository.findByBankStatement(bankStatement).fetch().stream()
        .map(BankStatementLine::getBankDetails)
        .distinct()
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Override
  public Query<BankStatementLine> findByBankStatementBankDetailsAndLineType(
      BankStatement bankStatement, BankDetails bankDetails, int lineType) {
    return bankStatementLineRepository
        .all()
        .filter(
            "self.bankStatement = :bankStatement"
                + " AND self.bankDetails = :bankDetails"
                + " AND self.lineTypeSelect = :lineTypeSelect")
        .bind("bankStatement", bankStatement)
        .bind("bankDetails", bankDetails)
        .bind("lineTypeSelect", lineType);
  }

  @Override
  public Query<BankStatementLine> findByBankDetailsLineTypeExcludeBankStatement(
      BankStatement bankStatement, BankDetails bankDetails, int lineType) {
    return bankStatementLineRepository
        .all()
        .filter(
            "self.bankStatement != :bankStatement"
                + " AND self.bankDetails = :bankDetails"
                + " AND self.lineTypeSelect = :lineTypeSelect")
        .bind("bankStatement", bankStatement)
        .bind("bankDetails", bankDetails)
        .bind("lineTypeSelect", lineType);
  }

  @Override
  public BankStatementLine getLastBankStatementLineFromBankDetails(BankDetails bankDetails) {
    if (bankDetails == null) {
      return null;
    }
    Optional<BankStatementLine> id =
        bankStatementLineRepository
            .all()
            .filter(
                "self.bankDetails is not null AND self.bankDetails.id = :bankDetailsId"
                    + " AND self.lineTypeSelect = :lineTypeSelect")
            .bind("bankDetailsId", bankDetails.getId())
            .bind("lineTypeSelect", BankStatementLineRepository.LINE_TYPE_FINAL_BALANCE)
            .fetchStream()
            .min(Comparator.comparing(BankStatementLine::getOperationDate));
    return id.map(line -> bankStatementLineRepository.find(line.getId())).orElse(null);
  }
}
