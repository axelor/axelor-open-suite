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
