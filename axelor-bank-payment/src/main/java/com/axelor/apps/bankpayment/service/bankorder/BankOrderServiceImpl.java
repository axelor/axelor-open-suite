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
package com.axelor.apps.bankpayment.service.bankorder;

import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.BankOrderFileFormat;
import com.axelor.apps.bankpayment.db.BankOrderLine;
import com.axelor.apps.bankpayment.db.repo.BankOrderFileFormatRepository;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.bankpayment.service.app.AppBankPaymentService;
import com.axelor.apps.bankpayment.service.bankorder.file.directdebit.BankOrderFile00800101Service;
import com.axelor.apps.bankpayment.service.bankorder.file.directdebit.BankOrderFile00800102Service;
import com.axelor.apps.bankpayment.service.bankorder.file.directdebit.BankOrderFile008Service;
import com.axelor.apps.bankpayment.service.bankorder.file.transfer.BankOrderFile00100102Service;
import com.axelor.apps.bankpayment.service.bankorder.file.transfer.BankOrderFile00100103Service;
import com.axelor.apps.bankpayment.service.bankorder.file.transfer.BankOrderFileAFB160DCOService;
import com.axelor.apps.bankpayment.service.bankorder.file.transfer.BankOrderFileAFB160ICTService;
import com.axelor.apps.bankpayment.service.bankorder.file.transfer.BankOrderFileAFB320XCTService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import jakarta.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import javax.xml.datatype.DatatypeConfigurationException;

public class BankOrderServiceImpl implements BankOrderService {

  protected BankOrderRepository bankOrderRepository;
  protected BankDetailsService bankDetailsService;
  protected AppBankPaymentService appBankPaymentService;
  protected BankOrderEncryptionService bankOrderEncryptionService;

  @Inject
  public BankOrderServiceImpl(
      BankOrderRepository bankOrderRepository,
      BankDetailsService bankDetailsService,
      AppBankPaymentService appBankPaymentService,
      BankOrderEncryptionService bankOrderEncryptionService) {

    this.bankOrderRepository = bankOrderRepository;
    this.bankDetailsService = bankDetailsService;
    this.appBankPaymentService = appBankPaymentService;
    this.bankOrderEncryptionService = bankOrderEncryptionService;
  }

  public void processBankOrderStatus(BankOrder bankOrder, PaymentMode paymentMode)
      throws AxelorException {
    validate(bankOrder);
  }

  @Override
  @Transactional
  public void sign(BankOrder bankOrder) {

    // TODO

  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void validate(BankOrder bankOrder) throws AxelorException {
    bankOrder.setValidationDateTime(LocalDateTime.now());

    bankOrder.setStatusSelect(BankOrderRepository.STATUS_VALIDATED);

    bankOrderRepository.save(bankOrder);
  }

  @Transactional
  protected void markAsSent(BankOrder bankOrder) {
    bankOrder.setHasBeenSentToBank(true);
  }

  public void setNbOfLines(BankOrder bankOrder) {

    if (bankOrder.getBankOrderLineList() == null) {
      return;
    }

    bankOrder.setNbOfLines(bankOrder.getBankOrderLineList().size());
  }

  @Override
  public String createDomainForBankDetails(BankOrder bankOrder) {

    String domain =
        bankDetailsService.getActiveCompanyBankDetails(
            bankOrder.getSenderCompany(), bankOrder.getBankOrderCurrency());

    // filter on the bank details identifier type from the bank order file
    // format
    if (bankOrder.getBankOrderFileFormat() != null) {
      String acceptedIdentifiers = bankOrder.getBankOrderFileFormat().getBankDetailsTypeSelect();
      if (acceptedIdentifiers != null && !acceptedIdentifiers.equals("")) {
        domain += " AND self.bank.bankDetailsTypeSelect IN (" + acceptedIdentifiers + ")";
      }
    }
    return domain;
  }

  @Override
  public File generateFile(BankOrder bankOrder)
      throws JAXBException, IOException, AxelorException, DatatypeConfigurationException {

    if (bankOrder.getBankOrderLineList() == null || bankOrder.getBankOrderLineList().isEmpty()) {
      return null;
    }

    bankOrder.setFileGenerationDateTime(LocalDateTime.now());

    BankOrderFileFormat bankOrderFileFormat = bankOrder.getBankOrderFileFormat();

    File file = null;

    switch (bankOrderFileFormat.getOrderFileFormatSelect()) {
      case BankOrderFileFormatRepository.FILE_FORMAT_PAIN_001_001_02_SCT:
        file = new BankOrderFile00100102Service(bankOrder).generateFile();
        break;

      case BankOrderFileFormatRepository.FILE_FORMAT_PAIN_001_001_03_SCT:
        file = new BankOrderFile00100103Service(bankOrder).generateFile();
        break;

      case BankOrderFileFormatRepository.FILE_FORMAT_PAIN_XXX_CFONB320_XCT:
        file = new BankOrderFileAFB320XCTService(bankOrder).generateFile();
        break;

      case BankOrderFileFormatRepository.FILE_FORMAT_PAIN_XXX_CFONB160_ICT:
        file = new BankOrderFileAFB160ICTService(bankOrder).generateFile();
        break;

      case BankOrderFileFormatRepository.FILE_FORMAT_PAIN_XXX_CFONB160_DCO:
        file = new BankOrderFileAFB160DCOService(bankOrder).generateFile();
        break;

      case BankOrderFileFormatRepository.FILE_FORMAT_PAIN_008_001_01_SDD:
        file =
            new BankOrderFile00800101Service(bankOrder, BankOrderFile008Service.SEPA_TYPE_CORE)
                .generateFile();
        break;

      case BankOrderFileFormatRepository.FILE_FORMAT_PAIN_008_001_01_SBB:
        file =
            new BankOrderFile00800101Service(bankOrder, BankOrderFile008Service.SEPA_TYPE_SBB)
                .generateFile();
        break;

      case BankOrderFileFormatRepository.FILE_FORMAT_PAIN_008_001_02_SDD:
        file =
            new BankOrderFile00800102Service(bankOrder, BankOrderFile008Service.SEPA_TYPE_CORE)
                .generateFile();
        break;

      case BankOrderFileFormatRepository.FILE_FORMAT_PAIN_008_001_02_SBB:
        file =
            new BankOrderFile00800102Service(bankOrder, BankOrderFile008Service.SEPA_TYPE_SBB)
                .generateFile();
        break;

      default:
        throw new AxelorException(
            bankOrder,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(BankPaymentExceptionMessage.BANK_ORDER_FILE_UNKNOWN_FORMAT));
    }

    if (file == null) {
      throw new AxelorException(
          bankOrder,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_ORDER_ISSUE_DURING_FILE_GENERATION),
          bankOrder.getBankOrderSeq());
    }

    if (appBankPaymentService.getAppBankPayment().getEnableBankOrderFileEncryption()) {
      file = bankOrderEncryptionService.encryptFile(file);
    }

    MetaFiles metaFiles = Beans.get(MetaFiles.class);

    try (InputStream is = new FileInputStream(file)) {
      metaFiles.attach(is, file.getName(), bankOrder);
      bankOrder.setGeneratedMetaFile(metaFiles.upload(file));
    }

    return file;
  }

