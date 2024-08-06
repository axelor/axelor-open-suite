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
package com.axelor.apps.bankpayment.service.bankstatementline;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class BankStatementLineDeleteServiceImpl implements BankStatementLineDeleteService {

  protected BankStatementLineRepository bankStatementLineRepository;

  @Inject
  public BankStatementLineDeleteServiceImpl(
      BankStatementLineRepository bankStatementLineRepository) {
    this.bankStatementLineRepository = bankStatementLineRepository;
  }

  @Transactional
  public void deleteBankStatementLines(BankStatement bankStatement) {
    List<BankStatementLine> bankStatementLines;
    bankStatementLines =
        bankStatementLineRepository
            .all()
            .filter("self.bankStatement = :bankStatement")
            .bind("bankStatement", bankStatement)
            .fetch();
    for (BankStatementLine bsl : bankStatementLines) {
      bankStatementLineRepository.remove(bsl);
    }
  }
}
