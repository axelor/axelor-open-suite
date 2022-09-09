package com.axelor.apps.base.test;

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
  public void testComputeFullName() {
    BankDetails bankDetails1 = new BankDetails();
    bankDetails1.setCode("Bank details Code 1");
    bankDetails1.setLabel("Bank details Label 1");
    bankDetails1.setIban("FR7699333020961963437764029");

    BankDetails bankDetails2 = new BankDetails();
    bankDetails2.setLabel("Bank details Label 2");
    bankDetails2.setBankCode("99333");
    bankDetails2.setSortCode("02096");
    bankDetails2.setAccountNbr("19634377640");
    bankDetails2.setBbanKey("29");

    BankDetails bankDetails3 = new BankDetails();
    bankDetails3.setBankCode("99333");
    bankDetails3.setSortCode("02096");
    bankDetails3.setAccountNbr("19634377640");
    bankDetails3.setBbanKey("29");

    BankDetails bankDetails4 = new BankDetails();

    Assert.assertEquals(
        "Bank details Code 1 - Bank details Label 1 - FR7699333020961963437764029",
        bankDetailsService.computeBankDetailsFullName(bankDetails1));
    Assert.assertEquals(
        "Bank details Label 2 - 99333020961963437764029",
        bankDetailsService.computeBankDetailsFullName(bankDetails2));
    Assert.assertEquals(
        "99333020961963437764029", bankDetailsService.computeBankDetailsFullName(bankDetails3));
    Assert.assertEquals("", bankDetailsService.computeBankDetailsFullName(bankDetails4));
  }
}
