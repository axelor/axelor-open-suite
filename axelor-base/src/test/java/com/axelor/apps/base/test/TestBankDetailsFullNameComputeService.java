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
package com.axelor.apps.base.test;

import com.axelor.apps.base.db.Bank;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.service.BankDetailsFullNameComputeService;
import com.axelor.apps.base.service.BankDetailsFullNameComputeServiceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestBankDetailsFullNameComputeService {

  protected BankDetailsFullNameComputeService bankDetailsFullNameComputeService;

  @Before
  public void prepare() {
    bankDetailsFullNameComputeService = new BankDetailsFullNameComputeServiceImpl();
  }

  @Test
  public void testComputeEmptyFullName() {
    BankDetails emptyBankDetails = new BankDetails();

    String result = bankDetailsFullNameComputeService.computeBankDetailsFullName(emptyBankDetails);

    Assert.assertEquals("", result);
  }

  @Test
  public void testComputeFullnameMinimal() {
    Bank bank = createBank("CCCCFRPPXXX - BNP Paribas");
    BankDetails bankDetails = createBankDetails(bank, "FR7699333020961963437764029", "", "");

    String result = bankDetailsFullNameComputeService.computeBankDetailsFullName(bankDetails);

    Assert.assertEquals("FR7699333020961963437764029 - CCCCFRPPXXX - BNP Paribas", result);
  }

  @Test
  public void testComputeFullnameWithLabel() {
    Bank bank = createBank("CCCCFRPPXXX - BNP Paribas");
    BankDetails bankDetails = createBankDetails(bank, "FR7699333020961963437764029", "Axelor", "");

    String result = bankDetailsFullNameComputeService.computeBankDetailsFullName(bankDetails);

    Assert.assertEquals("Axelor - FR7699333020961963437764029 - CCCCFRPPXXX - BNP Paribas", result);
  }

  @Test
  public void testComputeFullnameWithCode() {
    BankDetails bankDetails3 = new BankDetails();
    Bank bank3 = createBank("CCCCFRPPXXX - BNP Paribas");
    bankDetails3.setBank(bank3);
    bankDetails3.setIban("FR7699333020961963437764029");
    bankDetails3.setCode("AXE");

    String result = bankDetailsFullNameComputeService.computeBankDetailsFullName(bankDetails3);

    Assert.assertEquals("AXE - FR7699333020961963437764029 - CCCCFRPPXXX - BNP Paribas", result);
  }

  @Test
  public void testComputeFullnameWithCodeAndLabel() {

    Bank bank = createBank("CCCCFRPPXXX - BNP Paribas");
    BankDetails bankDetails =
        createBankDetails(bank, "FR7699333020961963437764029", "Axelor", "AXE");

    String result = bankDetailsFullNameComputeService.computeBankDetailsFullName(bankDetails);

    Assert.assertEquals(
        "AXE - Axelor - FR7699333020961963437764029 - CCCCFRPPXXX - BNP Paribas", result);
  }

  protected Bank createBank(String fullName) {
    Bank bank = new Bank();
    bank.setFullName(fullName);
    return bank;
  }

  protected BankDetails createBankDetails(Bank bank, String iban, String label, String code) {
    BankDetails bankDetails = new BankDetails();
    if (bank != null) {
      bankDetails.setBank(bank);
    }

    if (!iban.isEmpty()) {
      bankDetails.setIban(iban);
    }

    if (!label.isEmpty()) {
      bankDetails.setLabel(label);
    }

    if (!code.isEmpty()) {
      bankDetails.setCode(code);
    }

    return bankDetails;
  }
}
