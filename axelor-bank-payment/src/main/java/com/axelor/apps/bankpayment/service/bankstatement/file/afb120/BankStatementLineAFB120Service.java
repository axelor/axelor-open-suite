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
package com.axelor.apps.bankpayment.service.bankstatement.file.afb120;

import com.axelor.apps.account.db.InterbankCodeLine;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.BankStatementLineAFB120;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementLineService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Currency;
import com.axelor.db.mapper.Mapper;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;

public class BankStatementLineAFB120Service extends BankStatementLineService {

  @Inject
  public BankStatementLineAFB120Service() {

    super();
  }

  public BankStatementLineAFB120 createBankStatementLine(
      BankStatement bankStatement,
      int sequence,
      BankDetails bankDetails,
      BigDecimal debit,
      BigDecimal credit,
      Currency currency,
      String description,
      LocalDate operationDate,
      LocalDate valueDate,
      InterbankCodeLine operationInterbankCodeLine,
      InterbankCodeLine rejectInterbankCodeLine,
      String origin,
      String reference,
      int lineType,
      String unavailabilityIndexSelect,
      String commissionExemptionIndexSelect) {

    BankStatementLine bankStatementLine =
        super.createBankStatementLine(
            bankStatement,
            sequence,
            bankDetails,
            debit,
            credit,
            currency,
            description,
            operationDate,
            valueDate,
            operationInterbankCodeLine,
            rejectInterbankCodeLine,
            origin,
            reference);

    BankStatementLineAFB120 bankStatementLineAFB120 =
        Mapper.toBean(BankStatementLineAFB120.class, Mapper.toMap(bankStatementLine));

    bankStatementLineAFB120.setLineTypeSelect(lineType);

    if (lineType != BankStatementLineAFB120Repository.LINE_TYPE_MOVEMENT) {
      bankStatementLineAFB120.setAmountRemainToReconcile(BigDecimal.ZERO);
    }

    if (!Strings.isNullOrEmpty(unavailabilityIndexSelect)) {
      bankStatementLineAFB120.setUnavailabilityIndexSelect(
          Integer.parseInt(unavailabilityIndexSelect));
    }
    if (!Strings.isNullOrEmpty(commissionExemptionIndexSelect)) {
      bankStatementLineAFB120.setCommissionExemptionIndexSelect(
          Integer.parseInt(commissionExemptionIndexSelect));
    }

    return bankStatementLineAFB120;
  }
}