  @Override
  public void resetReceivers(BankOrder bankOrder) {
    if (ObjectUtils.isEmpty(bankOrder.getBankOrderLineList())) {
      return;
    }

    resetPartners(bankOrder);
    resetBankDetails(bankOrder);
  }

  protected void resetPartners(BankOrder bankOrder) {
    if (bankOrder.getPartnerTypeSelect() == BankOrderRepository.PARTNER_TYPE_COMPANY) {
      for (BankOrderLine bankOrderLine : bankOrder.getBankOrderLineList()) {
        bankOrderLine.setPartner(null);
      }

      return;
    }

    for (BankOrderLine bankOrderLine : bankOrder.getBankOrderLineList()) {
      bankOrderLine.setReceiverCompany(null);
      Partner partner = bankOrderLine.getPartner();

      if (partner == null) {
        continue;
      }

      boolean keep;

      switch (bankOrder.getPartnerTypeSelect()) {
        case BankOrderRepository.PARTNER_TYPE_SUPPLIER:
          keep = partner.getIsSupplier();
          break;
        case BankOrderRepository.PARTNER_TYPE_EMPLOYEE:
          keep = partner.getIsEmployee();
          break;
        case BankOrderRepository.PARTNER_TYPE_CUSTOMER:
          keep = partner.getIsCustomer();
          break;
        default:
          keep = false;
      }

      if (!keep) {
        bankOrderLine.setPartner(null);
      }
    }
  }

  protected void resetBankDetails(BankOrder bankOrder) {
    for (BankOrderLine bankOrderLine : bankOrder.getBankOrderLineList()) {
      if (bankOrderLine.getReceiverBankDetails() == null) {
        continue;
      }

      Collection<BankDetails> bankDetailsCollection;

      if (bankOrderLine.getReceiverCompany() != null) {
        bankDetailsCollection = bankOrderLine.getReceiverCompany().getBankDetailsList();
      } else if (bankOrderLine.getPartner() != null) {
        bankDetailsCollection = bankOrderLine.getPartner().getBankDetailsList();
      } else {
        bankDetailsCollection = Collections.emptyList();
      }

      if (ObjectUtils.isEmpty(bankDetailsCollection)
          || !bankDetailsCollection.contains(bankOrderLine.getReceiverBankDetails())) {
        bankOrderLine.setReceiverBankDetails(null);
      }
    }
  }

  @Override
  public ActionViewBuilder buildBankOrderLineView(
      String gridViewName, String formViewName, String viewDomain) {
    return ActionView.define(I18n.get("Bank Order Lines"))
        .model(BankOrderLine.class.getName())
        .add("grid", gridViewName)
        .add("form", formViewName)
        .domain(viewDomain);
  }

  @Transactional
  @Override
  public void setStatusToDraft(BankOrder bankOrder) {
    bankOrder.setStatusSelect(BankOrderRepository.STATUS_DRAFT);
    bankOrderRepository.save(bankOrder);
  }
}
