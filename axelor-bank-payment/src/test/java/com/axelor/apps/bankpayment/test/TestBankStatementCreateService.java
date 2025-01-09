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
package com.axelor.apps.bankpayment.test;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementFileFormat;
import com.axelor.apps.bankpayment.db.EbicsPartner;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementCreateService;
import java.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestBankStatementCreateService {

  protected BankStatementCreateService bankStatementCreateService;

  @Before
  public void prepare() {
    bankStatementCreateService = new BankStatementCreateService();
  }

  @Test
  public void testComputeName() {

    EbicsPartner ebicsPartner = new EbicsPartner();
    ebicsPartner.setPartnerId("01");

    BankStatementFileFormat bankStatementFileFormat = new BankStatementFileFormat();
    bankStatementFileFormat.setName("camt.xxx.cfonb120.stm - Relevé de compte");
    bankStatementFileFormat.setStatementFileFormatSelect("camt.xxx.cfonb120.stm");

    BankStatement bankStatement = new BankStatement();
    bankStatement.setEbicsPartner(ebicsPartner);
    bankStatement.setBankStatementFileFormat(bankStatementFileFormat);
    bankStatement.setFromDate(LocalDate.of(2022, 7, 16));
    bankStatement.setToDate(LocalDate.of(2022, 7, 18));

    Assert.assertEquals(
        "01-camt.xxx.cfonb120.stm - Relevé de compte-2022/07/16-2022/07/18",
        bankStatementCreateService.computeName(bankStatement));
  }
}
