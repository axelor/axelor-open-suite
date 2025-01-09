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
package com.axelor.apps.bankpayment.service.bankstatement;

import com.axelor.apps.account.db.InterbankCodeLine;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.BankStatementLineAFB120;
import com.axelor.apps.bankpayment.db.repo.BankPaymentBankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.service.config.BankPaymentConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.BirtTemplate;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.birt.template.BirtTemplateService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public class BankStatementLineService {

  protected BankPaymentBankStatementLineAFB120Repository
      bankPaymentBankStatementLineAFB120Repository;
  protected BankPaymentConfigService bankPaymentConfigService;
  protected BirtTemplateService birtTemplateService;

  @Inject
  public BankStatementLineService(
      BankPaymentBankStatementLineAFB120Repository bankPaymentBankStatementLineAFB120Repository,
      BankPaymentConfigService bankPaymentConfigService,
      BirtTemplateService birtTemplateService) {
    this.bankPaymentBankStatementLineAFB120Repository =
        bankPaymentBankStatementLineAFB120Repository;
    this.bankPaymentConfigService = bankPaymentConfigService;
    this.birtTemplateService = birtTemplateService;
  }

  public BankStatementLine createBankStatementLine(
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
      String reference) {

    BankStatementLine bankStatementLine = new BankStatementLine();
    bankStatementLine.setBankStatement(bankStatement);
    bankStatementLine.setSequence(sequence);
    bankStatementLine.setBankDetails(bankDetails);
    bankStatementLine.setDebit(debit);
    bankStatementLine.setCredit(credit);
    bankStatementLine.setCurrency(currency);
    bankStatementLine.setDescription(description);
    bankStatementLine.setOperationDate(operationDate);
    bankStatementLine.setValueDate(valueDate);
    bankStatementLine.setOperationInterbankCodeLine(operationInterbankCodeLine);
    bankStatementLine.setRejectInterbankCodeLine(rejectInterbankCodeLine);
    bankStatementLine.setOrigin(origin);
    bankStatementLine.setReference(reference);

    // Used for Bank reconcile process
    bankStatementLine.setAmountRemainToReconcile(
        bankStatementLine.getDebit().add(bankStatementLine.getCredit()));

    return bankStatementLine;
  }

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

  @Transactional
  public void removeBankReconciliationLines(BankStatement bankStatement) {
    JPA.em()
        .createQuery(
            "delete from BankStatementLineAFB120 self where self.bankStatement.id = "
                + bankStatement.getId())
        .executeUpdate();
  }
}
