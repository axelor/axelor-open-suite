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
package com.axelor.apps.bankpayment.service.bankstatementline.afb120;

import com.axelor.apps.account.db.InterbankCodeLine;
import com.axelor.apps.account.db.repo.InterbankCodeLineRepository;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.BankStatementLineAFB120;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementImportService;
import com.axelor.apps.bankpayment.service.bankstatementline.BankStatementLineCreateAbstractService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.repo.BankDetailsRepository;
import com.axelor.apps.base.db.repo.CurrencyRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.utils.helpers.file.FileHelper;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BankStatementLineCreateAFB120Service extends BankStatementLineCreateAbstractService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected BankStatementLineCreationAFB120Service bankStatementLineCreationAFB120Service;
  protected BankStatementLineAFB120Repository bankStatementLineAFB120Repository;
  protected CurrencyRepository currencyRepository;
  protected BankDetailsRepository bankDetailsRepository;
  protected InterbankCodeLineRepository interbankCodeLineRepository;
  protected BankStatementLineMapperAFB120Service bankStatementLineMapperAFB120Service;

  @Inject
  public BankStatementLineCreateAFB120Service(
      BankStatementRepository bankStatementRepository,
      BankStatementImportService bankStatementService,
      BankStatementLineCreationAFB120Service bankStatementLineCreationAFB120Service,
      BankStatementLineAFB120Repository bankStatementLineAFB120Repository,
      CurrencyRepository currencyRepository,
      BankDetailsRepository bankDetailsRepository,
      InterbankCodeLineRepository interbankCodeLineRepository,
      BankStatementLineMapperAFB120Service bankStatementLineMapperAFB120Service) {
    super(bankStatementRepository, bankStatementService);
    this.bankStatementLineCreationAFB120Service = bankStatementLineCreationAFB120Service;
    this.bankStatementLineAFB120Repository = bankStatementLineAFB120Repository;
    this.currencyRepository = currencyRepository;
    this.bankDetailsRepository = bankDetailsRepository;
    this.interbankCodeLineRepository = interbankCodeLineRepository;
    this.bankStatementLineMapperAFB120Service = bankStatementLineMapperAFB120Service;
  }

  @Transactional
  protected BankStatementLine createBankStatementLine(
      StructuredContentLine structuredContentLine, int sequence) {

    String description = structuredContentLine.getDescription();
    LocalDate operationDate = structuredContentLine.getOperationDate();
    LocalDate valueDate = structuredContentLine.getValueDate();
    int lineType = structuredContentLine.getLineType();

    if (StringUtils.notEmpty(structuredContentLine.getAdditionalInformation())) {
      description += "\n" + structuredContentLine.getAdditionalInformation();
    }

    BankDetails bankDetails = null;
    if (structuredContentLine.getBankDetails() != null) {
      bankDetails = bankDetailsRepository.find(structuredContentLine.getBankDetails().getId());
    }

    Currency currency = null;
    if (structuredContentLine.getCurrency() != null) {
      currency = currencyRepository.find(structuredContentLine.getCurrency().getId());
    }

    InterbankCodeLine operationInterbankCodeLine = null;
    if (structuredContentLine.getOperationInterbankCodeLine() != null) {
      operationInterbankCodeLine =
          interbankCodeLineRepository.find(
              structuredContentLine.getOperationInterbankCodeLine().getId());
    }

    InterbankCodeLine rejectInterbankCodeLine = null;
    if (structuredContentLine.getRejectInterbankCodeLine() != null) {
      rejectInterbankCodeLine =
          interbankCodeLineRepository.find(
              structuredContentLine.getRejectInterbankCodeLine().getId());
    }

    BankStatementLineAFB120 bankStatementLineAFB120 =
        bankStatementLineCreationAFB120Service.createBankStatementLine(
            findBankStatement(),
            sequence,
            bankDetails,
            structuredContentLine.getDebit(),
            structuredContentLine.getCredit(),
            currency,
            description,
            operationDate,
            valueDate,
            operationInterbankCodeLine,
            rejectInterbankCodeLine,
            structuredContentLine.getOrigin(),
            structuredContentLine.getReference(),
            lineType,
            structuredContentLine.getUnavailabilityIndexSelect(),
            structuredContentLine.getCommissionExemptionIndexSelect());

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

  protected List<StructuredContentLine> readFile() throws IOException, AxelorException {

    List<StructuredContentLine> structuredContent = Lists.newArrayList();

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
