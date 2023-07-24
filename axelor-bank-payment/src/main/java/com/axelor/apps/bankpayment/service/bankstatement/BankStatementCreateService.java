/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.service.bankstatement;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementFileFormat;
import com.axelor.apps.bankpayment.db.EbicsPartner;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BankStatementCreateService {

  public BankStatement createBankStatement(
      File file,
      LocalDate fromDate,
      LocalDate toDate,
      BankStatementFileFormat bankStatementFileFormat,
      EbicsPartner ebicsPartner,
      LocalDateTime executionDateTime)
      throws IOException {

    BankStatement bankStatement = new BankStatement();
    bankStatement.setFromDate(fromDate);
    bankStatement.setToDate(toDate);
    bankStatement.setBankStatementFileFormat(bankStatementFileFormat);
    bankStatement.setEbicsPartner(ebicsPartner);
    bankStatement.setGetDateTime(executionDateTime);
    bankStatement.setBankStatementFile(Beans.get(MetaFiles.class).upload(file));
    bankStatement.setName(this.computeName(bankStatement));
    bankStatement.setStatusSelect(BankStatementRepository.STATUS_RECEIVED);
    return bankStatement;
  }

  public String computeName(BankStatement bankStatement) {

    String name = "";

    if (bankStatement.getEbicsPartner() != null) {
      name += bankStatement.getEbicsPartner().getPartnerId();
    }

    if (bankStatement.getBankStatementFileFormat() != null) {
      if (name != "") {
        name += "-";
      }
      name += bankStatement.getBankStatementFileFormat().getName();
    }

    try {
      if (bankStatement.getFromDate() != null) {
        if (name != "") {
          name += "-";
        }
        name += bankStatement.getFromDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
      }
      if (bankStatement.getToDate() != null) {
        if (name != "") {
          name += "-";
        }
        name += bankStatement.getToDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
      }
    } catch (Exception e) {
    }

    return name;
  }
}
