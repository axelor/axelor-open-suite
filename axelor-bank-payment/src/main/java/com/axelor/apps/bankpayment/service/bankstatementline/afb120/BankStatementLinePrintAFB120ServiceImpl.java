package com.axelor.apps.bankpayment.service.bankstatementline.afb120;

import com.axelor.apps.bankpayment.db.BankStatementLineAFB120;
import com.axelor.apps.bankpayment.db.repo.BankPaymentBankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.service.config.BankPaymentConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.BirtTemplate;
import com.axelor.apps.base.service.birt.template.BirtTemplateService;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.Map;

public class BankStatementLinePrintAFB120ServiceImpl
    implements BankStatementLinePrintAFB120Service {

  protected BankPaymentBankStatementLineAFB120Repository
      bankPaymentBankStatementLineAFB120Repository;
  protected BankPaymentConfigService bankPaymentConfigService;
  protected BirtTemplateService birtTemplateService;

  @Inject
  public BankStatementLinePrintAFB120ServiceImpl(
      BankPaymentBankStatementLineAFB120Repository bankPaymentBankStatementLineAFB120Repository,
      BankPaymentConfigService bankPaymentConfigService,
      BirtTemplateService birtTemplateService) {
    this.bankPaymentBankStatementLineAFB120Repository =
        bankPaymentBankStatementLineAFB120Repository;
    this.bankPaymentConfigService = bankPaymentConfigService;
    this.birtTemplateService = birtTemplateService;
  }

  @Override
  public String print(
      LocalDate fromDate, LocalDate toDate, BankDetails bankDetails, String exportType)
      throws AxelorException {
    String fileLink = null;

    BankStatementLineAFB120 initalBankStatementLine =
        bankPaymentBankStatementLineAFB120Repository.findLineBetweenDate(
            fromDate,
            toDate,
            BankStatementLineAFB120Repository.LINE_TYPE_INITIAL_BALANCE,
            true,
            bankDetails);
    BankStatementLineAFB120 finalBankStatementLine =
        bankPaymentBankStatementLineAFB120Repository.findLineBetweenDate(
            fromDate,
            toDate,
            BankStatementLineAFB120Repository.LINE_TYPE_FINAL_BALANCE,
            false,
            bankDetails);
    if (ObjectUtils.notEmpty(initalBankStatementLine)
        && ObjectUtils.notEmpty(finalBankStatementLine)) {
      BirtTemplate bankStatementLinesBirtTemplate =
          bankPaymentConfigService.getBankStatementLineBirtTemplate(bankDetails.getCompany());
      fromDate = initalBankStatementLine.getOperationDate();
      toDate = finalBankStatementLine.getOperationDate();

      fileLink =
          birtTemplateService.generateBirtTemplateLink(
              bankStatementLinesBirtTemplate,
              null,
              Map.of("FromDate", fromDate, "ToDate", toDate, "BankDetails", bankDetails.getId()),
              "Bank statement lines - " + fromDate + " to " + toDate,
              bankStatementLinesBirtTemplate.getAttach(),
              exportType);
    }
    return fileLink;
  }
}
