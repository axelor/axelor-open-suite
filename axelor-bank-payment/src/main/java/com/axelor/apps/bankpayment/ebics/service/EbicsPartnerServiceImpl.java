/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.bankpayment.ebics.service;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementFileFormat;
import com.axelor.apps.bankpayment.db.EbicsPartner;
import com.axelor.apps.bankpayment.db.EbicsUser;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.db.repo.EbicsPartnerRepository;
import com.axelor.apps.bankpayment.exception.IExceptionMessage;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementCreateService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.tool.date.DateTool;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class EbicsPartnerServiceImpl implements EbicsPartnerService {

  protected BankStatementCreateService bankStatementCreateService;
  protected EbicsService ebicsService;
  protected BankStatementRepository bankStatementRepository;

  @Inject
  public EbicsPartnerServiceImpl(
      BankStatementCreateService bankStatementCreateService,
      EbicsService ebicsService,
      BankStatementRepository bankStatementRepository) {

    this.bankStatementCreateService = bankStatementCreateService;
    this.ebicsService = ebicsService;
    this.bankStatementRepository = bankStatementRepository;
  }

  @Transactional
  public List<BankStatement> getBankStatements(EbicsPartner ebicsPartner)
      throws AxelorException, IOException {
    return getBankStatements(ebicsPartner, null);
  }

  @Transactional
  public List<BankStatement> getBankStatements(
      EbicsPartner ebicsPartner,
      Collection<BankStatementFileFormat> bankStatementFileFormatCollection)
      throws AxelorException, IOException {

    List<BankStatement> bankStatementList = Lists.newArrayList();

    EbicsUser transportEbicsUser = ebicsPartner.getTransportEbicsUser();

    if (ebicsPartner.getBsEbicsPartnerServiceList() == null
        || ebicsPartner.getBsEbicsPartnerServiceList().isEmpty()
        || transportEbicsUser == null) {
      return bankStatementList;
    }

    LocalDateTime executionDateTime = LocalDateTime.now();

    Date startDate = null;
    Date endDate = null;
    LocalDate bankStatementStartDate = null;
    LocalDate bankStatementToDate = null;

    if (ebicsPartner.getBankStatementGetModeSelect() == EbicsPartnerRepository.GET_MODE_PERIOD) {
      bankStatementStartDate = ebicsPartner.getBankStatementStartDate();
      if (bankStatementStartDate != null) {
        startDate = DateTool.toDate(bankStatementStartDate);
      }
      bankStatementToDate = ebicsPartner.getBankStatementEndDate();
      if (bankStatementToDate != null) {
        endDate = DateTool.toDate(bankStatementToDate);
      }
    } else {
      if (ebicsPartner.getBankStatementLastExeDateT() != null) {
        bankStatementStartDate = ebicsPartner.getBankStatementLastExeDateT().toLocalDate();
      }
      bankStatementToDate = executionDateTime.toLocalDate();
    }

    for (com.axelor.apps.bankpayment.db.EbicsPartnerService bsEbicsPartnerService :
        ebicsPartner.getBsEbicsPartnerServiceList()) {

      BankStatementFileFormat bankStatementFileFormat =
          bsEbicsPartnerService.getBankStatementFileFormat();
      if (bankStatementFileFormatCollection != null
          && !bankStatementFileFormatCollection.isEmpty()
          && !bankStatementFileFormatCollection.contains(bankStatementFileFormat)) {
        continue;
      }

      try {
        File file =
            ebicsService.sendFDLRequest(
                transportEbicsUser,
                null,
                startDate,
                endDate,
                bsEbicsPartnerService.getEbicsCodification());

        BankStatement bankStatement =
            bankStatementCreateService.createBankStatement(
                file,
                bankStatementStartDate,
                bankStatementToDate,
                bankStatementFileFormat,
                ebicsPartner,
                executionDateTime);

        bankStatementRepository.save(bankStatement);

        bankStatementList.add(bankStatement);

      } catch (Exception e) {
        TraceBackService.trace(e);
      }
    }

    ebicsPartner.setBankStatementLastExeDateT(executionDateTime);

    Beans.get(EbicsPartnerRepository.class).save(ebicsPartner);

    return bankStatementList;
  }

  public void checkBankDetailsMissingCurrency(EbicsPartner ebicsPartner) throws AxelorException {
    List<com.axelor.apps.bankpayment.db.EbicsPartnerService> ebicsPartnerServiceSet =
        ebicsPartner.getBoEbicsPartnerServiceList();
    if (ebicsPartnerServiceSet == null) {
      return;
    }
    boolean allowOrderCurrDiffFromBankDetails = false;
    for (com.axelor.apps.bankpayment.db.EbicsPartnerService ebicsPartnerService :
        ebicsPartnerServiceSet) {
      allowOrderCurrDiffFromBankDetails =
          allowOrderCurrDiffFromBankDetails
              || ebicsPartnerService
                  .getBankOrderFileFormat()
                  .getAllowOrderCurrDiffFromBankDetails();
      if (allowOrderCurrDiffFromBankDetails) {
        break;
      }
    }

    if (!allowOrderCurrDiffFromBankDetails) {
      return;
    }

    Set<BankDetails> bankDetailsSet = ebicsPartner.getBankDetailsSet();
    if (bankDetailsSet == null) {
      return;
    }

    List<String> bankDetailsWithoutCurrency = new ArrayList<>();
    for (BankDetails bankDetails : bankDetailsSet) {
      if (bankDetails.getCurrency() == null) {
        bankDetailsWithoutCurrency.add(bankDetails.getFullName());
      }
    }

    if (!bankDetailsWithoutCurrency.isEmpty()) {
      Function<String, String> addLi =
          new Function<String, String>() {
            @Override
            public String apply(String s) {
              return "<li>".concat(s).concat("</li>").toString();
            }
          };

      throw new AxelorException(
          String.format(
              I18n.get(IExceptionMessage.EBICS_PARTNER_BANK_DETAILS_WARNING),
              "<ul>"
                  + Joiner.on("").join(Iterables.transform(bankDetailsWithoutCurrency, addLi))
                  + "<ul>"),
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          ebicsPartner);
    }
  }
}
