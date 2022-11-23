package com.axelor.apps.base.test;

import com.axelor.apps.base.db.Bank;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.BankDetailsServiceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestBankDetailsService {

  protected BankDetailsService bankDetailsService;

  @Before
  public void prepare() {
    bankDetailsService = new BankDetailsServiceImpl();
  }

  @Test
  public void testComputeEmptyFullName() {
    BankDetails emptyBankDetails = new BankDetails();

    String result = bankDetailsService.computeBankDetailsFullName(emptyBankDetails);

    Assert.assertEquals("", result);
  }

  @Test
  public void testComputeFullnameMinimal() {
    BankDetails bankDetails1 = new BankDetails();
    Bank bank1 = new Bank();
    bank1.setFullName("CCCCFRPPXXX - BNP Paribas");
    bankDetails1.setIban("FR7699333020961963437764029");
    bankDetails1.setBank(bank1);

    String result = bankDetailsService.computeBankDetailsFullName(bankDetails1);

    Assert.assertEquals("FR7699333020961963437764029 - CCCCFRPPXXX - BNP Paribas", result);
  }

  @Test
  public void testComputeFullnameWithLabel() {
    BankDetails bankDetails2 = new BankDetails();
    Bank bank2 = new Bank();
    bank2.setFullName("CCCCFRPPXXX - BNP Paribas");
    bankDetails2.setBank(bank2);
    bankDetails2.setLabel("Axelor");
    bankDetails2.setIban("FR7699333020961963437764029");

    String result = bankDetailsService.computeBankDetailsFullName(bankDetails2);

    Assert.assertEquals("Axelor - FR7699333020961963437764029 - CCCCFRPPXXX - BNP Paribas", result);
  }

  @Test
  public void testComputeFullnameWithCode() {
    BankDetails bankDetails3 = new BankDetails();
    Bank bank3 = new Bank();
    bank3.setFullName("CCCCFRPPXXX - BNP Paribas");
    bankDetails3.setBank(bank3);
    bankDetails3.setIban("FR7699333020961963437764029");
    bankDetails3.setCode("AXE");

    String result = bankDetailsService.computeBankDetailsFullName(bankDetails3);

    Assert.assertEquals("AXE - FR7699333020961963437764029 - CCCCFRPPXXX - BNP Paribas", result);
  }

  @Test
  public void testComputeFullnameWithCodeAndLabel() {
    BankDetails bankDetails4 = new BankDetails();
    Bank bank4 = new Bank();
    bank4.setFullName("CCCCFRPPXXX - BNP Paribas");
    bankDetails4.setBank(bank4);
    bankDetails4.setIban("FR7699333020961963437764029");
    bankDetails4.setLabel("Axelor");
    bankDetails4.setCode("AXE");

    String result = bankDetailsService.computeBankDetailsFullName(bankDetails4);

    Assert.assertEquals(
        "AXE - Axelor - FR7699333020961963437764029 - CCCCFRPPXXX - BNP Paribas", result);
  }
}
