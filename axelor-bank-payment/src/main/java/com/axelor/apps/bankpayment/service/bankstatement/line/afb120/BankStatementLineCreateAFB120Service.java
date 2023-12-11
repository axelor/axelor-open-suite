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
package com.axelor.apps.bankpayment.service.bankstatement.line.afb120;

import com.axelor.apps.account.db.InterbankCodeLine;
import com.axelor.apps.account.db.repo.InterbankCodeLineRepository;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.BankStatementLineAFB120;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementImportService;
import com.axelor.apps.bankpayment.service.bankstatement.line.BankStatementLineCreateAbstractService;
import com.axelor.apps.bankpayment.service.bankstatementline.afb120.BankStatementLineAFB120Service;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.repo.BankDetailsRepository;
import com.axelor.apps.base.db.repo.CurrencyRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.utils.helpers.file.FileHelper;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BankStatementLineCreateAFB120Service extends BankStatementLineCreateAbstractService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected BankStatementLineAFB120Service bankStatementLineAFB120Service;
  protected BankStatementLineAFB120Repository bankStatementLineAFB120Repository;
  protected CurrencyRepository currencyRepository;
  protected BankDetailsRepository bankDetailsRepository;
  protected InterbankCodeLineRepository interbankCodeLineRepository;
  protected BankStatementLineMapperAFB120Service bankStatementLineMapperAFB120Service;

  @Inject
  public BankStatementLineCreateAFB120Service(
      BankStatementRepository bankStatementRepository,
      BankStatementImportService bankStatementService,
      BankStatementLineAFB120Service bankStatementLineAFB120Service,
      BankStatementLineAFB120Repository bankStatementLineAFB120Repository,
      CurrencyRepository currencyRepository,
      BankDetailsRepository bankDetailsRepository,
      InterbankCodeLineRepository interbankCodeLineRepository,
      BankStatementLineMapperAFB120Service bankStatementLineMapperAFB120Service) {
    super(bankStatementRepository, bankStatementService);
    this.bankStatementLineAFB120Service = bankStatementLineAFB120Service;
    this.bankStatementLineAFB120Repository = bankStatementLineAFB120Repository;
    this.currencyRepository = currencyRepository;
    this.bankDetailsRepository = bankDetailsRepository;
    this.interbankCodeLineRepository = interbankCodeLineRepository;
    this.bankStatementLineMapperAFB120Service = bankStatementLineMapperAFB120Service;
  }

  @Transactional
  protected BankStatementLine createBankStatementLine(
      Map<String, Object> structuredContentLine, int sequence) {

    String description = (String) structuredContentLine.get("description");
    LocalDate operationDate = (LocalDate) structuredContentLine.get("operationDate");
    LocalDate valueDate = (LocalDate) structuredContentLine.get("valueDate");
    int lineType = (int) structuredContentLine.get("lineType");

    if (structuredContentLine.containsKey("additionalInformation")
        && structuredContentLine.get("additionalInformation") != null) {
      description += "\n" + (String) structuredContentLine.get("additionalInformation");
    }

    BankDetails bankDetails = null;
    if (structuredContentLine.containsKey("bankDetails")
        && structuredContentLine.get("bankDetails") != null) {
      bankDetails =
          bankDetailsRepository.find(
              ((BankDetails) structuredContentLine.get("bankDetails")).getId());
    }

    Currency currency = null;
    if (structuredContentLine.containsKey("currency")
        && structuredContentLine.get("currency") != null) {
      currency =
          currencyRepository.find(((Currency) structuredContentLine.get("currency")).getId());
    }

    InterbankCodeLine operationInterbankCodeLine = null;
    if (structuredContentLine.containsKey("operationInterbankCodeLine")
        && structuredContentLine.get("operationInterbankCodeLine") != null) {
      operationInterbankCodeLine =
          interbankCodeLineRepository.find(
              ((InterbankCodeLine) structuredContentLine.get("operationInterbankCodeLine"))
                  .getId());
    }

    InterbankCodeLine rejectInterbankCodeLine = null;
    if (structuredContentLine.containsKey("rejectInterbankCodeLine")
        && structuredContentLine.get("rejectInterbankCodeLine") != null) {
      rejectInterbankCodeLine =
          interbankCodeLineRepository.find(
              ((InterbankCodeLine) structuredContentLine.get("rejectInterbankCodeLine")).getId());
    }

    BankStatementLineAFB120 bankStatementLineAFB120 =
        bankStatementLineAFB120Service.createBankStatementLine(
            findBankStatement(),
            sequence,
            bankDetails,
            (BigDecimal) structuredContentLine.get("debit"),
            (BigDecimal) structuredContentLine.get("credit"),
            currency,
            description,
            operationDate,
            valueDate,
            operationInterbankCodeLine,
            rejectInterbankCodeLine,
            (String) structuredContentLine.get("origin"),
            (String) structuredContentLine.get("reference"),
            lineType,
            (String) structuredContentLine.get("unavailabilityIndexSelect"),
            (String) structuredContentLine.get("commissionExemptionIndexSelect"));

    updateBankStatementDate(operationDate, lineType);

    return bankStatementLineAFB120Repository.save(bankStatementLineAFB120);
  }

  protected void updateBankStatementDate(LocalDate operationDate, int lineType) {
    if (operationDate == null) {
      return;
    }

    if (ObjectUtils.notEmpty(bankStatement.getFromDate())
        && lineType == BankStatementLineAFB120Repository.LINE_TYPE_INITIAL_BALANCE) {
      if (operationDate.isBefore(bankStatement.getFromDate()))
        bankStatement.setFromDate(operationDate);
    } else if (lineType == BankStatementLineAFB120Repository.LINE_TYPE_INITIAL_BALANCE) {
      bankStatement.setFromDate(operationDate);
    }

    if (ObjectUtils.notEmpty(bankStatement.getToDate())
        && lineType == BankStatementLineAFB120Repository.LINE_TYPE_FINAL_BALANCE) {
      if (operationDate.isAfter(bankStatement.getToDate())) {
        bankStatement.setToDate(operationDate);
      }
    } else {
      if (lineType == BankStatementLineAFB120Repository.LINE_TYPE_FINAL_BALANCE) {
        bankStatement.setToDate(operationDate);
      }
    }
  }

  protected List<Map<String, Object>> readFile() throws IOException, AxelorException {

    List<Map<String, Object>> structuredContent = Lists.newArrayList();

    List<String> fileContent = FileHelper.reader(file.getPath());

    for (String lineContent : fileContent) {
      log.info("Read line : {}", lineContent);
      String lineData = null;
      int i = 0;

      while (i < lineContent.length()) {

        lineData = lineContent.substring(i, i + 120);

        bankStatementLineMapperAFB120Service.writeStructuredContent(lineData, structuredContent);

        i = i + 120;
      }
    }

    return structuredContent;
  }
}
